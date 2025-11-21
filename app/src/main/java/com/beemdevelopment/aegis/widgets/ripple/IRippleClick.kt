package com.beemdevelopment.aegis.widgets.ripple

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.View

interface IRippleClick {
    @SuppressLint("UseCompatLoadingForDrawables")
    fun setBackground(context: Context, view: View, bg: Drawable?) {
        var background = bg
        if (background == null) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true
            )
            background = context.getDrawable(outValue.resourceId)
        }

        try {
            view.background =
                RippleDrawable(
                        ColorStateList.valueOf(context.getColor(android.R.color.darker_gray)),
                        background,
                        null
                )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
