package com.example.projekt.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projekt.R

class NotificationsSummaryActivity : AppCompatActivity() {

    private lateinit var switchEnableNotifications: Switch
    private lateinit var textViewAlarms: TextView
    private lateinit var buttonEditNotifications: Button

    private val sharedPrefsName = "notification_prefs"
    private val keyEnabled = "notif_enabled"
    private val keyTimes = "notif_times"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_summary)

        switchEnableNotifications = findViewById(R.id.switchEnableNotifications)
        textViewAlarms = findViewById(R.id.textViewAlarms)
        buttonEditNotifications = findViewById(R.id.buttonEditNotifications)

        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        switchEnableNotifications.isChecked = prefs.getBoolean(keyEnabled, true)

        updateAlarmsText()

        switchEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(keyEnabled, isChecked).apply()
            if (!isChecked) {
                // Wyłącz powiadomienia
                NotificationsSettingsActivity.cancelAllRemindersStatic(this)
                Toast.makeText(this, "Powiadomienia wyłączone", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Włączono powiadomienia. Edytuj godziny alarmów.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonEditNotifications.setOnClickListener {
            val intent = Intent(this, NotificationsSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateAlarmsText() {
        val prefs = getSharedPreferences(sharedPrefsName, Context.MODE_PRIVATE)
        val savedTimesString = prefs.getString(keyTimes, "") ?: ""
        if (savedTimesString.isEmpty()) {
            textViewAlarms.text = "Brak ustawionych alarmów"
        } else {
            val times = savedTimesString.split(";").joinToString(", ")
            textViewAlarms.text = "Ustawione godziny: $times"
        }
    }

    override fun onResume() {
        super.onResume()
        // Odśwież listę alarmów po powrocie z edycji
        updateAlarmsText()
    }
}


