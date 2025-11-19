package com.kuemiin.reversevoice.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.Dimension
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.kuemiin.base.extension.isBuildLargerThan
import com.kuemiin.reversevoice.BaseApplication.Companion.getAppInstance
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


fun Any.log() {
    Timber.tag("COUNTDOWN").d(if (this is String) this else this.toString())
}

private var currentToast : Toast? = null
fun Fragment.toast(text : String){
    requireActivity().runOnUiThread {
        if(currentToast?.view?.isShown == true){
            currentToast?.setText(text)
        }else{
            currentToast = Toast.makeText(getAppInstance(), text, Toast.LENGTH_SHORT)
            currentToast?.show()
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("ShowToast")
fun Context.showToast(message: String) {
    try {
        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
            currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            when {
                currentToast == null -> {
                    // Create toast if found null, it would he the case of first call only
                    currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                currentToast!!.view == null -> {
                    // Toast not showing, so create new one
                    currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                else -> {
                    // Updating toast message is showing
                    currentToast!!.setText(message)
                }
            }
        }
        // Showing toast finally
        currentToast?.show()
    }catch (e : Exception){

    }
}

internal fun postDelayInTime(callback: () -> Unit, time: Long = 500L) {
    Handler().postDelayed({
        callback.invoke()
    }, time)
}

fun View.visible(timeDelay : Long = 0L) {
    postDelayed({
        this.visibility = View.VISIBLE
    }, timeDelay)
}
fun View.gone(timeDelay : Long = 0L) {
    postDelayed({
        this.visibility = View.GONE
     }, timeDelay)
}
fun View.invisible(timeDelay : Long = 0L) {
    postDelayed({
        this.visibility = View.INVISIBLE
    }, timeDelay)
}

@Dimension
fun View.getPixels(@DimenRes id: Int): Int =
    resources.getDimensionPixelSize(id)

fun <T> Fragment.observer(liveData: LiveData<T>?, onDataChange: (T?) -> Unit) {
    liveData?.observe(viewLifecycleOwner, Observer(onDataChange))
}


fun <T> AppCompatActivity.observer(liveData: LiveData<T>?, onDataChange: (T?) -> Unit) {
    liveData?.observe(this, Observer(onDataChange))
}


fun Any.loge(message: Any?) {
    val newMes = message ?: "null"
    Timber.e("REVERSE123$newMes")
}

fun isAndroid13() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
fun isAndroidR() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R//android 11
fun isAndroidQ() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q//android 10
fun isAndroid9() : Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P//android 9

fun String.isImage() : Boolean{
    return this.endsWith(".png") || this.endsWith(".jpg") || this.endsWith(".jpeg")
}

fun <T : Any> addItemsAdsToList(originalList: List<T>, native : T, range: Int): ArrayList<T> {
    val updatedList = ArrayList<T>()
    for (i in originalList.indices) {
        updatedList.add(originalList[i])
        // Add the new item every 'range' items
        if ((i + 1) % range == 0 && (i + 1) < originalList.size) {
            updatedList.add(native)
        }
    }
    return updatedList
}

fun <T : Any> addItemsAdsToList(originalList: ArrayList<T>, native : T, range: Int): ArrayList<T> {
    val updatedList = ArrayList<T>()
    for (i in originalList.indices) {
        updatedList.add(originalList[i])
        // Add the new item every 'range' items
        if ((i + 1) % range == 0 && (i + 1) < originalList.size) {
            updatedList.add(native)
        }
    }
    return updatedList
}


private var jobLoading: Job? = null
fun <T> doJob(
    doIn: (scopeDoIn: CoroutineScope) -> T,
    doOut: (T) -> Unit = {},
    dispatcherIn: CoroutineDispatcher = Dispatchers.IO,
    dispathcherOut: CoroutineDispatcher = Dispatchers.Unconfined
) {
    jobLoading = async(doIn, doOut, dispatcherIn, dispathcherOut)
}

fun <T> async(
    doIn: (scopeDoIn: CoroutineScope) -> T,
    doOut: (T) -> Unit = {},
    dispatcherIn: CoroutineDispatcher = Dispatchers.IO,
    dispathcherOut: CoroutineDispatcher = Dispatchers.Unconfined
): Job {
    return GlobalScope.launch(dispatcherIn) {
        val data = doIn(this)
        withContext(dispathcherOut) {
            doOut(data)
        }
    }
}


