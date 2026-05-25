package uz.apprica.plannote.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import uz.apprica.plannote.receiver.AlarmReceiver
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_TYPE        = "alarm_type"
        const val EXTRA_ITEM_ID     = "alarm_item_id"
        const val EXTRA_TITLE       = "alarm_title"
        const val EXTRA_CONTENT     = "alarm_content"

        const val TYPE_NOTE         = "note"
        const val TYPE_TASK         = "task"
        const val TYPE_MOTIVATION   = "motivation"

        private const val MOTIVATION_HOUR   = 8
        private const val MOTIVATION_MINUTE = 0
    }

    // ── Note reminders ────────────────────────────────────────────────────────

    fun scheduleNoteReminder(noteId: Long, title: String, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val intent  = buildIntent(TYPE_NOTE, noteId, title, "")
        val pending = pendingIntent(requestCodeFor(TYPE_NOTE, noteId), intent)
        scheduleExact(triggerAtMillis, pending)
    }

    fun cancelNoteReminder(noteId: Long) {
        val intent  = buildIntent(TYPE_NOTE, noteId, "", "")
        val pending = pendingIntent(requestCodeFor(TYPE_NOTE, noteId), intent)
        alarmManager.cancel(pending)
        pending.cancel()
    }

    // ── Task reminders ────────────────────────────────────────────────────────

    fun scheduleTaskReminder(taskId: Long, title: String, description: String, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val intent  = buildIntent(TYPE_TASK, taskId, title, description)
        val pending = pendingIntent(requestCodeFor(TYPE_TASK, taskId), intent)
        scheduleExact(triggerAtMillis, pending)
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent  = buildIntent(TYPE_TASK, taskId, "", "")
        val pending = pendingIntent(requestCodeFor(TYPE_TASK, taskId), intent)
        alarmManager.cancel(pending)
        pending.cancel()
    }

    // ── Daily motivation ──────────────────────────────────────────────────────

    fun scheduleDailyMotivation() {
        scheduleDailyMotivationAt(MOTIVATION_HOUR, MOTIVATION_MINUTE)
    }

    fun scheduleDailyMotivationAt(hour: Int, minute: Int) {
        val triggerAt = nextOccurrenceMillis(hour, minute)
        val intent    = buildIntent(TYPE_MOTIVATION, 0L, "", "")
        val pending   = pendingIntent(requestCodeFor(TYPE_MOTIVATION, 0L), intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancelDailyMotivation() {
        val intent  = buildIntent(TYPE_MOTIVATION, 0L, "", "")
        val pending = pendingIntent(requestCodeFor(TYPE_MOTIVATION, 0L), intent)
        alarmManager.cancel(pending)
        pending.cancel()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun scheduleExact(triggerAtMillis: Long, pending: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    private fun buildIntent(type: String, itemId: Long, title: String, content: String): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE,    type)
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_TITLE,   title)
            putExtra(EXTRA_CONTENT, content)
        }

    private fun pendingIntent(requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun requestCodeFor(type: String, id: Long): Int =
        (type.hashCode() * 31 + id).toInt()

    private fun nextOccurrenceMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
