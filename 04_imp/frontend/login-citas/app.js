// Simulación de base de datos local en el navegador para guardar múltiples pacientes
let baseDeDatosPacientes = JSON.parse(localStorage.getItem('pacientesCNS')) || [];
let pacienteActual = null;

// Referencias a las vistas
const vistaInicio = document.getElementById('vistaInicio');
const vistaRegistro = document.getElementById('vistaRegistro');
const vistaLogin = document.getElementById('vistaLogin');
const vistaDatosPaciente = document.getElementById('vistaDatosPaciente');
const vistaPanel = document.getElementById('vistaPanel');

// Referencias a la campanita
const zonaNotificaciones = document.getElementById('zonaNotificaciones');
const btnCampanita = document.getElementById('btnCampanita');
const badgeNotificacion = document.getElementById('badgeNotificacion');
const dropdownNotificacion = document.getElementById('dropdownNotificacion');
const textoNotificacion = document.getElementById('textoNotificacion');

// Función para cambiar de pantalla
function mostrarVista(vistaAMostrar) {
    [vistaInicio, vistaRegistro, vistaLogin, vistaDatosPaciente, vistaPanel].forEach(vista => {
        vista.classList.add('oculto');
    });
    vistaAMostrar.classList.remove('oculto');
}

// --- NAVEGACIÓN INICIAL ---
document.getElementById('btnIrRegistro').addEventListener('click', () => mostrarVista(vistaRegistro));
document.getElementById('btnIrLoginDirecto').addEventListener('click', () => mostrarVista(vistaLogin));
document.getElementById('btnVolverInicio1').addEventListener('click', () => mostrarVista(vistaInicio));
document.getElementById('btnIrRegistroDesdeLogin').addEventListener('click', () => mostrarVista(vistaRegistro));

// --- PASO 1: REGISTRAR PACIENTE (Nro de Afiliado y Password únicos) ---
document.getElementById('formRegistro').addEventListener('submit', function(e) {
    e.preventDefault();

    const nroAfiliado = document.getElementById('regAfiliado').value.trim();
    const password = document.getElementById('regPassword').value.trim();

    // Verificar que el Nro de Afiliado no esté repetido
    const existe = baseDeDatosPacientes.some(p => p.nroAfiliado === nroAfiliado);
    if (existe) {
        alert('Error: Ese Nro. de Afiliado ya está registrado. Intente con otro o inicie sesión.');
        return;
    }

    // Guardar el nuevo paciente con sus credenciales únicas
    const nuevoPaciente = {
        nroAfiliado: nroAfiliado,
        password: password,
        datosCompletos: false,
        idAfi: null,
        eiPersona: null,
        nombre: null,
        fecha: null
    };

    baseDeDatosPacientes.push(nuevoPaciente);
    localStorage.setItem('pacientesCNS', JSON.stringify(baseDeDatosPacientes));

    alert('¡Paciente registrado! Ahora inicie sesión con su Nro. de Afiliado y contraseña.');
    this.reset();
    mostrarVista(vistaLogin);
});

// --- PASO 2: LOGIN (Nro de Afiliado y Password) ---
document.getElementById('formLogin').addEventListener('submit', function(e) {
    e.preventDefault();

    const nroAfiliado = document.getElementById('loginAfiliado').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    // Buscar paciente por su número único y contraseña
    const pacienteEncontrado = baseDeDatosPacientes.find(
        p => p.nroAfiliado === nroAfiliado && p.password === password
    );

    if (!pacienteEncontrado) {
        alert('Credenciales incorrectas. Verifique su Nro. de Afiliado y Password.');
        return;
    }

    pacienteActual = pacienteEncontrado;
    this.reset();

    // Si aún no llenó IDAFI, EI persona, nombre y fecha, lo llevamos a ese formulario
    if (!pacienteActual.datosCompletos) {
        mostrarVista(vistaDatosPaciente);
    } else {
        // Si ya los tiene, enviamos el JSON al controlador directamente
        procesarValidacionJSON(pacienteActual);
    }
});

// --- PASO 3: LLENAR DATOS (IDAFI, EI persona, nombre, fecha) ---
document.getElementById('formDatosPaciente').addEventListener('submit', function(e) {
    e.preventDefault();

    // Capturar con los tipos de datos exactos solicitados
    const idAfi = Number(document.getElementById('inputIdAfi').value);       // long
    const eiPersona = parseInt(document.getElementById('inputEiPersona').value, 10); // int
    const nombre = document.getElementById('inputNombre').value.trim();      // string
    const fecha = document.getElementById('inputFecha').value;               // date (YYYY-MM-DD)

    // Actualizar datos del paciente actual
    pacienteActual.idAfi = idAfi;
    pacienteActual.eiPersona = eiPersona;
    pacienteActual.nombre = nombre;
    pacienteActual.fecha = fecha;
    pacienteActual.datosCompletos = true;

    // Guardar cambios en localStorage
    const index = baseDeDatosPacientes.findIndex(p => p.nroAfiliado === pacienteActual.nroAfiliado);
    baseDeDatosPacientes[index] = pacienteActual;
    localStorage.setItem('pacientesCNS', JSON.stringify(baseDeDatosPacientes));

    this.reset();
    
    // Enviar los datos en formato JSON al controlador
    procesarValidacionJSON(pacienteActual);
});

// --- PASO 4: COMUNICACIÓN JSON CON EL CONTROLADOR Y CAMPANITA ---
async function procesarValidacionJSON(paciente) {
    // 1. Este es el JSON exacto que se envía al controlador de tu compañero
    const payloadJSON = {
        nroAfiliado: paciente.nroAfiliado,
        idAfi: paciente.idAfi,         // long
        eiPersona: paciente.eiPersona, // int
        nombre: paciente.nombre,       // string
        fecha: paciente.fecha          // date
    };

    console.log("JSON enviado al controlador:", JSON.stringify(payloadJSON, null, 2));

    /* -------------------------------------------------------------------------
       NOTA PARA CUANDO CONECTES CON TU COMPAÑERO:
       Cuando tu compañero te dé la ruta de su controlador (ej. http://ip-servidor/api/validar),
       descomenta este bloque fetch y borra la simulación de abajo:

       const respuesta = await fetch('URL_DEL_CONTROLADOR_DE_TU_COMPAÑERO', {
           method: 'POST',
           headers: { 'Content-Type': 'application/json' },
           body: JSON.stringify(payloadJSON)
       });
       const respuestaControlador = await respuesta.json();
    ------------------------------------------------------------------------- */

    // SIMULACIÓN DE RESPUESTA DEL CONTROLADOR (Lo que te devuelve el backend)
    const respuestaControlador = {
        status: "OK",
        habilitado: true,
        tieneFicha: true,
        mensajeNotificacion: `Estimado/a ${paciente.nombre}, usted tiene una cita médica habilitada para la fecha ${paciente.fecha} a las 08:30 AM.`,
        datosRecibidos: payloadJSON
    };

    // Mostrar el panel principal y el JSON en pantalla para que lo puedas demostrar
    mostrarVista(vistaPanel);
    document.getElementById('infoPacienteResumen').innerHTML = `
        <strong>Paciente:</strong> ${paciente.nombre}<br>
        <strong>Nro. Afiliado:</strong> ${paciente.nroAfiliado}<br>
        <strong>IDAFI:</strong> ${paciente.idAfi}<br>
        <strong>EI Persona:</strong> ${paciente.eiPersona}<br>
        <strong>Fecha Registrada:</strong> ${paciente.fecha}
    `;
    document.getElementById('visorJson').textContent = JSON.stringify(respuestaControlador, null, 2);

    // 2. ACTIVAR LA CAMPANITA SI EL CONTROLADOR DICE QUE TIENE FICHA
    if (respuestaControlador.habilitado && respuestaControlador.tieneFicha) {
        zonaNotificaciones.classList.remove('oculto');
        badgeNotificacion.classList.remove('oculto');
        textoNotificacion.textContent = respuestaControlador.mensajeNotificacion;
    }
}

// Abrir/cerrar el mensaje al hacer clic en la campanita
btnCampanita.addEventListener('click', () => {
    dropdownNotificacion.classList.toggle('oculto');
    // Quitar el punto rojo al leer la notificación
    badgeNotificacion.classList.add('oculto');
});

// Cerrar sesión
document.getElementById('btnCerrarSesion').addEventListener('click', () => {
    pacienteActual = null;
    zonaNotificaciones.classList.add('oculto');
    dropdownNotificacion.classList.add('oculto');
    mostrarVista(vistaInicio);
});