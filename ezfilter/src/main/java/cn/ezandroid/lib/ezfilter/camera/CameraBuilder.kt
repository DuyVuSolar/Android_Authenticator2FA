package cn.ezandroid.lib.ezfilter.camera

import android.graphics.SurfaceTexture
import android.hardware.Camera
import cn.ezandroid.lib.ezfilter.EZFilter
import cn.ezandroid.lib.ezfilter.core.FBORender
import cn.ezandroid.lib.ezfilter.core.FilterRender
import cn.ezandroid.lib.ezfilter.core.environment.IFitView
import cn.ezandroid.lib.ezfilter.extra.IAdjustable

/**
 * 摄像头处理构造器
 *
 * @author like
 * @date 2017-09-15
 */
class CameraBuilder(private val mCamera: Camera, private val mPreviewSize: Camera.Size, private val callback : CameraBuilder.SurfaceCallback? = null) :
    EZFilter.Builder() {
    private var mCameraInput: CameraInput? = null

    interface SurfaceCallback {
        fun callback(surfaceTexture: SurfaceTexture?)
    }

    override fun getStartPointRender(view: IFitView?): FBORender? {
        if (mCameraInput == null) {
            mCameraInput = CameraInput(view, mCamera, mPreviewSize)
        }
        return mCameraInput
    }

    override fun getAspectRatio(view: IFitView?): Float {
        return mPreviewSize.height * 1.0f / mPreviewSize.width
    }

    public override fun addFilter(filterRender: FilterRender?): CameraBuilder? {
        return super.addFilter(filterRender) as CameraBuilder?
    }

    public override fun <T> addFilter(
        filterRender: T?,
        progress: Float
    ): CameraBuilder? where T : FilterRender?, T : IAdjustable? {
        return super.addFilter<T>(filterRender, progress) as CameraBuilder?
    }

    public override fun enableRecord(
        outputPath: String?,
        recordVideo: Boolean,
        recordAudio: Boolean
    ): CameraBuilder? {
        return super.enableRecord(outputPath, recordVideo, recordAudio) as CameraBuilder?
    }
}
