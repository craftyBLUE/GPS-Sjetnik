package com.craftyblue.gpssjetnik.alpha

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import android.location.Location
import androidx.core.app.NotificationCompat.Builder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import java.lang.Runnable

var currentLocationPin : Location = Location("skynet")

var gettingLocation : Boolean = false

class TrackingService: Service() {

  private lateinit var handler: Handler
  private lateinit var runnable: Runnable

  val mainHandler = Handler(Looper.getMainLooper())

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      Actions.START.toString() -> start()
      Actions.STOP.toString() -> stopSelf()
    }
    return super.onStartCommand(intent, flags, startId)
  }

  @SuppressLint("MissingPermission")
  override fun onCreate() {
    super.onCreate()
    Log.d("background service", "Service created")

    val locationClient = LocationServices.getFusedLocationProviderClient(this)

    handler = Handler(Looper.getMainLooper())
    runnable = Runnable {
      Log.d("background service", "tick")
      if (gettingLocation) {
        handler.postDelayed(runnable, 250)
      }
      else {
        gettingLocation = true
        val notification = NotificationCompat.Builder(this, "tracking_channel")

        GlobalScope.launch(Dispatchers.IO) {
          val result = locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
          ).await()
          result?.let { fetchedLocation ->
            //Log.d(TAG, fetchedLocation.toString())
            currentLocationPin = fetchedLocation

            saveLocation()

            val text =
              "lat: ${fetchedLocation.latitude}\nlong: ${fetchedLocation.longitude}"
            //"lat: ${fetchedLocation.latitude}\nlong: ${fetchedLocation.longitude}\nfetched at ${System.currentTimeMillis()}"
            Log.d("background service", text)

            val mapScreenIntent = Intent(applicationContext, MapActivity::class.java)
            mapScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val mapScreenPendingIntent = PendingIntent.getActivity(applicationContext, 0, mapScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            notificationManager.notify(
              1, notification
                .setSmallIcon(R.drawable.gps_sjetnik_icon_big)
                .setContentTitle("GPS sjetnik")
                .setContentText(text)
                .setStyle(
                  NotificationCompat.BigTextStyle()
                    .bigText(text)
                )
                .setOngoing(true)
                .setFullScreenIntent(mapScreenPendingIntent, true)
                .build()
            )

            sendBroadcast(
              Intent(ACTION_LOCATION_CHANGED)
                .putExtra("text", text)
                .setPackage(applicationContext.packageName))

            mainHandler.post {
              displayPinAdapter.calculateDistanceToAll(applicationContext, currentLocationPin)
            }

            gettingLocation = false
          }
        }

        handler.postDelayed(runnable, 5 * 1000)
      }
    }
    handler.post(runnable)
  }

  override fun onDestroy() {
    handler.removeCallbacks(runnable)
    notificationManager.cancel(1)
    super.onDestroy()
    Log.d("background service", "Service destroyed")
  }

  private fun start() {
    gettingLocation = false
    val notification = Builder(this, "tracking_channel")
      .setSmallIcon(R.drawable.gps_sjetnik_icon_big)
      .setContentTitle("GPS sjetnik")
      .setContentText("Tracking location")
      .setOngoing(true)
      .build()
    startForeground(1, notification)
  }

  enum class Actions {
    START, STOP
  }
}