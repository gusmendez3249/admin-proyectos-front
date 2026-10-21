$(document).ready(function() {

	// Control de sesión: sin usuario autenticado se regresa al login
	if(!localStorage.getItem("usuario")){
		window.location.href = 'login';
		return;
	}
	
	//Seleccionar estado
	document.getElementById("etiqueta").selectedIndex = "0";

	
	var usuarioString = localStorage.getItem("usuario");
	var usuario = JSON.parse(usuarioString);
	var imagen = document.getElementById("imagenUsuario");
	imagen.style.backgroundColor = usuario.imagen_usuario;
	var inicial = usuario.nombre[0];
	document.getElementById("Nombre").innerHTML = usuario.nombre;
	document.getElementById("Email").innerHTML = usuario.correo_electronico;
	document.getElementById("inicial").innerHTML = inicial;
	
	var urlParams = new URLSearchParams(window.location.search);
	var id = urlParams.get('id_proyecto');
	var id_proyecto = Number(id);
	console.log(id_proyecto);
	document.getElementById("IdProyectoURL").value = id_proyecto;
	
	llenarBarraLateral(id_proyecto);
	
		$.ajax({
	        url: '/consultarUsuarios',
	        type: 'POST',
	        dataType: 'json',
	    	contentType: 'application/json',
	        success: function(data){
				var contenedor = $('#miembrosLateral');
	            contenedor.empty();
	            
	             data.forEach(function(usuario) {
					var miembro = `
		            <p>${usuario.nombre}</p>
	            	`;
	             contenedor.append(miembro);})
	       }

         });


	
	$.ajax({
        url: '/obtenerTareas',
        data : JSON.stringify({id_proyecto: id_proyecto}),
        type: 'POST',
        dataType: 'json',
    	contentType: 'application/json',
        success: function(data){
			var contenedor = $('#tareasPendientes');
            contenedor.empty();
            var contenedor2 = $('#enCurso');
            contenedor2.empty();
            var contenedor3 = $('#trabajoTerminado');
            contenedor3.empty();
            
            var contenedorLateral1 = $('#dropdownPendientes');
            contenedorLateral1.empty();
            var contenedorLateral2 = $('#dropdownEnCurso');
            contenedorLateral2.empty();
            var contenedorLateral3 = $('#dropdownTerminado');
            contenedorLateral3.empty();
            
            // Crear tarjetas para cada proyecto

          data.forEach(function(tarea) {
			if(tarea.lista == "pendientes"){
				var tarjeta = `
	            <ul class="my-2">
	              <li>
					<div class="my-2 badge text-bg-light-subtle mx-auto d-flex">
						<button type="button" class="btn btn-light text-start d-grid gap-2 border border-${tarea.estado}" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
						<button data-bs-toggle="modal" data-bs-target="#modalAlerta" onclick="obtenerInfo(${tarea.id_tarea}, 'hiddenId', '${tarea.nombre}')" class="bg-light-subtle m-1 p-2 border border-0"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash3" viewBox="0 0 16 16">
						  <path d="M6.5 1h3a.5.5 0 0 1 .5.5v1H6v-1a.5.5 0 0 1 .5-.5M11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3A1.5 1.5 0 0 0 5 1.5v1H1.5a.5.5 0 0 0 0 1h.538l.853 10.66A2 2 0 0 0 4.885 16h6.23a2 2 0 0 0 1.994-1.84l.853-10.66h.538a.5.5 0 0 0 0-1zm1.958 1-.846 10.58a1 1 0 0 1-.997.92h-6.23a1 1 0 0 1-.997-.92L3.042 3.5zm-7.487 1a.5.5 0 0 1 .528.47l.5 8.5a.5.5 0 0 1-.998.06L5 5.03a.5.5 0 0 1 .47-.53Zm5.058 0a.5.5 0 0 1 .47.53l-.5 8.5a.5.5 0 1 1-.998-.06l.5-8.5a.5.5 0 0 1 .528-.47M8 4.5a.5.5 0 0 1 .5.5v8.5a.5.5 0 0 1-1 0V5a.5.5 0 0 1 .5-.5"/>
						  </svg>
						</button>
					</div>
				  </li>
				</ul>
            	`;
            	
            	var lateral1 = `
	            	<button type="button" class="btn btn-light text-start d-grid gap-2 dropdown-item" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
            	`;
            	
            	
            	
	              // Crea comentarios
			      /*$.ajax({
			        url: '/obtenerComentarios',
			        data : JSON.stringify({id_tarea: tarea.id_tarea}),
	        		type: 'POST',
	        		dataType: 'json',
	    			contentType: 'application/json',
			        success: function(data){
						var contenedorComentario = $('#contenedorComentario');
			            contenedorComentario.empty();
			            
			            data.forEach(function(comentarios) {
								var comentario = `
								<div class="text-start bg-secondary p-2 bg-opacity-50 mb-3 rounded">
								    <strong class="me-auto">Liliana Gómez Martínez</strong>
								    <small class="text-body-secondary">${comentarios.fecha_hora}</small>
								  <div>
								    ${comentarios.texto_comentario}
								  </div>
							    </div>
								`
								contenedorComentario.append(comentario);
						});
			        }
			       });*/
			}
			
			if(tarea.lista == "encurso"){
				var tarjeta2 = `
	            <ul class="my-2">
	              <li>
					<div class="my-2 badge text-bg-light-subtle mx-auto d-flex">
						<button type="button" class="btn btn-light text-start d-grid gap-2 border border-${tarea.estado}" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
						<button onclick="eliminarTarea(${tarea.id_tarea});" class="bg-light-subtle m-1 p-2 border border-0"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash3" viewBox="0 0 16 16">
						  <path d="M6.5 1h3a.5.5 0 0 1 .5.5v1H6v-1a.5.5 0 0 1 .5-.5M11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3A1.5 1.5 0 0 0 5 1.5v1H1.5a.5.5 0 0 0 0 1h.538l.853 10.66A2 2 0 0 0 4.885 16h6.23a2 2 0 0 0 1.994-1.84l.853-10.66h.538a.5.5 0 0 0 0-1zm1.958 1-.846 10.58a1 1 0 0 1-.997.92h-6.23a1 1 0 0 1-.997-.92L3.042 3.5zm-7.487 1a.5.5 0 0 1 .528.47l.5 8.5a.5.5 0 0 1-.998.06L5 5.03a.5.5 0 0 1 .47-.53Zm5.058 0a.5.5 0 0 1 .47.53l-.5 8.5a.5.5 0 1 1-.998-.06l.5-8.5a.5.5 0 0 1 .528-.47M8 4.5a.5.5 0 0 1 .5.5v8.5a.5.5 0 0 1-1 0V5a.5.5 0 0 1 .5-.5"/>
						  </svg>
						</button>
					</div>
				  </li>
				</ul>
            	`;
            	
            	var lateral2 = `
	            	<button type="button" class="btn btn-light text-start d-grid gap-2 dropdown-item" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
            	`;
            	
            	// Crea comentarios
		     /* $.ajax({
			        url: '/obtenerComentarios',
			        data : JSON.stringify({id_tarea: tarea.id_tarea}),
	        		type: 'POST',
	        		dataType: 'json',
	    			contentType: 'application/json',
			        success: function(data){
						var contenedorComentario = $('#contenedorComentario');
			            contenedorComentario.empty();
			            
			            data.forEach(function(comentarios) {
								var comentario = `
								<div class="text-start bg-secondary p-2 bg-opacity-50 mb-3 rounded">
								    <strong class="me-auto">Liliana Gómez Martínez</strong>
								    <small class="text-body-secondary">${comentarios.fecha_hora}</small>
								  <div>
								    ${comentarios.texto_comentario}
								  </div>
							    </div>
								`
								contenedorComentario.append(comentario);
						});
			        }
			       });*/
			}
           if(tarea.lista == "terminado"){
			   var tarjeta3 = `
	            <ul class="my-2">
	              <li>
					<div class="my-2 badge text-bg-light-subtle mx-auto d-flex">
						<button type="button" class="btn btn-light text-start d-grid gap-2 border border-${tarea.estado}" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
						<button onclick="eliminarTarea(${tarea.id_tarea});" class="bg-light-subtle m-1 p-2 border border-0"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash3" viewBox="0 0 16 16">
						  <path d="M6.5 1h3a.5.5 0 0 1 .5.5v1H6v-1a.5.5 0 0 1 .5-.5M11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3A1.5 1.5 0 0 0 5 1.5v1H1.5a.5.5 0 0 0 0 1h.538l.853 10.66A2 2 0 0 0 4.885 16h6.23a2 2 0 0 0 1.994-1.84l.853-10.66h.538a.5.5 0 0 0 0-1zm1.958 1-.846 10.58a1 1 0 0 1-.997.92h-6.23a1 1 0 0 1-.997-.92L3.042 3.5zm-7.487 1a.5.5 0 0 1 .528.47l.5 8.5a.5.5 0 0 1-.998.06L5 5.03a.5.5 0 0 1 .47-.53Zm5.058 0a.5.5 0 0 1 .47.53l-.5 8.5a.5.5 0 1 1-.998-.06l.5-8.5a.5.5 0 0 1 .528-.47M8 4.5a.5.5 0 0 1 .5.5v8.5a.5.5 0 0 1-1 0V5a.5.5 0 0 1 .5-.5"/>
						  </svg>
						</button>
					</div>
				  </li>
				</ul>
	            `;
	            
	            var lateral3 = `
	            	<button type="button" class="btn btn-light text-start d-grid gap-2 dropdown-item" data-bs-toggle="modal" data-bs-target="#editarTarea" onclick="actualizarTarea(${tarea.id_tarea});comentarios(${tarea.id_tarea})">${tarea.nombre}</button>
            	`;
	            
	            // Crea comentarios
		       /*$.ajax({
			        url: '/obtenerComentarios',
			        data : JSON.stringify({id_tarea: tarea.id_tarea}),
	        		type: 'POST',
	        		dataType: 'json',
	    			contentType: 'application/json',
			        success: function(data){
						var contenedorComentario = $('#contenedorComentario');
			            contenedorComentario.empty();
			            
			            data.forEach(function(comentarios) {
								var comentario = `
								<div class="text-start bg-secondary p-2 bg-opacity-50 mb-3 rounded">
								    <strong class="me-auto">Liliana Gómez Martínez</strong>
								    <small class="text-body-secondary">${comentarios.fecha_hora}</small>
								  <div>
								    ${comentarios.texto_comentario}
								  </div>
							    </div>
								`
								contenedorComentario.append(comentario);
						});
			        }
			       });*/
		   }
            
            
            contenedor.append(tarjeta);
            contenedor2.append(tarjeta2);
            contenedor3.append(tarjeta3);
            
            contenedorLateral1.append(lateral1);
            contenedorLateral2.append(lateral2);
            contenedorLateral3.append(lateral3);

          });
		},
		  error: function(error) {
          console.error('Error al cargar tareas:', error);}
      });
       
});

function newTarea(){
	var inputTarea = document.getElementById("inputTarea");
	var displayStyle = window.getComputedStyle(inputTarea).display;
	var btnTarea = document.getElementById("btnTarea");
	var displayStyle2 = window.getComputedStyle(btnTarea).display;
	
	if(displayStyle === "none" & displayStyle2 === "none"){
		inputTarea.style.display = "block";
		btnTarea.style.display = "block";
		inputTarea.focus();
	}
	else{
		inputTarea.style.display = "none";
		btnTarea.style.display = "none";
	}
}
function newTarea2(){
	var inputTarea2 = document.getElementById("inputTarea2");
	var displayStyle = window.getComputedStyle(inputTarea2).display;
	var btnTarea2 = document.getElementById("btnTarea2");
	var displayStyle2 = window.getComputedStyle(btnTarea2).display;
	
	if(displayStyle === "none" & displayStyle2 === "none"){
		inputTarea2.style.display = "block";
		btnTarea2.style.display = "block";
		inputTarea2.focus();
	}
	else{
		inputTarea2.style.display = "none";
		btnTarea2.style.display = "none";
	}
}
function newTarea3(){
	var inputTarea3 = document.getElementById("inputTarea3");
	var displayStyle = window.getComputedStyle(inputTarea3).display;
	var btnTarea3 = document.getElementById("btnTarea3");
	var displayStyle2 = window.getComputedStyle(btnTarea3).display;
	
	if(displayStyle === "none" & displayStyle2 === "none"){
		inputTarea3.style.display = "block";
		btnTarea3.style.display = "block";
		inputTarea3.focus();
	}
	else{
		inputTarea3.style.display = "none";
		btnTarea3.style.display = "none";
	}
}

function Fechas(){
	var inputFechas = document.getElementById("inputFechas");
	var displayStyle = window.getComputedStyle(inputFechas).display;
	
	if(displayStyle === "none"){
		inputFechas.style.display = "block";
		inputFechas.focus();
	}
	else{
		inputFechas.style.display = "none";
	}
}

function validarTarea(){
	if($.trim($("#inputTarea").val()) == ""){
		alert("Escriba el nombre de la tarea");
		return;
	}
	
	var urlParams = new URLSearchParams(window.location.search);
	var id = urlParams.get('id_proyecto');
	var id_proyecto = Number(id);
	console.log(id_proyecto);

	$.ajax({
		url : "/verificarTarea",
		contentType:"application/json",
		data : JSON.stringify({nombre:$("#inputTarea").val(), lista:$("#estado").val(), id_proyecto: id_proyecto}),
		type : "POST",
		success: function(data){
			console.log(data);
            var id = document.getElementById("IdProyectoURL").value;
			var url = `proyecto?id_proyecto=${id}`;
            window.location.href= url;}
	})
}

function validarTarea2(){
	if($.trim($("#inputTarea2").val()) == ""){
		alert("Escriba el nombre de la tarea");
		return;
	}
	
	var urlParams = new URLSearchParams(window.location.search);
	var id = urlParams.get('id_proyecto');
	var id_proyecto = Number(id);
	console.log(id_proyecto);
	
	$.ajax({
		url : "/verificarTarea",
		contentType:"application/json",
		data : JSON.stringify({nombre:$("#inputTarea2").val(), lista:$("#estado2").val(), id_proyecto: id_proyecto}),
		type : "POST",
		success: function(data){
			console.log(data);
            var id = document.getElementById("IdProyectoURL").value;
			var url = `proyecto?id_proyecto=${id}`;
            window.location.href= url;}
	})
}
function validarTarea3(){
	if($.trim($("#inputTarea3").val()) == ""){
		alert("Escriba el nombre de la tarea");
		return;
	}
	
	var urlParams = new URLSearchParams(window.location.search);
	var id = urlParams.get('id_proyecto');
	var id_proyecto = Number(id);
	console.log(id_proyecto);
	
	console.log(id);
	
	$.ajax({
		url : "/verificarTarea",
		contentType:"application/json",
		data : JSON.stringify({nombre:$("#inputTarea3").val(), lista:$("#estado3").val(), id_proyecto: id_proyecto}),
		type : "POST",
		success: function(data){
			console.log(data);
            var id = document.getElementById("IdProyectoURL").value;
			var url = `proyecto?id_proyecto=${id}`;
            window.location.href= url;}
	})
}

function eliminarTarea(id_tarea){
	
	$.ajax({
		url : "/eliminarTarea",
		contentType:"application/json",
		data : JSON.stringify({id_tarea:id_tarea}),
		type : "POST",
		success: function(){
			var id = document.getElementById("IdProyectoURL").value;
			var url = `proyecto?id_proyecto=${id}`;
            window.location.href= url;}
	});
}

function actualizarTarea(id_tarea){
	
	/*let now = new Date();
	let day = ('0' + now.getDate()).slice(-2);        // Día del mes (dos dígitos)
	let month = ('0' + (now.getMonth() + 1)).slice(-2); // Mes (dos dígitos)
	let year = now.getFullYear().toString(); // Año (dos últimos dígitos)
	let fecha = day + '/' + month + '/' + year;
	console.log(fecha);
	
	document.getElementById("descripcionT").value = "_";
	var fechas = document.getElementById('inputFechas');
	fechas.value = fecha;
	
	document.getElementById("etiqueta").value = "primary";
	console.log(document.getElementById("etiqueta").value);
	console.log(document.getElementById("inputFechas").value);*/
			
	
	$.ajax({
		url : "/actualizarTarea",
		dataType : "json",
		contentType:"application/json",
		data : JSON.stringify({id_tarea:id_tarea}),
		type : "POST",
		success: function(data){
			$('#editarTarea').modal('show');
			
			var modal = $('#editarTarea');
			modal.find('.modal-title').text(data.nombre);
			
			document.getElementById("id_tarea").value = data.id_tarea;
			document.getElementById("id_proyecto").value = data.id_proyecto;
			document.getElementById("descripcionT").value = data.descripcion;
			document.getElementById("inputFechas").value = data.fecha_vencimiento;
			document.getElementById("etiqueta").value = data.estado;
			document.getElementById("cambiarLista").value = data.lista;
			document.getElementById("nombre").value = data.nombre;
		},
		error:function(){
			console.log("Error");
			alert("No fue posible obtener la tarea solicitada");
			setTimeout(function(){ bootstrap.Modal.getOrCreateInstance(document.getElementById('editarTarea')).hide(); }, 500);
		}
	});
}/*
function verificarComentario(){
	$.ajax({
		url : "/verificarTarea",
		contentType:"application/json",
		data : JSON.stringify({texto_comentario:$("#comentarioT").val()}),
		type : "POST",
		success: function(data){
			console.log(data);
			var id = document.getElementById("IdProyectoURL").value;
			var url = `proyecto?id_proyecto=${id}`;
            window.location.href= url;}
	})
}*/

function guardarComentario(){
	var comentario = $('#comentarioT').val();
	if($.trim(comentario) == ""){
		alert("Escriba un comentario antes de enviarlo");
		return;
	}
	let now = new Date();
	let day = ('0' + now.getDate()).slice(-2);        // Día del mes (dos dígitos)
	let month = ('0' + (now.getMonth() + 1)).slice(-2); // Mes (dos dígitos)
	let year = now.getFullYear().toString().slice(-2); // Año (dos últimos dígitos)
	let hour = ('0' + now.getHours()).slice(-2);       // Hora (dos dígitos)
	let minute = ('0' + now.getMinutes()).slice(-2);   // Minutos (dos dígitos)
	let fecha = `${year}/${month}/${day} ${hour}:${minute}`;
	
	var usuarioString = localStorage.getItem("usuario");
	var usuario = JSON.parse(usuarioString);
	
	$.ajax({
            url: '/saveComentario',
            contentType: 'application/json',
            data: JSON.stringify({
                id_tarea: $('#id_tarea').val(), // Obtener el ID de la tarea
                texto_comentario: comentario,
                fecha_hora: fecha,
                id_usuario: usuario.nombre
            }),
            type: 'POST',
            success: function(response) {
				
                // Manejar la respuesta del servidor según sea necesario
                console.log('Comentario guardado exitosamente');
                // Puedes actualizar la UI si es necesario
                var id = document.getElementById("IdProyectoURL").value;
				var url = `proyecto?id_proyecto=${id}`;
	            window.location.href= url;
            },
            error: function(error) {
                console.error('Error al guardar el comentario:', error);
                // Manejar errores según sea necesario
            }
        }); 
	
}


function llenarBarraLateral(idProyecto){
	$.ajax({
		url : '/actualizarProyecto',
		dataType: "json",
		contentType:'application/json',
		data : JSON.stringify({id_proyecto: idProyecto}),
		type : "POST",
		success: function(data){
			console.log(data);
			
			var nombre = document.getElementById("offcanvasScrollingLabel");
			nombre.innerHTML = data.nombre;
			
			var descripcion = document.getElementById("despliegueDescripcion");
			descripcion.innerHTML = data.descripcion;
		
		},
		
		error: function() {
            console.log("Error");
			alert("El proyecto solicitado no existe");
			window.location.href = 'inicio';
        }
	});
}

function comentarios(idTarea){
	
	$.ajax({
	    url: '/obtenerComentarios',
	    data : JSON.stringify({id_tarea: idTarea}),
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json',
	    success: function(data){
			var contenedorComentario = $('#contenedorComentario');
	        contenedorComentario.empty();
	        
	        data.forEach(function(comentarios) {
					var comentario = `
					<div class="text-start bg-secondary p-2 bg-opacity-50 mb-3 rounded">
					    <strong class="me-auto">${comentarios.id_usuario}</strong>
					    <small class="text-body-secondary">${comentarios.fecha_hora}</small>
					  <div>
					    ${comentarios.texto_comentario}
					  </div>
				    </div>
					`
					contenedorComentario.append(comentario);
			});
	    }
   });
}

function IrInicio(){
	window.location.href = 'inicio';
}

function obtenerInfo(datos, objetivo, nombre) {
	var nuevoTexto = "¿Esta seguro que sea eliminar la tarea " + nombre + "?";
	document.getElementById("textAlerta").innerText = nuevoTexto;
    $(`#${objetivo}`).val(datos);
}

function botonEliminar(){
	
	var id = document.getElementById("hiddenId").value;
	eliminarTarea(id);
}