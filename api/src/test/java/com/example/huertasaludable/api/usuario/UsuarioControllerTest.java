package com.example.huertasaludable.api.usuario;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.huertasaludable.api.usuario.dto.UsuarioCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository repository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void limpiarUsuarios() {
		repository.deleteAll();
	}

	@Test
	void crearUsuarioGuardaPasswordCifradaYNoLaDevuelve() throws Exception {
		UsuarioCreateRequest request = new UsuarioCreateRequest(
				" Ana Pérez ", "ANA@HUERTA.CL", "secreto123", Rol.COLABORADOR, "Taller 5A", null);

		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email", is("ana@huerta.cl")))
				.andExpect(jsonPath("$.nombre", is("Ana Pérez")))
				.andExpect(jsonPath("$.password").doesNotExist());

		Usuario guardado = repository.findAll().get(0);
		org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("secreto123", guardado.getPassword()));
		org.junit.jupiter.api.Assertions.assertTrue(guardado.isActivo());
	}

	@Test
	void rechazarEmailDuplicadoYDatosInvalidos() throws Exception {
		repository.save(usuario("ana@huerta.cl"));

		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new UsuarioCreateRequest("Ana", "ANA@HUERTA.CL", "secreto123",
										Rol.COLABORADOR, "Taller", true))))
				.andExpect(status().isConflict());

		mockMvc.perform(post("/api/usuarios")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"","email":"invalido","password":"","rol":"NINGUNO","tallerGrupo":""}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void actualizarMantienePasswordCuandoSeOmiteYPermiteEliminar() throws Exception {
		Usuario usuario = repository.save(usuario("ana@huerta.cl"));
		String passwordOriginal = usuario.getPassword();

		mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nombre":"Ana Actualizada","email":"ana@huerta.cl","rol":"ADMIN",
								"tallerGrupo":"Taller 6A","activo":false}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo", is(false)))
				.andExpect(jsonPath("$.password").doesNotExist());

		org.junit.jupiter.api.Assertions.assertEquals(passwordOriginal,
				repository.findById(usuario.getId()).orElseThrow().getPassword());
		mockMvc.perform(delete("/api/usuarios/{id}", usuario.getId()))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/usuarios/{id}", usuario.getId()))
				.andExpect(status().isNotFound());
	}

	private Usuario usuario(String email) {
		Usuario usuario = new Usuario();
		usuario.setNombre("Ana");
		usuario.setEmail(email);
		usuario.setPassword("hash");
		usuario.setRol(Rol.COLABORADOR);
		usuario.setTallerGrupo("Taller");
		return usuario;
	}
}
