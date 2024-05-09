package com.craftyblue.gpssjetnik.alpha

import android.location.Location
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class Location2 {
  private var mProvider: String? = null
  private var mTimeMs: Long = 0
  private var mElapsedRealtimeNs: Long = 0
  private var mLatitudeDegrees = 0.0
  private var mLongitudeDegrees = 0.0
  private var mHorizontalAccuracyMeters = 0f
  private var mAltitudeMeters = 0.0
  private var mAltitudeAccuracyMeters = 0f
  private var mSpeedMetersPerSecond = 0f
  private var mSpeedAccuracyMetersPerSecond = 0f
  private var mBearingDegrees = 0f
  private var mBearingAccuracyDegrees = 0f

  fun serialize(location : Location) {
    mTimeMs = location.time
    mElapsedRealtimeNs = location.elapsedRealtimeNanos
    mLatitudeDegrees = location.latitude
    mLongitudeDegrees = location.longitude
    mHorizontalAccuracyMeters = location.accuracy
    mAltitudeMeters = location.altitude
    mAltitudeAccuracyMeters = location.verticalAccuracyMeters
    mSpeedMetersPerSecond = location.speed
    mSpeedAccuracyMetersPerSecond = location.speedAccuracyMetersPerSecond
    mBearingDegrees = location.bearing
    mBearingAccuracyDegrees = location.bearingAccuracyDegrees
  }

  fun deserialize() : Location {
    val location = Location(mProvider)
    location.apply {
      time = mTimeMs
      elapsedRealtimeNanos = mElapsedRealtimeNs
      latitude = mLatitudeDegrees
      longitude = mLongitudeDegrees
      accuracy = mHorizontalAccuracyMeters
      altitude = mAltitudeMeters
      verticalAccuracyMeters = mAltitudeAccuracyMeters
      speed = mSpeedMetersPerSecond
      speedAccuracyMetersPerSecond = mSpeedAccuracyMetersPerSecond
      bearing = mBearingDegrees
      bearingAccuracyDegrees = mBearingAccuracyDegrees
    }
    return location
  }
}

fun saveLocation() {
  val currentLocation2 = Location2()
  File("${workingDirectory}/lastKnownLocation.gpssjetnik").writeText(
    "${Json.encodeToString(currentLocation2.serialize(currentLocationPin))}")
}

fun loadLocation() {
  if (File("${workingDirectory}/lastKnownLocation.gpssjetnik").exists()) {
    var tempCurrentLocation2 = File("${workingDirectory}/lastKnownLocation.gpssjetnik")
      .readText()
    currentLocationPin =
      Json.decodeFromString<Location2>(tempCurrentLocation2).deserialize()
  }
  else {
    Log.d("loadState", "lastKnownLocation.gpssjetnik does not exist")
  }
}