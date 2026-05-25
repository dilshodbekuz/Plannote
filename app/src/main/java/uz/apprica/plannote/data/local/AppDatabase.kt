package uz.apprica.plannote.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import uz.apprica.plannote.data.local.converter.RoomConverters
import uz.apprica.plannote.data.local.dao.HabitDao
import uz.apprica.plannote.data.local.dao.HabitLogDao
import uz.apprica.plannote.data.local.dao.NoteDao
import uz.apprica.plannote.data.local.dao.TaskDao
import uz.apprica.plannote.data.local.entity.HabitEntity
import uz.apprica.plannote.data.local.entity.HabitLogEntity
import uz.apprica.plannote.data.local.entity.NoteEntity
import uz.apprica.plannote.data.local.entity.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao

    companion object {
        const val DATABASE_NAME = "plannote.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

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
