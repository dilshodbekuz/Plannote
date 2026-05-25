package uz.apprica.plannote.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uz.apprica.plannote.notification.AlarmScheduler
import uz.apprica.plannote.notification.NotificationHelper

/**
 * Shows the daily motivational notification and re-schedules the next day's alarm
 * so the cycle repeats indefinitely.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val quotes = listOf(
        "Every day is a new beginning. Take a deep breath and start again. 💪",
        "Small steps every day lead to big results. Keep going! 🚀",
        "You have the power to create the life you want. Start now! ✨",
        "Believe in yourself. You are stronger than you think! 🌟",
        "Today is the perfect day to be productive and make progress. 📈",
        "Focus on progress, not perfection. You are doing great! 🎯",
        "Your future is shaped by what you do today. Make it count! 🔥",
        "Success is the sum of small efforts repeated day in and day out. 💡"
    )

    override suspend fun doWork(): Result {
        val quote = quotes[System.currentTimeMillis().toInt().and(Int.MAX_VALUE) % quotes.size]

        NotificationHelper(applicationContext).showDailyMotivation(quote)

        // Re-schedule next day's alarm
        AlarmScheduler(applicationContext).scheduleDailyMotivation()

        return Result.success()
    }
}
