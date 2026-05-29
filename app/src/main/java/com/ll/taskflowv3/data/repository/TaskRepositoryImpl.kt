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

    override suspend fun getDashboardStats(): Result<com.ll.taskflowv3.data.remote.DashboardStatsDto, DataError.Network> {
        return try {
            val response = api.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(DataError.Network.SERVER_ERROR)
            }
        } catch (e: IOException) {
            Result.Error(DataError.Network.NO_INTERNET)
        }
    }

    override fun getTasks(): Flow<List<Task>> {
        return dao.getAllTasks().map { listaEntities ->
            listaEntities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun syncPendingTasks() {
        val pendingTasks = dao.getUnsyncedTasks().map { it.toDomain() }

        for (task in pendingTasks) {
            try {
                // Usamos toDto() para que viaje con categoría y recordatorio
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
                // Usamos toDomain() y luego toEntity() para preservar TODO
                val serverTasks = response.body()!!.map { dto ->
                    dto.toDomain().toEntity()
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
            // Usamos toDto() para no perder datos en la red
            val response = api.createTask(task.toDto())

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
        status = this.status.name,
        priority = this.priority,
        dueDate = this.dueDate,
        category = this.category,
        reminderTime = this.reminderTime
    )
}

fun com.ll.taskflowv3.data.remote.TaskDto.toDomain(): com.ll.taskflowv3.domain.model.Task {
    return com.ll.taskflowv3.domain.model.Task(
        id = this.id,
        title = this.title,
        description = this.description,
        status = com.ll.taskflowv3.domain.model.TaskStatus.valueOf(this.status),
        priority = this.priority,
        isSynced = true,
        dueDate = this.dueDate,
        category = this.category,
        reminderTime = this.reminderTime
    )

}
