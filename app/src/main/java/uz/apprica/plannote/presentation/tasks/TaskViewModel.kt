package uz.apprica.plannote.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import uz.apprica.plannote.domain.model.Priority
import uz.apprica.plannote.domain.model.Task
import uz.apprica.plannote.domain.usecase.task.AddTaskUseCase
import uz.apprica.plannote.domain.usecase.task.DeleteTaskUseCase
import uz.apprica.plannote.domain.usecase.task.GetTodayTasksUseCase
import uz.apprica.plannote.domain.usecase.task.ToggleTaskUseCase
import uz.apprica.plannote.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task>    = emptyList(),
    val completedCount: Int  = 0,
    val totalCount: Int      = 0,
    val progressFraction: Float = 0f,
    val isLoading: Boolean   = true,
    val showAddSheet: Boolean = false,
    val error: String?       = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTodayTasksUseCase: GetTodayTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTodayTasksUseCase().collect { tasks ->
                val done = tasks.count { it.isCompleted }
                _uiState.update {
                    it.copy(
                        tasks            = tasks,
                        completedCount   = done,
                        totalCount       = tasks.size,
                        progressFraction = if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size,
                        isLoading        = false
                    )
                }
            }
        }
    }

    fun showAddSheet()  { _uiState.update { it.copy(showAddSheet = true)  } }
    fun hideAddSheet()  { _uiState.update { it.copy(showAddSheet = false) } }

    fun addTask(
        title: String,
        category: String,
        priority: Priority
    ) {
        viewModelScope.launch {
            try {
                addTaskUseCase(
                    Task(
                        title    = title.trim(),
                        category = category,
                        priority = priority
                    )
                )
                hideAddSheet()
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            // Cancel reminder when task is marked as completed
            if (isCompleted) alarmScheduler.cancelTaskReminder(id)
            toggleTaskUseCase(id, isCompleted)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            alarmScheduler.cancelTaskReminder(task.id)
            deleteTaskUseCase(task)
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
