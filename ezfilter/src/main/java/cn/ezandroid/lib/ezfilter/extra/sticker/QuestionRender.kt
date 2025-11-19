package cn.ezandroid.lib.ezfilter.extra.sticker

import android.content.Context
import android.opengl.GLES20
import cn.ezandroid.lib.ezfilter.core.FilterRender
import cn.ezandroid.lib.ezfilter.core.cache.IBitmapCache
import cn.ezandroid.lib.ezfilter.extra.sticker.model.ScreenAnchor
import cn.ezandroid.lib.ezfilter.extra.sticker.model.Sticker

/**
 * 贴纸渲染器
 *
 * @author like
 * @date 2018-01-05
 */
open class QuestionRender(protected var mContext: Context) : FilterRender() {
    // Sticker Model
    protected var mSticker: Sticker? = null
    var typeRender = ComponentRender.TYPE_RENDER_NORMAL

    // Anchor Group
    protected var mScreenAnchor: ScreenAnchor? = null

    // Component Painter List
    protected var mComponentRenders: MutableList<ComponentRender> = ArrayList()

    var sticker: Sticker?
        get() = mSticker
        /**
         * 设置贴纸模型
         *
         * @param sticker
         */
        set(sticker) {
            mSticker = sticker

            mComponentRenders.clear()
            for (component in mSticker!!.components) {
                val componentRender = ComponentRender(mContext, component)
                componentRender.setBitmapCache(bitmapCache)
                componentRender.typeRender = typeRender
                mComponentRenders.add(componentRender)
            }
        }

    /**
     * Set the display anchor point
     *
     * @param screenAnchor
     */
    fun setScreenAnchor(screenAnchor: ScreenAnchor?) {
        mScreenAnchor = screenAnchor
    }

    override fun destroy() {
        super.destroy()

        for (componentRender in mComponentRenders) {
            componentRender.destroy()
        }
    }

    override fun setBitmapCache(bitmapCache: IBitmapCache) {
        super.setBitmapCache(bitmapCache)
        for (componentRender in mComponentRenders) {
            componentRender.setBitmapCache(bitmapCache)
        }
    }

    override fun onRenderSizeChanged() {
        super.onRenderSizeChanged()
        mScreenAnchor?.width = mWidth
        mScreenAnchor?.height = mHeight
    }

    override fun onDraw() {
        super.onDraw()
        // Turn on mixing
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        for (componentRender in mComponentRenders) {
            componentRender.setScreenAnchor(mScreenAnchor)
            componentRender.updateRenderVertices(width, height)
            componentRender.onDraw(mTextureHandle, mPositionHandle, mTextureCoordHandle, mTextureVertices[2]!!, 0, currentQuiz, isRecording, isHide, totalScore)
        }

        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
