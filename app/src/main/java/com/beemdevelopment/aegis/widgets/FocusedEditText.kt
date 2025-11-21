package com.beemdevelopment.aegis.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.utils.extension.toPixel

class FocusedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val defaultLinePaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.transparent)
        strokeWidth = 1f
    }

    private val focusedLinePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.blue_text) // Use your desired focus color
        strokeWidth = 1f.toPixel
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = if (isFocused) focusedLinePaint else defaultLinePaint
        val startX = 0f
        val stopX = (width).toFloat()
        val startY = (height - 1f.toPixel)
        val stopY = startY

        canvas.drawLine(startX, startY, stopX, stopY, paint)
    }
}