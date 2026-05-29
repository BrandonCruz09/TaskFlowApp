package com.ll.taskflowv3.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ll.taskflowv3.data.remote.DashboardStatsDto
import com.ll.taskflowv3.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val stats: DashboardStatsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getDashboardStats()) {
                is com.ll.taskflowv3.core.util.Result.Success -> {
                    _state.update { it.copy(stats = result.data, isLoading = false) }
                }
                is com.ll.taskflowv3.core.util.Result.Error -> {
                    _state.update { it.copy(error = "Error al conectar con el servidor central.", isLoading = false) }
                }
            }
        }
    }
}