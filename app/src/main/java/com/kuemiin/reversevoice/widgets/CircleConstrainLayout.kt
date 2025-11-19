//package com.kuemiin.reversevoice.widgets
//
//import android.content.Context
//import android.util.AttributeSet
//import android.util.DisplayMetrics
//import carbon.widget.ConstraintLayout
//import com.intuit.sdp.R
//import com.kuemiin.reversevoice.BaseApplication
//
//class CircleConstrainLayout @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null
//) : ConstraintLayout(context, attrs) {
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        val max = getMaxWidthScreen()
//        setCornerRadius((max / 2).toFloat())
//        setMeasuredDimension(max, max)
//    }
//
//    private fun getMaxWidthScreen(): Int {
//        val displayMetrics = DisplayMetrics()
//        BaseApplication.getAppInstance().windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        val dp = resources.getDimensionPixelSize(R.dimen._80sdp)
//        return displayMetrics.widthPixels - dp
//    }
//}