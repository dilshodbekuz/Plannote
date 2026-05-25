package uz.apprica.plannote.domain.repository

import uz.apprica.plannote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    /** Arxivlanmagan barcha eslatmalar (real-time) */
    fun getAllNotes(): Flow<List<Note>>

    /** Arxivlangan eslatmalar */
    fun getArchivedNotes(): Flow<List<Note>>

    /** Sarlavha yoki kontent bo'yicha qidiruv */
    fun searchNotes(query: String): Flow<List<Note>>

    /** ID bo'yicha bir eslatma (null — topilmasa) */
    suspend fun getNoteById(id: Long): Note?

    /** Yangi eslatma qo'shish → qaytarilgan ID */
    suspend fun addNote(note: Note): Long

    /** Mavjud eslatmani yangilash */
    suspend fun updateNote(note: Note)

    /** Eslatmani o'chirish */
    suspend fun deleteNote(note: Note)

    /** ID orqali o'chirish */
    suspend fun deleteNoteById(id: Long)

    /** Pin holatini o'zgartirish */
    suspend fun togglePin(id: Long, isPinned: Boolean)

    /** Arxiv holatini o'zgartirish */
    suspend fun toggleArchive(id: Long, isArchived: Boolean)
}
