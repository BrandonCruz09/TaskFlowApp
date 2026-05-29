package com.ll.taskflowv3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus

@Entity(tableName = "tasks_table")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String, // Room no guarda Enums directamente, guardamos un String
    val priority: Int,
    val isSynced: Boolean,
    val dueDate: Long? = null,
    val category: String = "General",
    val reminderTime: Long? = null
) {
    // Función traductora: Convierte la "Tabla" al modelo puro del "Domain"
    fun toDomain(): Task = Task(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = priority,
        isSynced = isSynced,
        dueDate = dueDate,
        category = category,         // ESTO DEBE ESTAR
        reminderTime = reminderTime
    )
}

// Función traductora inversa: Convierte de Domain a "Tabla"
fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    status = status.name,
    priority = priority,
    isSynced = isSynced,
    dueDate = dueDate,
    category = category,
    reminderTime = reminderTime
)