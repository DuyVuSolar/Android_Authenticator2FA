package com.kuemiin.reversevoice.utils.glide

import android.os.AsyncTask
import com.bumptech.glide.Glide
import com.kuemiin.reversevoice.utils.CommonUtils
import com.kuemiin.reversevoice.BaseApplication.Companion.getAppInstance
import timber.log.Timber

class ClearGlideCacheAsyncTask(private val onSuccess : () -> Unit) : AsyncTask<Void?, Void?, Boolean?>() {
    private var result = false
    override fun doInBackground(vararg params: Void?): Boolean {
        try {
            CommonUtils.isClearing = true
            Glide.get(getAppInstance()).clearDiskCache()
            result = true
        } catch (e: Exception) {
        }
        return result
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        CommonUtils.isClearing = false
        if (this.result) {
            onSuccess.invoke()
            Timber.e("onPostExecute ClearGlideCacheAsyncTask ${System.currentTimeMillis()}")
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Timber.e(System.currentTimeMillis().toString())
    }
}