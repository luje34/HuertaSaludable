package com.example.huertasaludable.api.usuario;

import java.util.List;
import java.util.Locale;

import com.example.huertasaludable.api.usuario.dto.UsuarioCreateRequest;
import com.example.huertasaludable.api.usuario.dto.UsuarioResponse;
import com.example.huertasaludable.api.usuario.dto.UsuarioUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

	private final UsuarioRepository repository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listar() {
		return repository.findAll().stream().map(UsuarioResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(Long id) {
		return UsuarioResponse.from(obtenerUsuario(id));
	}

	public UsuarioResponse crear(UsuarioCreateRequest request) {
		String email = normalizarEmail(request.email());
		if (repository.existsByEmailIgnoreCase(email)) {
			throw new EmailDuplicadoException();
		}

		Usuario usuario = new Usuario();
		usuario.setNombre(request.nombre().trim());
		usuario.setEmail(email);
		usuario.setPassword(passwordEncoder.encode(request.password()));
		usuario.setRol(request.rol());
		usuario.setTallerGrupo(request.tallerGrupo().trim());
		usuario.setActivo(request.activo() == null || request.activo());
		return UsuarioResponse.from(repository.save(usuario));
	}

	public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
		Usuario usuario = obtenerUsuario(id);
		String email = normalizarEmail(request.email());
		if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
			throw new EmailDuplicadoException();
		}

		usuario.setNombre(request.nombre().trim());
		usuario.setEmail(email);
		usuario.setRol(request.rol());
		usuario.setTallerGrupo(request.tallerGrupo().trim());
		usuario.setActivo(request.activo());
		if (request.password() != null && !request.password().isBlank()) {
			usuario.setPassword(passwordEncoder.encode(request.password()));
		}
		return UsuarioResponse.from(repository.save(usuario));
	}

	public void eliminar(Long id) {
		repository.delete(obtenerUsuario(id));
	}

	private Usuario obtenerUsuario(Long id) {
		return repository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
