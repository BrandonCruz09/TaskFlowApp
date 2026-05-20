package com.ll.taskflowv3.presentation.home

import com.ll.taskflowv3.domain.model.Task

// Las 3 cosas que pueden pasar en tu pantalla principal
data class HomeState(
    val tasks: List<Task> = emptyList(), // La lista de tareas
    val isLoading: Boolean = false,      // Si está cargando/sincronizando con PHP
    val error: String? = null            // Si hubo un error (ej. sin internet)
)