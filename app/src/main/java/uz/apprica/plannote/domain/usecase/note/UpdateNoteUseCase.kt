package uz.apprica.plannote.domain.usecase.note

import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.usecase.base.SuspendUseCase
import javax.inject.Inject

/** Mavjud eslatmani yangilaydi; updatedAt avtomatik o'rnatiladi */
class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) : SuspendUseCase<Note, Unit>() {

    override suspend fun execute(params: Note) {
        repository.updateNote(
            params.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
