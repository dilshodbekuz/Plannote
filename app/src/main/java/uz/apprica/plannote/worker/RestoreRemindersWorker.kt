package uz.apprica.plannote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uz.apprica.plannote.data.local.AppDatabase
import uz.apprica.plannote.notification.AlarmScheduler

class RestoreRemindersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db        = AppDatabase.getInstance(applicationContext)
        val scheduler = AlarmScheduler(applicationContext)
        val now       = System.currentTimeMillis()

        db.noteDao()
            .getNotesWithFutureReminders(now)
            .forEach { note ->
                note.reminderAt?.let { time ->
                    scheduler.scheduleNoteReminder(note.id, note.title, time)
                }
            }

        db.taskDao()
            .getTasksWithFutureReminders(now)
            .forEach { task ->
                task.reminderAt?.let { time ->
                    scheduler.scheduleTaskReminder(task.id, task.title, task.description, time)
                }
            }

        scheduler.scheduleDailyMotivation()
        return Result.success()
    }
}
