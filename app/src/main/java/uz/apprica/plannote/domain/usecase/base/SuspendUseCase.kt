package uz.apprica.plannote.domain.usecase.base

/**
 * Parametrli, suspend qaytaruvchi UseCase asosiy klassi.
 *
 * Ishlatish:
 * ```kotlin
 * class AddNoteUseCase @Inject constructor(...) : SuspendUseCase<Note, Long>() {
 *     override suspend fun execute(params: Note) = repository.addNote(params)
 * }
 * val id = addNoteUseCase(note)
 * ```
 */
abstract class SuspendUseCase<in Params, out Result> {

    suspend operator fun invoke(params: Params): Result = execute(params)

    protected abstract suspend fun execute(params: Params): Result
}

/**
 * Parametrsiz suspend variant.
 */
abstract class NoParamSuspendUseCase<out Result> {

    suspend operator fun invoke(): Result = execute()

    protected abstract suspend fun execute(): Result
}
