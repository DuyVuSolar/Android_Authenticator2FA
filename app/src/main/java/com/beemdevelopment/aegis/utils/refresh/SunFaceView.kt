package com.beemdevelopment.aegis.utils.refresh

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import kotlin.math.min

/**
 * Created by cjj on 2016/2/20.
 */
class SunFaceView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mHeight = 0
    private var mWidth = 0
    private var mCirclePaint: Paint? = null //画圆的画笔
    private var mSunRadius = 0 //sun radius
    private var mEyesRadius = DEFAULT_EYES_RADIUS
    private var debugRect: Rect? = null
    private var mouthRect: RectF? = null
    private var mSunColor = 0 //sun color
    private var isDrawFace = true
    private var mouthStro = 3

    init {
        init()
    }

    private fun init() {
        Log.i(Tag, "init")

        mSunRadius = changeDp(DEFAULT_SUN_RADIUS)
        mEyesRadius = changeDp(DEFAULT_EYES_RADIUS)

        //圆的配置
        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCirclePaint!!.color = Color.RED
        mCirclePaint!!.style = Paint.Style.FILL

        debugRect = Rect()
        mouthRect = RectF()
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

    /**
     * 设置眼睛大小
     * @param eyesSize
     */
    fun setEyesSize(eyesSize: Int) {
        mEyesRadius = changeDp(eyesSize)
        invalidate()
    }

    /**
     * 设置嘴巴粗细
     * @param mouthStro
     */
    fun setMouthStro(mouthStro: Int) {
        this.mouthStro = mouthStro
        invalidate()
    }


    /**
     * 刷新用的效果
     * @param sunRadius
     * @param per
     */
    fun setPerView(sunRadius: Int, per: Float) {
        var sunRadius = sunRadius
        var per = per
        sunRadius = changeDp(sunRadius)
        isDrawFace = if (per >= 0.8) {
            true
        } else {
            false
        }
        per = min(per.toDouble(), 1.0).toFloat()
        val tempRadius = (sunRadius * per).toFloat()
        mSunRadius = tempRadius.toInt()
        mCirclePaint!!.alpha = per.toInt() * 255
        invalidate()
    }

    /**
     * 设置sun color
     *
     * @param sunColor
     */
    fun setSunColor(sunColor: Int) {
        mSunColor = sunColor
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i(Tag, "w---->$w  -------  h----->$h")
        mWidth = w
        mHeight = h


        debugRect!!.left = mWidth / 2 - mSunRadius
        debugRect!!.right = mWidth / 2 + mSunRadius
        debugRect!!.top = mHeight / 2 - mSunRadius
        debugRect!!.bottom = mHeight / 2 + mSunRadius


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
            mSunRadius * 2 + paddingRight + paddingLeft
        }

        val height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else {
            mSunRadius * 2 + paddingTop + paddingBottom
        }

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
        mCirclePaint!!.style = Paint.Style.STROKE
        //        canvas.drawRect(debugRect, mCirclePaint);
//        canvas.drawRect(mouthRect, mCirclePaint);
    }


    private fun drawCircle(canvas: Canvas) {
        mCirclePaint!!.color = mSunColor
        mCirclePaint!!.style = Paint.Style.FILL
        canvas.drawCircle(
            (mWidth / 2).toFloat(), (mHeight / 2).toFloat(), mSunRadius.toFloat(),
            mCirclePaint!!
        )
        mCirclePaint!!.color = Color.WHITE
        if (isDrawFace) {
            canvas.save()
            canvas.drawCircle(
                (mWidth / 2 - mSunRadius / 2 + mEyesRadius).toFloat(),
                (mHeight / 2 - mSunRadius / 2 + mEyesRadius).toFloat(),
                mEyesRadius.toFloat(),
                mCirclePaint!!
            )
            canvas.drawCircle(
                (mWidth / 2 + mSunRadius / 2 - mEyesRadius).toFloat(),
                (mHeight / 2 - mSunRadius / 2 + mEyesRadius).toFloat(),
                mEyesRadius.toFloat(),
                mCirclePaint!!
            )
            mCirclePaint!!.style = Paint.Style.STROKE
            mCirclePaint!!.strokeWidth = mouthStro.toFloat()
            canvas.drawArc(mouthRect!!, 20f, 140f, false, mCirclePaint!!)
            canvas.restore()
        }
    }


    fun changeDp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        private val Tag: String = SunFaceView::class.java.simpleName
        private const val DEFAULT_SUN_RADIUS = 12 //太阳的半径
        private const val DEFAULT_EYES_RADIUS = 2 //眼睛的半径
    }
}
