package uz.apprica.plannote.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.apprica.plannote.MainActivity
import uz.apprica.plannote.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val NOTES_CHANNEL_ID      = "notes_reminder_channel"
        const val TASKS_CHANNEL_ID      = "tasks_reminder_channel"
        const val MOTIVATION_CHANNEL_ID = "daily_motivation_channel"

        private const val NOTES_CHANNEL_NAME      = "Note Reminders"
        private const val TASKS_CHANNEL_NAME      = "Task Reminders"
        private const val MOTIVATION_CHANNEL_NAME = "Daily Motivation"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        listOf(
            NotificationChannel(NOTES_CHANNEL_ID, NOTES_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for your saved notes"
                enableVibration(true)
            },
            NotificationChannel(TASKS_CHANNEL_ID, TASKS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for pending tasks"
                enableVibration(true)
            },
            NotificationChannel(MOTIVATION_CHANNEL_ID, MOTIVATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily motivational quotes every morning"
            }
        ).forEach(manager::createNotificationChannel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNoteReminder(noteId: Long, title: String, content: String) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, NOTES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { "Note Reminder" })
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityPendingIntent())
            .build()
        NotificationManagerCompat.from(context).notify(notificationId("note", noteId), notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTaskReminder(taskId: Long, title: String, description: String) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, TASKS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { "Task Reminder" })
            .setContentText(description.ifBlank { "You have a pending task" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(description.ifBlank { "You have a pending task" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityPendingIntent())
            .build()
        NotificationManagerCompat.from(context).notify(notificationId("task", taskId), notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDailyMotivation(quote: String) {
        if (!canPost()) return
        val notification = NotificationCompat.Builder(context, MOTIVATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Good morning! 🌅")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(mainActivityPendingIntent())
            .build()
        NotificationManagerCompat.from(context).notify(notificationId("motivation", 0L), notification)
    }

    private fun canPost(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    private fun mainActivityPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationId(tag: String, id: Long): Int =
        (tag.hashCode() * 31 + id).toInt()
}
