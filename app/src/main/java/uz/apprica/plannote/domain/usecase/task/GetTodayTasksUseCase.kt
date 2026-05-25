package uz.apprica.plannote.domain.usecase.task

import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.domain.usecase.base.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Barcha kunlik vazifalarni qaytaradi.
 * Vazifalar sana filtrisiz — qo'shilgan tartibda ko'rsatiladi.
 * Yangi kun boshida PlanNoteApplication avtomatik uncheck qiladi.
 */
class GetTodayTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) : NoParamFlowUseCase<List<Task>>() {

    override fun execute(): Flow<List<Task>> =
        repository.getAllTasks()   // TaskDao: ORDER BY created_at ASC
}
