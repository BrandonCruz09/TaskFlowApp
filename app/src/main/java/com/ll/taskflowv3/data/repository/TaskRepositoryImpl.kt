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
        return dao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
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
        return Result.Success(Unit)
    }
}