package com.example.projekt.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.projekt.R
import com.example.projekt.ui.home.HomeActivity

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "measurement_reminder_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tworzymy kanał powiadomień (od Androida 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Przypomnienia o pomiarach",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kanał powiadomień przypominających o pomiarach"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Kliknięcie w powiadomienie otworzy HomeActivity
        val intentToOpen = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentToOpen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)  // Podmień na własną ikonę
            .setContentTitle("Przypomnienie o pomiarze")
            .setContentText("Nie zapomnij wykonać dzisiejszego pomiaru!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
