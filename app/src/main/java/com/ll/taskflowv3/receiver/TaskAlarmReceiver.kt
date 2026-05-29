package com.ll.taskflowv3.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class TaskAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Recibimos el título de la tarea que nos mandará la pantalla al guardarla
        val taskTitle = intent.getStringExtra("EXTRA_TITLE") ?: "Tienes una tarea pendiente"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8 en adelante exige crear un "Canal" de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TASK_CHANNEL",
                "Recordatorios de TaskFlow",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Construimos el diseño de la tarjetita de notificación
        val notification = NotificationCompat.Builder(context, "TASK_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Ícono de reloj nativo de Android
            .setContentTitle("¡TaskFlowV3 Recordatorio!")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se borra al tocarla
            .build()

        // ¡Disparamos la notificación! Usamos el código hash para que no se sobreescriban
        notificationManager.notify(taskTitle.hashCode(), notification)
    }
}