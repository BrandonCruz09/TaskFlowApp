package com.ll.taskflowv3.data.remote

import com.ll.taskflowv3.domain.model.Task
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {

    @GET("tasks")
    suspend fun getTasks(): Response<List<TaskDto>> // Usamos TaskDto

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): Response<Unit> // Usamos TaskDto

    // Le pasamos el ID en la URL y el nuevo estado en el cuerpo
    @PUT("tasks/{id}")
    suspend fun updateStatus(
        @Path("id") id: String,
        @Body statusUpdate: Map<String, String> // Mandamos un JSON: {"status": "COMPLETED"}
    ): Response<Unit>

    // Le pasamos el ID en la URL para borrar
    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<Unit>
}