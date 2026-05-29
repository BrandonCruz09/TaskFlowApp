package com.ll.taskflowv3.presentation.create_task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ll.taskflowv3.domain.model.Task
import com.ll.taskflowv3.domain.model.TaskStatus
import com.ll.taskflowv3.domain.repository.TaskRepository
import com.ll.taskflowv3.receiver.TaskAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    @ApplicationContext private val context: Context // <- INYECTAMOS EL CONTEXTO
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTaskState())
    val state: StateFlow<CreateTaskState> = _state.asStateFlow()

    fun onTitleChange(title: String) { _state.update { it.copy(title = title, error = null) } }
    fun onDescriptionChange(description: String) { _state.update { it.copy(description = description, error = null) } }
    fun onDueDateChange(dateMillis: Long?) { _state.update { it.copy(dueDate = dateMillis) } }
    fun onCategoryChange(newCategory: String) { _state.update { it.copy(category = newCategory) } }

    // GUARDAMOS LA HORA ELEGIDA
    fun onReminderTimeChange(timeMillis: Long?) { _state.update { it.copy(reminderTime = timeMillis) } }

    fun saveTask() {
        val currentState = _state.value

        if (currentState.title.isBlank() || currentState.description.isBlank()) {
            _state.update { it.copy(error = "El título y la descripción son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = currentState.title,
                description = currentState.description,
                status = TaskStatus.PENDING,
                priority = 1,
                isSynced = false,
                dueDate = currentState.dueDate,
                category = currentState.category,
                reminderTime = currentState.reminderTime
            )

            // 1. Guardar en Base de Datos (Room)
            repository.createTask(newTask)

            // 2. Si el usuario eligió una hora, programamos la alarma nativa
            if (currentState.reminderTime != null) {
                programarAlarma(currentState.title, currentState.reminderTime)
            }

            _state.update { it.copy(isLoading = false, isSaved = true) }
        }
    }

    private fun programarAlarma(title: String, timeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            title.hashCode(), // ID único para cada tarea
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}