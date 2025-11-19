package cn.ezandroid.lib.ezfilter

import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Size
import cn.ezandroid.lib.ezfilter.camera.Camera2Builder
import cn.ezandroid.lib.ezfilter.camera.CameraBuilder
import cn.ezandroid.lib.ezfilter.core.FBORender
import cn.ezandroid.lib.ezfilter.core.FilterRender
import cn.ezandroid.lib.ezfilter.core.GLRender
import cn.ezandroid.lib.ezfilter.core.RenderPipeline
import cn.ezandroid.lib.ezfilter.core.cache.IBitmapCache
import cn.ezandroid.lib.ezfilter.core.cache.LruBitmapCache
import cn.ezandroid.lib.ezfilter.core.environment.IFitView
import cn.ezandroid.lib.ezfilter.extra.IAdjustable
import cn.ezandroid.lib.ezfilter.image.BitmapBuilder
import cn.ezandroid.lib.ezfilter.media.record.RecordableRender
import cn.ezandroid.lib.ezfilter.multi.MultiBuilder
import cn.ezandroid.lib.ezfilter.multi.MultiInput
import cn.ezandroid.lib.ezfilter.video.VideoBuilder
import cn.ezandroid.lib.ezfilter.view.ViewBuilder
import cn.ezandroid.lib.ezfilter.view.glview.IGLView

/**
 * 滤镜处理
 *
 * @author like
 * @date 2017-09-15
 */
object EZFilter {
    /**
     * 获取图片缓存
     *
     * @return
     */
    /**
     * 设置图片缓存
     *
     * @param bitmapCache
     */
    /**
     * 默认的图片缓存 4分支1最大内存
     */
    var bitmapCache: IBitmapCache = LruBitmapCache((Runtime.getRuntime().maxMemory() / 4).toInt())

    /**
     * 设置图片输入
     *
     * @param bitmap 图片
     * @return
     */
    @JvmStatic
    fun input(bitmap: Bitmap): BitmapBuilder {
        return BitmapBuilder(bitmap)
    }

    /**
     * 设置视频输入
     *
     * @param uri 视频Uri
     * @return
     */
    @JvmStatic
    fun input(uri: Uri?): VideoBuilder {
        return VideoBuilder(uri)
    }

    /**
     * 设置Camera输入
     *
     * @param camera 摄像头
     * @param size   预览尺寸
     * @return
     */
    @JvmStatic
    fun input(camera: Camera, size: Camera.Size): CameraBuilder {
        return CameraBuilder(camera, size)
    }

    /**
     * 设置Camera2输入
     *
     * @param camera2 摄像头
     * @param size    预览尺寸
     * @return
     */
    fun input(camera2: CameraDevice?, size: Size?, callback : CameraBuilder.SurfaceCallback): Camera2Builder {
        return Camera2Builder(camera2, size, callback)
    }

    /**
     * 设置View输入
     *
     * @param view 视图
     * @return
     */
    @JvmStatic
    fun input(view: IGLView?): ViewBuilder {
        return ViewBuilder(view)
    }

    /**
     * Setting up multiple input sources
     *
     * @param builders   Input Source List
     * @param multiInput Multiple Input Source Renderer
     * @return
     */
    fun input(builders: List<Builder>, multiInput: MultiInput): MultiBuilder {
        return MultiBuilder(builders, multiInput)
    }

    /**
     * Constructor base class
     *
     *
     * Support chain operations
     */
    abstract class Builder protected constructor() {
        @JvmField
        protected var mFilterRenders: MutableList<FilterRender> = ArrayList()

        protected var mEnableRecordVideo: Boolean = false
        protected var mEnableRecordAudio: Boolean = false

        protected var mOutputPath: String? = null

        protected var mMainHandler: Handler = Handler(Looper.getMainLooper())

        /**
         * Get the rendering starting point
         *
         * @return Rendering start point
         */
        abstract fun getStartPointRender(view: IFitView?): FBORender?

        /**
         * Get the aspect ratio of the rendering view
         *
         * @return Aspect Ratio
         */
        abstract fun getAspectRatio(view: IFitView?): Float

        /**
         * Add a filter
         *
         * @param filterRender
         * @return
         */
        protected open fun addFilter(filterRender: FilterRender?): Builder? {
            if (filterRender != null && !mFilterRenders.contains(filterRender)) {
                filterRender.bitmapCache = bitmapCache
                mFilterRenders.add(filterRender)
            }
            return this
        }

        /**
         * 添加滤镜，并设置强度
         *
         * @param filterRender
         * @param progress
         * @param <T>
         * @return
        </T> */
        protected open fun <T> addFilter(
            filterRender: T?,
            progress: Float
        ): Builder? where T : FilterRender?, T : IAdjustable? {
            if (filterRender != null && !mFilterRenders.contains(filterRender)) {
                filterRender.bitmapCache = bitmapCache
                // 调节强度
                filterRender.adjust(progress)
                mFilterRenders.add(filterRender)
            }
            return this
        }

        /**
         * 支持录制开关
         *
         * @param outputPath  输出路径
         * @param recordVideo 录制影像
         * @param recordAudio 录制音频
         * @return
         */
        protected open fun enableRecord(
            outputPath: String?,
            recordVideo: Boolean,
            recordAudio: Boolean
        ): Builder? {
            mOutputPath = outputPath

            mEnableRecordVideo = recordVideo
            mEnableRecordAudio = recordAudio
            return this
        }

        /**
         * 渲染到View中
         *
         * @param view  要渲染到的View
         * @param clean 是否要清空渲染管道
         * @return 渲染管道
         */
        fun into(view: IFitView, clean: Boolean): RenderPipeline? {
            var pipeline = view.renderPipeline
            if (pipeline != null && clean) {
                pipeline.clean()
            }

            view.initRenderPipeline(getStartPointRender(view))

            pipeline = view.renderPipeline

            val aspectRatio = getAspectRatio(view)
            val change = view.setAspectRatio(aspectRatio, 0, 0)
            view.requestRender()

            if (pipeline != null) {
                pipeline.clearEndPointRenders()

                // 添加用于显示的终点渲染器
                pipeline.addEndPointRender(GLRender())

                // 要支持录制时，添加用于录制的终点渲染器
                if (mEnableRecordVideo || mEnableRecordAudio) {
                    pipeline.addEndPointRender(
                        RecordableRender(
                            mOutputPath,
                            mEnableRecordVideo, mEnableRecordAudio
                        )
                    )
                }

                for (filterRender in mFilterRenders) {
                    pipeline.addFilterRender(filterRender)
                }
                pipeline.startRender()
            }

            if (change) {
                // 确保requestLayout()在主线程调用
                mMainHandler.post { view.requestLayout() }
            }
            return pipeline
        }

        /**
         * 渲染到View中
         *
         * @param view 要渲染到的View
         * @return 渲染管道
         */
        open fun into(view: IFitView): RenderPipeline? {
            return into(view, true)
        }
    }
}
