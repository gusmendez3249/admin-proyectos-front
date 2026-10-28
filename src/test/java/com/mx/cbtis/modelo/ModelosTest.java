package com.mx.cbtis.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Constructores y getters/setters de los modelos del frontend. */
class ModelosTest {

	@Test
	void usuarioConstructorYAccesores() {
		Usuario u = new Usuario(1, "Ana", "ana@mail.com", "123", "Admin", "a.jpg");
		assertEquals(1, u.getId_usuario());
		assertEquals("Ana", u.getNombre());
		assertEquals("ana@mail.com", u.getCorreo_electronico());
		assertEquals("123", u.getContrasena());
		assertEquals("Admin", u.getRol());
		assertEquals("a.jpg", u.getImagen_usuario());

		Usuario v = new Usuario();
		assertNull(v.getId_usuario());
		v.setId_usuario(2);
		v.setNombre("Luis");
		v.setCorreo_electronico("luis@mail.com");
		v.setContrasena("456");
		v.setRol("User");
		v.setImagen_usuario("#000000");
		assertEquals(2, v.getId_usuario());
		assertEquals("Luis", v.getNombre());
		assertEquals("luis@mail.com", v.getCorreo_electronico());
		assertEquals("456", v.getContrasena());
		assertEquals("User", v.getRol());
		assertEquals("#000000", v.getImagen_usuario());
	}

	@Test
	void proyectosConstructorYAccesores() {
		Proyectos p = new Proyectos(197, "API", "desc", "2024-05-31", "2024-06-29", "En proceso", "/Images/a.gif");
		assertEquals(197, p.getId_proyecto());
		assertEquals("API", p.getNombre());
		assertEquals("desc", p.getDescripcion());
		assertEquals("2024-05-31", p.getFecha_inicio());
		assertEquals("2024-06-29", p.getFecha_finalizacion());
		assertEquals("En proceso", p.getEstado_proyecto());
		assertEquals("/Images/a.gif", p.getFondo());

		Proyectos q = new Proyectos();
		assertNull(q.getId_proyecto());
		q.setId_proyecto(1);
		q.setNombre("N");
		q.setDescripcion("D");
		q.setFecha_inicio("2024-01-01");
		q.setFecha_finalizacion("2024-02-01");
		q.setEstado_proyecto("Terminado");
		q.setFondo("f");
		assertEquals(1, q.getId_proyecto());
		assertEquals("N", q.getNombre());
		assertEquals("D", q.getDescripcion());
		assertEquals("2024-01-01", q.getFecha_inicio());
		assertEquals("2024-02-01", q.getFecha_finalizacion());
		assertEquals("Terminado", q.getEstado_proyecto());
		assertEquals("f", q.getFondo());
	}

	@Test
	void tareasConstructorYAccesores() {
		Tareas t = new Tareas(199, "BD", "Tablas", "2024-06-13", "terminado", "danger", 197);
		assertEquals(199, t.getId_tarea());
		assertEquals("BD", t.getNombre());
		assertEquals("Tablas", t.getDescripcion());
		assertEquals("2024-06-13", t.getFecha_vencimiento());
		assertEquals("terminado", t.getLista());
		assertEquals("danger", t.getEstado());
		assertEquals(197, t.getId_proyecto());

		Tareas s = new Tareas();
		assertNull(s.getId_tarea());
		s.setId_tarea(1);
		s.setNombre("N");
		s.setDescripcion("D");
		s.setFecha_vencimiento("2024-01-01");
		s.setLista("pendiente");
		s.setEstado("success");
		s.setId_proyecto(2);
		assertEquals(1, s.getId_tarea());
		assertEquals("N", s.getNombre());
		assertEquals("D", s.getDescripcion());
		assertEquals("2024-01-01", s.getFecha_vencimiento());
		assertEquals("pendiente", s.getLista());
		assertEquals("success", s.getEstado());
		assertEquals(2, s.getId_proyecto());
	}

	@Test
	void comentariosConstructorYAccesores() {
		Comentarios c = new Comentarios(201, "Perla", 199, "Subirlo", "2024-06-26 13:48:00");
		assertEquals(201, c.getId_comentario());
		assertEquals("Perla", c.getId_usuario());
		assertEquals(199, c.getId_tarea());
		assertEquals("Subirlo", c.getTexto_comentario());
		assertEquals("2024-06-26 13:48:00", c.getFecha_hora());

		Comentarios d = new Comentarios();
		assertNull(d.getId_comentario());
		d.setId_comentario(1);
		d.setId_usuario("Ana");
		d.setId_tarea(2);
		d.setTexto_comentario("hola");
		d.setFecha_hora("2024-01-01 10:00:00");
		assertEquals(1, d.getId_comentario());
		assertEquals("Ana", d.getId_usuario());
		assertEquals(2, d.getId_tarea());
		assertEquals("hola", d.getTexto_comentario());
		assertEquals("2024-01-01 10:00:00", d.getFecha_hora());
	}

	@Test
	void asignacionesConstructorYAccesores() {
		Asignaciones a = new Asignaciones(20, 131, 199, "2024-06-01");
		assertEquals(20, a.getId_asignacion());
		assertEquals(131, a.getId_usuario());
		assertEquals(199, a.getId_tarea());
		assertEquals("2024-06-01", a.getFecha_asignacion());

		Asignaciones b = new Asignaciones();
		assertNull(b.getId_asignacion());
		b.setId_asignacion(1);
		b.setId_usuario(2);
		b.setId_tarea(3);
		b.setFecha_asignacion("2024-01-01");
		assertEquals(1, b.getId_asignacion());
		assertEquals(2, b.getId_usuario());
		assertEquals(3, b.getId_tarea());
		assertEquals("2024-01-01", b.getFecha_asignacion());
	}
}
