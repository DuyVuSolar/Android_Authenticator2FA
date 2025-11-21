package com.beemdevelopment.aegis.utils.refresh

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import com.beemdevelopment.aegis.utils.refresh.Util.dip2px
import com.beemdevelopment.aegis.utils.refresh.Util.limitValue
import kotlin.math.max

class MaterialWaveView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    com.beemdevelopment.aegis.utils.refresh.MaterialHeadListener {
    var waveHeight: Int = 0
    var headHeight: Int = 0
    private var path: Path? = null
    private var paint: Paint? = null
    var mColor = 0

    init {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        path = Path()
        paint = Paint()
        paint!!.isAntiAlias = true
    }

    fun getColor(): Int {
        return mColor
    }

    fun setColor(color: Int) {
        this.mColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path!!.reset()
        paint!!.color = mColor
        path!!.lineTo(0f, headHeight.toFloat())
        path!!.quadTo(
            (measuredWidth / 2).toFloat(),
            (headHeight + waveHeight).toFloat(),
            measuredWidth.toFloat(),
            headHeight.toFloat()
        )
        path!!.lineTo(measuredWidth.toFloat(), 0f)
        canvas.drawPath(path!!, paint!!)
    }


    override fun onComlete(br: MaterialRefreshLayout) {
        waveHeight = 0
        val animator = ValueAnimator.ofInt(headHeight, 0)
        animator.setDuration(200)
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            headHeight = value
            invalidate()
        }
    }

    override fun onBegin(br: MaterialRefreshLayout) {
    }

    override fun onPull(br: MaterialRefreshLayout, fraction: Float) {
        headHeight = (dip2px(
            context,
            defaulHeadHeight.toFloat()
        ) * limitValue(1f, fraction)).toInt()
        waveHeight = (dip2px(
            context,
            defaulWaveHeight.toFloat()
        ) * max(0.0, (fraction - 1).toDouble())).toInt()
        invalidate()
    }

    override fun onRelease(br: MaterialRefreshLayout, fraction: Float) {
    }

    override fun onRefreshing(br: MaterialRefreshLayout) {
        headHeight = (dip2px(
            context,
            defaulHeadHeight.toFloat()
        ))
        val animator = ValueAnimator.ofInt(waveHeight, 0)
        animator.addUpdateListener(object : AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                Log.i("anim", "value--->" + animation.animatedValue as Int)
                waveHeight = animation.animatedValue as Int
                invalidate()
            }
        })
        animator.interpolator = BounceInterpolator()
        animator.setDuration(200)
        animator.start()
    }


    companion object {
        var defaulWaveHeight: Int = 0

        var defaulHeadHeight: Int = 0

    }
}
