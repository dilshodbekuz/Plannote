package uz.apprica.plannote.domain.usecase.task

import uz.apprica.plannote.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Vazifaning bajarilganlik holatini o'zgartiradi.
 *
 * Ishlatish:
 * ```kotlin
 * toggleTaskUseCase(taskId = 5L, isCompleted = true)
 * ```
 */
class ToggleTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    data class Params(val taskId: Long, val isCompleted: Boolean)

    suspend operator fun invoke(params: Params) =
        repository.setCompleted(params.taskId, params.isCompleted)

    /** Qulaylik uchun ikki parametrli variant */
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) =
        repository.setCompleted(taskId, isCompleted)
}
