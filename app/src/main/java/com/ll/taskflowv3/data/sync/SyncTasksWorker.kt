package com.ll.taskflowv3.data.sync
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ll.taskflowv3.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
@HiltWorker
class SyncTasksWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
// Llama a la lógica centralizada del repositorio para subir pendientes
            repository.syncPendingTasks()
            Result.success()
        } catch (e: Exception) {
// Si ocurre un fallo recuperable, se solicita reintento automático
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}