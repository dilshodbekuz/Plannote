package uz.apprica.plannote.domain.repository

import uz.apprica.plannote.domain.model.HabitStats
import uz.apprica.plannote.domain.model.OverallStats
import uz.apprica.plannote.domain.model.TaskDayStats
import kotlinx.coroutines.flow.Flow

/**
 * Statistika ma'lumotlarini agregatsiya qiladigan repository.
 *
 * Ushbu interfeys TaskRepository, HabitRepository, NoteRepository va
 * PreferencesDataStore dan olingan ma'lumotlarni birlashtiradi va
 * UI uchun tayyor shaklda qaytaradi.
 */
interface StatsRepository {

    /**
     * Joriy hafta (Dush–Yak) uchun har bir kun vazifalar statistikasini qaytaradi.
     * List o'lchami doim 7 ta (index 0 = Dushanba).
     */
    fun getWeeklyTaskStats(): Flow<List<TaskDayStats>>

    /**
     * Barcha faol odatlar uchun haftalik bajarilganlik statistikasini qaytaradi.
     */
    fun getWeeklyHabitStats(): Flow<List<HabitStats>>

    /**
     * Ilovaning umumiy statistikasini qaytaradi (jami, bajarilganlar, streak va h.k.).
     */
    fun getOverallStats(): Flow<OverallStats>
}
