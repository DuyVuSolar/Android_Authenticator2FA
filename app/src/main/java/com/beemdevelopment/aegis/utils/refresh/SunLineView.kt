package com.beemdevelopment.aegis.utils.refresh

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DrawFilter
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

/**
 * Created by cjj on 2016/2/22.
 */
class SunLineView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mHeight = 0
    private var mWidth = 0
    private var mLinePaint: Paint? = null //线的画笔
    private var mLineLeft = 0
    private var mLineTop = 0 // 线的左、上位置
    private var mLineHeight = 0 // 短线长度
    private var mLineWidth = 0 //线宽度
    private var mFixLineHeight = 0
    private var mLineBottom = 0
    private var mSunRadius = 0
    private var debugRect: Rect? = null
    private var mouthRect: RectF? = null
    private var mDrawFilter: DrawFilter? = null
    private var mLineColor = 0
    private var mLineLevel = 0

    init {
        init()
    }

    private fun init() {
        Log.i(Tag, "init")

        mLineWidth = changeDp(1)
        mLineHeight = changeDp(3)
        mFixLineHeight = changeDp(6)
        mSunRadius = changeDp(12)
        mLineColor = Color.RED
        mLineLevel = 30

        //线的配置
        mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mLinePaint!!.color = mLineColor
        mLinePaint!!.style = Paint.Style.FILL_AND_STROKE
        // 设置画笔宽度
        mLinePaint!!.strokeWidth = mLineWidth.toFloat()
        mDrawFilter = PaintFlagsDrawFilter(
            0, Paint.ANTI_ALIAS_FLAG
                    or Paint.FILTER_BITMAP_FLAG
        )
        debugRect = Rect()
        mouthRect = RectF()
    }

    fun setLineColor(lineColor: Int) {
        mLineColor = lineColor
        invalidate()
    }

    fun setLineWidth(lineWidth: Int) {
        mLineWidth = changeDp(lineWidth)
        invalidate()
    }

    fun setLineHeight(lineHeight: Int) {
        mLineHeight = changeDp(lineHeight)
        mFixLineHeight = mLineHeight * 2
        invalidate()
    }

    /**
     * 设置太阳半径
     *
     * @param sunRadius
     */
    fun setSunRadius(sunRadius: Int) {
        mSunRadius = changeDp(sunRadius)
        invalidate()
    }

    fun setLineLevel(level: Int) {
        mLineLevel = level
        invalidate()
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i(Tag, "w---->$w  -------  h----->$h")
        mWidth = w
        mHeight = h

        mLineLeft = mWidth / 2 - mLineWidth / 2
        mLineTop = h / 2 - mSunRadius - mFixLineHeight
        mLineBottom = mLineTop + mLineHeight

        debugRect!!.left = mWidth / 2 - mSunRadius - mFixLineHeight
        debugRect!!.right = mWidth / 2 + mSunRadius + mFixLineHeight
        debugRect!!.top = mHeight / 2 - mSunRadius - mFixLineHeight
        debugRect!!.bottom = mHeight / 2 + mSunRadius + mFixLineHeight


        mouthRect!!.left = (mWidth / 2 - mSunRadius / 2).toFloat()
        mouthRect!!.right = (mWidth / 2 + mSunRadius / 2).toFloat()
        mouthRect!!.top = (mHeight / 2 - mSunRadius / 2).toFloat()
        mouthRect!!.bottom = (mHeight / 2 + mSunRadius / 2).toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i(Tag, "onMeasure")
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else {
            (mSunRadius + mFixLineHeight + mLineHeight) * 2 + paddingRight + paddingLeft
        }

        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            (mSunRadius + mFixLineHeight + mLineHeight) * 2 + paddingTop + paddingBottom
        }

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas) {
        canvas.drawFilter = mDrawFilter
        super.onDraw(canvas)
        drawLines(canvas)
    }

    /**
     * 绘制line
     * @param canvas
     */
    private fun drawLines(canvas: Canvas) {
        for (i in 0..360) {
            if (i % mLineLevel == 0) {
                canvas.save()
                canvas.rotate(i.toFloat(), (mWidth / 2).toFloat(), (mHeight / 2).toFloat())
                canvas.drawLine(
                    mLineLeft.toFloat(),
                    mLineTop.toFloat(),
                    mLineLeft.toFloat(),
                    mLineBottom.toFloat(),
                    mLinePaint!!
                )
                canvas.restore()
            }
        }
    }

    fun changeDp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private val Tag: String = SunLineView::class.java.simpleName
    }
}
