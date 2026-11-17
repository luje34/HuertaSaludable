package com.example.huertasaludable.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Patterns
import com.example.huertasaludable.data.local.entity.UsuarioEntity

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val usuarioLogueado: UsuarioEntity? = null
)

class AuthViewModel(private val repository: HuertaRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun limpiarEstadoSesion() {
        repository.logout()
        UserSessionManager.logout()
        _uiState.value = LoginUiState()
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                emailError = if (newEmail.isBlank()) "El correo es obligatorio" else null,
                generalError = null
            )
        }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordError = if (newPassword.isBlank()) "La contraseña es obligatoria" else null,
                generalError = null
            )
        }
    }

    fun iniciarSesion() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var hasError = false
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Ingresa tu correo electrónico") }
            hasError = true
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Ingresa tu contraseña") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val usuario = repository.login(email, password)
                UserSessionManager.login(usuario)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        usuarioLogueado = usuario
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = error.message ?: "No se pudo iniciar sesión con la API"
                    )
                }
            }
        }
    }
    fun registrarColaborador(
        nombre: String,
        email: String,
        password: String,
        tallerGrupo: String
    ) {
        val nombreLimpio = nombre.trim()
        val emailLimpio = email.trim()
        val tallerGrupoLimpio = tallerGrupo.trim()

        if (
            nombreLimpio.isBlank() || emailLimpio.isBlank() ||
            password.isBlank() || tallerGrupoLimpio.isBlank()
        ) {
            _uiState.update {
                it.copy(
                    generalError = "Completa todos los campos",
                    isSuccess = false
                )
            }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailLimpio).matches()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    generalError = "Ingresa un correo válido"
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            try {
                val usuarioRegistrado = repository.register(
                    nombre = nombreLimpio,
                    email = emailLimpio,
                    password = password,
                    tallerGrupo = tallerGrupoLimpio
                )
                UserSessionManager.login(usuarioRegistrado)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        usuarioLogueado = usuarioRegistrado,
                        generalError = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = error.message ?: "No se pudo crear la cuenta con la API"
                    )
                }
            }
        }
    }
    class Factory(private val repository: HuertaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
