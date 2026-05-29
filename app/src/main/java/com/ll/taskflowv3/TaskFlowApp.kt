package com.ll.taskflowv3

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskFlowApplication : Application(), Configuration.Provider {

    // Hilt nos inyecta esta fábrica mágica
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Le decimos a Android que use esta fábrica para construir los obreros
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}