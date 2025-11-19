package com.kuemiin.reversevoice.utils.refresh

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.kuemiin.reversevoice.utils.refresh.Util.limitValue

/**
 * Created by cjj on 2016/2/22.
 */
class SunLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr),
    com.kuemiin.reversevoice.utils.refresh.MaterialHeadListener {
    protected var mSunView: SunFaceView? = null
    protected var mLineView: SunLineView? = null
    private var mSunRadius = 0
    private var mSunColor = 0
    private var mEyesSize = 0
    private var mLineLevel = 0
    private var mMouthStro = 0
    private var mLineColor = 0
    private var mLineWidth = 0
    private var mLineHeight = 0

    private var mAnimator: ObjectAnimator? = null

    init {
        init()
    }

    private fun init() {
        /**
         * 这里偷懒了，如果你想在xml配置太阳的各个属性，可以在这里设置，然后传给SunFaceView和SunLineView就可以了
         */
        mSunRadius = DEFAULT_SUN_RADIUS
        mSunColor = DEFAULT_SUN_COLOR
        mEyesSize = DEFAULT_SUN_EYES_SIZE
        mLineColor = DEFAULT_LINE_COLOR
        mLineHeight = DEFAULT_LINE_HEIGHT
        mLineWidth = DEFAULT_LINE_WIDTH
        mLineLevel = DEFAULT_LINE_LEVEL
        mMouthStro = DEFAULT_MOUTH_WIDTH

        val context = context
        mSunView = SunFaceView(context)
        mSunView!!.setSunRadius(mSunRadius)
        mSunView!!.setSunColor(mSunColor)
        mSunView!!.setEyesSize(mEyesSize)
        mSunView!!.setMouthStro(mMouthStro)
        addView(mSunView)

        mLineView = SunLineView(context)
        mLineView!!.setSunRadius(mSunRadius)
        mLineView!!.setLineLevel(mLineLevel)
        mLineView!!.setLineColor(mLineColor)
        mLineView!!.setLineHeight(mLineHeight)
        mLineView!!.setLineWidth(mLineWidth)
        addView(mLineView)

        startSunLineAnim(mLineView)
    }

    /**
     * 设置太阳半径
     *
     * @param sunRadius
     */
    fun setSunRadius(sunRadius: Int) {
        mSunRadius = sunRadius
        mSunView!!.setSunRadius(mSunRadius)
        mLineView!!.setSunRadius(mSunRadius)
    }

    /**
     * 设置太阳颜色
     *
     * @param sunColor
     */
    fun setSunColor(sunColor: Int) {
        mSunColor = sunColor
        mSunView!!.setSunColor(mSunColor)
    }

    /**
     * 设置太阳眼睛大小
     *
     * @param eyesSize
     */
    fun setEyesSize(eyesSize: Int) {
        mEyesSize = eyesSize
        mSunView!!.setEyesSize(mEyesSize)
    }

    /**
     * 设置太阳线的数量等级
     *
     * @param level
     */
    fun setLineLevel(level: Int) {
        mLineLevel = level
        mLineView!!.setLineLevel(mLineLevel)
    }

    /**
     * 设置太阳线的颜色
     *
     * @param lineColor
     */
    fun setLineColor(lineColor: Int) {
        mLineColor = lineColor
        mLineView!!.setLineColor(mLineColor)
    }

    /**
     * 设置太阳线的宽度
     *
     * @param lineWidth
     */
    fun setLineWidth(lineWidth: Int) {
        mLineWidth = lineWidth
        mLineView!!.setLineWidth(mLineWidth)
    }

    /**
     * 设置太阳线的长度
     *
     * @param lineHeight
     */
    fun setLineHeight(lineHeight: Int) {
        mLineHeight = lineHeight
        mLineView!!.setLineHeight(mLineHeight)
    }

    /**
     * 设置嘴巴粗细
     *
     * @param mouthStro
     */
    fun setMouthStro(mouthStro: Int) {
        mMouthStro = mouthStro
        mSunView!!.setMouthStro(mMouthStro)
    }


    /**
     * 开启转圈圈
     *
     * @param v
     */
    fun startSunLineAnim(v: View?) {
        if (mAnimator == null) {
            mAnimator = ObjectAnimator.ofFloat(v, "rotation", 0f, 720f)
            mAnimator?.setDuration((7 * 1000).toLong())
            mAnimator?.setInterpolator(LinearInterpolator())
            mAnimator?.setRepeatCount(ValueAnimator.INFINITE)
        }
        if (!mAnimator!!.isRunning) mAnimator!!.start()
    }

    /**
     * 停止动画
     */
    fun cancelSunLineAnim() {
        if (mAnimator != null) {
            mAnimator!!.cancel()
        }
    }

    override fun onComlete(materialRefreshLayout: MaterialRefreshLayout) {
        cancelSunLineAnim()
        ViewCompat.setScaleX(this, 0f)
        ViewCompat.setScaleY(this, 0f)
    }

    override fun onBegin(materialRefreshLayout: MaterialRefreshLayout) {
        ViewCompat.setScaleX(this, 0.001f)
        ViewCompat.setScaleY(this, 0.001f)
    }

    override fun onPull(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
        val a = limitValue(1f, fraction)
        if (a >= 0.7) {
            mLineView!!.visibility = VISIBLE
        } else {
            mLineView!!.visibility = GONE
        }
        mSunView!!.setPerView(mSunRadius, a)
        ViewCompat.setScaleX(this, a)
        ViewCompat.setScaleY(this, a)
        ViewCompat.setAlpha(this, a)
    }

    override fun onRelease(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
    }

    override fun onRefreshing(materialRefreshLayout: MaterialRefreshLayout) {
        startSunLineAnim(mLineView)
    }

    companion object {
        private val Tag: String = SunLayout::class.java.simpleName
        protected const val DEFAULT_SUN_RADIUS: Int = 12 //太阳的半径
        private const val DEFAULT_SUN_COLOR = Color.RED
        private const val DEFAULT_SUN_EYES_SIZE = 2
        private const val DEFAULT_LINE_HEIGHT = 3
        private const val DEFAULT_LINE_WIDTH = 1
        private const val DEFAULT_LINE_LEVEL = 30
        private const val DEFAULT_MOUTH_WIDTH = 3
        private const val DEFAULT_LINE_COLOR = Color.RED
    }
}
