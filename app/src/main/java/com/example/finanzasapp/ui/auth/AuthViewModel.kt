package com.example.finanzasapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                delay(600)
                if (emailInput.isNotBlank() && passwordInput.length >= 6) {
                    _isAuthenticated.value = true
                    _uiState.value = AuthUiState.Success("Inicio de sesión exitoso")
                } else {
                    _uiState.value = AuthUiState.Error("Contraseña debe tener al menos 6 caracteres")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Error al iniciar sesión")
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                delay(600)
                if (emailInput.contains("@") && passwordInput.length >= 6) {
                    _isAuthenticated.value = true
                    _uiState.value = AuthUiState.Success("Registro exitoso. ¡Bienvenido!")
                } else {
                    _uiState.value = AuthUiState.Error("Ingresa un correo válido y contraseña de 6+ caracteres")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Error al registrarse")
            }
        }
    }

    fun signOut() {
        _isAuthenticated.value = false
        _uiState.value = AuthUiState.Idle
    }
}