package com.ll.taskflowv3.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ll.taskflowv3.data.local.datastore.AuthPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authPreferences: AuthPreferences // Hilt nos inyecta tu DataStore de seguridad
) : ViewModel() {

    // MutableStateFlow es reactivo. Si cambia aquí, la UI se redibuja sola.
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Funciones para actualizar el texto que escribe el usuario
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun login() {
        val currentState = _state.value

        // Validación básica
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Por favor, llena todos los campos.") }
            return
        }

        // Lanzamos una corrutina para hacer trabajo en segundo plano
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // SIMULAMOS una llamada a tu API PHP por ahora (espera 2 segundos)
            // Cuando tengas tu API lista, aquí pondremos la llamada real a Retrofit
            delay(2000)

            if (currentState.email == "admin@empresa.com" && currentState.password == "123456") {
                // Éxito: Guardamos un Token falso de forma ultra-segura en tu DataStore
                authPreferences.saveToken("mi_token_jwt_super_seguro_de_php")
                _state.update { it.copy(isLoading = false, isLoggedIn = true) }
            } else {
                // Error de credenciales
                _state.update { it.copy(isLoading = false, error = "Credenciales incorrectas") }
            }
        }
    }
}