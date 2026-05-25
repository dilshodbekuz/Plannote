package com.xakim.plannote.data.local.dao

import androidx.room.*
import com.xakim.plannote.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ── Queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM notes WHERE is_archived = 0 ORDER BY is_pinned DESC, updated_at DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_archived = 1 ORDER BY updated_at DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("""
        SELECT * FROM notes
        WHERE is_archived = 0
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
    """)
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE is_pinned = 1 AND is_archived = 0 ORDER BY updated_at DESC")
    fun getPinnedNotes(): Flow<List<NoteEntity>>

    // ── Mutations ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM notes WHERE is_archived = 1")
    suspend fun deleteAllArchived()

    @Query("UPDATE notes SET is_pinned = :isPinned, updated_at = :updatedAt WHERE id = :id")
    suspend fun togglePin(id: Long, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET is_archived = :isArchived, updated_at = :updatedAt WHERE id = :id")
    suspend fun toggleArchive(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())
}
