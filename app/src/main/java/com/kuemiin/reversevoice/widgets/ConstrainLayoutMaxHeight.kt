//package com.kuemiin.reversevoice.widgets
//
//import android.content.Context
//import android.util.AttributeSet
//import android.util.DisplayMetrics
//import carbon.widget.ConstraintLayout
//import com.kuemiin.reversevoice.BaseApplication
//
//
//class ConstrainLayoutMaxHeight @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null
//) : ConstraintLayout(context, attrs) {
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val mWidth = getMaxWidthScreen()
//        setMeasuredDimension(mWidth, heightMeasureSpec)
//    }
//
//    private fun getMaxWidthScreen(): Int {
//        val displayMetrics = DisplayMetrics()
//        BaseApplication.getAppInstance().windowManager?.defaultDisplay?.getMetrics(displayMetrics)
////        val dp = resources.getDimensionPixelSize(R.dimen._25sdp)
//        val dp = 0
//        return displayMetrics.heightPixels - dp
//    }
//}