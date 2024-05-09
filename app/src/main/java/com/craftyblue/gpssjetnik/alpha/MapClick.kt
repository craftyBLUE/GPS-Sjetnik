package com.craftyblue.gpssjetnik.alpha

import android.util.Log
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon


class TriggerRadius(map : MapView) : Polygon() {
  override fun onClickDefault(polygon: Polygon?, mapView: MapView?, eventPos: GeoPoint?): Boolean {
    return false
  }
}

lateinit var centerMarker : Marker
lateinit var circleTrigger : TriggerRadius
var circleTriggerRadius = 0.0

fun geoPointToLocation(p : GeoPoint): android.location.Location {
  val l = android.location.Location("geopoint")
  l.latitude = p.latitude
  l.longitude = p.longitude
  return l
}

fun geoPointDistance(p1 : GeoPoint, p2 : GeoPoint): Double {
  val l1 = geoPointToLocation(p1)
  val l2 = geoPointToLocation(p2)
  return l1.distanceTo(l2).toDouble()
}

fun redrawCircle() {
  if (map.overlays.contains(circleTrigger)) {
    map.overlays.remove(circleTrigger)
  }

  circleTrigger.points = Polygon.pointsAsCircle(centerMarker.position, circleTriggerRadius)

  circleTrigger.fillPaint.color = 0x12241212
  circleTrigger.outlinePaint.color = 0xFFFF0000.toInt()
  circleTrigger.outlinePaint.strokeWidth = 2f

  map.overlays.add(circleTrigger)

  map.invalidate()
}

//https://github.com/osmdroid/osmdroid/issues/295#issuecomment-1044323136 <3
class MapEventsReceiverImpl(
  var map : MapView
) : MapEventsReceiver {

  override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
    Log.d("singleTapConfirmedHelper", "${p.latitude} - ${p.longitude}")

    circleTriggerRadius = geoPointDistance(centerMarker.position, p)

    redrawCircle()

    return true
  }

  override fun longPressHelper(p: GeoPoint): Boolean {
    Log.d("longPressHelper", "${p.latitude} - ${p.longitude}")

    if (map.overlays.contains(centerMarker)) {
      map.overlays.remove(centerMarker)
    }

    centerMarker.setInfoWindow(null)
    centerMarker.position = GeoPoint(p.latitude, p.longitude)
    centerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    map.overlays.add(centerMarker)

    redrawCircle()

    map.invalidate()

    return true
  }
}