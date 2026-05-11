package com.smsforward

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (PreferenceManager(context).serviceEnabled) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, SmsForegroundService::class.java).apply {
                        action = SmsForegroundService.ACTION_START
                    }
                )
            }
        }
    }
}
