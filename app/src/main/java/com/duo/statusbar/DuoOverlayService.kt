package com.duo.statusbar

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class DuoOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var indicatorView: DuoIndicatorView? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                indicatorView?.batteryLevel = (level * 100) / scale
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        indicatorView = DuoIndicatorView(this)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val dpSize = (24 * resources.displayMetrics.density).toInt()
        val params = WindowManager.LayoutParams(
            dpSize,
            dpSize,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = (16 * resources.displayMetrics.density).toInt()
            y = (6 * resources.displayMetrics.density).toInt()
        }

        windowManager?.addView(indicatorView, params)

        // 注册电量监听
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // 注册网络监听
        val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                indicatorView?.post {
                    indicatorView?.wifiLevel = if (hasWifi) 4 else -1
                }
            }

            override fun onLost(network: Network) {
                indicatorView?.post { indicatorView?.wifiLevel = -1 }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        indicatorView?.let { windowManager?.removeView(it) }
    }
}
