package uz.apprica.plannote.domain.usecase.habit

import uz.apprica.plannote.domain.model.Habit
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.domain.usecase.base.NoParamFlowUseCase
import uz.apprica.plannote.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Bugungi kun uchun mo'ljallangan faol odatlarni qaytaradi.
 * Har kuni filtrlash DateUtils.todayDayBit() orqali amalga oshadi.
 */
class GetAllHabitsUseCase @Inject constructor(
    private val repository: HabitRepository
) : NoParamFlowUseCase<List<Habit>>() {

    override fun execute(): Flow<List<Habit>> =
        repository.getHabitsForDay(DateUtils.todayDayBit())
}
