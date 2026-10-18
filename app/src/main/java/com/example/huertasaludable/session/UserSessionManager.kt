package com.example.huertasaludable.session

import com.example.huertasaludable.data.local.entity.UsuarioEntity
import com.example.huertasaludable.data.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor singleton para administrar el usuario actualmente autenticado en la sesión.
 */
object UserSessionManager {

    private val _currentUser = MutableStateFlow<UsuarioEntity?>(null)
    val currentUser: StateFlow<UsuarioEntity?> = _currentUser.asStateFlow()

    /**
     * Inicia sesión guardando la entidad del usuario.
     */
    fun login(usuario: UsuarioEntity) {
        _currentUser.value = usuario
    }

    /**
     * Cierra la sesión actual limpiando el usuario.
     */
    fun logout() {
        _currentUser.value = null
    }

    /**
     * Devuelve el rol del usuario actualmente autenticado, o null si no hay sesión.
     */
    fun getRolActual(): Rol? {
        return _currentUser.value?.rol
    }

    /**
     * Indica si hay una sesión activa.
     */
    fun isUsuarioLogueado(): Boolean {
        return _currentUser.value != null
    }
}
