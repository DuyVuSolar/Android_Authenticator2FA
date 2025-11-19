package com.kuemiin.reversevoice.base

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuemiin.reversevoice.ui.dialog.NoInternetDialog
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.NetworkUtils
import kotlinx.coroutines.launch
import kotlin.coroutines.*

abstract class BaseViewModel : ViewModel() {

    suspend fun Any.await(): Any {
        return suspendCoroutine {
            it.resume(this)
        }
    }

    fun onClickNothing() = viewModelScope.launch {

    }

//    private var dialogInternet : NoInternetDialog? = null
//    fun checkInternetTo(callback: () -> Unit){
//        activity?.let {
//            if(NetworkUtils.isNetworkConnected(it)){
//                callback.invoke()
//            }else{
//                if(dialogInternet == null) dialogInternet = NoInternetDialog(it)
//                dialogInternet?.apply{
//                    setUpData {
//                        callback.invoke()
//                    }
//                    show()
//                }
//            }
//        }
//    }

    @SuppressLint("StaticFieldLeak")
    var activity: FragmentActivity? = null

    private class AndroidContinuation<T>(val continuation: Continuation<T>) :
        Continuation<T> by continuation {
        override fun resumeWith(result: Result<T>) {
            if (result.isSuccess) {
                postOnMainThread { continuation.resume(result.getOrThrow()) }
            } else if (result.exceptionOrNull() != null) {
                postOnMainThread { continuation.resumeWithException(result.exceptionOrNull()!!) }
            }
        }

        inline fun postOnMainThread(crossinline expr: () -> Unit) {
            if (Looper.myLooper() == Looper.getMainLooper()) expr()
            else Handler(Looper.getMainLooper()).post { expr() }
        }
    }

    object Android : AbstractCoroutineContextElement(ContinuationInterceptor),
        ContinuationInterceptor {
        override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            AndroidContinuation(continuation)
    }

    fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }
}