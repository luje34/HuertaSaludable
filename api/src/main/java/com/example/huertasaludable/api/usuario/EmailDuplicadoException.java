package com.example.huertasaludable.api.usuario;

public class EmailDuplicadoException extends RuntimeException {

	public EmailDuplicadoException() {
		super("Ya existe un usuario con ese correo electrónico");
	}
}
