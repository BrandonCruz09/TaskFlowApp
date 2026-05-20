package com.ll.taskflowv3.presentation.create_task

data class CreateTaskState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)