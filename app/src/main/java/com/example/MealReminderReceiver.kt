package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MealReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mealType = intent.getStringExtra("meal_type") ?: "Meal"
        val messages = mapOf(
            "Breakfast" to "🌅 Good morning! Time to log your breakfast and start the day right!",
            "Lunch" to "☀️ Lunchtime! Don't forget to scan and log your lunch meal.",
            "Dinner" to "🌙 Evening! Log your dinner to complete today's nutrition tracking.",
            "Snack" to "🍎 Snack time! Remember to log what you're eating."
        )
        val message = messages[mealType] ?: "Time to log your $mealType!"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "meal_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Meal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to log your meals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("NutriLens AI — $mealType Reminder")
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openIntent)
            .build()

        notificationManager.notify(mealType.hashCode(), notification)
    }
}
