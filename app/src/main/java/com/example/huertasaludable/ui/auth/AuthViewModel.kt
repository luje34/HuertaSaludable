package com.example.huertasaludable.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.huertasaludable.data.local.entity.UsuarioEntity
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun seleccionarCuentaPrueba(emailPrueba: String, passwordPrueba: String = "123") {
        _uiState.update {
            it.copy(
                email = emailPrueba,
                password = passwordPrueba,
                emailError = null,
                passwordError = null,
                generalError = null
            )
        }
        iniciarSesion()
    }

    fun iniciarSesion() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

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
            val usuario = repository.getUsuarioByEmail(email)

            if (usuario == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "No existe una cuenta registrada con este correo"
                    )
                }
            } else if (usuario.password != password) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Contraseña incorrecta. Inténtalo de nuevo"
                    )
                }
            } else if (!usuario.activo) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Esta cuenta se encuentra desactivada"
                    )
                }
            } else {
                UserSessionManager.login(usuario)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        usuarioLogueado = usuario
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
