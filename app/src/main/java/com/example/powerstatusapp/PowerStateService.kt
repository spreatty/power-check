package com.example.powerstatusapp

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast

class PowerStateService : Service() {
    override fun onBind(p: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val intentFilter = IntentFilter(Intent.ACTION_POWER_CONNECTED)
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(PowerConnectionReceiver(), intentFilter)
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show()
    }
}