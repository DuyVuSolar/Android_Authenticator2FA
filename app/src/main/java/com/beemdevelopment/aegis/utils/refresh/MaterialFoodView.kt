package com.beemdevelopment.aegis.utils.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.beemdevelopment.aegis.utils.refresh.Util.dip2px
import com.beemdevelopment.aegis.utils.refresh.Util.limitValue

class MaterialFoodView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    FrameLayout(context, attrs, defStyle),
    com.beemdevelopment.aegis.utils.refresh.MaterialHeadListener {
    private var materialWaveView: MaterialWaveView? = null
    private var circleProgressBar: CircleProgressBar? = null
    private var waveColor = 0
    private var progressTextColor = 0
    private var progress_colors: IntArray = intArrayOf()
    private var progressStokeWidth = 0
    private var isShowArrow = false
    private var isShowProgressBg = false
    private var progressValue = 0
    private var progressValueMax = 0
    private var textType = 0
    private var progressBg = 0
    private var progressSize = 0
    private val listener: com.beemdevelopment.aegis.utils.refresh.MaterialHeadListener? = null


    init {
        init(attrs, defStyle)
    }

    protected fun init(attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode) return
        clipToPadding = false
        setWillNotDraw(false)
    }

    fun getWaveColor(): Int {
        return waveColor
    }

    fun setWaveColor(waveColor: Int) {
        this.waveColor = waveColor
        if (null != materialWaveView) {
            materialWaveView!!.mColor = this.waveColor
        }
    }

    fun setProgressSize(progressSize: Int) {
        this.progressSize = progressSize
    }

    fun setProgressBg(progressBg: Int) {
        this.progressBg = progressBg
    }

    fun setIsProgressBg(isShowProgressBg: Boolean) {
        this.isShowProgressBg = isShowProgressBg
    }

    fun setProgressTextColor(textColor: Int) {
        this.progressTextColor = textColor
    }

    fun setProgressColors(colors: IntArray) {
        this.progress_colors = colors
    }

    fun setTextType(textType: Int) {
        this.textType = textType
    }

    fun setProgressValue(value: Int) {
        this.progressValue = value
        this.post {
            if (circleProgressBar != null) {
                circleProgressBar!!.progress = progressValue
            }
        }
    }

    fun setProgressValueMax(value: Int) {
        this.progressValueMax = value
    }

    fun setProgressStokeWidth(w: Int) {
        this.progressStokeWidth = w
    }

    fun showProgressArrow(isShowArrow: Boolean) {
        this.isShowArrow = isShowArrow
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        materialWaveView = MaterialWaveView(context)
        materialWaveView!!.mColor = waveColor
        addView(materialWaveView)

        circleProgressBar = CircleProgressBar(context)
        val layoutParams = LayoutParams(
            dip2px(
                context, progressSize.toFloat()
            ), dip2px(
                context, progressSize.toFloat()
            )
        )
        layoutParams.gravity = Gravity.CENTER
        circleProgressBar!!.layoutParams = layoutParams
        circleProgressBar!!.setColorSchemeColors(*progress_colors)
        circleProgressBar!!.progressStokeWidth = progressStokeWidth
        circleProgressBar!!.isShowArrow = isShowArrow
        circleProgressBar!!.isShowProgressText = textType == 0
        circleProgressBar!!.setTextColor(progressTextColor)
        circleProgressBar!!.progress = progressValue
        circleProgressBar!!.max = progressValueMax
        circleProgressBar!!.setCircleBackgroundEnabled(isShowProgressBg)
        circleProgressBar!!.setProgressBackGroundColor(progressBg)
        addView(circleProgressBar)
    }

    override fun onComlete(materialRefreshLayout: MaterialRefreshLayout) {
        if (materialWaveView != null) {
            materialWaveView!!.onComlete(materialRefreshLayout)
        }
        if (circleProgressBar != null) {
            circleProgressBar!!.onComlete(materialRefreshLayout)
            ViewCompat.setTranslationY(circleProgressBar, 0f)
            ViewCompat.setScaleX(circleProgressBar, 0f)
            ViewCompat.setScaleY(circleProgressBar, 0f)
        }
    }

    override fun onBegin(materialRefreshLayout: MaterialRefreshLayout) {
        if (materialWaveView != null) {
            materialWaveView!!.onBegin(materialRefreshLayout)
        }
        if (circleProgressBar != null) {
            circleProgressBar!!.onBegin(materialRefreshLayout)
            ViewCompat.setScaleX(circleProgressBar, 1f)
            ViewCompat.setScaleY(circleProgressBar, 1f)
        }
    }

    override fun onPull(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
        if (materialWaveView != null) {
            materialWaveView!!.onPull(materialRefreshLayout, fraction)
        }
        if (circleProgressBar != null) {
            circleProgressBar!!.onPull(materialRefreshLayout, fraction)
            val a = limitValue(1f, fraction)
            ViewCompat.setScaleX(circleProgressBar, 1f)
            ViewCompat.setScaleY(circleProgressBar, 1f)
            ViewCompat.setAlpha(circleProgressBar, a)
        }
    }

    override fun onRelease(materialRefreshLayout: MaterialRefreshLayout, fraction: Float) {
    }

    override fun onRefreshing(materialRefreshLayout: MaterialRefreshLayout) {
        if (materialWaveView != null) {
            materialWaveView!!.onRefreshing(materialRefreshLayout)
        }
        if (circleProgressBar != null) {
            circleProgressBar!!.onRefreshing(materialRefreshLayout)
        }
    }
}


