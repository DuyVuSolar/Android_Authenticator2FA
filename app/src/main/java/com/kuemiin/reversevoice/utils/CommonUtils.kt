package com.kuemiin.reversevoice.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.net.Uri.*
import android.os.Build
import android.os.Environment.*
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.kuemiin.base.extension.windowManager
import com.kuemiin.base.utils.getTypeFromFile
import com.kuemiin.reversevoice.BaseApplication.Companion.getAppInstance
import com.kuemiin.reversevoice.BuildConfig
import com.kuemiin.reversevoice.utils.Constant.MY_FOLDER_EXTERNAL
import com.kuemiin.reversevoice.utils.glide.ClearGlideCacheAsyncTask
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import androidx.core.graphics.createBitmap


@Suppress("DEPRECATION")
object CommonUtils {

    private fun copyFileToDownloads(context: FragmentActivity, input: File): Uri? {
        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, input.name)
                put(MediaStore.MediaColumns.MIME_TYPE, getTypeFromFile(input.absolutePath))
                put(MediaStore.MediaColumns.SIZE, input.length())
            }
            resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            val authority = "${context.packageName}.provider"
            val destinyFile = File(getExternalStorageDirectory().absolutePath, MY_FOLDER_EXTERNAL)
            FileProvider.getUriForFile(context, authority, destinyFile)
        }?.also { downloadedUri ->
            resolver.openOutputStream(downloadedUri).use { outputStream ->
                val brr = ByteArray(1024)
                var len: Int
                val bufferedInputStream = BufferedInputStream(FileInputStream(input.absoluteFile))
                while ((bufferedInputStream.read(brr, 0, brr.size).also { len = it }) != -1) {
                    outputStream?.write(brr, 0, len)
                }
                outputStream?.flush()
                bufferedInputStream.close()
            }
        }
    }

    private fun showDialogAccessPermission(activity: Activity, callback: (() -> Unit)? = null) {
//        ConfirmPermission(activity).apply {
//            setUpData {
//                if (callback == null) {
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        .setData(Uri.fromParts("package", context.packageName, null))
//                    activity.startActivity(intent)
//                } else {
//                    callback.invoke()
//                }
//            }
//            show()
//        }
    }

    fun checkWriteSystemSetting(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    private fun openWriteSettingPermission(
        activity: Activity,
        requestCode: Int = Constant.REQUEST_CODE_OPEN_SETTINGS_SYSTEM
    ) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = parse("package:" + activity.packageName)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivityForResult(intent, requestCode)
    }

    var isClearing = false

    @SuppressLint("DefaultLocale")
    fun clearCache() {
        if (isClearing) {
            return
        }
        ClearGlideCacheAsyncTask {}.execute()
        Glide.get(getAppInstance()).clearMemory()
    }

    @Throws(IOException::class)
    fun createFileFromRaw(
        outputFile: String,
        context: Context,
        inputRawResources: Array<Int>
    ) {
        val outputStream = FileOutputStream(outputFile)
        val resources: Resources = context.resources
        val largeBuffer = ByteArray(1024 * 4)
        var totalBytes = 0
        var bytesRead = 0
        for (resource in inputRawResources) {
            val inputStream: InputStream = resources.openRawResource(resource)
            while (inputStream.read(largeBuffer).also { bytesRead = it } > 0) {
                if (largeBuffer.size == bytesRead) {
                    outputStream.write(largeBuffer)
                } else {
                    val shortBuffer = ByteArray(bytesRead)
                    System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead)
                    outputStream.write(shortBuffer)
                }
                totalBytes += bytesRead
            }
            inputStream.close()
        }
        outputStream.flush()
        outputStream.close()
    }

    fun getMaxWidthScreen(): Int {
        val displayMetrics = DisplayMetrics()
        getAppInstance().windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun shareApp(activity: FragmentActivity) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        sendIntent.type = "text/plain"
        activity.startActivity(sendIntent)
    }

    private var deviceName: String = Build.MODEL // returns model name

    var deviceManufacturer: String = Build.MANUFACTURER // returns manufacturer

    fun sendFb(activity: FragmentActivity, content: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        val mail = "receivefeedback@gmail.com"//TODO ads
        val subject = "Feedback PDFViewer"

        val body = "Version App : ${BuildConfig.VERSION_CODE} \n " +
                "Device model : $deviceName\n" +
                "Android version: ${Build.VERSION.SDK_INT}\n" +
                "Content: $content\n"

        intent.data = parse("mailto:$mail?subject=$subject&body=$body")

        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }

    fun rateApp(activity: FragmentActivity) {
        val uri: Uri = parse("market://details?id=${activity.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    parse("http://play.google.com/store/apps/details?id=${activity.packageName}")
                )
            )
        }
    }

    fun colorHex(color: Int): String {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b)
    }
}


fun createGradientBitmap(
    width: Int,
    height: Int,
    startColor: Int,
    endColor: Int,
    angleDegrees: Float = 270f
): Bitmap {//top to bottom
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // Calculate gradient points based on angle.
    // This is a simplified example for common angles.
    // For arbitrary angles, you'd need more complex trigonometry.
    var x0 = 0f
    var y0 = 0f
    var x1 = 0f
    var y1 = 0f

    when (angleDegrees) {
        0f -> { // Left to Right
            x0 = 0f; y0 = height / 2f; x1 = width.toFloat(); y1 = height / 2f
        }

        45f -> { // Bottom-Left to Top-Right
            x0 = 0f; y0 = height.toFloat(); x1 = width.toFloat(); y1 = 0f
        }

        90f -> { // Bottom to Top
            x0 = width / 2f; y0 = height.toFloat(); x1 = width / 2f; y1 = 0f
        }

        135f -> { // Bottom-Right to Top-Left
            x0 = width.toFloat(); y0 = height.toFloat(); x1 = 0f; y1 = 0f
        }

        180f -> { // Right to Left
            x0 = width.toFloat(); y0 = height / 2f; x1 = 0f; y1 = height / 2f
        }

        270f -> { // Top to Bottom
            x0 = width / 2f; y0 = 0f; x1 = width / 2f; y1 = height.toFloat()
        }

        else -> { // Default to Bottom to Top if angle is unrecognized
            x0 = width / 2f; y0 = height.toFloat(); x1 = width / 2f; y1 = 0f
        }
    }

    val shader = LinearGradient(x0, y0, x1, y1, startColor, endColor, Shader.TileMode.CLAMP)
    paint.shader = shader
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    return bitmap
}



