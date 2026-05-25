package uz.apprica.plannote.domain.usecase.task

import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/** Yangi vazifa qo'shadi → yangi ID qaytaradi */
class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) : SuspendUseCase<Task, Long>() {

    override suspend fun execute(params: Task): Long {
        require(params.title.isNotBlank()) { "Vazifa nomi bo'sh bo'lishi mumkin emas" }
        return repository.addTask(
            params.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
