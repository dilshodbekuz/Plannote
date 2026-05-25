package uz.apprica.plannote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uz.apprica.plannote.notification.NotificationHelper

class NoteReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_NOTE_ID = "note_id"
        const val KEY_TITLE   = "title"
        const val KEY_CONTENT = "content"
    }

    override suspend fun doWork(): Result {
        val noteId  = inputData.getLong(KEY_NOTE_ID, -1L)
        val title   = inputData.getString(KEY_TITLE)   ?: ""
        val content = inputData.getString(KEY_CONTENT) ?: ""

        if (noteId == -1L) return Result.failure()

        NotificationHelper(applicationContext).showNoteReminder(noteId, title, content)
        return Result.success()
    }
}
