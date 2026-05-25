package uz.apprica.plannote.domain.usecase.note

import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/**
 * Eslatmani o'chiradi.
 * Params — [Note] (to'liq obyekt) yoki faqat ID orqali ham ishlatsa bo'ladi.
 */
class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) : SuspendUseCase<Note, Unit>() {

    override suspend fun execute(params: Note) =
        repository.deleteNote(params)

    /** Faqat ID bilan o'chirish uchun qo'shimcha operator */
    suspend operator fun invoke(id: Long) =
        repository.deleteNoteById(id)
}
