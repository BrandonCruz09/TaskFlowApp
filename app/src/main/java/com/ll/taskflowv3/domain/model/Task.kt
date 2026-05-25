package com.ll.taskflowv3.domain.model


data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Int = 0,
    val isSynced: Boolean = false,
    val dueDate: Long? = null
)

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED
}