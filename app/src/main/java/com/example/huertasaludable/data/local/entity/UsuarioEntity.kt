package com.example.huertasaludable.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.huertasaludable.data.model.Rol

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val email: String,
    @ColumnInfo(name = "password")
    val legacyCredential: String = "",
    val rol: Rol,
    val tallerGrupo: String,
    val activo: Boolean = true,
    @ColumnInfo(defaultValue = "0")
    val synchronizedWithApi: Boolean = false
)
