package uz.apprica.plannote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import uz.apprica.plannote.worker.RestoreRemindersWorker

/**
 * Listens for BOOT_COMPLETED and re-queues a WorkManager job to restore
 * all future reminders after a device reboot (exact alarms are wiped on reboot).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val request = OneTimeWorkRequestBuilder<RestoreRemindersWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
