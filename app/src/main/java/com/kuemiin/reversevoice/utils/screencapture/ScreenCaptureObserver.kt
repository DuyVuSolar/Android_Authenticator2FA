package com.kuemiin.reversevoice.utils.screencapture
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import com.kuemiin.reversevoice.utils.extension.loge
import com.kuemiin.reversevoice.utils.loge

class ScreenCaptureObserver(
    handler: Handler,
    private val context: Context,
    private val onScreenCapture: (Uri) -> Unit
) : ContentObserver(handler) {

    @SuppressLint("Range")
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        try {
            uri?.let {
                if(isUriValid(context.contentResolver, it)){
                    val cursor = context.contentResolver.query(
                        it,
                        arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED),
                        null,
                        null,
                        MediaStore.Images.Media.DATE_ADDED + " DESC"
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val displayName = it.getString(it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                            if (displayName.contains("Screenshot") || displayName.contains("Screen capture")) {
                                onScreenCapture(uri)
                            }
                        }
                    }
                }
            }
        }catch (e : Exception){
            loge(e.toString())
        }
    }

    private fun isUriValid(contentResolver: ContentResolver, uri: Uri): Boolean {
        val isValid = contentResolver.getType(uri) != null
        loge(isValid)
        return isValid
    }
}