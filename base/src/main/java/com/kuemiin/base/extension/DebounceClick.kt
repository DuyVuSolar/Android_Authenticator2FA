package com.kuemiin.base.extension

import android.os.SystemClock
import android.view.View
private const val DEFAULT_DEBOUNCE_INTERVAL = 500L

private var mLastClickTime = 0L

abstract class DebounceClickListener(
    private val maxTime: Long = DEFAULT_DEBOUNCE_INTERVAL
) : View.OnClickListener {

    // region override function
    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < maxTime)
            return
        mLastClickTime = SystemClock.elapsedRealtime()
        onDebounceClick(v)
    }
    // endregion

    // region abstract function
    abstract fun onDebounceClick(v: View?)
    // endregion
}


fun View.onDebounceClick(
    maxTime: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: (view: View?) -> Unit
) {
    when (this) {
        else -> {
            setOnClickListener(object : DebounceClickListener(maxTime) {
                override fun onDebounceClick(v: View?) = onClick(v)
            })
        }
    }
}


/*
// Thử nghiệm
@ExperimentalCoroutinesApi
fun View.onClicked() = callbackFlow {
    setOnClickListener { offer(Unit) }
    awaitClose { setOnClickListener(null) }
}

@FlowPreview
fun Fragment.debounceClick(
    view: View,
    maxTime: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: (view: View?) -> Unit
) {
    // #1
    view.onClicked()
        .debounce(maxTime)
        .onEach {
            onClick(view)
        }
        .launchIn(viewLifecycleOwner.lifecycleScope)
    // #2
    view.onClicked()
        .throttleFirst(maxTime)
        .onEach {
            onClick(view)
        }
        .launchIn(viewLifecycleOwner.lifecycleScope)
}

@FlowPreview
fun View.debounceClick(
    maxTime: Long = DEFAULT_DEBOUNCE_INTERVAL,
    onClick: (view: View?) -> Unit
) {
    // #1
    this.onClicked()
        .debounce(maxTime)
        .onEach {
            onClick(this)
        }
        .launchIn(GlobalScope)
    // #2
    this.onClicked()
        .throttleFirst(maxTime)
        .onEach {
            onClick(this)
        }
        .launchIn(GlobalScope)
}

@FlowPreview
@ExperimentalCoroutinesApi
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { upstream ->
        val currentTime = System.currentTimeMillis()
        val mayEmit = currentTime - lastEmissionTime > windowDuration
        if (mayEmit) {
            lastEmissionTime = currentTime
            emit(upstream)
        }
    }
}*/
