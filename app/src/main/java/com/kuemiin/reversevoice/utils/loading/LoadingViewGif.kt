package com.kuemiin.reversevoice.utils.loading

import android.content.Context
import android.util.AttributeSet
import pl.droidsonroids.gif.GifImageView


class LoadingViewGif : GifImageView {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        post {
//            setImageResNormal(R.drawable.loading_color)//fix not display in android 8 M20
        }
    }
}