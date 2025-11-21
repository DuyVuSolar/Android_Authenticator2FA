package com.beemdevelopment.aegis.utils.extension

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import com.beemdevelopment.aegis.BaseApplication
import timber.log.Timber
import java.net.Inet6Address
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun Any.loge(message: String?) {
    val newMes = message ?: "null"
    Timber.e("COUNTDOWN123 $newMes")
}

fun toast(text : String){
    Toast.makeText(getApplication(), text, Toast.LENGTH_LONG).show()
}

fun toast(resId : Int){
    Toast.makeText(getApplication(), resId, Toast.LENGTH_LONG).show()
}

private var appInstance: Application? = null

fun Application.initBaseApplication() {
    appInstance = this
}

fun getApplication() = appInstance ?: BaseApplication.getAppInstance()


fun formatFullTime(timeInMillis: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val mm = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val ss = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hour, mm, ss)
}

fun formatFullTimeSrt(timeInSecond: Long): String {
    val hour = TimeUnit.SECONDS.toHours(timeInSecond)
    val mm = TimeUnit.SECONDS.toMinutes(timeInSecond) % 60
    val ss = TimeUnit.SECONDS.toSeconds(timeInSecond) % 60
    return String.format(Locale.US, "%02d:%02d:%02d,000", hour, mm, ss)
}

fun formatFullTimeSrtStart(timeInSecond: Float): String {
    val hour = TimeUnit.SECONDS.toHours(timeInSecond.toLong())
    val mm = TimeUnit.SECONDS.toMinutes(timeInSecond.toLong()) % 60
    val ss = TimeUnit.SECONDS.toSeconds(timeInSecond.toLong()) % 60
    return if(isInteger(timeInSecond)){
        String.format(Locale.US, "%02d:%02d:%02d,000", hour, mm, ss)
    }else{
        String.format(Locale.US, "%02d:%02d:%02d,500", hour, mm, ss)
    }
}

fun isInteger(floatNumber: Float): Boolean {
    // Convert float value
    // of N to integer
    val X = floatNumber.toInt()
    val temp2 = floatNumber - X

    // If N is not equivalent
    // to any integer
    return temp2 <= 0
}


fun formatTimePattern(
    timeInMillis: Long?,
    pattern: String,
    locale: Locale = Locale.US
): String {
    val sdf = SimpleDateFormat(pattern, locale)
    return sdf.format(timeInMillis ?: 0)
}

val <T> T.exhaustive: T
    get() = this

fun formatIntTwoDigits(value : Int): String {
    return String.format(Locale.US, "%02d", value)
}

fun formatTimeFull(timeInSecond: Long): String {
    val hour = TimeUnit.SECONDS.toHours(timeInSecond)
    val mm = TimeUnit.SECONDS.toMinutes(timeInSecond) % 60
    val ss = TimeUnit.SECONDS.toSeconds(timeInSecond) % 60
    return if (hour == 0L) String.format(
        Locale.US,
        "%02d:%02d",
        mm,
        ss
    ) else String.format(Locale.US, "%02d:%02d:%02d", hour, mm, ss)
}

fun formatTime(timeInSecond: Long): String {
    val mm = TimeUnit.SECONDS.toMinutes(timeInSecond)
    val ss = TimeUnit.SECONDS.toSeconds(timeInSecond) % 60
    return String.format(Locale.US, "%02d:%02d", mm, ss)
}

fun formatTimeVideo(timeInMillis: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val mm = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val ss = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return if (hour == 0L) String.format(
        Locale.US,
        "%02d:%02d",
        mm,
        ss
    ) else String.format(Locale.US, "%02d:%02d:%02d", hour, mm, ss)
}

fun formatTimeVideoNotHour(timeInMillis: Long): String {
    val hour = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val mm = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val ss = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return String.format(Locale.US, "%02d:%02d", mm + hour * 60, ss)
}

internal fun postDelayInTime(callback: () -> Unit, time: Long = 500L) {
    Handler().postDelayed({
        callback.invoke()
    }, time)
}

internal fun formatLongTwoDigits(time : Long) : String{
    return String.format(Locale.US, "%02d", time)
}

fun String.isFileText() : Boolean = this.lowercase().endsWith(".txt") || this.lowercase().endsWith(".text")

fun String.isFileVideo() : Boolean = this.lowercase().endsWith(".mp4")

fun String.isFileImage() : Boolean = this.lowercase().endsWith(".png")  || this.lowercase().endsWith(".jpg")|| this.lowercase().endsWith(".jpeg")

fun isNumber(s: String): Boolean {
    return try {
        s.toInt()
        true
    } catch (ex: NumberFormatException) {
        false
    }
}


fun pathIsUri(path : String) : Boolean = path.startsWith("content://")

val Uri.isTreeDocumentFile: Boolean
    get() = path?.startsWith("/tree/") == true

fun InetAddress.asString(): String = if (this is Inet6Address) "[${this.hostAddress}]" else this.hostAddress ?: ""

var dateMinuteAgo = ""
var dateMinutesAgo = ""
var dateHourAgo = ""
var dateHoursAgo = ""

fun Long.formatDate(): String {
    val now = Date()
    val duration = now.time - this
    // If time difference is less than a day
    if (duration < TimeUnit.DAYS.toMillis(1)) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        if (minutes < 60) {
            return "$minutes ${if (minutes == 1L) dateMinuteAgo else dateMinutesAgo}"
        }
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        return "$hours ${if (hours == 1L) dateHourAgo else dateHoursAgo}"
    }

    // If time difference is 1 day or more
    val sdf = SimpleDateFormat("dd/MM/yyyy")
    return sdf.format(this)
}