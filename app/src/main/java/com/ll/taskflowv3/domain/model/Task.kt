package com.ll.taskflowv3.domain.model


data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: Int,
    // ¡Clave para el modo Offline-First!
    val isSynced: Boolean = true
)

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED
}