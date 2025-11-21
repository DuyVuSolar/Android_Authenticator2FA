package com.beemdevelopment.aegis.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.ViewGroup
import android.view.animation.BounceInterpolator

object AnimationUtils {
    fun animateDialog(viewGroup: ViewGroup) {
        val set = AnimatorSet()
        val animatorX = ObjectAnimator.ofFloat(viewGroup, ViewGroup.SCALE_X, 0.7f, 1f)
        val animatorY = ObjectAnimator.ofFloat(viewGroup, ViewGroup.SCALE_Y, 0.7f, 1f)
        set.playTogether(animatorX, animatorY)
        set.interpolator = BounceInterpolator()
        set.duration = 300
        set.start()
    }

    fun animateDialogBottomToTOp(viewGroup: ViewGroup) {
//        val set = AnimatorSet()
//        val animatorX = ObjectAnimator.ofFloat(viewGroup, ViewGroup.SCALE_X, 0.7f, 1f)
//        val animatorY = ObjectAnimator.ofFloat(viewGroup, ViewGroup.SCALE_Y, 0.7f, 1f)
//        set.playTogether(animatorX, animatorY)
//        set.interpolator = BounceInterpolator()
//        set.duration = 300
//        set.start()
    }
}