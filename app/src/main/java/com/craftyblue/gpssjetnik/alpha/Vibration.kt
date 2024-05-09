package com.craftyblue.gpssjetnik.alpha

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

//Compatibility for different versions of android by Masoud on Stack Overflow https://stackoverflow.com/a/76889747/23385556 <3
@Suppress("DEPRECATION")
fun Context.vibratorMs(durationMillis: Long = 50) {
  when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      // For Android 12 (S) and above
      val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      val vibrationEffect = VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
      val vibrator = vibratorManager.defaultVibrator
      vibrator.vibrate(vibrationEffect)
    }

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
      // For Android 8.0 (Oreo) to Android 11 (R)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      val vibrationEffect = VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
      vibrator.vibrate(vibrationEffect)
    }

    /*else -> {
      // For Android versions below Oreo (API level 26)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(durationMillis)
    }*/
  }
}

@Suppress("DEPRECATION")
fun Context.vibratorPattern(vibrationPattern : LongArray, repeat : Int = -1) {
  when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      // For Android 12 (S) and above
      val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, repeat)
      val vibrator = vibratorManager.defaultVibrator
      vibrator.vibrate(vibrationEffect)
    }

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
      // For Android 8.0 (Oreo) to Android 11 (R)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, repeat)
      vibrator.vibrate(vibrationEffect)
    }

    /*else -> {
      // For Android versions below Oreo (API level 26)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(durationMillis)
    }*/
  }
}

@Suppress("DEPRECATION")
fun Context.vibratorCancel() {
  when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      // For Android 12 (S) and above
      val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      val vibrator = vibratorManager.defaultVibrator
      vibrator.cancel()
    }

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
      // For Android 8.0 (Oreo) to Android 11 (R)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.cancel()
    }

    /*else -> {
      // For Android versions below Oreo (API level 26)
      val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      vibrator.vibrate(durationMillis)
    }*/
  }
}