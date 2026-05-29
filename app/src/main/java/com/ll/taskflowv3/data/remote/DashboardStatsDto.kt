package com.ll.taskflowv3.data.remote

data class DashboardStatsDto(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val escuelaTasks: Int,
    val personalTasks: Int,
    val trabajoTasks: Int
)