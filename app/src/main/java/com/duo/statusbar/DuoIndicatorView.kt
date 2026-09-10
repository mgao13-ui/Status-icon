package com.duo.statusbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DuoIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var batteryLevel: Int = 100
        set(value) { field = value; invalidate() }

    var wifiLevel: Int = 4 // 0~4, -1 为未连接
        set(value) { field = value; invalidate() }

    var cellLevel: Int = 4 // 0~4
        set(value) { field = value; invalidate() }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = minOf(width, height)
        val cx = width / 2f
        val cy = height / 2f

        // 1. 绘制外圈电量环 (Battery Arc)
        val stroke = size * 0.08f
        val radius = (size / 2f) - stroke
        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.strokeCap = Paint.Cap.ROUND

        // 轨道底色（淡灰半透明）
        paint.color = Color.argb(60, 255, 255, 255)
        canvas.drawArc(rectF, 0f, 360f, false, paint)

        // 实际电量颜色
        paint.color = when {
            batteryLevel <= 20 -> Color.parseColor("#FF453A") // 低电红
            else -> Color.WHITE
        }
        val sweepAngle = (batteryLevel / 100f) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, paint)

        // 2. 绘制中间 Wi-Fi 弧线
        paint.color = Color.WHITE
        paint.strokeWidth = stroke * 0.8f
        if (wifiLevel > 0) {
            val wifiRadius = radius * 0.55f
            val wifiRect = RectF(cx - wifiRadius, cy - wifiRadius * 1.2f, cx + wifiRadius, cy + wifiRadius * 0.8f)
            canvas.drawArc(wifiRect, -135f, 90f, false, paint)
        }

        // 3. 绘制底部蜂窝信号圆点 (Cell Signal)
        paint.style = Paint.Style.FILL
        val dotCount = 4
        val dotRadius = stroke * 0.45f
        val dotSpacing = dotRadius * 2.8f
        val startX = cx - ((dotCount - 1) * dotSpacing) / 2f
        val dotY = cy + (radius * 0.5f)

        for (i in 0 until dotCount) {
            paint.color = if (i < cellLevel) Color.WHITE else Color.argb(80, 255, 255, 255)
            canvas.drawCircle(startX + i * dotSpacing, dotY, dotRadius, paint)
        }
    }
}
