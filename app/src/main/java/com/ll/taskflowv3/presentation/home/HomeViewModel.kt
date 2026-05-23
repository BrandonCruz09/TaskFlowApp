package com.ll.taskflowv3.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ll.taskflowv3.core.util.DataError
import com.ll.taskflowv3.core.util.Result
import com.ll.taskflowv3.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository // Hilt nos inyecta el repositorio mágico
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        // En cuanto se abre la pantalla, empezamos a escuchar la base de datos y a sincronizar
        observeTasks()
        syncTasks()
    }

    private fun observeTasks() {
        // Leemos el Flow de Room. Cada que haya un insert/update, la lista se refresca sola.
        repository.getTasks()
            .onEach { taskList ->
                _state.update { it.copy(tasks = taskList) }
            }
            .launchIn(viewModelScope)
    }

    private fun syncTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Llamamos a la API PHP
            when (val result = repository.syncTasks()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    // Si falla por internet, no asustamos al usuario, le avisamos sutilmente
                    val errorMsg = if (result.error == DataError.Network.NO_INTERNET) {
                        "Modo Offline activo. Los cambios se guardarán localmente."
                    } else {
                        "No se pudo sincronizar con el servidor."
                    }
                    _state.update { it.copy(isLoading = false, error = errorMsg) }
                }
            }
        }
    }
    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            // Si estaba pendiente, la pasamos a completada, y viceversa
            val newStatus = if (task.status == TaskStatus.COMPLETED) {
                TaskStatus.PENDING
            } else {
                TaskStatus.COMPLETED
            }
            repository.updateTaskStatus(task.id, newStatus)
        }
    }
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task.id)
        }
    }
}