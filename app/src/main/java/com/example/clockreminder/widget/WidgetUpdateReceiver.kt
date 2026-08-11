package com.example.clockreminder.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetCommon.updateAllStyles(context)
    }

    companion object {
        const val ACTION_TICK = "com.example.clockreminder.ACTION_TICK"
    }
}
