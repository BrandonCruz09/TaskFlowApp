package com.ll.taskflowv3.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AdminLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Acceso Administrativo") },
        text = {
            Column {
                Text(text = "Ingrese el PIN de administrador para continuar.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isError = false
                    },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = isError,
                    supportingText = { if (isError) Text("PIN incorrecto") else null }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validación directa y sencilla
                    if (password == "1234") {
                        onLoginSuccess()
                        onDismiss()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Entrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}