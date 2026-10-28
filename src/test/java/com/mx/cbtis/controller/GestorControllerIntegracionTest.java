package com.mx.cbtis.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.sun.net.httpserver.HttpServer;

/**
 * Pruebas automáticas del frontend (GestorController) contra un backend simulado (HttpServer
 * en un puerto libre que se inyecta en la propiedad gestor.api.url). Cubren casos de éxito (AUT-E)
 * y de fracaso (AUT-F); no interfieren con una API real levantada en el puerto 8081.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GestorControllerIntegracionTest {

	private static HttpServer backend;
	/** ruta -> {status, cuerpo JSON} que devuelve el backend simulado */
	private static final Map<String, Object[]> rutas = new ConcurrentHashMap<>();
	/** ruta -> número de llamadas recibidas */
	private static final Map<String, Integer> llamadas = new ConcurrentHashMap<>();
	private static final Map<String, String> cuerpos = new ConcurrentHashMap<>();

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	static void iniciarBackendSimulado() throws IOException {
		backend = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		backend.createContext("/", ex -> {
			String ruta = ex.getRequestURI().getPath();
			llamadas.merge(ruta, 1, Integer::sum);
			cuerpos.put(ruta, new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			Object[] r = rutas.getOrDefault(ruta, new Object[] { 404, "{}" });
			byte[] out = ((String) r[1]).getBytes(StandardCharsets.UTF_8);
			ex.getResponseHeaders().add("Content-Type", "application/json");
			ex.sendResponseHeaders((Integer) r[0], out.length == 0 ? -1 : out.length);
			if (out.length > 0) ex.getResponseBody().write(out);
			ex.close();
		});
		backend.start();
	}

	@DynamicPropertySource
	static void urlDelBackendSimulado(DynamicPropertyRegistry registro) {
		registro.add("gestor.api.url", () -> "http://127.0.0.1:" + backend.getAddress().getPort());
	}

	@AfterAll
	static void detenerBackendSimulado() {
		backend.stop(0);
	}

	@BeforeEach
	void limpiar() {
		rutas.clear();
		llamadas.clear();
		cuerpos.clear();
	}

	private static void backendResponde(String ruta, int status, String json) {
		rutas.put(ruta, new Object[] { status, json });
	}

	// ------------------------------------------------------------------ casos de éxito

	@Test
	@DisplayName("AUT-E01 Servicio de consulta: el backend devuelve información válida")
	void AUT_E01_consultaProyectosDevuelveLista() throws Exception {
		backendResponde("/buscarProyectos", 200,
				"[{\"id_proyecto\":1,\"nombre\":\"Proyecto A\"},{\"id_proyecto\":2,\"nombre\":\"Proyecto B\"}]");

		mockMvc.perform(post("/consultarProyectos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nombre").value("Proyecto A"));
		assertEquals(1, llamadas.getOrDefault("/buscarProyectos", 0));
	}

	@Test
	@DisplayName("AUT-E02 Formulario: se proporcionan datos válidos")
	void AUT_E02_registroUsuarioConDatosValidos() throws Exception {
		backendResponde("/buscarUsuarios", 200, "[]");
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Ana QA").param("correo_electronico", "ana.qa@mail.com")
				.param("contrasena", "1234").param("rol", "Estudiante").param("imagen_usuario", "#112233"))
				.andExpect(redirectedUrl("/login"));
		assertEquals(1, llamadas.getOrDefault("/addUsuario", 0));
		assertTrue(cuerpos.get("/addUsuario").contains("ana.qa@mail.com"));
	}

	@Test
	@DisplayName("AUT-E03 Componente / Vista: la consulta finaliza y los datos llegan a la vista")
	void AUT_E03_vistaProyectoRecibeUsuarios() throws Exception {
		backendResponde("/buscarUsuarios", 200,
				"[{\"id_usuario\":1,\"nombre\":\"Ana\"},{\"id_usuario\":2,\"nombre\":\"Luis\"}]");

		mockMvc.perform(get("/proyecto"))
				.andExpect(status().isOk())
				.andExpect(view().name("proyecto"))
				.andExpect(model().attributeExists("listaUsuarios"))
				.andExpect(model().attribute("listaUsuarios", org.hamcrest.Matchers.hasSize(2)));
	}

	@Test
	@DisplayName("AUT-E04 Servicio de login: credenciales válidas devuelven el usuario")
	void AUT_E04_loginValido() throws Exception {
		backendResponde("/getUsuarioLogin", 200, "{\"id_usuario\":7,\"nombre\":\"Ana QA\",\"correo_electronico\":\"ana.qa@mail.com\"}");

		mockMvc.perform(post("/verificarLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana.qa@mail.com\",\"contrasena\":\"1234\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Ana QA"));
	}

	// ------------------------------------------------------------------ casos de fracaso

	@Test
	@DisplayName("AUT-F01 Servicio de consulta: el backend devuelve un error y el frontend lo controla")
	void AUT_F01_errorDelBackendEsControlado() throws Exception {
		backendResponde("/buscarProyectos", 500, "{\"error\":\"Internal Server Error\"}");

		mockMvc.perform(post("/consultarProyectos"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	@DisplayName("AUT-F02 Formulario: datos incompletos no se envían al backend")
	void AUT_F02_registroConCorreoVacioNoSeEnvia() throws Exception {
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Sin Correo").param("correo_electronico", "")
				.param("contrasena", "1234").param("rol", "Estudiante"))
				.andExpect(redirectedUrl("/register?error=campos"));
		assertEquals(0, llamadas.getOrDefault("/addUsuario", 0), "No debe llamarse al backend con datos incompletos");
	}

	@Test
	@DisplayName("AUT-F03 Componente / Vista: el recurso solicitado no existe")
	void AUT_F03_proyectoInexistenteDevuelve404() throws Exception {
		backendResponde("/buscarProyecto", 200,
				"{\"id_proyecto\":null,\"nombre\":null,\"descripcion\":null,\"fecha_inicio\":null,\"fecha_finalizacion\":null,\"estado_proyecto\":null,\"fondo\":null}");

		mockMvc.perform(post("/actualizarProyecto").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":999}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	@DisplayName("AUT-F04 Servicio de login: credenciales inválidas se informan sin romper la vista")
	void AUT_F04_loginInvalido() throws Exception {
		backendResponde("/getUsuarioLogin", 200, "{\"id_usuario\":null,\"nombre\":\"000\"}");

		mockMvc.perform(post("/verificarLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"x@mail.com\",\"contrasena\":\"mala\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("000"));
	}

	@Test
	@DisplayName("AUT-F05 Servicio de login: el backend no responde correctamente y el frontend lo controla")
	void AUT_F05_loginConBackendConError() throws Exception {
		backendResponde("/getUsuarioLogin", 500, "{\"error\":\"Internal Server Error\"}");

		mockMvc.perform(post("/verificarLogin").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo_electronico\":\"ana.qa@mail.com\",\"contrasena\":\"1234\"}"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	@DisplayName("AUT-F06 Componente / Vista: error del backend al obtener tareas se controla")
	void AUT_F06_obtenerTareasConBackendConError() throws Exception {
		backendResponde("/getTarea", 500, "{\"error\":\"Internal Server Error\"}");

		mockMvc.perform(post("/obtenerTareas").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_proyecto\":197}"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	@DisplayName("AUT-F07 Formulario: proyecto con fecha de finalización anterior a la de inicio")
	void AUT_F07_proyectoConFechasInvertidasNoSeEnvia() throws Exception {
		backendResponde("/addProyecto", 200, "1");

		mockMvc.perform(post("/saveProyecto")
				.param("nombre", "Proyecto QA").param("descripcion", "Fechas invertidas")
				.param("fecha_inicio", "2026-12-15").param("fecha_finalizacion", "2026-10-01")
				.param("estado_proyecto", "En proceso").param("fondo", "/Images/naranja.png"))
				.andExpect(redirectedUrl("/inicio?error=fechas"));
		assertEquals(0, llamadas.getOrDefault("/addProyecto", 0), "No debe llamarse al backend con fechas inválidas");
	}

	@Test
	@DisplayName("AUT-F08 Componente / Vista: la tarea solicitada no existe")
	void AUT_F08_tareaInexistenteDevuelve404() throws Exception {
		backendResponde("/buscarTarea", 200,
				"{\"id_tarea\":null,\"nombre\":null,\"descripcion\":null,\"fecha_vencimiento\":null,\"lista\":null,\"estado\":null,\"id_proyecto\":null}");

		mockMvc.perform(post("/actualizarTarea").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":999}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensaje").exists());
	}

	@Test
	@DisplayName("AUT-F09 Formulario: correo ya registrado no se vuelve a registrar")
	void AUT_F09_registroConCorreoDuplicadoNoSeEnvia() throws Exception {
		backendResponde("/buscarUsuarios", 200, "[{\"id_usuario\":1,\"nombre\":\"Ana\",\"correo_electronico\":\"ana.qa@mail.com\"}]");
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Otra Ana").param("correo_electronico", "ANA.QA@mail.com")
				.param("contrasena", "1234").param("rol", "Estudiante").param("imagen_usuario", "#112233"))
				.andExpect(redirectedUrl("/register?error=correo"));
		assertEquals(0, llamadas.getOrDefault("/addUsuario", 0), "No debe registrarse un correo duplicado");
	}

	@Test
	@DisplayName("AUT-F10 Formulario: rol sin seleccionar no se envía al backend")
	void AUT_F10_registroSinRolNoSeEnvia() throws Exception {
		backendResponde("/buscarUsuarios", 200, "[]");
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Sin Rol").param("correo_electronico", "sin.rol@mail.com")
				.param("contrasena", "1234").param("rol", "0").param("imagen_usuario", "#112233"))
				.andExpect(redirectedUrl("/register?error=rol"));
		assertEquals(0, llamadas.getOrDefault("/addUsuario", 0), "No debe registrarse un usuario sin rol");
	}

	// ------------------------------------------------------------------ vistas
	// (AUT-V: las páginas se renderizan con Thymeleaf y reciben los objetos del formulario)

	@Test
	@DisplayName("AUT-V01 Páginas estáticas: home, tutorial, manual y login se renderizan")
	void AUT_V01_paginasEstaticas() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
		mockMvc.perform(get("/tutorial")).andExpect(status().isOk()).andExpect(view().name("tutorial"));
		mockMvc.perform(get("/manual")).andExpect(status().isOk()).andExpect(view().name("manual"));
		mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
		assertTrue(llamadas.isEmpty(), "Las páginas estáticas no deben consultar al backend");
	}

	@Test
	@DisplayName("AUT-V02 Registro e inicio reciben un objeto vacío para el formulario")
	void AUT_V02_formulariosRecibenModelo() throws Exception {
		mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("register"))
				.andExpect(model().attributeExists("usuario"));
		mockMvc.perform(get("/inicio"))
				.andExpect(status().isOk())
				.andExpect(view().name("inicio"))
				.andExpect(model().attributeExists("proyectos"));
	}

	@Test
	@DisplayName("AUT-V03 /tarea entrega la vista de proyecto con una tarea vacía")
	void AUT_V03_vistaTarea() throws Exception {
		mockMvc.perform(get("/tarea"))
				.andExpect(status().isOk())
				.andExpect(view().name("proyecto"))
				.andExpect(model().attributeExists("tarea"));
	}

	// ------------------------------------------------------------------ resto de servicios

	@Test
	@DisplayName("AUT-E05 Registro: usuarios existentes con otro correo (o sin correo) no impiden el registro")
	void AUT_E05_registroConUsuarioSinCorreoEnLista() throws Exception {
		backendResponde("/buscarUsuarios", 200, "[{\"id_usuario\":1,\"nombre\":\"Sin correo\"},{\"id_usuario\":2,\"correo_electronico\":\"otra@mail.com\"}]");
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Luis").param("correo_electronico", " luis@mail.com ")
				.param("contrasena", "1234").param("rol", "Docente"))
				.andExpect(redirectedUrl("/login"));
		assertEquals(1, llamadas.getOrDefault("/addUsuario", 0));
	}

	@Test
	@DisplayName("AUT-E06 Registro: el backend no devuelve lista de usuarios y se registra igual")
	void AUT_E06_registroConListaNula() throws Exception {
		backendResponde("/buscarUsuarios", 200, "");
		backendResponde("/addUsuario", 200, "1");

		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Luis").param("correo_electronico", "luis@mail.com")
				.param("contrasena", "1234").param("rol", "Docente"))
				.andExpect(redirectedUrl("/login"));
		assertEquals(1, llamadas.getOrDefault("/addUsuario", 0));
	}

	@Test
	@DisplayName("AUT-F11 Registro: rol ausente, nombre en blanco o sin contraseña se rechazan")
	void AUT_F11_registroConCamposInvalidos() throws Exception {
		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Luis").param("correo_electronico", "luis@mail.com").param("contrasena", "1234"))
				.andExpect(redirectedUrl("/register?error=rol"));
		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "   ").param("correo_electronico", "luis@mail.com").param("contrasena", "1234"))
				.andExpect(redirectedUrl("/register?error=campos"));
		mockMvc.perform(post("/saveUsuario")
				.param("nombre", "Luis").param("correo_electronico", "luis@mail.com"))
				.andExpect(redirectedUrl("/register?error=campos"));
		assertTrue(llamadas.isEmpty(), "No debe consultarse al backend con datos inválidos");
	}

	@Test
	@DisplayName("AUT-E07 Proyecto válido (o sin fechas completas) se envía al backend")
	void AUT_E07_guardarProyectoValido() throws Exception {
		backendResponde("/addProyecto", 200, "1");

		mockMvc.perform(post("/saveProyecto")
				.param("nombre", "Proyecto QA").param("fecha_inicio", "2026-10-01").param("fecha_finalizacion", "2026-12-15"))
				.andExpect(redirectedUrl("/inicio"));
		mockMvc.perform(post("/saveProyecto").param("nombre", "Sin fechas"))
				.andExpect(redirectedUrl("/inicio"));
		mockMvc.perform(post("/saveProyecto").param("nombre", "Solo inicio").param("fecha_inicio", "2026-10-01"))
				.andExpect(redirectedUrl("/inicio"));
		assertEquals(3, llamadas.getOrDefault("/addProyecto", 0));
		assertTrue(cuerpos.get("/addProyecto").contains("Solo inicio"));
	}

	@Test
	@DisplayName("AUT-E08 Eliminar proyecto envía DELETE al backend")
	void AUT_E08_eliminarProyecto() throws Exception {
		backendResponde("/deleteProyecto", 200, "1");

		mockMvc.perform(post("/eliminarProyecto").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":197}"))
				.andExpect(redirectedUrl("/inicio"));
		assertEquals(1, llamadas.getOrDefault("/deleteProyecto", 0));
		assertTrue(cuerpos.get("/deleteProyecto").contains("197"));
	}

	@Test
	@DisplayName("AUT-E09 Consultar un proyecto existente para editarlo")
	void AUT_E09_actualizarProyectoExistente() throws Exception {
		backendResponde("/buscarProyecto", 200, "{\"id_proyecto\":197,\"nombre\":\"My API Rest\"}");

		mockMvc.perform(post("/actualizarProyecto").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id_proyecto").value(197))
				.andExpect(jsonPath("$.nombre").value("My API Rest"));
	}

	@Test
	@DisplayName("AUT-E10 Consultar usuarios, tareas y comentarios devuelve las listas del backend")
	void AUT_E10_consultasGenerales() throws Exception {
		backendResponde("/buscarUsuarios", 200, "[{\"id_usuario\":1,\"nombre\":\"Ana\"}]");
		backendResponde("/buscarTareas", 200, "[{\"id_tarea\":1},{\"id_tarea\":2}]");
		backendResponde("/buscarComentarios", 200, "[{\"id_comentario\":1},{\"id_comentario\":2},{\"id_comentario\":3}]");

		mockMvc.perform(post("/consultarUsuarios"))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Ana"));
		mockMvc.perform(post("/consultarTareas"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
		mockMvc.perform(post("/consultarComentarios"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
	}

	@Test
	@DisplayName("AUT-E11 Tareas y comentarios filtrados por proyecto / tarea")
	void AUT_E11_obtenerTareasYComentarios() throws Exception {
		backendResponde("/getTarea", 200, "[{\"id_tarea\":199,\"id_proyecto\":197,\"nombre\":\"Base de datos\"}]");
		backendResponde("/getComentario", 200, "[{\"id_comentario\":201,\"id_tarea\":199,\"texto_comentario\":\"hola\"}]");

		mockMvc.perform(post("/obtenerTareas").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":197}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Base de datos"));
		assertTrue(cuerpos.get("/getTarea").contains("197"));

		mockMvc.perform(post("/obtenerComentarios").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].texto_comentario").value("hola"));
		assertTrue(cuerpos.get("/getComentario").contains("199"));
	}

	@Test
	@DisplayName("AUT-E12 Guardar tarea redirige al proyecto con su id")
	void AUT_E12_guardarTarea() throws Exception {
		backendResponde("/addTareas", 200, "1");

		mockMvc.perform(post("/saveTarea").param("nombre", "Nueva").param("id_proyecto", "197"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/proyecto?id_proyecto=197"));
		assertEquals(1, llamadas.getOrDefault("/addTareas", 0));
	}

	@Test
	@DisplayName("AUT-E13 Guardar comentario devuelve la respuesta del backend")
	void AUT_E13_guardarComentario() throws Exception {
		backendResponde("/addComentario", 200, "1");

		mockMvc.perform(post("/saveComentario").contentType(MediaType.APPLICATION_JSON)
				.content("{\"id_tarea\":199,\"texto_comentario\":\"Listo\"}"))
				.andExpect(status().isOk())
				.andExpect(content().string("1"));
		assertTrue(cuerpos.get("/addComentario").contains("Listo"));
	}

	@Test
	@DisplayName("AUT-E14 Consultar una tarea existente para editarla")
	void AUT_E14_actualizarTareaExistente() throws Exception {
		backendResponde("/buscarTarea", 200, "{\"id_tarea\":199,\"nombre\":\"Base de datos\"}");

		mockMvc.perform(post("/actualizarTarea").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Base de datos"));
	}

	@Test
	@DisplayName("AUT-E15 Eliminar tarea envía DELETE al backend")
	void AUT_E15_eliminarTarea() throws Exception {
		backendResponde("/deleteTareas", 200, "1");

		mockMvc.perform(post("/eliminarTarea").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199}"))
				.andExpect(redirectedUrl("/proyecto"));
		assertEquals(1, llamadas.getOrDefault("/deleteTareas", 0));
	}

	@Test
	@DisplayName("AUT-E16 Verificar y cambiar tareas reenvían los datos al backend")
	void AUT_E16_verificarYCambiarTareas() throws Exception {
		backendResponde("/addTareas", 200, "1");

		mockMvc.perform(post("/verificarTarea").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Nueva\",\"id_proyecto\":197}"))
				.andExpect(status().isOk()).andExpect(content().string("1"));
		mockMvc.perform(post("/cambiosTareas").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":199,\"lista\":\"terminado\"}"))
				.andExpect(status().isOk()).andExpect(content().string("1"));
		assertEquals(2, llamadas.getOrDefault("/addTareas", 0));
		assertTrue(cuerpos.get("/addTareas").contains("terminado"));
	}

	@Test
	@DisplayName("AUT-F12 Proyecto o tarea inexistentes: el backend devuelve cuerpo vacío")
	void AUT_F12_proyectoYTareaNulos() throws Exception {
		backendResponde("/buscarProyecto", 200, "");
		backendResponde("/buscarTarea", 200, "");

		mockMvc.perform(post("/actualizarProyecto").contentType(MediaType.APPLICATION_JSON).content("{\"id_proyecto\":1}"))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/actualizarTarea").contentType(MediaType.APPLICATION_JSON).content("{\"id_tarea\":1}"))
				.andExpect(status().isNotFound());
	}
}
