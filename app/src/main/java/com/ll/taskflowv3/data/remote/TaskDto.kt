package com.ll.taskflowv3.data.remote


// Así es como llega la información de tu API en PHP (JSON)
data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: Int,
    val dueDate: Long? = null
)