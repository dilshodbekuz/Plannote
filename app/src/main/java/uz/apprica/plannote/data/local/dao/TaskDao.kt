package uz.apprica.plannote.data.local.dao

import androidx.room.*
import uz.apprica.plannote.data.local.entity.TaskEntity
import uz.apprica.plannote.data.local.entity.TaskPriority
import uz.apprica.plannote.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM tasks ORDER BY created_at ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 0 ORDER BY due_date ASC, priority DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = 1 ORDER BY updated_at DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE priority = :priority AND is_completed = 0 ORDER BY due_date ASC")
    fun getTasksByPriority(priority: TaskPriority): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY due_date ASC")
    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category AND is_completed = 0 ORDER BY due_date ASC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE due_date BETWEEN :startOfDay AND :endOfDay
        ORDER BY due_date ASC
    """)
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE due_date < :now AND is_completed = 0
        ORDER BY due_date ASC
    """)
    fun getOverdueTasks(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    fun getActiveTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE reminder_at IS NOT NULL AND reminder_at > :now AND is_completed = 0")
    suspend fun getTasksWithFutureReminders(now: Long): List<TaskEntity>

    // ── Mutations ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE is_completed = 1")
    suspend fun deleteAllCompleted()

    /** Har kuni yangi kun boshida barcha vazifalarni uncheck qiladi */
    @Query("UPDATE tasks SET is_completed = 0, status = :status, updated_at = :now")
    suspend fun resetAllCompletions(
        status: TaskStatus = TaskStatus.TODO,
        now: Long = System.currentTimeMillis()
    )

    @Query("UPDATE tasks SET is_completed = :isCompleted, status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun setCompleted(
        id: Long,
        isCompleted: Boolean,
        status: TaskStatus,
        updatedAt: Long = System.currentTimeMillis()
    )
}
