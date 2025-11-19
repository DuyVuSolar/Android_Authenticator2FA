package com.kuemiin.reversevoice.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.kuemiin.reversevoice.R


class RoundedCornerTextView @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle // Use default TextView style
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val clipPath = Path()
    private val rectF = RectF()

    // --- Corner Radius Properties ---
    private var cornerRadius: Float = 0f

    // For individual corner control (optional)
    private var topLeftRadius: Float = 0f
    private var topRightRadius: Float = 0f
    private var bottomLeftRadius: Float = 0f
    private var bottomRightRadius: Float = 0f
    private var hasSpecificCorners = false


    // --- Background Color Handling ---
    // We handle background color internally to ensure it's drawn before clipping text
    // If you set android:background, it might be drawn after our clipPath.
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var customBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            backgroundPaint.color = value
            invalidate()
        }


    init {
        // Load custom attributes from XML
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.RoundedCornerTextView, // Ensure you have this styleable
                defStyleAttr,
                0
            )
            val commonRadius =
                typedArray.getDimension(R.styleable.RoundedCornerTextView_rctv_cornerRadius, 0f)

            if (typedArray.hasValue(R.styleable.RoundedCornerTextView_rctv_topLeftRadius) ||
                typedArray.hasValue(R.styleable.RoundedCornerTextView_rctv_topRightRadius) ||
                typedArray.hasValue(R.styleable.RoundedCornerTextView_rctv_bottomLeftRadius) ||
                typedArray.hasValue(R.styleable.RoundedCornerTextView_rctv_bottomRightRadius)
            ) {
                hasSpecificCorners = true
                topLeftRadius = typedArray.getDimension(
                    R.styleable.RoundedCornerTextView_rctv_topLeftRadius,
                    commonRadius
                )
                topRightRadius = typedArray.getDimension(
                    R.styleable.RoundedCornerTextView_rctv_topRightRadius,
                    commonRadius
                )
                bottomLeftRadius = typedArray.getDimension(
                    R.styleable.RoundedCornerTextView_rctv_bottomLeftRadius,
                    commonRadius
                )
                bottomRightRadius = typedArray.getDimension(
                    R.styleable.RoundedCornerTextView_rctv_bottomRightRadius,
                    commonRadius
                )
            } else {
                hasSpecificCorners = false
                cornerRadius = commonRadius
            }

            // Handle background color attribute
            if (typedArray.hasValue(R.styleable.RoundedCornerTextView_rctv_backgroundColor)) {
                customBackgroundColor = typedArray.getColor(
                    R.styleable.RoundedCornerTextView_rctv_backgroundColor,
                    Color.TRANSPARENT
                )
            } else {
                // If rctv_backgroundColor is not set, check android:background
                // This is a bit tricky because android:background can be a color or a drawable
                // For simplicity, we'll check if it's a ColorDrawable
                background?.let { bg ->
                    if (bg is ColorDrawable) {
                        customBackgroundColor = bg.color
                    }
                }
            }
            // Important: Set the standard android:background to null if we are handling it,
            // to prevent double drawing or conflicts.
            if (customBackgroundColor != Color.TRANSPARENT) {
                super.setBackground(null)
            }


            typedArray.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
        updateClipPath()
    }

    private fun updateClipPath() {
        clipPath.reset()
        if (hasSpecificCorners) {
            val radii = floatArrayOf(
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius
            )
            clipPath.addRoundRect(rectF, radii, Path.Direction.CW)
        } else if (cornerRadius > 0f) {
            clipPath.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        // If no radius, clipPath remains empty, so no clipping occurs.
    }

    override fun draw(canvas: Canvas) {
        // Draw custom background color first, clipped
        if (customBackgroundColor != Color.TRANSPARENT && !clipPath.isEmpty) {
            val saveCount = canvas.save()
            canvas.clipPath(clipPath)
            canvas.drawRect(rectF, backgroundPaint)
            canvas.restoreToCount(saveCount)
        } else if (customBackgroundColor != Color.TRANSPARENT && clipPath.isEmpty) {
            // No rounding, just draw background color
            canvas.drawRect(rectF, backgroundPaint)
        }

        // If android:background was set to a non-color drawable, let super.draw() handle it
        // but it will be drawn *after* our potential background color and *before* text clipping.
        // This part is tricky. For full control, avoid android:background if using rctv_backgroundColor.

        if (!clipPath.isEmpty) {
            val saveCount = canvas.save()
            canvas.clipPath(clipPath)
            super.draw(canvas) // Draws text and other foreground elements
            canvas.restoreToCount(saveCount)
        } else {
            super.draw(canvas) // No clipping needed
        }
    }


    // --- Public setters for programmatic changes ---

    fun setCornerRadius(radius: Float) {
        this.hasSpecificCorners = false
        this.cornerRadius = radius
        updateClipPath()
        invalidate()
    }

    fun setCornerRadii(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        this.hasSpecificCorners = true
        this.topLeftRadius = topLeft
        this.topRightRadius = topRight
        this.bottomRightRadius = bottomRight
        this.bottomLeftRadius = bottomLeft
        updateClipPath()
        invalidate()
    }

    /**
     * Sets the background color that will be rounded.
     * Note: This overrides any color set via android:background if you want rounded corners
     * for the background color. If you set a complex drawable via android:background,
     * it might not be clipped correctly by this custom view's logic without further changes.
     */
    fun setRoundedBackgroundColor(color: Int) {
        this.customBackgroundColor = color
        super.setBackground(null) // Prevent conflict with android:background
        invalidate()
    }

    // Override setBackgroundColor to use our custom handling
    override fun setBackgroundColor(color: Int) {
        setRoundedBackgroundColor(color)
    }
}

// If you need to support complex android:background drawables and have them clipped,
// you would need to avoid calling super.draw(canvas) directly after clipping.
// Instead, you'd let super.onDraw(canvas) draw the background, then clip, then super.onDraw(canvas) for text.
// However, super.onDraw() is protected.