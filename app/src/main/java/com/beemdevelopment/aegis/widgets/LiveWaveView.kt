package com.beemdevelopment.aegis.widgets
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class FitWavyLineView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 10f // Adjust for desired base thickness
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val path = Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePath(w.toFloat(), h.toFloat())
    }

    private fun updatePath(width: Float, height: Float) {
        path.reset()

        // Define the relative points and control points based on the view's dimensions.
        // The values here are ratios (0.0 to 1.0) of the width and height.
        val startX = width * 0.1f
        val startY = height * 0.7f

        val controlX1_1 = width * 0.3f
        val controlY1_1 = height * 0.05f
        val controlX2_1 = width * 0.4f
        val controlY2_1 = height * 0.9f
        val endX1 = width * 0.6f
        val endY1 = height * 0.5f

        val controlX1_2 = width * 0.7f
        val controlY1_2 = height * 0.3f
        val controlX2_2 = width * 0.83f
        val controlY2_2 = height * 0.8f
        val endX2 = width * 0.9f // Extend to the right edge
        val endY2 = height * 0.5f

        path.moveTo(startX, startY)
        path.cubicTo(controlX1_1, controlY1_1, controlX2_1, controlY2_1, endX1, endY1)
        path.cubicTo(controlX1_2, controlY1_2, controlX2_2, controlY2_2, endX2, endY2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
        invalidate() // Redraw the view with the new stroke width
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidate() // Redraw the view with the new color
    }
}
