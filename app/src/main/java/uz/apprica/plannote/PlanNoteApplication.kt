package uz.apprica.plannote

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import uz.apprica.plannote.data.datastore.PreferencesDataStore
import uz.apprica.plannote.data.datastore.PreferencesDataStore.Companion.todayStr
import uz.apprica.plannote.data.streak.StreakManager
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.notification.AlarmScheduler
import uz.apprica.plannote.notification.NotificationHelper
import javax.inject.Inject

/**
 * Hilt DI uchun talab qilinadigan Application klassi.
 *
 * No-plugin mode: Hilt Gradle plugini AGP 9.x bilan mos kelmagani uchun
 * KSP tomonidan generatsiya qilingan Hilt_PlanNoteApplication sinfini
 * to'g'ridan-to'g'ri kengaytirish orqali ishlatilmoqda.
 */
@HiltAndroidApp(Application::class)
class PlanNoteApplication : Hilt_PlanNoteApplication() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var alarmScheduler: AlarmScheduler
    @Inject lateinit var streakManager: StreakManager
    @Inject lateinit var prefsDataStore: PreferencesDataStore
    @Inject lateinit var taskRepository: TaskRepository

    /** App uchun global coroutine scope — Application yashash davrida ishlaydi */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannels()
        alarmScheduler.scheduleDailyMotivation()

        appScope.launch {
            // Streak tekshirish
            streakManager.checkAndUpdateStreak()

            // Yangi kun bo'lsa — barcha vazifalarni uncheck qilish
            val lastReset = prefsDataStore.getLastTaskResetDate().first()
            if (lastReset != todayStr()) {
                taskRepository.resetAllCompletions()
                prefsDataStore.markTaskResetDone()
            }
        }
    }
}
