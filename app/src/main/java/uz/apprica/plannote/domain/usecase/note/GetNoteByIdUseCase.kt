package uz.apprica.plannote.domain.usecase.note

import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/** ID bo'yicha eslatma; topilmasa null qaytaradi */
class GetNoteByIdUseCase @Inject constructor(
    private val repository: NoteRepository
) : SuspendUseCase<Long, Note?>() {

    override suspend fun execute(params: Long): Note? =
        repository.getNoteById(params)
}
