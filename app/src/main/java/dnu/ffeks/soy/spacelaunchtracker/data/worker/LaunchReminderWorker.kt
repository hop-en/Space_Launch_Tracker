package dnu.ffeks.soy.spacelaunchtracker.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dnu.ffeks.soy.spacelaunchtracker.R

class LaunchReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val launchName = inputData.getString(KEY_LAUNCH_NAME) ?: context.getString(R.string.details_unknown)
        val launchId = inputData.getString(KEY_LAUNCH_ID) ?: return Result.failure()
        val timeLeft = inputData.getString(KEY_TIME_LEFT) ?: "1h"

        showNotification(launchName, launchId, timeLeft)
        return Result.success()
    }

    private fun showNotification(launchName: String, launchId: String, timeLeft: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "launch_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.settings_launch_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationText = if (timeLeft == "24h") {
            context.getString(R.string.notification_body_24h, launchName)
        } else {
            context.getString(R.string.notification_body, launchName)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationId = if (timeLeft == "24h") {
            (launchId + "_24h").hashCode()
        } else {
            launchId.hashCode()
        }
        notificationManager.notify(notificationId, notification)
    }

    companion object {
        const val KEY_LAUNCH_NAME = "launch_name"
        const val KEY_LAUNCH_ID = "launch_id"
        const val KEY_TIME_LEFT = "time_left"
    }
}