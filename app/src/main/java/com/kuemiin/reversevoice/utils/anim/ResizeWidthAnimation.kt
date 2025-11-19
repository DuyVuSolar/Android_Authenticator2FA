package com.kuemiin.reversevoice.utils.anim

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class ResizeWidthAnimation(private val mView: View, private val mWidth: Int) : Animation() {

    private val mStartWidth: Int

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newWidth = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()
        mView.layoutParams.width = newWidth
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }

    init {
        mStartWidth = mView.width
    }
}