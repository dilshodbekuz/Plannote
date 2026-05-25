package uz.apprica.plannote.domain.usecase.note

import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/**
 * Yangi eslatma qo'shadi.
 * Bo'sh sarlavha + kontent bo'lsa [IllegalArgumentException] chiqaradi.
 * Muvaffaqiyatli bo'lsa yangi ID qaytaradi.
 */
class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) : SuspendUseCase<Note, Long>() {

    override suspend fun execute(params: Note): Long {
        require(!params.isEmpty) { "Eslatma bo'sh bo'lishi mumkin emas" }
        return repository.addNote(
            params.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
