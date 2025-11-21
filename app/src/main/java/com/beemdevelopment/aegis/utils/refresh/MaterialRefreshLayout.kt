package com.beemdevelopment.aegis.utils.refresh

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.utils.refresh.MaterialRefreshLayout
import com.beemdevelopment.aegis.utils.refresh.Util.dip2px
import kotlin.math.max
import kotlin.math.min

class MaterialRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr) {
    private var mMaterialHeaderView: MaterialHeaderView? = null
    private var mMaterialFooterView: MaterialFooterView? = null
    private var mSunLayout: SunLayout? = null
    private var isOverlay = false
    private var waveType = 0
    private var waveColor = 0
    protected var mWaveHeight: Float = 0f
    protected var mHeadHeight: Float = 0f
    private var mChildView: View? = null
    protected var isRefreshing: Boolean = false
    private var mTouchY = 0f
    private var mCurrentY = 0f
    private var decelerateInterpolator: DecelerateInterpolator? = null
    private var headHeight = 0f
    private var waveHeight = 0f
    private var colorSchemeColors: IntArray = intArrayOf()
    private var colorsId = 0
    private var progressTextColor = 0
    private var progressValue = 0
    private var progressMax = 0
    private var showArrow = true
    private var textType = 0
    private var refreshListener: MaterialRefreshListener? = null
    private var showProgressBg = false
    private var progressBg = 0
    private var isShowWave = false
    private var progressSizeType = 0
    private var progressSize = 0
    private var isLoadMoreing = false
    private var isLoadMore = false
    private var isSunStyle = false

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defstyleAttr: Int) {
        if (isInEditMode) {
            return
        }

        if (childCount > 1) {
            throw RuntimeException("can only have one child widget")
        }

        decelerateInterpolator = DecelerateInterpolator(10f)


        val t = context.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialRefreshLayout,
            defstyleAttr,
            0
        )
        isOverlay = t.getBoolean(R.styleable.MaterialRefreshLayout_overlay, false)
        /**attrs for materialWaveView */
        waveType = t.getInt(R.styleable.MaterialRefreshLayout_wave_height_type, 0)
        if (waveType == 0) {
            headHeight = DEFAULT_HEAD_HEIGHT.toFloat()
            waveHeight = DEFAULT_WAVE_HEIGHT.toFloat()
            MaterialWaveView.defaulHeadHeight = DEFAULT_HEAD_HEIGHT
            MaterialWaveView.defaulWaveHeight = DEFAULT_WAVE_HEIGHT
        } else {
            headHeight = hIGHER_HEAD_HEIGHT.toFloat()
            waveHeight = HIGHER_WAVE_HEIGHT.toFloat()
            MaterialWaveView.defaulHeadHeight = hIGHER_HEAD_HEIGHT
            MaterialWaveView.defaulWaveHeight = HIGHER_WAVE_HEIGHT
        }
        waveColor = t.getColor(R.styleable.MaterialRefreshLayout_wave_color, Color.WHITE)
        isShowWave = t.getBoolean(R.styleable.MaterialRefreshLayout_wave_show, true)

        /**attrs for circleprogressbar */
        colorsId = t.getResourceId(
            R.styleable.MaterialRefreshLayout_progress_colors,
            R.array.material_colors
        )
        colorSchemeColors = context.resources.getIntArray(colorsId)
        showArrow = t.getBoolean(R.styleable.MaterialRefreshLayout_progress_show_arrow, true)
        textType = t.getInt(R.styleable.MaterialRefreshLayout_progress_text_visibility, 1)
        progressTextColor =
            t.getColor(R.styleable.MaterialRefreshLayout_progress_text_color, Color.BLACK)
        progressValue = t.getInteger(R.styleable.MaterialRefreshLayout_progress_value, 0)
        progressMax = t.getInteger(R.styleable.MaterialRefreshLayout_progress_max_value, 100)
        showProgressBg =
            t.getBoolean(R.styleable.MaterialRefreshLayout_progress_show_circle_backgroud, true)
        progressBg = t.getColor(
            R.styleable.MaterialRefreshLayout_progress_backgroud_color,
            CircleProgressBar.DEFAULT_CIRCLE_BG_LIGHT
        )
        progressSizeType = t.getInt(R.styleable.MaterialRefreshLayout_progress_size_type, 0)
        progressSize = if (progressSizeType == 0) {
            DEFAULT_PROGRESS_SIZE
        } else {
            BIG_PROGRESS_SIZE
        }
        isLoadMore = t.getBoolean(R.styleable.MaterialRefreshLayout_isLoadMore, false)
        t.recycle()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i(Tag, "onAttachedToWindow")

        val context = context

        mChildView = getChildAt(0)

        if (mChildView == null) {
            return
        }

        setWaveHeight(dip2px(context, waveHeight).toFloat())
        setHeaderHeight(dip2px(context, headHeight).toFloat())

        if (isSunStyle) {
            mSunLayout = SunLayout(context)
            val layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dip2px(context, hIGHER_HEAD_HEIGHT.toFloat())
            )
            layoutParams.gravity = Gravity.TOP
            mSunLayout!!.visibility = GONE
            setHeaderView(mSunLayout!!)
        } else {
            mMaterialHeaderView = MaterialHeaderView(context)
            val layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dip2px(context, hIGHER_HEAD_HEIGHT.toFloat())
            )
            layoutParams.gravity = Gravity.TOP
            mMaterialHeaderView!!.layoutParams = layoutParams
            mMaterialHeaderView!!.setWaveColor(if (isShowWave) waveColor else Color.TRANSPARENT)
            mMaterialHeaderView!!.showProgressArrow(showArrow)
            mMaterialHeaderView!!.setProgressSize(progressSize)
            mMaterialHeaderView!!.setProgressColors(colorSchemeColors)
            mMaterialHeaderView!!.setProgressStokeWidth(PROGRESS_STOKE_WIDTH)
            mMaterialHeaderView!!.setTextType(textType)
            mMaterialHeaderView!!.setProgressTextColor(progressTextColor)
            mMaterialHeaderView!!.setProgressValue(progressValue)
            mMaterialHeaderView!!.setProgressValueMax(progressMax)
            mMaterialHeaderView!!.setIsProgressBg(showProgressBg)
            mMaterialHeaderView!!.setProgressBg(progressBg)
            mMaterialHeaderView!!.visibility = GONE
            setHeaderView(mMaterialHeaderView!!)
        }


        mMaterialFooterView = MaterialFooterView(context)
        val layoutParams2 = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dip2px(context, hIGHER_HEAD_HEIGHT.toFloat())
        )
        layoutParams2.gravity = Gravity.BOTTOM
        mMaterialFooterView!!.layoutParams = layoutParams2
        mMaterialFooterView!!.showProgressArrow(showArrow)
        mMaterialFooterView!!.setProgressSize(progressSize)
        mMaterialFooterView!!.setProgressColors(colorSchemeColors)
        mMaterialFooterView!!.setProgressStokeWidth(PROGRESS_STOKE_WIDTH)
        mMaterialFooterView!!.setTextType(textType)
        mMaterialFooterView!!.setProgressValue(progressValue)
        mMaterialFooterView!!.setProgressValueMax(progressMax)
        mMaterialFooterView!!.setIsProgressBg(showProgressBg)
        mMaterialFooterView!!.setProgressBg(progressBg)
        mMaterialFooterView!!.visibility = GONE
        setFooderView(mMaterialFooterView)
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) return true
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchY = ev.y
                mCurrentY = mTouchY
            }

            MotionEvent.ACTION_MOVE -> {
                val currentY = ev.y
                val dy = currentY - mTouchY
                if (dy > 0 && !canChildScrollUp()) {
                    if (mMaterialHeaderView != null) {
                        mMaterialHeaderView!!.visibility = VISIBLE
                        mMaterialHeaderView!!.onBegin(this)
                    } else if (mSunLayout != null) {
                        mSunLayout!!.visibility = VISIBLE
                        mSunLayout!!.onBegin(this)
                    }
                    return true
                } else if (dy < 0 && !canChildScrollDown() && isLoadMore) {
                    if (mMaterialFooterView != null && !isLoadMoreing) {
                        soveLoadMoreLogic()
                    }
                    return super.onInterceptTouchEvent(ev)
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun soveLoadMoreLogic() {
        isLoadMoreing = true
        mMaterialFooterView!!.visibility = VISIBLE
        mMaterialFooterView!!.onBegin(this)
        mMaterialFooterView!!.onRefreshing(this)
        if (refreshListener != null) {
            refreshListener!!.onRefreshLoadMore(this@MaterialRefreshLayout)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (isRefreshing) {
            return super.onTouchEvent(e)
        }

        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                mCurrentY = e.y
                var dy = mCurrentY - mTouchY
                dy = min((mWaveHeight * 2).toDouble(), dy.toDouble()).toFloat()
                dy = max(0.0, dy.toDouble()).toFloat()
                if (mChildView != null) {
                    val offsetY =
                        (decelerateInterpolator!!.getInterpolation(dy / mWaveHeight / 2) * dy / 2).toFloat()
                    val fraction = offsetY / mHeadHeight
                    if (mMaterialHeaderView != null) {
                        mMaterialHeaderView!!.layoutParams.height = offsetY.toInt()
                        mMaterialHeaderView!!.requestLayout()
                        mMaterialHeaderView!!.onPull(this, fraction)
                    } else if (mSunLayout != null) {
                        mSunLayout!!.layoutParams.height = offsetY.toInt()
                        mSunLayout!!.requestLayout()
                        mSunLayout!!.onPull(this, fraction)
                    }
                    if (!isOverlay) ViewCompat.setTranslationY(mChildView, offsetY)
                }
                return true
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mChildView != null) {
                    if (mMaterialHeaderView != null) {
                        if (isOverlay) {
                            if (mMaterialHeaderView!!.layoutParams.height > mHeadHeight) {
                                updateListener()

                                mMaterialHeaderView!!.layoutParams.height = mHeadHeight.toInt()
                                mMaterialHeaderView!!.requestLayout()
                            } else {
                                mMaterialHeaderView!!.layoutParams.height = 0
                                mMaterialHeaderView!!.requestLayout()
                            }
                        } else {
                            if (ViewCompat.getTranslationY(mChildView) >= mHeadHeight) {
                                createAnimatorTranslationY(
                                    mChildView!!, mHeadHeight,
                                    mMaterialHeaderView!!
                                )
                                updateListener()
                            } else {
                                createAnimatorTranslationY(mChildView!!, 0f, mMaterialHeaderView!!)
                            }
                        }
                    } else if (mSunLayout != null) {
                        if (isOverlay) {
                            if (mSunLayout!!.layoutParams.height > mHeadHeight) {
                                updateListener()

                                mSunLayout!!.layoutParams.height = mHeadHeight.toInt()
                                mSunLayout!!.requestLayout()
                            } else {
                                mSunLayout!!.layoutParams.height = 0
                                mSunLayout!!.requestLayout()
                            }
                        } else {
                            if (ViewCompat.getTranslationY(mChildView) >= mHeadHeight) {
                                createAnimatorTranslationY(mChildView!!, mHeadHeight, mSunLayout!!)
                                updateListener()
                            } else {
                                createAnimatorTranslationY(mChildView!!, 0f, mSunLayout!!)
                            }
                        }
                    }
                }
                return true
            }
        }

        return super.onTouchEvent(e)
    }

    fun setSunStyle(isSunStyle: Boolean) {
        this.isSunStyle = isSunStyle
    }

    fun autoRefresh() {
        this.postDelayed({
            if (!isRefreshing) {
                if (mMaterialHeaderView != null) {
                    mMaterialHeaderView!!.visibility = VISIBLE

                    if (isOverlay) {
                        mMaterialHeaderView!!.layoutParams.height = mHeadHeight.toInt()
                        mMaterialHeaderView!!.requestLayout()
                    } else {
                        createAnimatorTranslationY(
                            mChildView!!, mHeadHeight,
                            mMaterialHeaderView!!
                        )
                    }
                } else if (mSunLayout != null) {
                    mSunLayout!!.visibility = VISIBLE
                    if (isOverlay) {
                        mSunLayout!!.layoutParams.height = mHeadHeight.toInt()
                        mSunLayout!!.requestLayout()
                    } else {
                        createAnimatorTranslationY(mChildView!!, mHeadHeight, mSunLayout!!)
                    }
                }

                updateListener()
            }
        }, 50)
    }

    fun autoRefreshLoadMore() {
        this.post {
            if (isLoadMore) {
                soveLoadMoreLogic()
            } else {
                throw RuntimeException("you must setLoadMore ture")
            }
        }
    }

    fun updateListener() {
        isRefreshing = true

        if (mMaterialHeaderView != null) {
            mMaterialHeaderView!!.onRefreshing(this@MaterialRefreshLayout)
        } else if (mSunLayout != null) {
            mSunLayout!!.onRefreshing(this@MaterialRefreshLayout)
        }

        if (refreshListener != null) {
            refreshListener!!.onRefresh(this@MaterialRefreshLayout)
        }
    }

    fun setLoadMore(isLoadMore: Boolean) {
        this.isLoadMore = isLoadMore
    }

    fun setProgressColors(colors: IntArray) {
        this.colorSchemeColors = colors
    }

    fun setShowArrow(showArrow: Boolean) {
        this.showArrow = showArrow
    }

    fun setShowProgressBg(showProgressBg: Boolean) {
        this.showProgressBg = showProgressBg
    }

    fun setWaveColor(waveColor: Int) {
        this.waveColor = waveColor
    }

    fun setWaveShow(isShowWave: Boolean) {
        this.isShowWave = isShowWave
    }

    fun setIsOverLay(isOverLay: Boolean) {
        this.isOverlay = isOverLay
    }

    //    public void setProgressValue(int progressValue) {
    //        this.progressValue = progressValue;
    //        mMaterialHeaderView.setProgressValue(progressValue);
    //    }
    fun createAnimatorTranslationY(v: View, h: Float, fl: FrameLayout) {
        val viewPropertyAnimatorCompat = ViewCompat.animate(v)
        viewPropertyAnimatorCompat.setDuration(250)
        viewPropertyAnimatorCompat.setInterpolator(DecelerateInterpolator())
        viewPropertyAnimatorCompat.translationY(h)
        viewPropertyAnimatorCompat.start()
        viewPropertyAnimatorCompat.setUpdateListener {
            val height = ViewCompat.getTranslationY(v)
            fl.layoutParams.height = height.toInt()
            fl.requestLayout()
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    fun canChildScrollUp(): Boolean {
        if (mChildView == null) {
            return false
        }
        return ViewCompat.canScrollVertically(mChildView, -1)
    }

    fun canChildScrollDown(): Boolean {
        if (mChildView == null) {
            return false
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView is AbsListView) {
                val absListView = mChildView as AbsListView
                if (absListView.childCount > 0) {
                    val lastChildBottom = absListView.getChildAt(absListView.childCount - 1).bottom
                    return absListView.lastVisiblePosition == absListView.adapter.count - 1 && lastChildBottom <= absListView.measuredHeight
                } else {
                    return false
                }
            } else {
                return ViewCompat.canScrollVertically(mChildView, 1) || mChildView!!.scrollY > 0
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, 1)
        }
    }

    fun setWaveHigher() {
        headHeight = hIGHER_HEAD_HEIGHT.toFloat()
        waveHeight = HIGHER_WAVE_HEIGHT.toFloat()
        MaterialWaveView.defaulHeadHeight = hIGHER_HEAD_HEIGHT
        MaterialWaveView.defaulWaveHeight = HIGHER_WAVE_HEIGHT
    }

    fun finishRefreshing() {
        if (mChildView != null) {
            val viewPropertyAnimatorCompat = ViewCompat.animate(
                mChildView!!
            )
            viewPropertyAnimatorCompat.setDuration(200)
            viewPropertyAnimatorCompat.y(ViewCompat.getTranslationY(mChildView))
            viewPropertyAnimatorCompat.translationY(0f)
            viewPropertyAnimatorCompat.setInterpolator(DecelerateInterpolator())
            viewPropertyAnimatorCompat.start()

            if (mMaterialHeaderView != null) {
                mMaterialHeaderView!!.onComlete(this@MaterialRefreshLayout)
            } else if (mSunLayout != null) {
                mSunLayout!!.onComlete(this@MaterialRefreshLayout)
            }

            if (refreshListener != null) {
                refreshListener!!.onfinish()
            }
        }
        isRefreshing = false
        progressValue = 0
    }

    fun finishRefresh() {
        this.post { finishRefreshing() }
    }

    fun finishRefresh1s() {
        this.postDelayed({ finishRefreshing() }, 200L)
    }

    fun finishRefreshLoadMore() {
        this.post {
            if (mMaterialFooterView != null && isLoadMoreing) {
                isLoadMoreing = false
                mMaterialFooterView!!.onComlete(this@MaterialRefreshLayout)
            }
        }
    }

    private fun setHeaderView(headerView: View) {
        addView(headerView)
    }

    fun setHeader(headerView: View) {
        setHeaderView(headerView)
    }

    fun setFooderView(fooderView: View?) {
        this.addView(fooderView)
    }


    fun setWaveHeight(waveHeight: Float) {
        this.mWaveHeight = waveHeight
    }

    fun setHeaderHeight(headHeight: Float) {
        this.mHeadHeight = headHeight
    }

    fun setMaterialRefreshListener(refreshListener: MaterialRefreshListener?) {
        this.refreshListener = refreshListener
    }

    companion object {
        val Tag: String = MaterialRefreshLayout::class.java.simpleName
        private const val DEFAULT_WAVE_HEIGHT = 140
        private const val HIGHER_WAVE_HEIGHT = 180
        private const val DEFAULT_HEAD_HEIGHT = 70
        private const val hIGHER_HEAD_HEIGHT = 100
        private const val DEFAULT_PROGRESS_SIZE = 50
        private const val BIG_PROGRESS_SIZE = 60
        private const val PROGRESS_STOKE_WIDTH = 3
    }
}
