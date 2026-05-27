package com.ll.taskflowv3.presentation.create_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus
import com.ll.taskflowv3.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun onTitleChange(title: String) {
        _state.update { it.copy(title = title, error = null) }
    }

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description, error = null) }
    }

    fun onDueDateChange(dateMillis: Long?) {
        _state.update { it.copy(dueDate = dateMillis) }
    }

    fun onCategoryChange(newCategory: String) {
        _state.update { it.copy(category = newCategory) }
    }

    fun saveTask() {
        val currentState = _state.value

        if (currentState.title.isBlank() || currentState.description.isBlank()) {
            _state.update { it.copy(error = "El título y la descripción son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = currentState.title,
                description = currentState.description,
                status = TaskStatus.PENDING,
                priority = 1,
                isSynced = false,
                dueDate = currentState.dueDate,
                category = currentState.category, // SE ASIGNA CORRECTAMENTE
                reminderTime = null
            )

            repository.createTask(newTask)
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}