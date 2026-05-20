package com.ll.taskflowv3.presentation.login


data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false // Para saber cuándo saltar a la siguiente pantalla
)