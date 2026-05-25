package uz.apprica.plannote.domain.usecase.base

import kotlinx.coroutines.flow.Flow

/**
 * Parametrli, Flow qaytaruvchi UseCase asosiy klassi.
 *
 * Ishlatish:
 * ```kotlin
 * class GetAllNotesUseCase @Inject constructor(...) : FlowUseCase<Unit, List<Note>>() {
 *     override fun execute(params: Unit) = repository.getAllNotes()
 * }
 * val flow = useCase(Unit)
 * ```
 */
abstract class FlowUseCase<in Params, out Result> {

    operator fun invoke(params: Params): Flow<Result> = execute(params)

    protected abstract fun execute(params: Params): Flow<Result>
}

/**
 * Parametrsiz variant.
 */
abstract class NoParamFlowUseCase<out Result> {

    operator fun invoke(): Flow<Result> = execute()

    protected abstract fun execute(): Flow<Result>
}
