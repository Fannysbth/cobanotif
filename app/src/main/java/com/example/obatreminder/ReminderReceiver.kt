package com.example.obatreminder

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicineName") ?: return

        val appIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("medicineName", medicineName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Waktunya minum obat!")
            .setContentText("Obat: $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Pengingat Obat",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(medicineName.hashCode(), builder.build())
    }
}
