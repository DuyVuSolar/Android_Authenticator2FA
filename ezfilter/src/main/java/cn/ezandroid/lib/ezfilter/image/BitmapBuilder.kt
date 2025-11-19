package cn.ezandroid.lib.ezfilter.image

import android.graphics.Bitmap
import cn.ezandroid.lib.ezfilter.EZFilter
import cn.ezandroid.lib.ezfilter.core.FBORender
import cn.ezandroid.lib.ezfilter.core.FilterRender
import cn.ezandroid.lib.ezfilter.core.environment.IFitView
import cn.ezandroid.lib.ezfilter.extra.IAdjustable
import cn.ezandroid.lib.ezfilter.image.offscreen.OffscreenImage

/**
 * 图片处理构造器
 *
 * @author like
 * @date 2017-09-15
 */
class BitmapBuilder(private val mBitmap: Bitmap) : EZFilter.Builder() {
    private var mBitmapInput: BitmapInput? = null

    fun output(): Bitmap {
        // 离屏渲染
        val offscreenImage = OffscreenImage(mBitmap)
        for (filterRender in mFilterRenders) {
            offscreenImage.addFilterRender(filterRender)
        }
        return offscreenImage.capture()
    }

    fun output(width: Int, height: Int): Bitmap {
        // 离屏渲染
        val offscreenImage = OffscreenImage(mBitmap)
        for (filterRender in mFilterRenders) {
            offscreenImage.addFilterRender(filterRender)
        }
        return offscreenImage.capture(width, height)
    }

    override fun getStartPointRender(view: IFitView?): FBORender? {
        if (mBitmapInput == null) {
            mBitmapInput = BitmapInput(mBitmap)
        }
        return mBitmapInput
    }

    override fun getAspectRatio(view: IFitView?): Float {
        return mBitmap.width * 1.0f / mBitmap.height
    }

    public override fun addFilter(filterRender: FilterRender?): BitmapBuilder {
        return super.addFilter(filterRender) as BitmapBuilder
    }

    public override fun <T> addFilter(
        filterRender: T?,
        progress: Float
    ): BitmapBuilder? where T : FilterRender?, T : IAdjustable? {
        return super.addFilter<T>(filterRender, progress) as BitmapBuilder?
    }

    public override fun enableRecord(
        outputPath: String?,
        recordVideo: Boolean,
        recordAudio: Boolean
    ): BitmapBuilder? {
        return super.enableRecord(outputPath, recordVideo, recordAudio) as BitmapBuilder?
    }
}
