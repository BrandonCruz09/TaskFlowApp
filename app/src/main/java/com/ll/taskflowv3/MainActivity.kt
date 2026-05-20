package com.ll.taskflowv3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ll.taskflowv3.presentation.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

// ¡ESTA ETIQUETA ES OBLIGATORIA! Le dice a Hilt que prepare las inyecciones para esta ventana
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Llamamos a nuestro mapa de navegación en lugar de un texto estático
                    AppNavigation()
                }
            }
        }
    }
}