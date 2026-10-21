package com.mx.cbtis.modelo;

public class Comentarios {
	
	private Integer id_comentario;
	private String id_usuario;
	private Integer id_tarea;
	private String texto_comentario;
	private String fecha_hora;
	
	public Comentarios() {
		super();
	}

	public Comentarios(Integer id_comentario, String id_usuario, Integer id_tarea, String texto_comentario,
			String fecha_hora) {
		super();
		this.id_comentario = id_comentario;
		this.id_usuario = id_usuario;
		this.id_tarea = id_tarea;
		this.texto_comentario = texto_comentario;
		this.fecha_hora = fecha_hora;
	}

	public Integer getId_comentario() {
		return id_comentario;
	}

	public void setId_comentario(Integer id_comentario) {
		this.id_comentario = id_comentario;
	}

	public String getId_usuario() {
		return id_usuario;
	}

	public void setId_usuario(String id_usuario) {
		this.id_usuario = id_usuario;
	}

	public Integer getId_tarea() {
		return id_tarea;
	}

	public void setId_tarea(Integer id_tarea) {
		this.id_tarea = id_tarea;
	}

	public String getTexto_comentario() {
		return texto_comentario;
	}

	public void setTexto_comentario(String texto_comentario) {
		this.texto_comentario = texto_comentario;
	}

	public String getFecha_hora() {
		return fecha_hora;
	}

	public void setFecha_hora(String fecha_hora) {
		this.fecha_hora = fecha_hora;
	}
}
