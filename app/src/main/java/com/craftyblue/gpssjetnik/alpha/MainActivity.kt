package com.craftyblue.gpssjetnik.alpha

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.craftyblue.gpssjetnik.alpha.databinding.ActivityMainBinding

lateinit var displayPinAdapter : DisplayPinAdapter
lateinit var workingDirectory : String

const val ACTION_LOCATION_CHANGED = "com.craftyblue.gpssjetnik.alpha.ACTION_LOCATION_CHANGED"
const val ACTION_VIBRATE_PATTERN = "com.craftyblue.gpssjetnik.alpha.ACTION_VIBRATE_PATTERN"
const val ACTION_VIBRATE_CANCEL = "com.craftyblue.gpssjetnik.alpha.ACTION_VIBRATE_CANCEL"

class MainActivity : ComponentActivity() {
  lateinit var binding : ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    ActivityCompat.requestPermissions(
      this,
      arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.VIBRATE,
      ),
      0
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.FOREGROUND_SERVICE),
        0
      )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION),
        0
      )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        0
      )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.USE_FULL_SCREEN_INTENT),
        0
      )
    }

    Log.d("fileStorage", applicationContext.filesDir.toString())
    workingDirectory = applicationContext.filesDir.toString()

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    displayPinAdapter = DisplayPinAdapter(arrayListOf())
    displayPinAdapter.loadState()

    loadLocation()

    binding.recyclerView.adapter = displayPinAdapter
    binding.recyclerView.layoutManager = LinearLayoutManager(this)

    binding.addLocationButton.setOnClickListener {
      var newLat = binding.editTextLat.text.toString().toDouble()
      var newLong = binding.editTextLong.text.toString().toDouble()
      var newRadius = binding.editTextRadius.text.toString().toDouble()
      var newAlarmTitle = binding.alarmTitleEditText.text.toString()
      var newLocation = Location("user-input")
      newLocation.apply {
        latitude = newLat
        longitude = newLong
      }
      displayPinAdapter.addPin(DisplayPin(newLocation, newRadius, "activeNotTriggered", newAlarmTitle))
      binding.editTextLat.setText("")
      binding.editTextLong.setText("")
      binding.editTextRadius.setText("")
      binding.alarmTitleEditText.setText("")
    }

    binding.mapButton.setOnClickListener {
      startActivity(Intent(applicationContext, MapActivity::class.java))
    }

    registerReceiver(broadcastReceiver, IntentFilter(ACTION_LOCATION_CHANGED), RECEIVER_NOT_EXPORTED)
    registerReceiver(broadcastReceiver, IntentFilter(ACTION_VIBRATE_PATTERN), RECEIVER_NOT_EXPORTED)
    registerReceiver(broadcastReceiver, IntentFilter(ACTION_VIBRATE_CANCEL), RECEIVER_NOT_EXPORTED)


    Intent(applicationContext, TrackingService::class.java).also {
      it.action = TrackingService.Actions.START.toString()
      startService(it)
    }
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == ACTION_LOCATION_CHANGED) {
        val newText : String? = intent.getStringExtra("text")
        if (newText != null) { Log.d("broadcastReceiver", newText) }
        binding.currentLocationText.setText(newText)
      }
      else if (intent?.action == ACTION_VIBRATE_PATTERN) {
        val pattern : LongArray? = intent.getLongArrayExtra("pattern")
        val repeat : Int = intent.getIntExtra("repeat", -1)
        if (pattern != null) {
          applicationContext.vibratorPattern(pattern, repeat)
        }
      }
      else if (intent?.action == ACTION_VIBRATE_CANCEL) {
        applicationContext.vibratorCancel()
      }
    }
  }

  override fun onDestroy() {
    Intent(applicationContext, TrackingService::class.java).also {
      it.action = TrackingService.Actions.STOP.toString()
      startService(it)
    }

    unregisterReceiver(broadcastReceiver)

    saveLocation()
    displayPinAdapter.saveState()

    super.onDestroy()
  }
}