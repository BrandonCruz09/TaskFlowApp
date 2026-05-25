package com.ll.taskflowv3.domain.repository


import com.ll.taskflowv3.core.util.DataError
import com.ll.taskflowv3.core.util.Result
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // Flow emitirá una lista en tiempo real. Si la BD cambia, la UI se actualiza sola.
    fun getTasks(): Flow<List<Task>>

    // Funciones suspendidas para operaciones de red o base de datos
    suspend fun syncTasks(): Result<Unit, DataError.Network>

    suspend fun createTask(task: Task): Result<Unit, DataError.Network>

    suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus): Result<Unit, DataError.Network>

    suspend fun deleteTask(taskId: String): com.ll.taskflowv3.core.util.Result<Unit, com.ll.taskflowv3.core.util.DataError.Network>

    suspend fun syncPendingTasks()
}