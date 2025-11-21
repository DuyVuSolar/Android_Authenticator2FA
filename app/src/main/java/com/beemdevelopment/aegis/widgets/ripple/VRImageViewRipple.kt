//package com.beemdevelopment.aegis.widgets.ripple
//
//import android.content.Context
//import android.util.AttributeSet
//import carbon.widget.ImageView
//import com.beemdevelopment.aegis.R
//
//class VRImageViewRipple : ImageView {
//    constructor(context: Context) : super(context) {
//        init(null)
//    }
//
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
//        init(attrs)
//    }
//
//    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        init(attrs)
//    }
//
//    private fun init(attrs: AttributeSet?) {
//        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.GPSImageView, 0, 0)
//        val clickAble = array.getBoolean(R.styleable.GPSImageView_clickAble, true)
//        array.recycle()
//        isClickable = clickAble
//        if (clickAble) {
//            setBackgroundResource(R.drawable.ripple_press_imv)
//        }
//    }
//}