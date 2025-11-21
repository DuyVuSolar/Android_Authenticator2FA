package com.beemdevelopment.aegis.utils.refresh

import android.content.Context
import kotlin.math.max
import kotlin.math.min

object Util {
    @JvmStatic
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    @JvmStatic
    fun limitValue(a: Float, b: Float): Float {
        var valve = 0f
        val min = min(a.toDouble(), b.toDouble()).toFloat()
        val max = max(a.toDouble(), b.toDouble()).toFloat()
        valve = if (valve > min) valve else min
        valve = if (valve < max) valve else max
        return valve
    }
}
