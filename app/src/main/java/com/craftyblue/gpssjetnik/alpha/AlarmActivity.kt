package com.craftyblue.gpssjetnik.alpha

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.craftyblue.gpssjetnik.alpha.databinding.ActivityAlarmBinding

class AlarmActivity : Activity() {
  lateinit var binding : ActivityAlarmBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityAlarmBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.cancelButton.setOnClickListener {
      sendBroadcast(
        Intent(ACTION_VIBRATE_CANCEL)
          .setPackage(applicationContext.packageName))
      notificationManager.cancel(10) //TODO: intent.getStringExtra("notificationId")
    }

    binding.alarmTitleTextView.setText(intent.getStringExtra("alarmTitle"))
  }
}