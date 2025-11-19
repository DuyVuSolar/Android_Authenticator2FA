@file:Suppress("LABEL_NAME_CLASH")
package com.kuemiin.reversevoice.utils.binding

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MotionEventCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.kuemiin.base.extension.NAVIGATION_BAR_HEIGHT
import com.kuemiin.base.extension.STATUS_BAR_HEIGHT
import com.kuemiin.reversevoice.utils.extension.toPixel
import com.kuemiin.reversevoice.BaseApplication.Companion.getAppInstance
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.FlagPack
import com.kuemiin.reversevoice.utils.NotchUtils.getInternalDimensionSize
import com.kuemiin.reversevoice.utils.extension.gone
import com.kuemiin.reversevoice.utils.extension.visible
import com.kuemiin.reversevoice.utils.invisible
import com.kuemiin.reversevoice.utils.loge
import com.kuemiin.reversevoice.widgets.CircularProgressView
import java.util.*


@BindingAdapter("deboundClick")
fun View.onDeboundClick(listener: View.OnClickListener) {
    this.onDebouncedClick {
        listener.onClick(this)
    }
}
@BindingAdapter("deboundLongClick")
fun View.deboundLongClick(listener: View.OnClickListener) {
    this.onDebouncedClick(2000L) {
        listener.onClick(this)
    }
}

@SuppressLint("ClickableViewAccessibility")
@BindingAdapter("onToucheDown")
fun View.onToucheDown(listener: View.OnClickListener) {
    this.setOnTouchListener { v, event ->
        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
            listener.onClick(v)
        }
        return@setOnTouchListener false
    }
}

@BindingAdapter("setImageAnyNormal")
fun ImageView.setImageAnyNormal(res: Any?) {
    Glide.with(getAppInstance())
        .load(res)
        .override(width, height)
        .thumbnail()
        .into(this)
}

@BindingAdapter("setImageAnySkipWH")
fun ImageView.setImageAnySkipWH(res: Any?) {
    Glide.with(getAppInstance())
        .load(res)
        .dontAnimate()
        .thumbnail()
        .into(this)
}


@BindingAdapter("setImageAnySkipWHCached")
fun ImageView.setImageAnySkipWHCached(res: Any?) {
    Glide.with(getAppInstance())
        .load(res)
        .dontAnimate()
        .placeholder(null)
        .into(this)
}


@BindingAdapter("setImageResNoCache")
fun ImageView.setImageResNoCache(res: Int) {
    Glide.with(getAppInstance())
        .load(res)
        .override(width, height)
        .skipMemoryCache(true)
        .into(this)
}

@BindingAdapter("setImageResCache")
fun ImageView.setImageResCache(res: Int) {
    Glide.with(getAppInstance())
        .load(res)
        .override(width, height)
        .into(this)
}

@BindingAdapter("setImageCenter")
fun ImageView.setImageCenter(url: String) {
    Glide.with(getAppInstance())
        .load(url)
        .override(width, height)
        .centerCrop()
        .into(this)
}

@BindingAdapter("setImageDrawableNoCache")
fun ImageView.setImageDrawableNoCache(drawable: Drawable) {
    Glide.with(getAppInstance())
        .load(drawable)
        .override(width, height)
        .skipMemoryCache(true)
        .into(this)
}


@BindingAdapter("marginTop")
fun View.setMarginTop(space: Float) {
    when (this.layoutParams) {
        is ConstraintLayout.LayoutParams -> {
            val params = this.layoutParams as ConstraintLayout.LayoutParams
            val statusBarHeight = this.context.getInternalDimensionSize(
                STATUS_BAR_HEIGHT
            )

            val actionBarSize = statusBarHeight + space.toPixel
            params.setMargins(0, actionBarSize.toInt(), 0, 0)
        }

        is FrameLayout.LayoutParams -> {
            val params = this.layoutParams as FrameLayout.LayoutParams
            val statusBarHeight = this.context.getInternalDimensionSize(
                STATUS_BAR_HEIGHT
            )

            val actionBarSize = statusBarHeight + space.toPixel
            params.setMargins(0, actionBarSize.toInt(), 0, 0)
        }

        is LinearLayout.LayoutParams -> {
            val params = this.layoutParams as LinearLayout.LayoutParams
            val statusBarHeight = this.context.getInternalDimensionSize(
                STATUS_BAR_HEIGHT
            )

            val actionBarSize = statusBarHeight + space.toPixel
            params.setMargins(0, actionBarSize.toInt(), 0, 0)
        }
    }
}

@BindingAdapter("setTimeFromCalendar")
fun TextView.setTimeFromCalendar(time: Long) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    var defaultLeft = calendar.get(Calendar.HOUR_OF_DAY)
    if (defaultLeft > 12) defaultLeft -= 12
    val defaultRight = calendar.get(Calendar.MINUTE)
    val result = String.format(Locale.US, "%02d:%02d", defaultLeft, defaultRight)
    text = "$result"
}

@BindingAdapter("setAmPmCalendar")
fun TextView.setAmPmCalendar(time: Long) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = time
    val am_pm = calendar.get(Calendar.AM_PM)
    if (am_pm == Calendar.AM) {
        text = "am"
    } else if (am_pm == Calendar.PM) {
        text = "pm"
    }
}

@SuppressLint("SetTextI18n")
@BindingAdapter("setTimePrepare")
fun TextView.setTimePrepare(time: Int) {
    text = "${time}s"
}

@SuppressLint("SetTextI18n")
@BindingAdapter("animationFromBottom")
fun View.animationFromBottom(isVisible: Boolean) {
    if (isVisible) {
        visible()
        animate().translationY(0f).setDuration(200).start()
    } else {
        animate().translationY(height.toFloat()).setDuration(200).start()
        gone()
    }
}

@BindingAdapter("setEnableView")
fun View.setEnableView(enable: Boolean) {
    isEnabled = enable
}

@BindingAdapter("setEnableViewAlpha")
fun View.setEnableViewAlpha(enable: Boolean) {
    isEnabled = enable
    alpha = if (isEnabled) {
        1f
    } else {
        0.5f
    }
}
@BindingAdapter("loadFlagIcon")
fun carbon.widget.ImageView.loadFlagIcon(alpha : String) {
    val country = FlagPack.alpha2ToFlag[alpha]
    country?.let { this.setImageResCache(it) }
}

@BindingAdapter("marginBottom")
fun View.setMarginBottom(space: Float) {
    val marginHori = 0.toPixel.toInt()
    if (this.layoutParams is ConstraintLayout.LayoutParams) {
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        val navigationBarHeight = this.context.getInternalDimensionSize(
            NAVIGATION_BAR_HEIGHT
        )
        val actionBarSize = navigationBarHeight + space.toPixel
        params.setMargins(marginHori, 0, marginHori, actionBarSize.toInt())
    }else if (this.layoutParams is RelativeLayout.LayoutParams) {
        val params = this.layoutParams as RelativeLayout.LayoutParams
        val navigationBarHeight = this.context.getInternalDimensionSize(
            NAVIGATION_BAR_HEIGHT
        )
        val actionBarSize = navigationBarHeight + space.toPixel
        params.setMargins(marginHori, 0, marginHori, actionBarSize.toInt())
    }
}


fun Long.longDurationToString() : String{
    var seconds = (this % 60000 / 1000).toString()
    val minutes = (this / 60000).toString()
    return if (seconds.length == 1) {
        "0$minutes:0$seconds"
    } else {
        "0$minutes:$seconds"
    }
}


@BindingAdapter("setZoomAnimationLoop")
fun View.setZoomAnimationLoop(isAnimate: Boolean) {
    val zoomAnimation: Animation = AnimationUtils.loadAnimation(this.context, R.anim.zoom_loop)
    if (isAnimate) {
        startAnimation(zoomAnimation)
    } else {
        clearAnimation() // Stop animation on the play button
    }
}

@BindingAdapter("setProgressTimer")
fun CircularProgressView.setProgressTimer(value : Float) {
    val limit = Constant.timeRecord.toFloat()
    val progress = (value / 1000f) % (limit)
    val percent = progress / limit * 100f
    loge("setProgressTimer ---- $value ---- $percent")
    if(this.getProgress() > percent){
        this.invisible()
    }else{
        if(percent > 100f){
            setProgress(100f, false)
        }else{
            setProgress(percent, true)
        }
    }
}


@SuppressLint("SetTextI18n")
@BindingAdapter("setProgressDownload")
fun TextView.setProgressDownload(percent : Float) {
    text = "${percent.toInt()}%"
}

@BindingAdapter("setDurationInt")
fun TextView.setDurationInt(time :Int) {
    var seconds = (time % 60000 / 1000).toString()
    val minutes = (time / 60000).toString()
    text = if (seconds.length == 1) {
        "0$minutes:0$seconds"
    } else {
        "0$minutes:$seconds"
    }
}

@BindingAdapter("setProgressSeekbarFloat")
fun AppCompatSeekBar.setProgressSeekbarFloat(value: Float) {
    progress = value.toInt()
//    requestLayout()
}


