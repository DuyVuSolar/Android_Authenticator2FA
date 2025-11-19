package com.kuemiin.reversevoice.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Color.*
import android.util.AttributeSet
import android.widget.ImageView
import com.kuemiin.reversevoice.utils.getPixels


@SuppressLint("AppCompatCustomView")
class RectangleProgress : ImageView {
    private var path = Path()
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var length = 0f
    private var intervals = floatArrayOf(0f, 0f)
    private var border = getPixels(com.intuit.sdp.R.dimen._6sdp)
    private var strokeW = getPixels(com.intuit.sdp.R.dimen._5sdp)

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
        mPaint.color = parseColor("#B956FF")
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = strokeW.toFloat()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path.reset()
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        val inset = mPaint.strokeWidth
        rect.inset(inset, inset)
        path.addRoundRect(rect, border.toFloat(), border.toFloat(), Path.Direction.CW)
        length = PathMeasure(path, true).length
//        length = PathMeasure(path, false).length
        intervals[0] = length
        intervals[1] = length
        val effect: PathEffect = DashPathEffect(intervals, length)
        mPaint.pathEffect = effect

        val shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
            parseColor("#B956FF"),
            parseColor("#6E33FF"), Shader.TileMode.MIRROR)
        mPaint.setShader(shader)
    }

    fun setProgress(progress: Int) {
        val effect: PathEffect = DashPathEffect(intervals, length - length * progress / 100)
        mPaint.pathEffect = effect
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, mPaint)
    }

}