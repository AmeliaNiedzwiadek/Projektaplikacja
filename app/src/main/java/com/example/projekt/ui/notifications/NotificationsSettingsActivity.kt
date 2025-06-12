package com.example.projekt.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R
import java.util.*

class NotificationsSettingsActivity : AppCompatActivity() {

    private lateinit var switchEnableNotifications: Switch
    private lateinit var buttonSaveSettings: Button
    private lateinit var buttonAddTime: Button
    private lateinit var timeListLayout: LinearLayout

    private val sharedPrefsName = "notification_prefs"
    private val keyEnabled = "notif_enabled"
    private val keyTimes = "notif_times"  // zapisujemy godziny w formacie "HH:mm;HH:mm;HH:mm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_settings)

        switchEnableNotifications = findViewById(R.id.switchEnableNotifications)
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings)
        buttonAddTime = findViewById(R.id.buttonAddTime)
        timeListLayout = findViewById(R.id.timeListLayout)

        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(keyEnabled, true)
        switchEnableNotifications.isChecked = enabled

        // Wczytaj zapisane godziny i dodaj TimePickery
        val savedTimesString = prefs.getString(keyTimes, "") ?: ""
        if (savedTimesString.isNotEmpty()) {
            val times = savedTimesString.split(";")
            for (time in times) {
                val parts = time.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 9
                    val minute = parts[1].toIntOrNull() ?: 0
                    addTimePicker(hour, minute)
                }
            }
        } else {
            // Domyślnie dodaj jeden TimePicker o 9:00
            addTimePicker(9, 0)
        }

        buttonAddTime.setOnClickListener {
            addTimePicker(9, 0) // Dodaj nowy TimePicker domyślnie na 9:00
        }

        buttonSaveSettings.setOnClickListener {
            val notificationsEnabled = switchEnableNotifications.isChecked

            if (!notificationsEnabled) {
                cancelAllReminders()
                Toast.makeText(this, "Powiadomienia wyłączone", Toast.LENGTH_SHORT).show()
                saveEnabledState(false)
                finish() // wracamy po wyłączeniu
                return@setOnClickListener
            }

            val timesList = mutableListOf<Pair<Int, Int>>()
            for (i in 0 until timeListLayout.childCount) {
                val tp = timeListLayout.getChildAt(i) as TimePicker
                timesList.add(Pair(tp.hour, tp.minute))
            }
            if (timesList.isEmpty()) {
                Toast.makeText(this, "Dodaj co najmniej jedną godzinę powiadomień", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTimesToPrefs(timesList)
            scheduleAllReminders(timesList)
            saveEnabledState(true)

            Toast.makeText(this, "Powiadomienia ustawione", Toast.LENGTH_SHORT).show()
            finish() // wracamy po zapisie
        }
    }

    private fun addTimePicker(hour: Int, minute: Int) {
        val timePicker = TimePicker(this)
        timePicker.setIs24HourView(true)
        timePicker.hour = hour
        timePicker.minute = minute
        // Margines między TimePickerami
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0)
        timePicker.layoutParams = params

        timeListLayout.addView(timePicker)
    }

    private fun saveTimesToPrefs(times: List<Pair<Int, Int>>) {
        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val timesString = times.joinToString(";") { (h, m) -> String.format("%02d:%02d", h, m) }
        prefs.edit().putString(keyTimes, timesString).apply()
    }

    private fun saveEnabledState(enabled: Boolean) {
        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(keyEnabled, enabled).apply()
    }

    private fun scheduleAllReminders(times: List<Pair<Int, Int>>) {
        cancelAllReminders()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        times.forEachIndexed { index, (hour, minute) ->
            val intent = Intent(this, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                index, // różne requestCode aby alarmy się nie nadpisywały
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    private fun cancelAllReminders() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val savedTimesString = prefs.getString(keyTimes, "") ?: ""

        if (savedTimesString.isNotEmpty()) {
            val times = savedTimesString.split(";")
            for (index in times.indices) {
                val intent = Intent(this, ReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    companion object {
        fun cancelAllRemindersStatic(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            val savedTimesString = prefs.getString("notif_times", "") ?: ""

            if (savedTimesString.isNotEmpty()) {
                val times = savedTimesString.split(";")
                for (index in times.indices) {
                    val intent = Intent(context, ReminderReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        index,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                }
            }
        }
    }
}
