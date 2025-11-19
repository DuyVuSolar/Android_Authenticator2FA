package com.kuemiin.reversevoice.ui.fragment.camera.render

import android.content.Context
import cn.ezandroid.lib.ezfilter.extra.sticker.QuestionRender
import cn.ezandroid.lib.ezfilter.extra.sticker.model.AnchorPoint
import cn.ezandroid.lib.ezfilter.extra.sticker.model.Component
import cn.ezandroid.lib.ezfilter.extra.sticker.model.ScreenAnchor
import cn.ezandroid.lib.ezfilter.extra.sticker.model.Sticker
import cn.ezandroid.lib.ezfilter.extra.sticker.model.TextureAnchor
import com.kuemiin.reversevoice.pose.render.ComponentConvert
import com.kuemiin.reversevoice.ui.fragment.camera.canvas.CanvasFileFilter

/**
 * GraffitiStickerRender
 *
 * @author like
 * @date 2018-05-25
 */
class CongratRender(context: Context, val widthRender : Int, val typeFilter : String) : QuestionRender(context) {

    var heightRender = 0

    init {
        heightRender = (widthRender)
        val sticker = Sticker()
        sticker.components = ArrayList()
        val component = Component()
        component.duration = 400
        component.src = "${CanvasFileFilter.getFilterFormatFile("typeFilter")}/${CanvasFileFilter.folderCongrat}"
        component.width = widthRender
        component.height = heightRender
        val textureAnchor = TextureAnchor()
        textureAnchor.leftAnchor = AnchorPoint(AnchorPoint.LEFT_BOTTOM, 0, 0)
        textureAnchor.rightAnchor = AnchorPoint(AnchorPoint.RIGHT_BOTTOM, 0, 0)
        textureAnchor.width = component.width
        textureAnchor.height = component.height
        component.textureAnchor = textureAnchor
        sticker.components.add(component)
        ComponentConvert.convert(context, component, context.filesDir.absolutePath)
        super.sticker = sticker

        val screenAnchor = ScreenAnchor()
        screenAnchor.leftAnchor = AnchorPoint(AnchorPoint.LEFT_TOP, 0, 0)
        screenAnchor.rightAnchor = AnchorPoint(AnchorPoint.LEFT_TOP, 0, 0)
        setScreenAnchor(screenAnchor)
    }

    fun setPosition(x: Int, y: Int) {
        if (mSticker?.components?.isNotEmpty() == true) {
            mScreenAnchor!!.leftAnchor.x = (x - mSticker!!.components[0].width / 2).toFloat()
            mScreenAnchor!!.leftAnchor.y = y.toFloat()

            mScreenAnchor!!.rightAnchor.x = (x + mSticker!!.components[0].width / 2).toFloat() // Graffiti stickers with only one element
            mScreenAnchor!!.rightAnchor.y = y.toFloat()
        }
    }

}
