package com.ll.taskflowv3.data.repository

import com.ll.taskflowv3.core.util.DataError
import com.ll.taskflowv3.core.util.Result
import com.ll.taskflowv3.data.local.TaskDao
import com.ll.taskflowv3.data.local.toEntity
import com.ll.taskflowv3.data.remote.TaskApi
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus
import com.ll.taskflowv3.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val api: TaskApi
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { listaEntities ->
            listaEntities.map { entity -> entity.toDomain() }
        }
    }
    override suspend fun syncPendingTasks() {
        // Agregamos el mapeo para convertir los Entities a Tasks de dominio
        val pendingTasks = dao.getUnsyncedTasks().map { it.toDomain() }

        for (task in pendingTasks) {
            try {
                val response = api.createTask(task.toDto())
                if (response.isSuccessful) {
                    dao.markAsSynced(task.id)
                }
            } catch (e: Exception) {
                continue
            }
        }
    }
    override suspend fun syncTasks(): Result<Unit, DataError.Network> {
        return try {
            val response = api.getTasks()

            if (response.isSuccessful && response.body() != null) {
                val serverTasks = response.body()!!.map { dto ->
                    Task(dto.id, dto.title, dto.description, TaskStatus.valueOf(dto.status), dto.priority, true).toEntity()
                }
                dao.insertTasks(serverTasks)
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        } catch (e: HttpException) {
            Result.Error(DataError.Network.UNKNOWN)
        }
    }

    override suspend fun createTask(task: Task): Result<Unit, DataError.Network> {
        dao.insertTask(task.toEntity())

        return try {
            val dto = com.ll.taskflowv3.data.remote.TaskDto(
                id = task.id, title = task.title, description = task.description,
                status = task.status.name, priority = task.priority
            )
            val response = api.createTask(dto)

            if (response.isSuccessful) {
                dao.markAsSynced(task.id)
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        }
    }

    override suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus): Result<Unit, DataError.Network> {
        dao.updateTaskStatus(taskId, newStatus.name)
        return try {
            // en esta parte se le hace una llamada a RETROFIT para actualizar el estado en el servidor
            val response = api.updateStatus(taskId, mapOf("status" to newStatus.name))
            if (response.isSuccessful) {
                dao.markAsSynced(taskId)
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        }
    }
    override suspend fun deleteTask(taskId: String): Result<Unit, DataError.Network> {
        dao.deleteTask(taskId)
        return try {
            // en esta parte se le hace una llamada a RETROFIT para hacer la funcion delete
            val response = api.deleteTask(taskId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        }
    }

}

fun com.ll.taskflowv3.domain.model.Task.toDto(): com.ll.taskflowv3.data.remote.TaskDto {
    return com.ll.taskflowv3.data.remote.TaskDto(
        id = this.id,
        title = this.title,
        description = this.description,
        status = this.status.name, // Aquí convertimos el TaskStatus a String para PHP
        priority = this.priority,
        dueDate = this.dueDate,
        category = this.category,
        reminderTime =this.reminderTime
    )
}

fun com.ll.taskflowv3.data.remote.TaskDto.toDomain(): com.ll.taskflowv3.domain.model.Task {
    return com.ll.taskflowv3.domain.model.Task(
        id = this.id,
        title = this.title,
        description = this.description,
        status = com.ll.taskflowv3.domain.model.TaskStatus.valueOf(this.status), // De String a TaskStatus
        priority = this.priority,
        isSynced = true,
        dueDate = this.dueDate,
        category = this.category,
        reminderTime = this.reminderTime

    )
}