package com.angcyo.uiview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.angcyo.rtablayout.R

/**
 * Created by angcyo on 2018/06/24 11:05
 */
class RDrawBorder(view: View, attributeSet: AttributeSet? = null) : BaseDraw(view, attributeSet) {
    var drawBorder = false
    var borderColor: Int = Color.TRANSPARENT
    var borderFillColor: Int = Color.TRANSPARENT
    var borderRoundSize: Float = 45 * density()
    var borderWidth: Float = 2 * density()

    init {
        initAttribute(attributeSet)
    }

    override fun initAttribute(attr: AttributeSet?) {
        val typedArray = obtainStyledAttributes(attr, R.styleable.RDrawBorder)
        drawBorder = typedArray.getBoolean(R.styleable.RDrawBorder_r_border_show, drawBorder)

        borderColor = Color.RED

        borderColor = typedArray.getColor(R.styleable.RDrawBorder_r_border_color, borderColor)
        borderFillColor = typedArray.getColor(R.styleable.RDrawBorder_r_border_fill_color, borderFillColor)
        borderRoundSize = typedArray.getDimensionPixelOffset(R.styleable.RDrawBorder_r_border_round_size, borderRoundSize.toInt()).toFloat()
        borderWidth = typedArray.getDimensionPixelOffset(R.styleable.RDrawBorder_r_border_width, borderWidth.toInt()).toFloat()

        typedArray.recycle()
    }

    private val borderDrawRect: RectF by lazy {
        RectF()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawBorder) {

            borderDrawRect.set(paddingLeft + borderWidth / 2, paddingTop + borderWidth / 2,
                    viewWidth - paddingRight - borderWidth, viewHeight - paddingBottom - borderWidth / 2)

            if (borderFillColor != 0) {
                mBasePaint.style = Paint.Style.FILL
                mBasePaint.color = borderFillColor
                canvas.drawRoundRect(borderDrawRect, borderRoundSize, borderRoundSize, mBasePaint)
            }

            mBasePaint.strokeWidth = borderWidth
            mBasePaint.style = Paint.Style.STROKE
            mBasePaint.color = borderColor
            canvas.drawRoundRect(borderDrawRect, borderRoundSize, borderRoundSize, mBasePaint)
        }
    }
}