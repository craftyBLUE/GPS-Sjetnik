package com.craftyblue.gpssjetnik.alpha

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

lateinit var notificationManager: NotificationManager

class TrackingApp: Application() {
  override fun onCreate() {
    super.onCreate()
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      notificationManager.createNotificationChannel(
        NotificationChannel(
          "tracking_channel",
          "Tracking Notifications",
          NotificationManager.IMPORTANCE_LOW
        )
      )
      notificationManager.createNotificationChannel(
        NotificationChannel(
          "alarm_channel",
          "Alarm Notifications",
          NotificationManager.IMPORTANCE_HIGH
        ))
    }
  }
}