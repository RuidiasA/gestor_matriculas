const state = {
    perfil: null,
    resumen: null,
    cursos: [],
    horario: [],
    pagos: [],
    catalogo: [],
    detalleActual: null
};

function mostrarVista(vista, event) {
    if (event) event.preventDefault();

    const body = document.body;
    const asideDerecho = document.getElementById('aside-derecho');

    const vistas = {
        matricula: document.getElementById('vista-matricula'),
        horario: document.getElementById('vista-horario'),
        pensiones: document.getElementById('vista-pensiones'),
        solicitud: document.getElementById('vista-solicitud')
    };

    Object.values(vistas).forEach(div => {
        if (div) div.classList.add('hidden');
    });

    if (vista === 'matricula') {
        vistas.matricula.classList.remove('hidden');
        asideDerecho.classList.remove('hidden');
        body.classList.remove('layout-2-cols');
        inicializarHorario();

    } else if (vista === 'solicitud') {
        vistas.solicitud.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');

    } else if (vista === 'pensiones') {
        vistas.pensiones.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');

    } else if (vista === 'horario') {
        vistas.horario.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');
        inicializarHorario();
    }

    const linksMenu = document.querySelectorAll('.aside-left__container a');
    linksMenu.forEach(link => link.classList.remove('active'));

    if (event && event.currentTarget) {
        event.currentTarget.classList.add('active');
    }
}

/* ============================================================
   UTILIDADES
============================================================ */
async function fetchJson(url, errorMessage, options = {}) {
    try {
        const resp = await fetch(url, options);
        if (!resp.ok) throw new Error(errorMessage || 'Error de servidor');
        return await resp.json();
    } catch (err) {
        console.error(err);
        mostrarMensajeError(err.message || errorMessage);
        throw err;
    }
}

function mostrarMensajeError(msg) {
    const banner = document.querySelector('.toast-error') || document.createElement('div');
    banner.className = 'toast-error';
    banner.textContent = msg || 'Ocurrió un error inesperado';
    document.body.appendChild(banner);
    setTimeout(() => banner.remove(), 4000);
}

async function cargarCatalogo() {
    const ciclo = document.getElementById('filtro-ciclo')?.value || '';
    const modalidad = document.getElementById('filtro-modalidad')?.value || '';
    const texto = document.getElementById('filtro-texto')?.value || '';
    const params = new URLSearchParams();
    if (ciclo) params.append('ciclo', ciclo);
    if (modalidad) params.append('modalidad', modalidad);
    if (texto) params.append('q', texto);

    const url = `/alumno/cursos/disponibles${params.toString() ? '?' + params.toString() : ''}`;
    state.catalogo = await fetchJson(url, 'No se pudo cargar el catálogo');
    renderCatalogo();
}

function renderCatalogo() {
    const contenedor = document.getElementById('catalogo-cursos');
    if (!contenedor) return;
    contenedor.innerHTML = '';

    if (!state.catalogo.length) {
        contenedor.innerHTML = '<div class="catalogo-empty">No se encontraron cursos disponibles.</div>';
        return;
    }

    state.catalogo.forEach(item => {
        const card = document.createElement('div');
        card.classList.add('catalogo-card');
        card.innerHTML = `
            <h4>${item.codigoCurso || ''} — ${item.nombreCurso || ''}</h4>
            <div class="meta">
                <span class="badge-soft">${item.modalidad || 'Sin modalidad'}</span>
                <span>Cupos: ${item.cuposDisponibles ?? 0}</span>
                <span>Créditos: ${item.creditos ?? '-'}</span>
            </div>
            <small>${item.docente || 'Docente por confirmar'}</small>
        `;
        card.addEventListener('click', () => cargarDetalleCurso(item.seccionId));
        contenedor.appendChild(card);
    });
}

async function cargarDetalleCurso(seccionId) {
    const detalleContainer = document.querySelector('#detalle-curso');
    const body = detalleContainer?.querySelector('.detalle-body');
    if (!detalleContainer || !body) return;
    const detalle = await fetchJson(`/alumno/cursos/${seccionId}/detalle`, 'No se pudo cargar el detalle');
    state.detalleActual = detalle;
    detalleContainer.classList.remove('hidden');
    body.innerHTML = `
        <p><strong>Curso:</strong> ${detalle.nombreCurso || ''} (${detalle.codigoCurso || ''})</p>
        <p><strong>Docente:</strong> ${detalle.docente || 'Por asignar'}</p>
        <p><strong>Modalidad:</strong> ${detalle.modalidad || '-'}</p>
        <p><strong>Cupos:</strong> ${detalle.cuposDisponibles ?? 0}</p>
        <p><strong>Prerrequisitos:</strong> ${(detalle.prerrequisitos || []).join(', ') || 'Ninguno'}</p>
    `;
}

async function intentarMatricular() {
    if (!state.detalleActual?.seccionId) return;
    try {
        await fetchJson(`/alumno/matricula/${state.detalleActual.seccionId}`, 'No se pudo completar la matrícula', { method: 'POST' });
        await cargarDatosIniciales();
    } catch (err) {
        // manejo centralizado
    }
}

async function intentarRetirar() {
    if (!state.detalleActual?.seccionId) return;
    try {
        await fetch(`/alumno/matricula/${state.detalleActual.seccionId}`, { method: 'DELETE' });
        await cargarDatosIniciales();
    } catch (err) {
        mostrarMensajeError('No se pudo retirar el curso');
    }
}

/* ============================================================
   DATA DE CURSOS MATRICULADOS
============================================================ */
function generarTarjetasCursos() {
    const contenedor = document.querySelector('.cursos-inscritos');
    if (!contenedor) return;

    contenedor.innerHTML = '';

    state.cursos.forEach(curso => {
        const card = document.createElement('div');
        card.classList.add('curso-card');

        card.innerHTML = `
            <h3>${curso.codigoSeccion || ''} — ${curso.nombreCurso || ''}</h3>
            <p class="descripcion">Docente: ${curso.docente || 'Por asignar'}</p>

            <div class="curso-grid">
                <span><strong>Sección:</strong> ${curso.codigoSeccion || '-'}</span>
                <span><strong>Aula:</strong> ${curso.aula || '-'}</span>
                <span><strong>Horas semanales:</strong> ${curso.horasSemanales ?? '-'}</span>
                <span><strong>Créditos:</strong> ${curso.creditos ?? '-'}</span>
                <span><strong>Modalidad:</strong> ${curso.modalidad || '-'}</span>
            </div>
        `;

        contenedor.appendChild(card);
    });
}

function renderTablaCursos() {
    const tbody = document.querySelector('#vista-matricula table tbody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!state.cursos.length) {
        const tr = document.createElement('tr');
        tr.innerHTML = '<td colspan="10" class="muted">No tienes cursos inscritos en este ciclo</td>';
        tbody.appendChild(tr);
        return;
    }

    state.cursos.forEach(curso => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="checkbox__container"><input type="checkbox" disabled></td>
            <td>${curso.codigoSeccion || '-'}</td>
            <td>${curso.nombreCurso || '-'}</td>
            <td>${curso.horasSemanales ?? '-'}</td>
            <td>${curso.creditos ?? '-'}</td>
            <td>${state.perfil?.cicloActual ?? '-'}</td>
            <td>Activo</td>
            <td>-</td>
            <td>${curso.modalidad || '-'}</td>
            <td class="menu">⋮</td>
        `;
        tbody.appendChild(tr);
    });
}

/* ============================================================
   GENERADOR COMPLETO DE TABLA DE HORARIO
============================================================ */
function generarTablaHorario() {
    const tbody = document.querySelector('#vista-horario .horario-completo tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    const horaInicio = 8;
    const horaFin = 22;

    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

    for (let h = horaInicio; h < horaFin; h++) {
        const fila = document.createElement('tr');

        const horaInicioStr = String(h).padStart(2, '0') + ':00';
        const horaFinStr = String(h + 1).padStart(2, '0') + ':00';

        const celdaHora = document.createElement('td');
        celdaHora.textContent = `${horaInicioStr} - ${horaFinStr}`;
        fila.appendChild(celdaHora);

        dias.forEach(() => {
            const celda = document.createElement('td');
            celda.classList.add('empty');
            fila.appendChild(celda);
        });

        tbody.appendChild(fila);
    }
}

function pintarHorario() {
    const tbody = document.querySelector('#vista-horario .horario-completo tbody');
    if (!tbody) return;

    const mapaDias = {
        LUNES: 1,
        MARTES: 2,
        MIERCOLES: 3,
        JUEVES: 4,
        VIERNES: 5,
        SABADO: 6,
        DOMINGO: 7
    };

    state.horario.forEach(bloque => {
        const inicioHora = parseInt((bloque.horaInicio || '0').split(':')[0], 10);
        const finHora = parseInt((bloque.horaFin || '0').split(':')[0], 10);
        const columna = mapaDias[(bloque.dia || '').toUpperCase()];
        if (!columna) return;

        for (let h = inicioHora; h < finHora; h++) {
            const fila = tbody.children[h - 8];
            if (!fila) continue;
            const celda = fila.children[columna];
            if (!celda) continue;
            celda.classList.remove('empty');
            celda.innerHTML = `<strong>${bloque.curso || ''}</strong><br>${bloque.horaInicio || ''} - ${bloque.horaFin || ''}<br>${bloque.docente || ''}`;
        }
    });
}

/* ============================================================
   INICIALIZACIÓN GENERAL
============================================================ */
function inicializarHorario() {
    generarTablaHorario();
    pintarHorario();
}

async function cargarDatosIniciales() {
    try {
        const [perfil, matriculaActual, cursos, horario, pagos] = await Promise.all([
            fetchJson('/alumno/info', 'No se pudo obtener la información del alumno'),
            fetchJson('/alumno/matricula/actual', 'No se pudo obtener la matrícula actual'),
            fetchJson('/alumno/matricula/cursos', 'No se pudieron cargar los cursos'),
            fetchJson('/alumno/horario', 'No se pudo cargar el horario'),
            fetchJson('/alumno/pagos', 'No se pudieron cargar las pensiones')
        ]);

        state.perfil = perfil;
        state.resumen = matriculaActual;
        state.cursos = Array.isArray(cursos) ? cursos : [];
        state.horario = Array.isArray(horario) ? horario : [];
        state.pagos = Array.isArray(pagos) ? pagos : [];

        actualizarResumenMatricula();
        actualizarDatosPensiones();
        actualizarInformacionAdicional();
        generarTarjetasCursos();
        renderTablaCursos();
        inicializarHorario();
        await cargarCatalogo();
    } catch (err) {
        // Errores ya manejados en fetchJson
    }
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDatosIniciales();
    document.getElementById('btn-filtrar')?.addEventListener('click', cargarCatalogo);
    document.getElementById('btn-matricular')?.addEventListener('click', intentarMatricular);
    document.getElementById('btn-retirar')?.addEventListener('click', intentarRetirar);
});

function actualizarResumenMatricula() {
    const contenedor = document.querySelector('#aside-derecho .resumen');
    if (!contenedor) return;
    const items = contenedor.querySelectorAll('.resumen__info');
    const resumen = state.resumen || {};

    if (items[0]) items[0].textContent = `Cursos: ${resumen.totalCursos ?? state.cursos.length ?? 0}`;
    if (items[1]) items[1].textContent = `Horas semanales: ${resumen.totalHoras ?? '-'}`;
    if (items[2]) items[2].textContent = `Créditos: ${resumen.totalCreditos ?? '-'}`;
    if (items[3]) items[3].textContent = `Monto: S/ ${Number(resumen.montoTotal || 0).toFixed(2)}`;
}

function actualizarInformacionAdicional() {
    const adicional = document.querySelector('.adicional__info');
    if (!adicional || !state.perfil) return;
    adicional.innerHTML = `
        <strong>${state.perfil.nombres || ''} ${state.perfil.apellidos || ''}</strong><br>
        Código: ${state.perfil.codigo || '-'}<br>
        Carrera: ${state.perfil.carrera || '-'}<br>
        Ciclo: ${state.perfil.cicloActual || '-'}
    `;

    const contactoInfo = document.querySelector('.contact__info');
    if (contactoInfo) {
        contactoInfo.textContent = `Correo institucional: ${state.perfil.correoInstitucional || '-'}`;
    }
}

/* ============================================================
   PENSIONES
============================================================ */
function actualizarDatosPensiones() {
    const deudaCard = document.querySelector('.pension-card.alerta .monto');
    const proximoCard = document.querySelector('.pension-card.info .dato');
    const proximoSub = document.querySelector('.pension-card.info .subtexto');
    const cicloCard = document.querySelector('.pension-card.success .dato');
    const tabla = document.querySelector('.tabla-pensiones tbody');

    const pendientes = state.pagos.filter(p => p.estado !== 'PAGADO');
    const deudaTotal = pendientes.reduce((acc, p) => acc + (p.monto || 0), 0);
    if (deudaCard) deudaCard.textContent = `S/ ${deudaTotal.toFixed(2)}`;

    const proximo = pendientes
        .filter(p => p.vencimiento)
        .sort((a, b) => new Date(a.vencimiento) - new Date(b.vencimiento))[0];
    if (proximoCard) proximoCard.textContent = proximo?.vencimiento || '—';
    if (proximoSub) proximoSub.textContent = proximo?.concepto || '';

    if (cicloCard) cicloCard.textContent = state.pagos[0]?.periodo || '—';

    if (!tabla) return;
    tabla.innerHTML = '';

    if (!state.pagos.length) {
        tabla.innerHTML = '<tr><td colspan="7" class="muted">No hay pagos registrados</td></tr>';
        return;
    }

    state.pagos.forEach(pago => {
        const tr = document.createElement('tr');
        const total = (pago.monto || 0).toFixed(2);
        const badgeClass = pago.estado === 'PAGADO' ? 'pagado' : 'pendiente';
        tr.innerHTML = `
            <td>${pago.concepto || ''}</td>
            <td>${pago.vencimiento || '—'}</td>
            <td>${total}</td>
            <td>0.00</td>
            <td>${total}</td>
            <td><span class="badge ${badgeClass}">${pago.estado || ''}</span></td>
            <td class="action-cell"><ion-icon name="document-text-outline" class="icon-btn"></ion-icon></td>
        `;
        tabla.appendChild(tr);
    });
}

/* ============================================================
   MANEJO DE SOLICITUD DE SECCIÓN
============================================================ */
const form = document.querySelector('.solicitud-container');
const historialVacio = document.querySelector('.historial-vacio');
const historialLista = document.querySelector('.historial-lista');

if (form) {
    form.addEventListener('submit', function(e){
        e.preventDefault();

        const curso = document.getElementById('curso').value;
        const turno = document.getElementById('turno').value;
        const modalidad = document.getElementById('modalidad').value;
        const correo = document.getElementById('correo').value;
        const telefono = document.getElementById('telefono').value;
        const motivo = document.getElementById('motivo').value;
        const evidencia = document.getElementById('evidencia').files[0];

        const fecha = new Date().toLocaleDateString('es-PE', {
            day: 'numeric',
            month: 'short',
            year: 'numeric'
        });

        if (historialVacio) historialVacio.style.display = 'none';

        const card = document.createElement('div');
        card.classList.add('solicitud-card');

        card.innerHTML = `
            <div class="curso-titulo">${curso}</div>
            <strong>Turno:</strong> ${turno}<br>
            <strong>Modalidad:</strong> ${modalidad}<br>
            <strong>Correo:</strong> ${correo}<br>
            <strong>Teléfono:</strong> ${telefono}<br>
            <strong>Motivo:</strong> ${motivo}<br>
            <strong>Fecha de solicitud:</strong> ${fecha}<br>
        `;

        if (evidencia) {
            const link = document.createElement('a');
            link.classList.add('btn-descargar');
            link.textContent = `Descargar evidencia (${evidencia.name})`;

            const url = URL.createObjectURL(evidencia);
            link.href = url;
            link.download = evidencia.name;

            card.appendChild(link);
        }

        historialLista.appendChild(card);
        form.reset();
    });
}
