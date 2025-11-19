package com.kuemiin.reversevoice.utils.extension

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController

@ChecksSdkIntAtLeast(parameter = 0)
fun isBuildLargerThan(versionCode: Int) = Build.VERSION.SDK_INT >= versionCode

fun Int.asColor(context: Context) = ContextCompat.getColor(context, this)

fun Int.asDrawable(context: Context) = ContextCompat.getDrawable(context, this)

inline fun <reified T : Enum<T>> String.asEnumOrDefault(defaultValue: T? = null): T? =
    enumValues<T>().find { it.name.equals(this, ignoreCase = true) } ?: defaultValue

fun <T> Fragment.setNavigationResult(key: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(
        key,
        value
    )
}

fun <T> Fragment.getNavigationResult(@IdRes id: Int, key: String, onResult: (result: T) -> Unit) {
    val navBackStackEntry = findNavController().getBackStackEntry(id)

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME
            && navBackStackEntry.savedStateHandle.contains(key)
        ) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }

    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            navBackStackEntry.lifecycle.removeObserver(observer)
        }
    })
}


val Number.toPixel
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun String.isFileDocument() : Boolean{
    val lowercase = this.lowercase()
    return lowercase.isFilePDF()
        || lowercase.isFileWord()//word
        || lowercase.isFileExcel() //excel
        || lowercase.isFilePPT()//ppt
        || lowercase.isFileTXT()//txt
}

fun String.getTypeOfDocument() : String{
    val lowercase = this.lowercase()
    return if(lowercase.isFilePDF()) "PDF"
            else if(lowercase.isFileWord()) "Word"
            else if(lowercase.isFileExcel()) "Excel"
            else if(lowercase.isFilePPT()) "PPT"
            else "TXT"
}

fun String.isFilePDF() : Boolean{
    val lowercase = this.lowercase()
    return lowercase.endsWith(".pdf")
}

fun String.isFileWord() : Boolean {
    val lowercase = this.lowercase()
    return lowercase.endsWith(".docx") || lowercase.endsWith(".dot") || lowercase.endsWith(".dotx") || lowercase.endsWith(".eml")//word
}


fun String.isFileExcel() : Boolean{
    val lowercase = this.lowercase()
    return lowercase.endsWith(".xlm") || lowercase.endsWith(".xls") || lowercase.endsWith(".xlsm") || lowercase.endsWith(".xlsx") //excel
}

fun String.isFilePPT() : Boolean{
    val lowercase = this.lowercase()
    return lowercase.endsWith(".pptx")
}

fun String.isFileTXT() : Boolean{
    val lowercase = this.lowercase()
    return lowercase.endsWith(".txt")
}

