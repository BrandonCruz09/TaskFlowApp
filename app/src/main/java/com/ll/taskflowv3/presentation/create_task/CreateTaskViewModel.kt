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

    fun saveTask() {
        val currentState = _state.value

        if (currentState.title.isBlank() || currentState.description.isBlank()) {
            _state.update { it.copy(error = "El título y la descripción son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Creamos el modelo puro de negocio
            val newTask = Task(
                id = UUID.randomUUID().toString(), // ID único aleatorio
                title = currentState.title,
                description = currentState.description,
                status = TaskStatus.PENDING, // Toda tarea nueva nace como pendiente
                priority = 1, // Prioridad normal por defecto
                isSynced = false // Nace sin sincronizar hasta que Retrofit diga lo contrario
            )

            // Le pasamos el paquete al Repositorio. Él sabrá qué hacer.
            repository.createTask(newTask)

            // Como es Offline-First, asumimos que se guardó localmente con éxito
            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}