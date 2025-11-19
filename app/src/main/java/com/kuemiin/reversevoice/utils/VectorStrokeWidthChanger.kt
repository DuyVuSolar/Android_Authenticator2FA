package com.kuemiin.reversevoice.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import java.lang.reflect.Field

object VectorStrokeWidthChanger {

    private const val TAG = "VectorStrokeWidthChanger"

    @SuppressLint("SoonBlockedPrivateApi")
    fun changeStrokeWidth(context: Context, drawableResId: Int, newStrokeWidth: Float) {
        try {
            val drawable: Drawable? = VectorDrawableCompat.create(context.resources, drawableResId, null)

            if (drawable is VectorDrawableCompat) {
                val vectorStateField: Field = VectorDrawableCompat::class.java.getDeclaredField("mVectorState")
                vectorStateField.isAccessible = true
                val vectorState: Any = vectorStateField.get(drawable)

                val allFields: Array<Field> = vectorState.javaClass.declaredFields
                for (field in allFields) {
                    Log.d("field:", field.name)
                }
                val pathDataField: Field = vectorState.javaClass.getDeclaredField("mVPathRenderer")
                pathDataField.isAccessible = true
                val pathData: Any = pathDataField.get(vectorState)

                val allFields2: Array<Field> = pathData.javaClass.declaredFields
                for (field in allFields2) {
                    Log.d("allFields2 :", field.name)
                }
                val strokeWidthField: Field = pathData.javaClass.getDeclaredField("mStrokePaint")
                strokeWidthField.isAccessible = true
                strokeWidthField.setFloat("accessFlags", newStrokeWidth)

                drawable.invalidateSelf()
            } else {
                Log.e(TAG, "Drawable is not a VectorDrawableCompat")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error changing stroke width", e)
        }
    }
}