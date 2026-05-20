package com.ll.taskflowv3

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Esta etiqueta le dice a Hilt que empiece a generar código para toda la app
@HiltAndroidApp
class TaskFlowApp : Application()