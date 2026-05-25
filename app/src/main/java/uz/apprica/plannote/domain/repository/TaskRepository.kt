package uz.apprica.plannote.domain.repository

import uz.apprica.plannote.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    /** Barcha vazifalar (real-time) */
    fun getAllTasks(): Flow<List<Task>>

    /** Bajarilmagan vazifalar */
    fun getActiveTasks(): Flow<List<Task>>

    /** Bajarilgan vazifalar */
    fun getCompletedTasks(): Flow<List<Task>>

    /** Muayyan kun uchun vazifalar (startOfDay..endOfDay) */
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<Task>>

    /** Muddati o'tgan, bajarilmagan vazifalar */
    fun getOverdueTasks(): Flow<List<Task>>

    /** Sarlavha yoki tavsif bo'yicha qidiruv */
    fun searchTasks(query: String): Flow<List<Task>>

    /** Faol vazifalar soni (badge uchun) */
    fun getActiveTaskCount(): Flow<Int>

    /** ID bo'yicha bir vazifa */
    suspend fun getTaskById(id: Long): Task?

    /** Yangi vazifa qo'shish → ID */
    suspend fun addTask(task: Task): Long

    /** Vazifani yangilash */
    suspend fun updateTask(task: Task)

    /** Vazifani o'chirish */
    suspend fun deleteTask(task: Task)

    /** ID orqali o'chirish */
    suspend fun deleteTaskById(id: Long)

    /** Bajarildi/Bajarilmadi ni almashirish */
    suspend fun setCompleted(id: Long, isCompleted: Boolean)

    /** Barcha bajarilganlarni tozalash */
    suspend fun deleteAllCompleted()

    /** Yangi kun boshida barcha vazifalarni uncheck qilish */
    suspend fun resetAllCompletions()
}
