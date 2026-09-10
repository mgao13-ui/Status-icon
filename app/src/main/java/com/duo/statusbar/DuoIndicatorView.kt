package com.duo.statusbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DuoIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var batteryLevel: Int = 100
        set(value) { field = value; invalidate() }

    var isCharging: Boolean = false
        set(value) { field = value; invalidate() }

    var wifiLevel: Int = 4
        set(value) { field = value; invalidate() }

    var cellLevel: Int = 4
        set(value) { field = value; invalidate() }

    // 提高对比度：激活白色，未激活半透明白加深至 50%，增加微弱黑阴影防止浅色背景隐形
    private val activeColor = Color.WHITE
    private val inactiveColor = Color.parseColor("#80FFFFFF")
    private val shadowColor = Color.parseColor("#66000000")

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = activeColor
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = activeColor
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = inactiveColor
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        strokePaint.setShadowLayer(2.5f, 0f, 1f, shadowColor)
        fillPaint.setShadowLayer(2.5f, 0f, 1f, shadowColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val desiredWidth = (84 * density).toInt()
        val desiredHeight = (24 * density).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val h = height.toFloat()
        val centerY = h / 2f

        // 1. Wi-Fi 图标（最左侧，中心 x = 16dp）
        val wifiCenterX = 16f * density
        val wifiArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2.2f * density
            setShadowLayer(2.5f, 0f, 1f, shadowColor)
        }
        for (i in 0..2) {
            val r = (4f + i * 3.5f) * density
            val rect = RectF(wifiCenterX - r, centerY + 4f * density - r, wifiCenterX + r, centerY + 4f * density + r)
            wifiArcPaint.color = if (wifiLevel >= (i + 1)) activeColor else inactiveColor
            canvas.drawArc(rect, 225f, 90f, false, wifiArcPaint)
        }
        fillPaint.color = if (wifiLevel > 0) activeColor else inactiveColor
        canvas.drawCircle(wifiCenterX, centerY + 4.5f * density, 1.4f * density, fillPaint)

        // 2. 信号强度点（中间，中心 x = 44dp）
        val cellCenterX = 44f * density
        val dotRadius = 1.9f * density
        val dotGap = 5.5f * density
        for (i in 0 until 4) {
            val cx = cellCenterX - (1.5f * dotGap) + (i * dotGap)
            fillPaint.color = if (cellLevel > i) activeColor else inactiveColor
            canvas.drawCircle(cx, centerY, dotRadius, fillPaint)
        }

        // 3. 电池指示环（最右侧，中心 x = 70dp）
        val batCenterX = 70f * density
        val batRadius = 8f * density
        val strokeW = 2.5f * density

        bgPaint.strokeWidth = strokeW
        canvas.drawCircle(batCenterX, centerY, batRadius, bgPaint)

        strokePaint.strokeWidth = strokeW
        strokePaint.color = when {
            isCharging -> Color.parseColor("#00E676")
            batteryLevel <= 20 -> Color.parseColor("#FF5252")
            else -> activeColor
        }

        val sweepAngle = (batteryLevel / 100f) * 360f
        val batRect = RectF(batCenterX - batRadius, centerY - batRadius, batCenterX + batRadius, centerY + batRadius)
        canvas.drawArc(batRect, -90f, sweepAngle, false, strokePaint)

        if (isCharging) {
            fillPaint.color = Color.parseColor("#00E676")
            canvas.drawCircle(batCenterX, centerY, 2.5f * density, fillPaint)
        }
    }
}
