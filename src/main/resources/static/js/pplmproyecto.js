document.addEventListener("DOMContentLoaded", function() {
            var formRegistro = document.getElementById('form-registro');

            formRegistro.addEventListener('submit', function(event) {
                event.preventDefault(); // Prevenir el envío del formulario para validar primero

                var nombre = document.getElementById('nombre').value;
                var email = document.getElementById('email').value;
                var password = document.getElementById('password').value;
                var mensajeError = document.getElementById('mensaje-error');

                // Verificar si algún campo está vacío
                if (!nombre || !email || !password) {
                    mensajeError.innerText = "Por favor complete todos los campos.";
                    return;
                }

                // Si todos los campos están llenos, el formulario se envía
                // Aquí podrías agregar más validaciones si es necesario
                formRegistro.submit();
            });
            });