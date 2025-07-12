
package com.dailylist.pro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all alarms after reboot
            CoroutineScope(Dispatchers.IO).launch {
                val taskDao = AppDatabase.getDatabase(context).taskDao()
                val tasks = taskDao.getAllTasksOnce() // Assuming you add this method to TaskDao
                tasks.forEach { task ->
                    if (task.reminderEnabled) {
                        AlarmScheduler.scheduleAlarm(context, task)
                    }
                }
            }
        } else {
            val title = intent?.getStringExtra("TASK_TITLE")
            val notificationId = intent?.getIntExtra("NOTIFICATION_ID", 0) ?: 0

            if (title != null) {
                NotificationUtils.showNotification(context, "DailyList Pro Reminder", title, notificationId)
            }
        }
    }
}



