package uz.apprica.plannote.domain.usecase.note

import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.base.NoParamFlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Arxivlanmagan barcha eslatmalarni real-time qaytaradi.
 * Pin qilinganlar birinchi, keyin oxirgi yangilanganlar.
 */
class GetAllNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) : NoParamFlowUseCase<List<Note>>() {

    override fun execute(): Flow<List<Note>> = repository.getAllNotes()
}
