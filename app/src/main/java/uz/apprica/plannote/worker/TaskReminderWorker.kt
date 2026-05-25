package uz.apprica.plannote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uz.apprica.plannote.notification.NotificationHelper

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_TASK_ID     = "task_id"
        const val KEY_TITLE       = "title"
        const val KEY_DESCRIPTION = "description"
    }

    override suspend fun doWork(): Result {
        val taskId      = inputData.getLong(KEY_TASK_ID, -1L)
        val title       = inputData.getString(KEY_TITLE)       ?: ""
        val description = inputData.getString(KEY_DESCRIPTION) ?: ""

        if (taskId == -1L) return Result.failure()

        NotificationHelper(applicationContext).showTaskReminder(taskId, title, description)
        return Result.success()
    }
}
