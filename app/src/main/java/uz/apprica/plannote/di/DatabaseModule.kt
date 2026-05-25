package uz.apprica.plannote.di

import android.content.Context
import androidx.room.Room
import uz.apprica.plannote.data.local.AppDatabase
import uz.apprica.plannote.data.local.dao.HabitDao
import uz.apprica.plannote.data.local.dao.HabitLogDao
import uz.apprica.plannote.data.local.dao.NoteDao
import uz.apprica.plannote.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitLogDao(db: AppDatabase): HabitLogDao = db.habitLogDao()
}
