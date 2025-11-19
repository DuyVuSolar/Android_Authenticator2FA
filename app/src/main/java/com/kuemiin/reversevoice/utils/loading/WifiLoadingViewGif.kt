package com.kuemiin.reversevoice.utils.loading

import android.content.Context
import android.util.AttributeSet
import pl.droidsonroids.gif.GifImageView


class WifiLoadingViewGif : GifImageView {

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
//            setImageResNormal(R.drawable.ic_wifi_gif)
        }
    }
}