package com.craftyblue.gpssjetnik.alpha

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.roundToInt

class DisplayPin(
  var location : Location,
  var radius : Double,
  var status : String,
  var alarmTitle : String
){
  var distance : Double = -1.0

  fun calculateDistance(startPin: Location) : Double {
    distance = startPin.distanceTo(location).toDouble()
    return distance
  }

  fun checkTrigger(context: Context) {
    if ((distance <= radius)) {
      if (status == "activeNotTriggered") {
        status = "activeTriggered"
        val fullScreenIntent = Intent(context, AlarmActivity::class.java)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        fullScreenIntent.putExtra("alarmTitle", alarmTitle)

        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        context.sendBroadcast(
          Intent(ACTION_VIBRATE_PATTERN)
            .putExtra("pattern", longArrayOf(0, 250, 250, 250))
            .putExtra("repeat", 0)
            .setPackage(context.packageName))

        notificationManager.notify(10, NotificationCompat.Builder(context, "alarm_channel")
          .setSmallIcon(R.drawable.gps_sjetnik_icon_big)
          .setContentText("alarm")
          .setFullScreenIntent(fullScreenPendingIntent, true)
          .build()
        )
      }
    }
    else {
      if (status == "activeTriggered") {
        status = "activeNotTriggered"
      }
    }
  }
}

@Serializable
class DisplayPin2 {
  var location2 = Location2()
  var radius : Double = 0.0
  var status : String = ""
  var distance : Double = -1.0
  var alarmTitle : String = "Untitled alarm"
}

class DisplayPinAdapter(
  private val displayPins : MutableList<DisplayPin>
) : RecyclerView.Adapter<DisplayPinAdapter.DisplayPinViewHolder>() {

  class DisplayPinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var constraintLayout : ConstraintLayout
    val latTextView : TextView
    val longTextView : TextView
    val radiusTextView : TextView
    val deleteItemView : TextView
    var distanceTextView : TextView
    var alarmTitleTextView : TextView

    init {
      constraintLayout = itemView.findViewById(R.id.constraintLayout)
      latTextView = itemView.findViewById(R.id.latTextView)
      longTextView = itemView.findViewById(R.id.longTextView)
      radiusTextView = itemView.findViewById(R.id.radiusTextView)
      distanceTextView = itemView.findViewById(R.id.distanceTextView)
      deleteItemView = itemView.findViewById(R.id.deleteItemButton)
      alarmTitleTextView = itemView.findViewById(R.id.alarmTitleTextView)
    }
  }

  fun getDisplayPins() : MutableList<DisplayPin>{
    return displayPins
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayPinViewHolder {
    return DisplayPinViewHolder(
      LayoutInflater.from(parent.context).inflate(
        R.layout.item_pin,
        parent,
        false
      )
    )
  }

  fun calculateDistanceToAll(context: Context, startPin: Location) {
    for (i in 0..<displayPins.size) {
      displayPins[i].calculateDistance(startPin)
      displayPins[i].checkTrigger(context)
    }
    notifyDataSetChanged()
  }

  fun addPin(pin: DisplayPin) {
    displayPins.add(pin)
    notifyItemInserted(displayPins.size - 1)
    saveState()
  }

  override fun onBindViewHolder(holder: DisplayPinViewHolder, position: Int) {
    val currDisplayPin = displayPins[position]
    holder.latTextView.text = currDisplayPin.location.latitude.toString()
    holder.longTextView.text = currDisplayPin.location.longitude.toString()
    holder.radiusTextView.text = currDisplayPin.radius.toString()
    holder.distanceTextView.text = "${currDisplayPin.distance.roundToInt()} m"
    holder.alarmTitleTextView.text = currDisplayPin.alarmTitle

    holder.deleteItemView.setOnClickListener {
      displayPins.remove(currDisplayPin)
      notifyItemRemoved(position)
    }

    if (currDisplayPin.status == "activeNotTriggered") {
      holder.constraintLayout.setBackgroundColor(0xFFFFFFFF.toInt())
    }
    else if (currDisplayPin.status == "activeTriggered") {
      holder.constraintLayout.setBackgroundColor(0x7F00FF00.toInt())
    }
  }

  override fun getItemCount(): Int {
    return displayPins.size
  }

  fun saveState() {
    var outputString = ""

    for (i in 0..<displayPins.size) {
      val displayPin2 = DisplayPin2()
      displayPin2.location2.serialize(displayPins[i].location)
      displayPin2.radius = displayPins[i].radius
      displayPin2.status = displayPins[i].status
      displayPin2.distance = displayPins[i].distance
      displayPin2.alarmTitle = displayPins[i].alarmTitle
      outputString += "${Json.encodeToString(displayPin2)}\n"
    }
    File("${workingDirectory}/pins.gpssjetnik").writeText(outputString)
  }

  fun loadState() {
    if (File("${workingDirectory}/pins.gpssjetnik").exists()) {
      var tempDisplayPins2 = File("${workingDirectory}/pins.gpssjetnik").readLines()

      for (i in 0..<tempDisplayPins2.size) {
        val tempDisplayPin2 = Json.decodeFromString<DisplayPin2>(tempDisplayPins2[i])
        var tempDisplayPin = DisplayPin(Location("skynet"), 0.0, "", "")
        tempDisplayPin.location = tempDisplayPin2.location2.deserialize()
        tempDisplayPin.radius = tempDisplayPin2.radius
        tempDisplayPin.status = tempDisplayPin2.status
        tempDisplayPin.distance = tempDisplayPin2.distance
        tempDisplayPin.alarmTitle = tempDisplayPin2.alarmTitle

        displayPins.add(tempDisplayPin)
      }
    }
    else {
      Log.d("loadState", "pins.gpssjetnik does not exist")
    }
  }
}