package com.example.huertasaludable.api.usuario.dto;

import com.example.huertasaludable.api.usuario.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioCreateRequest(
		@NotBlank @Size(max = 100) String nombre,
		@NotBlank @Email @Size(max = 180) String email,
		@NotBlank @Size(max = 72) String password,
		@NotNull Rol rol,
		@NotBlank @Size(max = 120) String tallerGrupo,
		Boolean activo) {
}
