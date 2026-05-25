package uz.apprica.plannote.di

import uz.apprica.plannote.data.repository.HabitRepositoryImpl
import uz.apprica.plannote.data.repository.NoteRepositoryImpl
import uz.apprica.plannote.data.repository.QuoteRepositoryImpl
import uz.apprica.plannote.data.repository.StatsRepositoryImpl
import uz.apprica.plannote.data.repository.TaskRepositoryImpl
import uz.apprica.plannote.domain.repository.HabitRepository
import uz.apprica.plannote.domain.repository.NoteRepository
import uz.apprica.plannote.domain.repository.QuoteRepository
import uz.apprica.plannote.domain.repository.StatsRepository
import uz.apprica.plannote.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository interfeyslari ↔ implementatsiyalari binding-i.
 *
 * [Binds] ishlatamiz — [Provides] ga nisbatan kamroq boilerplate,
 * kompilyator vaqtida tekshiriladi.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(
        impl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        impl: StatsRepositoryImpl
    ): StatsRepository

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        impl: QuoteRepositoryImpl
    ): QuoteRepository
}
