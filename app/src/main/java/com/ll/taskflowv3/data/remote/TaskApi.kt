package com.ll.taskflowv3.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks") // Asumiendo que esta es la ruta de tu API
    suspend fun getTasks(): Response<List<TaskDto>>

    @POST("tasks")
    suspend fun createTask(@Body task: TaskDto): Response<Unit>

    @PUT("tasks/{id}/status")
    suspend fun updateStatus(@Path("id") taskId: String, @Body status: String): Response<Unit>
}