package com.sta.staenturno.util

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmHelper(private val context: Context) {

    fun setAlarm(timeString: String, message: String) {
        try {
            val time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
            
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_HOUR, time.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, time.minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false) // Let user confirm/see the alarm
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback: Try to start anyway, some devices/emulators hide the activity from resolve but allow start
                try {
                    context.startActivity(intent)
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(context, "No se encontró aplicación de reloj", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al configurar alarma: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
