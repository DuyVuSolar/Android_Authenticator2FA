package com.kuemiin.reversevoice.utils.glide

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout


class GlideImageLoader(
    imageView: ImageView?,
    viewLoading: ConstraintLayout?,
    viewNoInternet: ConstraintLayout,
    viewVisibleWhenError: ConstraintLayout,
    vararg views: View
) {
    private var mImageView: ImageView? = imageView
    private var mViewLoading: ConstraintLayout? = viewLoading
    private var mViewsGoneWhenError: ArrayList<View> = arrayListOf()
    private var mViewsVisibleWhenError: ConstraintLayout? = viewVisibleWhenError
    private var mViewNoInternet: ConstraintLayout? = viewNoInternet

    init {
        mViewsGoneWhenError.clear()
        mViewsGoneWhenError.addAll(views)
    }

}