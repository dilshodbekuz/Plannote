package uz.apprica.plannote.domain.usecase.task

import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.repository.TaskRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/** Vazifani o'chiradi; ID orqali ham ishlatish mumkin */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) : SuspendUseCase<Task, Unit>() {

    override suspend fun execute(params: Task) =
        repository.deleteTask(params)

    suspend operator fun invoke(id: Long) =
        repository.deleteTaskById(id)
}
