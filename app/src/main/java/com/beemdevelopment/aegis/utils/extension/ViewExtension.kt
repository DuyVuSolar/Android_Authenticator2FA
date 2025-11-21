package com.beemdevelopment.aegis.utils.extension

import android.graphics.PorterDuff
import android.os.SystemClock
import android.view.View
import android.widget.ImageView

fun View.visible() {
    this.post {
        this.visibility = View.VISIBLE
    }
}

fun View.invisible() {
    this.post {
        this.visibility = View.INVISIBLE
    }
}
fun View.gone() {
    this.post {
        this.visibility = View.GONE
    }
}

fun View.isVisible() = this.visibility == View.VISIBLE
fun View.isInvisible() = this.visibility == View.INVISIBLE
fun View.isGone() = this.visibility == View.GONE

var mLastClickTime = SystemClock.elapsedRealtime()
fun View.singleClick(callback : () -> Unit){
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return@setOnClickListener
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        callback.invoke()
    }
}

fun ImageView.setSvgColor(color: Int) = setColorFilter(color, PorterDuff.Mode.SRC_IN)

