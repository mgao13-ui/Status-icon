package com.duo.statusbar

import android.content.Context
import android.content.res.Configuration
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

    // 颜色动态适配深色/浅色模式
    private var activeColor = Color.BLACK
    private var inactiveColor = Color.parseColor("#33000000")

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    init {
        updateColorsForTheme(resources.configuration)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateColorsForTheme(newConfig)
        invalidate()
    }

    private fun updateColorsForTheme(config: Configuration) {
        val currentNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // 深色背景：亮白色
            activeColor = Color.WHITE
            inactiveColor = Color.parseColor("#44FFFFFF")
        } else {
            // 浅色背景：深石墨黑
            activeColor = Color.parseColor("#2C2C2E")
            inactiveColor = Color.parseColor("#332C2C2E")
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val size = (24 * density).toInt()
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f

        // 1. 电池：最外圈指示环
        val strokeW = 2.2f * density
        val batRadius = (Math.min(w, h) / 2f) - strokeW

        bgPaint.strokeWidth = strokeW
        bgPaint.color = inactiveColor
        canvas.drawCircle(cx, cy, batRadius, bgPaint)

        strokePaint.strokeWidth = strokeW
        strokePaint.color = when {
            isCharging -> Color.parseColor("#00C853")
            batteryLevel <= 20 -> Color.parseColor("#D50000")
            else -> activeColor
        }

        val sweepAngle = (batteryLevel / 100f) * 360f
        val batRect = RectF(cx - batRadius, cy - batRadius, cx + batRadius, cy + batRadius)
        canvas.drawArc(batRect, -90f, sweepAngle, false, strokePaint)

        // 2. Wi-Fi：内圈上半部分（双层弧线）
        val wifiArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 1.5f * density
        }
        val wifiCenterY = cy + 1.2f * density
        for (i in 0..1) {
            val r = (3.0f + i * 2.6f) * density
            val rect = RectF(cx - r, wifiCenterY - r, cx + r, wifiCenterY + r)
            wifiArcPaint.color = if (wifiLevel >= (i + 2)) activeColor else inactiveColor
            canvas.drawArc(rect, 225f, 90f, false, wifiArcPaint)
        }

        // 3. 蜂窝信号：内圈下半部分（3 个横向排布的圆点）
        val dotRadius = 1.3f * density
        val dotGap = 3.6f * density
        val dotY = cy + 4.8f * density
        for (i in 0 until 3) {
            val dotX = cx - dotGap + (i * dotGap)
            fillPaint.color = if (cellLevel > i) activeColor else inactiveColor
            canvas.drawCircle(dotX, dotY, dotRadius, fillPaint)
        }

        // 4. 充电状态：居中充电小圆点
        if (isCharging) {
            fillPaint.color = Color.parseColor("#00C853")
            canvas.drawCircle(cx, cy - 1.2f * density, 1.5f * density, fillPaint)
        }
    }
}
