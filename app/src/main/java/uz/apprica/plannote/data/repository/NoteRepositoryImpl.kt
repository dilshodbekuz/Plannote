package uz.apprica.plannote.data.repository

import uz.apprica.plannote.data.local.dao.NoteDao
import uz.apprica.plannote.data.local.mapper.toDomain
import uz.apprica.plannote.data.local.mapper.toEntity
import uz.apprica.plannote.domain.model.Note
import uz.apprica.plannote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { it.toDomain() }

    override fun getArchivedNotes(): Flow<List<Note>> =
        noteDao.getArchivedNotes().map { it.toDomain() }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query).map { it.toDomain() }

    override suspend fun getNoteById(id: Long): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun addNote(note: Note): Long =
        noteDao.insertNote(note.toEntity())

    override suspend fun updateNote(note: Note) =
        noteDao.updateNote(note.toEntity())

    override suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note.toEntity())

    override suspend fun deleteNoteById(id: Long) =
        noteDao.deleteNoteById(id)

    override suspend fun togglePin(id: Long, isPinned: Boolean) =
        noteDao.togglePin(id, isPinned)

    override suspend fun toggleArchive(id: Long, isArchived: Boolean) =
        noteDao.toggleArchive(id, isArchived)
}
