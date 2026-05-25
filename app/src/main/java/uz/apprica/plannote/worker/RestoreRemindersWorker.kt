package uz.apprica.plannote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uz.apprica.plannote.data.local.AppDatabase
import uz.apprica.plannote.notification.AlarmScheduler

/**
 * Runs once after device reboot to re-schedule all future alarms that were
 * wiped by the system (exact alarms don't survive reboots).
 */
class RestoreRemindersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db        = AppDatabase.getInstance(applicationContext)
        val scheduler = AlarmScheduler(applicationContext)
        val now       = System.currentTimeMillis()

        // Restore note reminders
        db.noteDao()
            .getNotesWithFutureReminders(now)
            .forEach { note ->
                note.reminderAt?.let { time ->
                    scheduler.scheduleNoteReminder(note.id, note.title, time)
                }
            }

        // Restore task reminders
        db.taskDao()
            .getTasksWithFutureReminders(now)
            .forEach { task ->
                task.reminderAt?.let { time ->
                    scheduler.scheduleTaskReminder(task.id, task.title, task.description, time)
                }
            }

        // Re-schedule daily motivation
        scheduler.scheduleDailyMotivation()

        return Result.success()
    }
}
