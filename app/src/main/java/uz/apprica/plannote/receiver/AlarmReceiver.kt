package uz.apprica.plannote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import uz.apprica.plannote.notification.AlarmScheduler
import uz.apprica.plannote.worker.DailyReminderWorker
import uz.apprica.plannote.worker.NoteReminderWorker
import uz.apprica.plannote.worker.TaskReminderWorker

/**
 * Receives exact alarm broadcasts and delegates to the appropriate WorkManager worker.
 * Using WorkManager ensures the work is done even if the app is killed right after the alarm fires.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type    = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE)    ?: return
        val itemId  = intent.getLongExtra(AlarmScheduler.EXTRA_ITEM_ID, -1L)
        val title   = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE)   ?: ""
        val content = intent.getStringExtra(AlarmScheduler.EXTRA_CONTENT) ?: ""

        val workManager = WorkManager.getInstance(context)

        when (type) {
            AlarmScheduler.TYPE_NOTE -> {
                val data = Data.Builder()
                    .putLong(NoteReminderWorker.KEY_NOTE_ID, itemId)
                    .putString(NoteReminderWorker.KEY_TITLE,   title)
                    .putString(NoteReminderWorker.KEY_CONTENT, content)
                    .build()

                val request = OneTimeWorkRequestBuilder<NoteReminderWorker>()
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
            }

            AlarmScheduler.TYPE_TASK -> {
                val data = Data.Builder()
                    .putLong(TaskReminderWorker.KEY_TASK_ID,     itemId)
                    .putString(TaskReminderWorker.KEY_TITLE,       title)
                    .putString(TaskReminderWorker.KEY_DESCRIPTION, content)
                    .build()

                val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
                    .setInputData(data)
                    .build()

                workManager.enqueue(request)
            }

            AlarmScheduler.TYPE_MOTIVATION -> {
                val request = OneTimeWorkRequestBuilder<DailyReminderWorker>().build()
                workManager.enqueue(request)
            }
        }
    }
}
