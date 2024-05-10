package com.craftyblue.gpssjetnik.alpha

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.math.roundToInt

lateinit var map : MapView

lateinit var currentLocationCircle : TriggerRadius

fun refreshTriggers(context: Context) {
  var displayPins = displayPinAdapter.getDisplayPins()

  for (i in 0..<displayPins.size) {
    val circle = TriggerRadius(map)
    circle.points = Polygon.pointsAsCircle(GeoPoint(displayPins[i].location.latitude, displayPins[i].location.longitude), displayPins[i].radius)

    circle.fillPaint.color = 0x12121212
    circle.outlinePaint.color = 0xFFFF0000.toInt()
    circle.outlinePaint.strokeWidth = 4f

    map.overlays.add(circle)
  }

  val items = ArrayList<OverlayItem>()
  //items.add(OverlayItem("Title", "Description", GeoPoint(0.0, 0.0)))

  for (i in 0..<displayPins.size) {
    items.add(OverlayItem(
      displayPins[i].alarmTitle,
      "${displayPins[i].location.latitude}, ${displayPins[i].location.longitude}\n" +
              "Trigger radius: ${displayPins[i].radius}m\n" +
              "Distance: ${displayPins[i].distance.roundToInt()}m",
      GeoPoint(displayPins[i].location.latitude, displayPins[i].location.longitude)))
  }

  var itemsOverlay = ItemizedOverlayWithFocus<OverlayItem>(items, object: ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
    override fun onItemSingleTapUp(index:Int, item:OverlayItem):Boolean {
      //do something
      Log.d("map tap", "${index} tap")
      return true
    }
    override fun onItemLongPress(index:Int, item:OverlayItem):Boolean {
      Log.d("map tap", "${index} long press")
      return true
    }
  }, context)
  itemsOverlay.setFocusItemsOnTap(true);
  map.overlays.add(itemsOverlay);
}

fun refreshMapOverlays(context: Context) {
  map.overlays.clear()

  val dm : DisplayMetrics = context.resources.displayMetrics
  val scaleBarOverlay = ScaleBarOverlay(map)
  scaleBarOverlay.setCentred(true)

  scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
  map.overlays.add(scaleBarOverlay)

  //https://github.com/osmdroid/osmdroid/issues/295#issuecomment-1044323136 <3
  val mapEventsReceiver = MapEventsReceiverImpl(map)
  val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
  map.overlays.add(mapEventsOverlay)

  refreshTriggers(context)
}

fun initMap(context: Context) {
  map.setTileSource(TileSourceFactory.MAPNIK)
  map.setMultiTouchControls(true)
  map.isHorizontalMapRepetitionEnabled = true
  map.isVerticalMapRepetitionEnabled = false
  map.controller.setZoom(13)
  map.isTilesScaledToDpi = true
  map.controller.setCenter(GeoPoint(currentLocationPin.latitude, currentLocationPin.longitude))

  /*var compassOverlay = CompassOverlay(applicationContext, InternalCompassOrientationProvider(applicationContext), map)
  compassOverlay.enableCompass()
  map.overlays.add(compassOverlay)*/

  /*val overlayGrid = LatLonGridlineOverlay2()
  map.overlays.add(overlayGrid)*/

  centerMarker = Marker(map)
  circleTrigger = TriggerRadius(map)

  refreshMapOverlays(context)

  //mapInitialized = true
}

fun initCurrentLocationCircle() {
  currentLocationCircle = TriggerRadius(map)

  currentLocationCircle.fillPaint.color = 0x24121224
  currentLocationCircle.outlinePaint.color = 0xFF0000FF.toInt()
  currentLocationCircle.outlinePaint.strokeWidth = 4f
}

fun displayCurrentLocation() {
  if (map.overlays.contains(currentLocationCircle)) {
    map.overlays.remove(currentLocationCircle)
  }

  currentLocationCircle.points = Polygon.pointsAsCircle(
    GeoPoint(currentLocationPin.latitude, currentLocationPin.longitude),
    currentLocationPin.accuracy.toDouble()
  )

  map.overlays.add(currentLocationCircle)
  map.invalidate()
}

class MapActivity : Activity() {
  private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

  //lateinit var binding : ActivityMapBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    //binding = ActivityMapBinding.inflate(layoutInflater)
    //setContentView(binding.root)

    getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    setContentView(R.layout.activity_map)

    map = findViewById<MapView>(R.id.map)
    //map = binding.map

    initMap(applicationContext)
    initCurrentLocationCircle()

    val rotationGestureOverlay = RotationGestureOverlay(map)

    var freeRotateButton = findViewById<Button>(R.id.freeRotationButton)
    freeRotateButton.setOnClickListener {
      if (map.overlays.contains(rotationGestureOverlay)) {
        map.overlays.remove(rotationGestureOverlay)
        map.mapOrientation = 0.0f
      }
      else {
        map.overlays.add(rotationGestureOverlay)
      }
      map.invalidate()
    }

    var addTriggerButton = findViewById<Button>(R.id.addTriggerButton)
    var titleEditText = findViewById<EditText>(R.id.titleEditText)
    addTriggerButton.setOnClickListener {
      displayPinAdapter.addPin(
        DisplayPin(geoPointToLocation(centerMarker.position),
          circleTriggerRadius,
          "activeNotTriggered",
          titleEditText.text.toString()))
      titleEditText.setText("")
      displayPinAdapter.saveState()
      
      refreshMapOverlays(applicationContext)
    }

    displayCurrentLocation()
  }

  override fun onResume() {
    super.onResume()
    map.onResume()
    registerReceiver(broadcastReceiver, IntentFilter(ACTION_LOCATION_CHANGED), RECEIVER_NOT_EXPORTED)
  }

  override fun onPause() {
    super.onPause()
    unregisterReceiver(broadcastReceiver)
    map.onPause()
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == ACTION_LOCATION_CHANGED) {
        displayCurrentLocation()
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    val permissionsToRequest = ArrayList<String>()
    var i = 0
    while (i < grantResults.size) {
      permissionsToRequest.add(permissions[i])
      i++
    }
    if (permissionsToRequest.size > 0) {
      ActivityCompat.requestPermissions(
        this,
        permissionsToRequest.toTypedArray(),
        REQUEST_PERMISSIONS_REQUEST_CODE)
    }
  }
}