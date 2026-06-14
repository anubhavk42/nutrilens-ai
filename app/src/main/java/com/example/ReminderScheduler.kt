package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {

    private val mealTimes = mapOf(
        "Breakfast" to Pair(8, 0),   // 8:00 AM
        "Lunch" to Pair(13, 0),      // 1:00 PM
        "Dinner" to Pair(19, 0),     // 7:00 PM
        "Snack" to Pair(16, 0)       // 4:00 PM
    )

    fun scheduleAllReminders(context: Context) {
        mealTimes.forEach { (mealType, time) ->
            scheduleMealReminder(context, mealType, time.first, time.second)
        }
    }

    fun cancelAllReminders(context: Context) {
        mealTimes.keys.forEach { mealType ->
            cancelMealReminder(context, mealType)
        }
    }

    private fun scheduleMealReminder(context: Context, mealType: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            action = "com.example.MEAL_REMINDER"
            putExtra("meal_type", mealType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            mealType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelMealReminder(context: Context, mealType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MealReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            mealType.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
