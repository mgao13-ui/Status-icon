package com.duo.statusbar

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 100, 60, 60)
        }

        val infoText = TextView(this).apply {
            text = "iPhone Duo 三合一状态栏指示器\n\n点击下方按钮开启悬浮窗权限，即可在右上角显示合成图标。"
            textSize = 16f
        }

        val startBtn = Button(this).apply {
            text = "开启/重启状态栏图标"
            setOnClickListener {
                checkAndStart()
            }
        }

        layout.addView(infoText)
        layout.addView(startBtn)
        setContentView(layout)
    }

    private fun checkAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val serviceIntent = Intent(this, DuoOverlayService::class.java)
            startService(serviceIntent)
        }
    }
}
