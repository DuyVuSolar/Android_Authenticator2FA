package com.beemdevelopment.aegis.widgets.ripple

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.beemdevelopment.aegis.R

class VRTextViewRipple : AppCompatTextView, IRippleClick {
    constructor(context: Context) : super(context){
        init(null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        init(null)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init(null)
    }


    private fun init(attrs: AttributeSet?) {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.GPSImageView, 0, 0)
        val clickAble = array.getBoolean(R.styleable.GPSImageView_clickAble, true)
        array.recycle()
        isClickable = clickAble
        if (clickAble) {
            setBackgroundResource(R.drawable.ripple_press_imv)
        }
    }
}