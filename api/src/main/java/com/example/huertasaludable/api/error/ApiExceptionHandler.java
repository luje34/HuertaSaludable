package com.example.huertasaludable.api.error;

import java.util.Map;
import java.util.stream.Collectors;

import com.example.huertasaludable.api.usuario.EmailDuplicadoException;
import com.example.huertasaludable.api.usuario.UsuarioNoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(UsuarioNoEncontradoException.class)
	public ProblemDetail manejarNoEncontrado(UsuarioNoEncontradoException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({EmailDuplicadoException.class, DataIntegrityViolationException.class})
	public ProblemDetail manejarConflicto(Exception exception) {
		return ProblemDetail.forStatusAndDetail(
				HttpStatus.CONFLICT,
				exception instanceof EmailDuplicadoException
						? exception.getMessage()
						: "Los datos entran en conflicto con un registro existente");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail manejarValidacion(MethodArgumentNotValidException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, "Revisa los campos enviados");
		Map<String, String> errores = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						error -> error.getField(),
						error -> error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage(),
						(primerError, ignorado) -> primerError));
		problem.setProperty("errores", errores);
		return problem;
	}
}
