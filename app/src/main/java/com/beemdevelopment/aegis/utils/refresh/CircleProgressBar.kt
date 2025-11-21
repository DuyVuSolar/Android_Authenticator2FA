/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beemdevelopment.aegis.utils.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import com.beemdevelopment.aegis.R
import kotlin.math.min
import androidx.core.content.withStyledAttributes

class CircleProgressBar : AppCompatImageView,
    com.beemdevelopment.aegis.utils.refresh.MaterialHeadListener {
    private var mListener: Animation.AnimationListener? = null
    private var mShadowRadius = 0
    private var mBackGroundColor = 0
    private var mProgressColor = 0
    private var mProgressStokeWidth = 0
    private var mArrowWidth = 0
    private var mArrowHeight = 0
    private var mProgress = 0
    var max: Int = 0
    private var mDiameter = 0
    private var mInnerRadius = 0
    private var mTextPaint: Paint? = null
    private var mTextColor = 0
    private var mTextSize = 0
    var isShowProgressText: Boolean = false
    private var mShowArrow = false
    var mProgressDrawable: MaterialProgressDrawable? = null
    private var mBgCircle: ShapeDrawable? = null
    private var mCircleBackgroundEnabled = false
    private var mColors = intArrayOf(Color.BLACK)

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }


    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            attrs, R.styleable.CircleProgressBar, defStyleAttr, 0
        ) {

            val density = getContext().resources.displayMetrics.density

            mBackGroundColor = getColor(
                R.styleable.CircleProgressBar_mlpb_background_color, DEFAULT_CIRCLE_BG_LIGHT
            )

            mProgressColor = getColor(
                R.styleable.CircleProgressBar_mlpb_progress_color, DEFAULT_CIRCLE_BG_LIGHT
            )
            mColors = intArrayOf(mProgressColor)

            mInnerRadius = getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_inner_radius, -1
            )

            mProgressStokeWidth = getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_progress_stoke_width,
                (STROKE_WIDTH_LARGE * density).toInt()
            )
            mArrowWidth = getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_arrow_width, -1
            )
            mArrowHeight = getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_arrow_height, -1
            )
            mTextSize = getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_progress_text_size,
                (DEFAULT_TEXT_SIZE * density).toInt()
            )
            mTextColor = getColor(
                R.styleable.CircleProgressBar_mlpb_progress_text_color, Color.BLACK
            )

            mShowArrow = getBoolean(R.styleable.CircleProgressBar_mlpb_show_arrow, false)
            mCircleBackgroundEnabled =
                getBoolean(R.styleable.CircleProgressBar_mlpb_enable_circle_background, true)


            mProgress = getInt(R.styleable.CircleProgressBar_mlpb_progress, 0)
            max = getInt(R.styleable.CircleProgressBar_mlpb_max, 100)
            val textVisible = getInt(R.styleable.CircleProgressBar_mlpb_progress_text_visibility, 1)
            if (textVisible != 1) {
                isShowProgressText = true
            }

            mTextPaint = Paint()
            mTextPaint!!.style = Paint.Style.FILL
            mTextPaint!!.color = mTextColor
            mTextPaint!!.textSize = mTextSize.toFloat()
            mTextPaint!!.isAntiAlias = true
        }
        mProgressDrawable = MaterialProgressDrawable(getContext(), this)
        mProgressDrawable!!.setStartEndTrim(0f, 0.75.toFloat())
        super.setImageDrawable(mProgressDrawable)
    }

    fun setProgressBackGroundColor(color: Int) {
        this.mBackGroundColor = color
        invalidate()
    }

    fun setTextColor(color: Int) {
        this.mTextColor = color
    }

    private fun elevationSupported(): Boolean {
        return Build.VERSION.SDK_INT >= 21
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!elevationSupported()) {
            setMeasuredDimension(
                measuredWidth + mShadowRadius * 2, measuredHeight
                        + mShadowRadius * 2
            )
        }
    }

    var progressStokeWidth: Int
        get() = mProgressStokeWidth
        set(mProgressStokeWidth) {
            val density = context.resources.displayMetrics.density
            this.mProgressStokeWidth = (mProgressStokeWidth * density).toInt()
            invalidate()
        }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val density = context.resources.displayMetrics.density
        mDiameter = min(measuredWidth.toDouble(), measuredHeight.toDouble()).toInt()
        if (mDiameter <= 0) {
            mDiameter = density.toInt() * DEFAULT_CIRCLE_DIAMETER
        }
        if (background == null && mCircleBackgroundEnabled) {
            val shadowYOffset = (density * Y_OFFSET).toInt()
            val shadowXOffset = (density * X_OFFSET).toInt()
            mShadowRadius = (density * SHADOW_RADIUS).toInt()

            if (elevationSupported()) {
                mBgCircle = ShapeDrawable(OvalShape())
                ViewCompat.setElevation(this, SHADOW_ELEVATION * density)
            } else {
                val oval: OvalShape = OvalShadow(mShadowRadius, mDiameter - mShadowRadius * 2)
                mBgCircle = ShapeDrawable(oval)
                ViewCompat.setLayerType(this, LAYER_TYPE_SOFTWARE, mBgCircle!!.paint)
                mBgCircle!!.paint.setShadowLayer(
                    mShadowRadius.toFloat(), shadowXOffset.toFloat(), shadowYOffset.toFloat(),
                    KEY_SHADOW_COLOR
                )
                val padding = mShadowRadius
                // set padding so the inner image sits correctly within the shadow.
                setPadding(padding, padding, padding, padding)
            }
            mBgCircle!!.paint.color = mBackGroundColor
            setBackgroundDrawable(mBgCircle)
        }
        mProgressDrawable!!.setBackgroundColor(mBackGroundColor)
        mProgressDrawable!!.setColorSchemeColors(*mColors)
        mProgressDrawable!!.setSizeParameters(
            mDiameter.toDouble(), mDiameter.toDouble(),
            (if (mInnerRadius <= 0) (mDiameter - mProgressStokeWidth * 2) / 4 else mInnerRadius).toDouble(),
            mProgressStokeWidth.toDouble(),
            (if (mArrowWidth < 0) mProgressStokeWidth * 4 else mArrowWidth).toFloat(),
            (if (mArrowHeight < 0) mProgressStokeWidth * 2 else mArrowHeight).toFloat()
        )
        if (isShowArrow) {
            mProgressDrawable!!.showArrowOnFirstStart(true)
            mProgressDrawable!!.setArrowScale(1f)
            mProgressDrawable!!.showArrow(true)
        }
        super.setImageDrawable(null)
        super.setImageDrawable(mProgressDrawable)
        mProgressDrawable!!.alpha = 255
        if (visibility == VISIBLE) {
            mProgressDrawable!!.setStartEndTrim(0f, 0.8.toFloat())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isShowProgressText) {
            val text = String.format("%s%%", mProgress)
            val x = width / 2 - text.length * mTextSize / 4
            val y = height / 2 + mTextSize / 4
            canvas.drawText(text, x.toFloat(), y.toFloat(), mTextPaint!!)
        }
    }

    override fun setImageResource(resId: Int) {
    }


    var isShowArrow: Boolean
        get() = mShowArrow
        set(showArrow) {
            this.mShowArrow = showArrow
            invalidate()
        }


    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
    }

    override fun setImageDrawable(drawable: Drawable?) {
    }

    fun setAnimationListener(listener: Animation.AnimationListener?) {
        mListener = listener
    }

    public override fun onAnimationStart() {
        super.onAnimationStart()
        if (mListener != null) {
            mListener!!.onAnimationStart(animation)
        }
    }

    public override fun onAnimationEnd() {
        super.onAnimationEnd()
        if (mListener != null) {
            mListener!!.onAnimationEnd(animation)
        }
    }


    /**
     * Set the color resources used in the progress animation from color resources.
     * The first color will also be the color of the bar that grows in response
     * to a user swipe gesture.
     *
     * @param colorResIds
     */
    fun setColorSchemeResources(vararg colorResIds: Int) {
        val res = resources
        val colorRes = IntArray(colorResIds.size)
        for (i in colorResIds.indices) {
            colorRes[i] = res.getColor(colorResIds[i])
        }
        setColorSchemeColors(*colorRes)
    }

    /**
     * Set the colors used in the progress animation. The first
     * color will also be the color of the bar that grows in response to a user
     * swipe gesture.
     *
     * @param colors
     */
    fun setColorSchemeColors(vararg colors: Int) {
        mColors = colors
        if (mProgressDrawable != null) {
            mProgressDrawable!!.setColorSchemeColors(*colors)
        }
    }

    /**
     * Update the background color of the mBgCircle image view.
     */
    @SuppressLint("ResourceType")
    override fun setBackgroundColor(colorRes: Int) {
        if (background is ShapeDrawable) {
            val res = resources
            (background as ShapeDrawable).paint.color = res.getColor(colorRes)
        }
    }

    var progress: Int
        get() = mProgress
        set(progress) {
            if (max > 0) {
                mProgress = progress
            }
            invalidate()
            Log.i("cjj_log", "progress------->>>>$progress")
        }


    fun circleBackgroundEnabled(): Boolean {
        return mCircleBackgroundEnabled
    }

    fun setCircleBackgroundEnabled(enableCircleBackground: Boolean) {
        this.mCircleBackgroundEnabled = enableCircleBackground
        invalidate()
    }

    override fun getVisibility(): Int {
        return super.getVisibility()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        //        if (mProgressDrawable != null) {
//            mProgressDrawable.setVisible(visibility == VISIBLE, false);
//            if (visibility != VISIBLE) {
//                mProgressDrawable.stop();
//            } else {
//                if (mProgressDrawable.isRunning()) {
//                    mProgressDrawable.stop();
//                }
//                mProgressDrawable.start();
//            }
//        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mProgressDrawable != null) {
            mProgressDrawable!!.stop()
            mProgressDrawable!!.setVisible(visibility == VISIBLE, false)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mProgressDrawable != null) {
            mProgressDrawable!!.stop()
            mProgressDrawable!!.setVisible(false, false)
        }
    }

    override fun onComlete(materialRefreshLayout: MaterialRefreshLayout) {
        if (mProgressDrawable != null) {
            mProgressDrawable!!.stop()
        }
        visibility = INVISIBLE
    }

    override fun onBegin(materialRefreshLayout: MaterialRefreshLayout) {
        visibility = VISIBLE
        //        mProgressDrawable.setStartEndTrim(0, (float) 0.75);
    }

    override fun onPull(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
        mProgressDrawable!!.setProgressRotation(fraction)
    }

    override fun onRelease(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
    }

    override fun onRefreshing(materialRefreshLayout: MaterialRefreshLayout) {
        if (mProgressDrawable != null) {
            mProgressDrawable!!.start()
        }
    }


    private inner class OvalShadow(
        private val mShadowRadius: Int,
        private val mCircleDiameter: Int
    ) : OvalShape() {
        private val mRadialGradient =
            RadialGradient(
                (mCircleDiameter / 2).toFloat(), (mCircleDiameter / 2).toFloat(),
                mShadowRadius.toFloat(), intArrayOf(
                    FILL_SHADOW_COLOR,
                    Color.TRANSPARENT
                ), null, Shader.TileMode.CLAMP
            )
        private val mShadowPaint = Paint()

        init {
            mShadowPaint.setShader(mRadialGradient)
        }

        override fun draw(canvas: Canvas, paint: Paint) {
            val viewWidth = this@CircleProgressBar.width
            val viewHeight = this@CircleProgressBar.height
            canvas.drawCircle(
                (viewWidth / 2).toFloat(),
                (viewHeight / 2).toFloat(),
                (mCircleDiameter / 2 + mShadowRadius).toFloat(),
                mShadowPaint
            )
            canvas.drawCircle(
                (viewWidth / 2).toFloat(),
                (viewHeight / 2).toFloat(),
                (mCircleDiameter / 2).toFloat(),
                paint
            )
        }
    }

    companion object {
        private const val KEY_SHADOW_COLOR = 0x1E000000
        private const val FILL_SHADOW_COLOR = 0x3D000000

        // PX
        private const val X_OFFSET = 0f
        private const val Y_OFFSET = 1.75f
        private const val SHADOW_RADIUS = 3.5f
        private const val SHADOW_ELEVATION = 4


        const val DEFAULT_CIRCLE_BG_LIGHT: Int = -0x50506
        private const val DEFAULT_CIRCLE_DIAMETER = 40
        private const val STROKE_WIDTH_LARGE = 3
        const val DEFAULT_TEXT_SIZE: Int = 9
    }
}
