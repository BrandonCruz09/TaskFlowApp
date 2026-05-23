package com.ll.taskflowv3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Flow hace que la pantalla se actualice sola si la base de datos cambia
    @Query("SELECT * FROM tasks_table ORDER BY priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks_table WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("UPDATE tasks_table SET isSynced = 1 WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)

    @Query("UPDATE tasks_table SET status = :status, isSynced = 0 WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, status: String)

    @Query("DELETE FROM tasks_table WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)
}