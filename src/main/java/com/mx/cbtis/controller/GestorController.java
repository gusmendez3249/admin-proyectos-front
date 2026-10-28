package com.mx.cbtis.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@Controller
public class GestorController {

	private static final Logger log = LoggerFactory.getLogger(GestorController.class);

	/** URL base del microservicio backend (configurable por propiedad o variable de entorno). */
	@Value("${gestor.api.url:http://localhost:8081}")
	private String urlApi;
	
	@GetMapping("/")
	public String getHome() {
		return "home";
	}
	
	@GetMapping("/tutorial")
	public String getTutorial() {
		return "tutorial";
	}
	
	@GetMapping("/manual")
	public String getManual() {
		return "manual";
	}
	
	@GetMapping("/login")
	public String getLogin() {
		return "login";
	}
	
	@GetMapping("/register")
	public String getRegister(Model modelo) {
		
		Usuario usuario = new Usuario();
		modelo.addAttribute("usuario", usuario);
		
		return "register";
	}
	
	@PostMapping("/saveUsuario")
	public ModelAndView setSaveUsuario(@ModelAttribute Usuario usuario) {

		if (esVacio(usuario.getNombre()) || esVacio(usuario.getCorreo_electronico()) || esVacio(usuario.getContrasena())) {
			return new ModelAndView("redirect:/register?error=campos");
		}

		if (esVacio(usuario.getRol()) || "0".equals(usuario.getRol())) {
			return new ModelAndView("redirect:/register?error=rol");
		}

		// El correo debe ser único: se consulta la lista de usuarios del backend
		RestTemplate consulta = new RestTemplate();
		Usuario[] existentes = consulta.postForObject(urlApi + "/buscarUsuarios", null, Usuario[].class);
		if (existentes != null) {
			for (Usuario u : existentes) {
				if (u.getCorreo_electronico() != null && u.getCorreo_electronico().equalsIgnoreCase(usuario.getCorreo_electronico().trim())) {
					return new ModelAndView("redirect:/register?error=correo");
				}
			}
		}
                   
    	RestTemplate template = new RestTemplate();
    	String urlservicebd = urlApi + "/addUsuario";
    	template.postForEntity(urlservicebd,usuario,Integer.class);

    	return new ModelAndView("redirect:/login");

	}
	
	@GetMapping("/inicio")
	public String getInicio(Model modelo) {
		
		Proyectos proyectos = new Proyectos();
		modelo.addAttribute("proyectos", proyectos);
		
		return "inicio";
	}
	
	@PostMapping("/saveProyecto")
	public ModelAndView setSaveProyecto(@ModelAttribute Proyectos proyecto) {

		if (!esVacio(proyecto.getFecha_inicio()) && !esVacio(proyecto.getFecha_finalizacion())
				&& proyecto.getFecha_finalizacion().compareTo(proyecto.getFecha_inicio()) < 0) {
			return new ModelAndView("redirect:/inicio?error=fechas");
		}

    	RestTemplate template = new RestTemplate();
    	String urlservicebd = urlApi + "/addProyecto";
    	template.postForEntity(urlservicebd,proyecto,Integer.class);

    	return new ModelAndView("redirect:/inicio");

	}
	
	@PostMapping("/consultarProyectos")
	public ResponseEntity<Object> consultarProyectos(Model modelo) {
		
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/buscarProyectos";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Proyectos[]  response = template.postForObject(urlservicebd, headers, Proyectos[].class);
		List<Proyectos> proyectos = Arrays.asList(response);
		modelo.addAttribute("listaProyectos", proyectos);
		
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));
	
		return objeto;
	}
	
	@PostMapping("/eliminarProyecto")
	public ModelAndView eliminarProyecto(@RequestBody Proyectos proyecto) {
		log.info("Eliminando proyecto con id {}", proyecto.getId_proyecto());
		RestTemplate  template = new RestTemplate();
		String urlservicebd = urlApi + "/deleteProyecto";
		HttpEntity<Proyectos> request = new HttpEntity<Proyectos>(proyecto); 
		template.exchange(urlservicebd, HttpMethod.DELETE,request,Integer.class);
	
		return new ModelAndView("redirect:/inicio");
	}
	
	@PostMapping ("/actualizarProyecto")
	public ResponseEntity<Object> updateProyecto(@RequestBody Proyectos proyecto) {
		RestTemplate  template = new RestTemplate();
		Map<String,Integer> params = new HashMap<String,Integer>();
		params.put("id_proyecto", proyecto.getId_proyecto());
		String urlservicebd = urlApi + "/buscarProyecto?id_proyecto={id_proyecto}";
		Proyectos proyectos = template.getForObject(urlservicebd, Proyectos.class, params);

		if (proyectos == null || proyectos.getId_proyecto() == null) {
			return noEncontrado("El proyecto solicitado no existe");
		}
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(proyectos, HttpStatusCode.valueOf(200));
		
		return objeto;
	}
	
	
	@PostMapping("/verificarLogin")
	public ResponseEntity<Usuario> verificarUsuario(@RequestBody Usuario paramUser) {
		String urlservicebd = urlApi + "/getUsuarioLogin";
		RestTemplate  template = new RestTemplate();
		ResponseEntity<Usuario> response = template.postForEntity(urlservicebd,paramUser,Usuario.class);
		
		return response;

	}
	
	/*@PostMapping("/obtenerTareas")
	public ResponseEntity<Object> obtenerTareas(@RequestBody Tareas idProyecto, Model modelo) {
		String urlservicebd = urlApi + "/getTarea";
		RestTemplate  template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas[] response = template.postForObject(urlservicebd, idProyecto, Tareas[].class);
		List<Tareas> listaTareas = Arrays.asList(response);
		modelo.addAttribute("tareas", listaTareas);
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(listaTareas, HttpStatusCode.valueOf(200));
	
		return objeto;

	}*/
	
	@PostMapping("/consultarUsuarios")
	public ResponseEntity<Object> consultarUsuarios(Model modelo) {
		
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/buscarUsuarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Usuario[]  response = template.postForObject(urlservicebd, headers, Usuario[].class);
		List<Usuario> usuarios = Arrays.asList(response);
		modelo.addAttribute("listaUsuarios", usuarios);
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(usuarios, HttpStatusCode.valueOf(200));
	
		return objeto;
	}
	
	@PostMapping("/obtenerTareas")
		public ResponseEntity<List<Tareas>> obtenerTareas(@RequestBody Tareas idProyecto, Model modelo) {
	    String urlservicebd = urlApi + "/getTarea";
	    RestTemplate template = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    Tareas[] response = template.postForObject(urlservicebd, idProyecto, Tareas[].class);
	    List<Tareas> listaTareas = Arrays.asList(response);
	    modelo.addAttribute("tareas", listaTareas);
	    return ResponseEntity.ok(listaTareas);
	}
	
	@PostMapping("/obtenerComentarios")
		public ResponseEntity<List<Comentarios>> obtenerComentarios(@RequestBody Comentarios idComentario, Model modelo) {
	    String urlservicebd = urlApi + "/getComentario";
	    RestTemplate template = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    Comentarios[] response = template.postForObject(urlservicebd, idComentario, Comentarios[].class);
	    List<Comentarios> listaComentarios = Arrays.asList(response);
	    modelo.addAttribute("comentarios", listaComentarios);
	    return ResponseEntity.ok(listaComentarios);
	}
	
	//Liliana
	@GetMapping("/proyecto")
	public String getProyecto(Model modelo) {
		Tareas tarea = new Tareas();
		modelo.addAttribute("tarea", tarea);
		
		Comentarios comentario = new Comentarios();
		modelo.addAttribute("comentarios", comentario);
		
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		String urlservicebdUsuario = urlApi + "/buscarUsuarios";
		Usuario[] responseUsuario = template.postForObject(urlservicebdUsuario, headers, Usuario[].class);  
		List<Usuario> usuarios = Arrays.asList(responseUsuario);
		modelo.addAttribute("listaUsuarios", usuarios);
		
		return "proyecto";
	}
	
	@GetMapping("/tarea")
	public String getTarea(Model modelo) {

		// La vista "proyecto" enlaza su formulario con th:object="${tarea}"
		Tareas tarea = new Tareas();
		modelo.addAttribute("tarea", tarea);
		
		return "proyecto";
	}
	
	@PostMapping("/saveTarea")
	public ModelAndView setSaveTareas(@ModelAttribute Tareas tareas) {
		
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/addTareas";
		template.postForEntity(urlservicebd, tareas, Integer.class);
		ModelAndView mav = new ModelAndView("redirect:/proyecto");
		mav.addObject("id_proyecto", tareas.getId_proyecto());
		
		return mav;
	}
	@PostMapping("/saveComentario")
	public ResponseEntity<Integer> setSaveComentario(@RequestBody Comentarios comentarios) {
		
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/addComentario";
		ResponseEntity<Integer> response = template.postForEntity(urlservicebd, comentarios, Integer.class); 
		
		return response;
	}
	
	
	@PostMapping("/actualizarTarea")
	public ResponseEntity<Object> updateTarea(@RequestBody  Tareas tarea) {
		RestTemplate template = new RestTemplate();
		Map<String,Integer> params = new HashMap<String,Integer>();
		params.put("id_tarea", tarea.getId_tarea());
		String urlservicebd = urlApi + "/buscarTarea?id_tarea={id_tarea}";
		Tareas tareas = template.getForObject(urlservicebd, Tareas.class, params);

		if (tareas == null || tareas.getId_tarea() == null) {
			return noEncontrado("La tarea solicitada no existe");
		}
		
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));
		
		return objeto;
	}
	
	@PostMapping("/eliminarTarea")
	public ModelAndView eliminarTarea(@RequestBody Tareas tareas) {
		log.info("Eliminando tarea con id {}", tareas.getId_tarea());
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/deleteTareas";
		HttpEntity<Tareas> request = new HttpEntity<Tareas>(tareas);
		template.exchange(urlservicebd, HttpMethod.DELETE,request,Integer.class);
		
		/*Map<String,Prestamo> params = new HashMap<String,Prestamo>();
		params.put("prestamo", prestamo);
		template.delete(urlservicebd,params);*/
		
		return new ModelAndView("redirect:/proyecto");
	}
	
	@PostMapping("/verificarTarea")
	public ResponseEntity<Integer> verificarTarea(@RequestBody Tareas paramName){
		String urlservicebd = urlApi + "/addTareas";
		RestTemplate template = new RestTemplate();
		ResponseEntity<Integer> response = template.postForEntity(urlservicebd, paramName, Integer.class);
		return response;
	}
	
	@PostMapping("/cambiosTareas")
	public ResponseEntity<Integer> cambiosTareas(@RequestBody Tareas paramDatos){
		String urlservicebd = urlApi + "/addTareas";
		RestTemplate template = new RestTemplate();
		ResponseEntity<Integer> response = template.postForEntity(urlservicebd, paramDatos, Integer.class);
		return response;
	}
	
	@PostMapping("/consultarTareas")
	public ResponseEntity<Object> consultarTareas() {	
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/buscarTareas";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Tareas[]  response = template.postForObject(urlservicebd, headers, Tareas[].class);
		List<Tareas> tareas = Arrays.asList(response);
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(tareas, HttpStatusCode.valueOf(200));
		
		return objeto;

	}
	
	@PostMapping("/consultarComentarios")
	public ResponseEntity<Object> consultarComentarios() {	
		RestTemplate template = new RestTemplate();
		String urlservicebd = urlApi + "/buscarComentarios";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Comentarios[]  response = template.postForObject(urlservicebd, headers, Comentarios[].class);
		List<Comentarios> comentarios = Arrays.asList(response);
		//ResponseEntity<List<Proyectos>> respuesta = (ResponseEntity<List<Proyectos>>) proyectos;
		ResponseEntity<Object> objeto = new ResponseEntity<Object>(comentarios, HttpStatusCode.valueOf(200));
		
		return objeto;

	}

	private boolean esVacio(String valor) {
		return valor == null || valor.trim().isEmpty();
	}

	private ResponseEntity<Object> noEncontrado(String mensaje) {
		Map<String, String> cuerpo = new HashMap<String, String>();
		cuerpo.put("mensaje", mensaje);
		return new ResponseEntity<Object>(cuerpo, HttpStatus.NOT_FOUND);
	}

	/** Errores al comunicarse con el backend (caído, 4xx/5xx): respuesta controlada para la interfaz. */
	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<Map<String, String>> errorBackend(RestClientException e) {
		Map<String, String> cuerpo = new HashMap<String, String>();
		cuerpo.put("mensaje", "El servicio no está disponible, intente más tarde.");
		return new ResponseEntity<Map<String, String>>(cuerpo, HttpStatus.SERVICE_UNAVAILABLE);
	}
}
