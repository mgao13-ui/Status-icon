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

    // 颜色配置
    private val activeColor = Color.WHITE
    private val inactiveColor = Color.parseColor("#55FFFFFF") // 未点亮部分带轻微半透明
    private val pillBgColor = Color.parseColor("#99000000")   // 药丸底托：约 60% 不透明纯黑，完美遮盖原生图标且不突兀

    private val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = pillBgColor
    }

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        // 留出药丸胶囊的左右内边距，总宽 92dp，高 24dp
        val desiredWidth = (92 * density).toInt()
        val desiredHeight = (24 * density).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f

        // 0. 绘制圆角黑色胶囊底托（遮挡底层原生图标，并为白色图标提供稳定深色对比衬底）
        val pillRect = RectF(0f, 0f, w, h)
        val cornerRadius = h / 2f
        canvas.drawRoundRect(pillRect, cornerRadius, cornerRadius, pillPaint)

        // 1. Wi-Fi 图标（位于药丸左侧，中心 x = 20dp）
        val wifiCenterX = 20f * density
        val wifiArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 2f * density
        }
        for (i in 0..2) {
            val r = (3.5f + i * 3.2f) * density
            val rect = RectF(wifiCenterX - r, centerY + 3.8f * density - r, wifiCenterX + r, centerY + 3.8f * density + r)
            wifiArcPaint.color = if (wifiLevel >= (i + 1)) activeColor else inactiveColor
            canvas.drawArc(rect, 225f, 90f, false, wifiArcPaint)
        }
        fillPaint.color = if (wifiLevel > 0) activeColor else inactiveColor
        canvas.drawCircle(wifiCenterX, centerY + 4.2f * density, 1.3f * density, fillPaint)

        // 2. 信号点（位于药丸中间，中心 x = 48dp）
        val cellCenterX = 48f * density
        val dotRadius = 1.8f * density
        val dotGap = 5.2f * density
        for (i in 0 until 4) {
            val cx = cellCenterX - (1.5f * dotGap) + (i * dotGap)
            fillPaint.color = if (cellLevel > i) activeColor else inactiveColor
            canvas.drawCircle(cx, centerY, dotRadius, fillPaint)
        }

        // 3. 电池环（位于药丸右侧，中心 x = 74dp）
        val batCenterX = 74f * density
        val batRadius = 7.5f * density
        val strokeW = 2.4f * density

        bgPaint.strokeWidth = strokeW
        canvas.drawCircle(batCenterX, centerY, batRadius, bgPaint)

        strokePaint.strokeWidth = strokeW
        strokePaint.color = when {
            isCharging -> Color.parseColor("#00E676") // 充电绿
            batteryLevel <= 20 -> Color.parseColor("#FF5252") // 低电红
            else -> activeColor
        }

        val sweepAngle = (batteryLevel / 100f) * 360f
        val batRect = RectF(batCenterX - batRadius, centerY - batRadius, batCenterX + batRadius, centerY + batRadius)
        canvas.drawArc(batRect, -90f, sweepAngle, false, strokePaint)

        if (isCharging) {
            fillPaint.color = Color.parseColor("#00E676")
            canvas.drawCircle(batCenterX, centerY, 2.3f * density, fillPaint)
        }
    }
}
