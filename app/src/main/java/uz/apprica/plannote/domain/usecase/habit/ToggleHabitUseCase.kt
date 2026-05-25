package uz.apprica.plannote.domain.usecase.habit

import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.utils.DateUtils
import javax.inject.Inject

/**
 * Odat bajarilganlik holatini berilgan kun uchun toggle qiladi.
 *
 * Ishlatish:
 * ```kotlin
 * // Bugun uchun
 * toggleHabitUseCase(habitId = 3L, isCompleted = true)
 *
 * // Boshqa kun uchun
 * toggleHabitUseCase(
 *     ToggleHabitUseCase.Params(habitId = 3L, date = someTimestamp, isCompleted = false)
 * )
 * ```
 */
class ToggleHabitUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    data class Params(
        val habitId: Long,
        val date: Long = System.currentTimeMillis(),
        val isCompleted: Boolean
    )

    suspend operator fun invoke(params: Params) =
        repository.toggleCompletion(params.habitId, params.date, params.isCompleted)

    /** Bugun uchun qulaylik varianti */
    suspend operator fun invoke(habitId: Long, isCompleted: Boolean) =
        repository.toggleCompletion(habitId, DateUtils.startOfDay(), isCompleted)
}
