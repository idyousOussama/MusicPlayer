package com.example.jamplayer.Services

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat


const val CHANNEL_ID = "channel_id"
const val  CHANNEL_NAME = "channel_name"

class BaseApplication : Application() {

    @SuppressLint("WrongConstant")
    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Notification for music playback control"
                setSound(null, null) // تعطيل الصوت
                enableVibration(false)

            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


    }

}