package com.xakim.plannote.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.xakim.plannote.data.local.converter.RoomConverters
import com.xakim.plannote.data.local.dao.HabitDao
import com.xakim.plannote.data.local.dao.NoteDao
import com.xakim.plannote.data.local.dao.TaskDao
import com.xakim.plannote.data.local.entity.HabitEntity
import com.xakim.plannote.data.local.entity.NoteEntity
import com.xakim.plannote.data.local.entity.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        HabitEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "plannote.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Hilt bilan ishlatilganda bu metod ishlatilmaydi —
         * di/DatabaseModule.kt orqali inject qilinadi.
         * Faqat test yoki standalone holatlarda qo'llang.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
