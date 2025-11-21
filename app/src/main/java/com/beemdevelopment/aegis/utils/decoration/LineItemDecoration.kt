package com.beemdevelopment.aegis.utils.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beemdevelopment.aegis.R

class LineItemDecoration(val context: Context, private val lineHeight: Int = 1, lineColorResId: Int = R.color.gray_text) : RecyclerView.ItemDecoration() {

   private val padding = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._10sdp)
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, lineColorResId)
        style = Paint.Style.STROKE
        strokeWidth = lineHeight.toFloat()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft + padding
        val right = parent.width - parent.paddingRight - padding

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + lineHeight

            c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), top.toFloat(), paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = lineHeight
    }
}
