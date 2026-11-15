package com.example.huertasaludable.api.usuario;

import java.net.URI;
import java.util.List;

import com.example.huertasaludable.api.usuario.dto.UsuarioCreateRequest;
import com.example.huertasaludable.api.usuario.dto.UsuarioResponse;
import com.example.huertasaludable.api.usuario.dto.UsuarioUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService service;

	public UsuarioController(UsuarioService service) {
		this.service = service;
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public UsuarioResponse buscarPorId(@PathVariable Long id) {
		return service.buscarPorId(id);
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
		UsuarioResponse creado = service.crear(request);
		return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public UsuarioResponse actualizar(
			@PathVariable Long id,
			@Valid @RequestBody UsuarioUpdateRequest request) {
		return service.actualizar(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		service.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
