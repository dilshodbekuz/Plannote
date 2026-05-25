package uz.apprica.plannote.data.repository

import uz.apprica.plannote.data.local.dao.TaskDao
import uz.apprica.plannote.data.local.entity.TaskStatus
import uz.apprica.plannote.data.local.mapper.toDomain
import uz.apprica.plannote.data.local.mapper.toEntity
import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { it.toDomain() }

    override fun getActiveTasks(): Flow<List<Task>> =
        taskDao.getActiveTasks().map { it.toDomain() }

    override fun getCompletedTasks(): Flow<List<Task>> =
        taskDao.getCompletedTasks().map { it.toDomain() }

    override fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<Task>> =
        taskDao.getTasksForDay(startOfDay, endOfDay).map { it.toDomain() }

    override fun getOverdueTasks(): Flow<List<Task>> =
        taskDao.getOverdueTasks().map { it.toDomain() }

    override fun searchTasks(query: String): Flow<List<Task>> =
        taskDao.searchTasks(query).map { it.toDomain() }

    override fun getActiveTaskCount(): Flow<Int> =
        taskDao.getActiveTaskCount()

    override suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun addTask(task: Task): Long =
        taskDao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task.toEntity())

    override suspend fun deleteTaskById(id: Long) =
        taskDao.deleteTaskById(id)

    override suspend fun setCompleted(id: Long, isCompleted: Boolean) {
        val newStatus = if (isCompleted) TaskStatus.DONE else TaskStatus.TODO
        taskDao.setCompleted(id, isCompleted, newStatus)
    }

    override suspend fun deleteAllCompleted() =
        taskDao.deleteAllCompleted()

    override suspend fun resetAllCompletions() =
        taskDao.resetAllCompletions()
}
