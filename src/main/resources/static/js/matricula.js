const state = {
    perfil: null,
    resumen: null,
    cursosMatriculados: [],
    horario: [],
    pagos: [],
    solicitudes: [],
    solicitudesCargadas: false,
    solicitudesSnapshot: new Map(),
    cursosSolicitables: [],
    cursosDisponibles: [],
    seccionesPorCurso: {},
    seccionSeleccionada: null
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

    Object.values(vistas).forEach(div => div?.classList.add('hidden'));

    if (vista === 'matricula') {
        vistas.matricula?.classList.remove('hidden');
        asideDerecho?.classList.remove('hidden');
        body.classList.remove('layout-2-cols');
        renderHorarioTablas();
    } else if (vista === 'solicitud') {
        vistas.solicitud?.classList.remove('hidden');
        asideDerecho?.classList.add('hidden');
        body.classList.add('layout-2-cols');
        cargarSolicitudesAlumno();
        cargarCursosSolicitables();
    } else if (vista === 'pensiones') {
        vistas.pensiones?.classList.remove('hidden');
        asideDerecho?.classList.add('hidden');
        body.classList.add('layout-2-cols');
    } else if (vista === 'horario') {
        vistas.horario?.classList.remove('hidden');
        asideDerecho?.classList.add('hidden');
        body.classList.add('layout-2-cols');
        renderHorarioTablas();
    }

    const linksMenu = document.querySelectorAll('.aside-left__container a');
    linksMenu.forEach(link => link.classList.remove('active'));
    if (event?.currentTarget) {
        event.currentTarget.classList.add('active');
    }
}

/* ============================================================
   UTILIDADES
============================================================ */
async function fetchJson(url, errorMessage, options = {}) {
    try {
        const resp = await fetch(url, options);
        const contentType = resp.headers.get('content-type') || '';
        const isJson = contentType.includes('application/json');
        const payload = isJson ? await resp.json().catch(() => null) : null;

        if (!resp.ok) {
            const detalle = payload?.message || payload?.mensajes?.join?.(' ') || errorMessage;
            throw new Error(detalle || 'Error de servidor');
        }

        if (isJson) {
            return payload;
        }
        return null;
    } catch (err) {
        console.error(err);
        mostrarMensajeError(err.message || errorMessage);
        throw err;
    }
}

function setText(selector, value) {
    const el = typeof selector === 'string' ? document.querySelector(selector) : selector;
    if (el) el.textContent = value ?? '—';
}

function ensureToastContainer() {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    return container;
}

function mostrarToast(mensaje, tipo = 'info') {
    const container = ensureToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast toast--${tipo}`;
    toast.textContent = mensaje || '';
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

function mostrarMensajeError(msg) {
    mostrarToast(msg || 'Ocurrió un error inesperado', 'error');
}

function mostrarMensajeExito(msg) {
    mostrarToast(msg || 'Operación exitosa', 'success');
}

function formatearFechaCorta(fecha) {
    if (!fecha) return '—';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' });
}

async function confirmarAccion(titulo, texto, confirmText = 'Confirmar') {
    if (window.Swal) {
        const resp = await Swal.fire({
            title: titulo,
            text: texto,
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: confirmText,
            cancelButtonText: 'Cancelar'
        });
        return resp.isConfirmed;
    }
    return window.confirm(`${titulo}\n${texto || ''}`);
}

function mostrarModalMensajes(titulo, mensajes = []) {
    const contenido = Array.isArray(mensajes) && mensajes.length
        ? `<ul>${mensajes.map(m => `<li>${m}</li>`).join('')}</ul>`
        : '';
    if (window.Swal) {
        Swal.fire({
            title: titulo,
            html: contenido || titulo,
            icon: 'info'
        });
    } else {
        mostrarMensajeError(mensajes.join('. '));
    }
}

function construirQueryCursos() {
    const ciclo = document.getElementById('filtro-ciclo')?.value || '';
    const modalidad = document.getElementById('filtro-modalidad')?.value || '';
    const texto = document.getElementById('filtro-texto')?.value || '';
    const params = new URLSearchParams();
    if (ciclo) params.append('ciclo', ciclo);
    if (modalidad) params.append('modalidad', modalidad);
    if (texto) params.append('q', texto);
    const query = params.toString();
    return query ? `?${query}` : '';
}

function formatearHorario(horario) {
    if (!horario) return '—';
    if (Array.isArray(horario)) {
        return horario.map(h => `${h.dia || h.diaSemana || ''} ${h.horaInicio || ''}-${h.horaFin || ''}`).join(' | ');
    }
    if (typeof horario === 'string') return horario;
    return `${horario.dia || ''} ${horario.horaInicio || ''}-${horario.horaFin || ''}`.trim();
}

function limpiarSeleccionSeccion() {
    state.seccionSeleccionada = null;
    document.querySelectorAll('.tabla-secciones tr.selected').forEach(tr => tr.classList.remove('selected'));
}

async function cargarPeriodos() {
    const select = document.getElementById('filtro-ciclo');
    if (!select) return;
    const opciones = Array.from({ length: 10 }, (_, idx) => idx + 1);
    const actual = state.perfil?.cicloActual;
    select.innerHTML = '<option value="">Todos</option>' + opciones
        .map(num => `<option value="${num}">${num}</option>`)
        .join('');
}

/* ============================================================
   RENDER DE CURSOS Y SECCIONES
============================================================ */
async function cargarCursosDisponibles() {
    const url = `/alumno/cursos/disponibles${construirQueryCursos()}`;
    state.seccionesPorCurso = {};
    state.cursosDisponibles = await fetchJson(url, 'No se pudo cargar el catálogo de cursos') || [];
    const estado = document.getElementById('estado-cursos');
    if (estado) {
        estado.textContent = state.cursosDisponibles.length
            ? `${state.cursosDisponibles.length} curso(s) encontrado(s)`
            : 'Sin resultados con los filtros actuales';
    }
    renderizarCursosTabla();
    poblarCursosSolicitud();
}

async function obtenerSeccionesCurso(cursoId) {
    if (!cursoId) return [];
    if (!state.seccionesPorCurso[cursoId]) {
        const secciones = await fetchJson(`/alumno/cursos/${cursoId}/secciones`, 'No se pudieron cargar las secciones');
        state.seccionesPorCurso[cursoId] = secciones;
    }
    return state.seccionesPorCurso[cursoId] || [];
}

function renderizarCursosTabla() {
    const tbody = document.getElementById('cursos-disponibles-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    limpiarSeleccionSeccion();

    if (!state.cursosDisponibles.length) {
        const tr = document.createElement('tr');
        tr.classList.add('fila-empty');
        tr.innerHTML = '<td colspan="8" class="estado-vacio">No se encontraron cursos disponibles para los filtros seleccionados.</td>';
        tbody.appendChild(tr);
        return;
    }

    state.cursosDisponibles.forEach(curso => {
        const tr = document.createElement('tr');
        tr.classList.add('fila-curso');
        const cursoId = curso.cursoId ?? curso.id ?? curso.codigoCurso;
        tr.dataset.cursoId = cursoId || '';

        const datos = [
            curso.codigoCurso || '—',
            curso.nombreCurso || '—',
            curso.creditos ?? '—',
            curso.ciclo || '—',
            curso.modalidad || '—',
            curso.docente || '—',
            curso.cuposDisponibles ?? curso.cupos ?? '—'
        ];

        datos.forEach(valor => {
            const td = document.createElement('td');
            td.textContent = valor;
            tr.appendChild(td);
        });

        const tdSolicitud = document.createElement('td');
        if (puedeSolicitarApertura(cursoId, curso)) {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.textContent = 'Solicitar';
            btn.className = 'btn-outline btn-sm';
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                abrirFormularioSolicitud(cursoId);
            });
            tdSolicitud.appendChild(btn);
        }
        tr.appendChild(tdSolicitud);

        tr.addEventListener('click', () => mostrarSecciones(curso, tr));
        tbody.appendChild(tr);
    });
}

function puedeSolicitarApertura(cursoId, curso) {
    const cupos = curso?.cuposDisponibles ?? 0;
    const pendiente = tieneSolicitudPendiente(cursoId);
    const opcionSolicitable = (state.cursosSolicitables || []).find(c => `${c.id}` === `${cursoId}`);
    return (!cupos || cupos <= 0 || opcionSolicitable) && !pendiente;
}

function tieneSolicitudPendiente(cursoId) {
    if (!cursoId) return false;
    return (state.solicitudes || []).some(s => (`${s.cursoId}` === `${cursoId}` || `${s.codigoCurso}` === `${cursoId}`) && s.estado === 'PENDIENTE');
}

function abrirFormularioSolicitud(cursoId) {
    mostrarVista('solicitud');
    const select = document.getElementById('curso');
    if (select) select.value = cursoId;
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function mostrarSecciones(curso, filaCurso) {
    const cursoId = filaCurso?.dataset?.cursoId;
    if (!cursoId) return;

    const abierto = document.querySelector(`.fila-subtabla[data-curso="${cursoId}"]`);
    if (abierto) {
        abierto.remove();
        filaCurso.classList.remove('abierto');
        limpiarSeleccionSeccion();
        return;
    }

    document.querySelectorAll('.fila-subtabla').forEach(fila => fila.remove());
    document.querySelectorAll('.fila-curso.abierto').forEach(fila => fila.classList.remove('abierto'));

    const secciones = await obtenerSeccionesCurso(cursoId);
    renderizarSeccionesTabla(cursoId, secciones, filaCurso, curso);
    filaCurso.classList.add('abierto');
}

function renderizarSeccionesTabla(cursoId, secciones = [], filaCurso, cursoPadre = {}) {
    const tbody = filaCurso?.parentElement;
    if (!tbody) return;

    const existente = document.querySelector(`.fila-subtabla[data-curso="${cursoId}"]`);
    existente?.remove();

    const detalleRow = document.createElement('tr');
    detalleRow.classList.add('fila-subtabla');
    detalleRow.dataset.curso = cursoId;

    const cell = document.createElement('td');
    cell.colSpan = 7;

    const contenedor = document.createElement('div');
    contenedor.className = 'subtabla-contenedor';

    const header = document.createElement('div');
    header.className = 'subtabla-titulo';
    header.innerHTML = `<h4>Secciones de ${cursoPadre.nombreCurso || cursoPadre.nombre || ''}</h4><p>Elige una sección para matricularte.</p>`;
    contenedor.appendChild(header);

    const tabla = document.createElement('table');
    tabla.className = 'tabla-secciones admin-table alumno-table alumno-table--compact';
    tabla.innerHTML = `
        <thead>
            <tr>
                <th>Sección</th>
                <th>Docente</th>
                <th>Turno</th>
                <th>Horario</th>
                <th>Aula</th>
                <th>Cupos</th>
                <th class="acciones-col">Acciones</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;

    const body = tabla.querySelector('tbody');
    if (!secciones.length) {
        const vacio = document.createElement('tr');
        vacio.innerHTML = '<td colspan="7" class="estado-vacio">No hay secciones disponibles para este curso.</td>';
        body.appendChild(vacio);
    } else {
        secciones.forEach(seccion => {
            const seccionId = seccion.seccionId ?? seccion.id ?? seccion.codigoSeccion;
            const matriculado = state.cursosMatriculados.some(c => String(c.seccionId ?? c.codigoSeccion) === String(seccionId));
            const tr = document.createElement('tr');
            tr.dataset.seccionId = seccionId;

            tr.innerHTML = `
                <td>${seccion.codigoSeccion || seccion.codigo || seccionId || '—'}</td>
                <td>${seccion.docente || 'Por asignar'}</td>
                <td>${seccion.turno || cursoPadre.turno || '—'}</td>
                <td>${formatearHorario(seccion.horario || seccion.horarios)}</td>
                <td>${seccion.aula || '—'}</td>
                <td>${(seccion.cuposDisponibles ?? '0')} / ${seccion.matriculados ?? '0'}</td>
                <td class="acciones-col"></td>
            `;

            const acciones = tr.querySelector('.acciones-col');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.textContent = matriculado ? 'Retirar' : 'Matricular';
            btn.className = matriculado ? 'btn-secondary btn-sm' : 'btn-primary btn-sm';
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                if (matriculado) {
                    retirar(seccionId);
                } else {
                    matricular(seccionId);
                }
            });
            acciones.appendChild(btn);

            tr.addEventListener('click', () => seleccionarSeccion(seccionId, tr));
            body.appendChild(tr);
        });
    }

    contenedor.appendChild(tabla);
    cell.appendChild(contenedor);
    detalleRow.appendChild(cell);

    if (filaCurso.nextSibling) {
        tbody.insertBefore(detalleRow, filaCurso.nextSibling);
    } else {
        tbody.appendChild(detalleRow);
    }
}

function seleccionarSeccion(seccionId, fila) {
    state.seccionSeleccionada = seccionId;
    document.querySelectorAll('.tabla-secciones tbody tr').forEach(tr => tr.classList.remove('selected'));
    fila?.classList.add('selected');
}

/* ============================================================
   ACCIONES DE MATRÍCULA
============================================================ */
async function matricular(seccionId) {
    if (!seccionId) {
        mostrarMensajeError('Selecciona una sección para matricularte');
        return;
    }

    try {
        const validacion = await fetchJson(`/alumno/matricula/validar/${seccionId}`, 'No se pudo validar la sección');
        if (validacion && validacion.puedeMatricular === false) {
            mostrarModalMensajes('No es posible matricular', validacion.mensajes || ['La sección no está disponible']);
            return;
        }

        const confirmado = await confirmarAccion('¿Matricularte en esta sección?', 'Se validarán horarios y prerrequisitos.', 'Matricular');
        if (!confirmado) return;

        await fetchJson(`/alumno/matricula/${seccionId}`, 'No se pudo completar la matrícula', { method: 'POST' });
        await refrescarEstadoMatricula();
        await cargarCursosDisponibles();
        mostrarMensajeExito('Se registró tu matrícula en la sección seleccionada');
    } catch (err) {
        // manejo centralizado
    }
}

async function retirar(seccionId) {
    if (!seccionId) {
        mostrarMensajeError('Selecciona una sección para retirar');
        return;
    }

    try {
        const confirmado = await confirmarAccion('Retirar sección', 'Se eliminará este curso de tu matrícula.', 'Retirar');
        if (!confirmado) return;

        await fetchJson(`/alumno/matricula/${seccionId}`, 'No se pudo retirar la sección', { method: 'DELETE' });
        await refrescarEstadoMatricula();
        await cargarCursosDisponibles();
        mostrarMensajeExito('Se retiró la sección de tu matrícula');
    } catch (err) {
        mostrarMensajeError(err.message || 'No se pudo retirar la sección');
    }
}

async function guardarHorario() {
    try {
        const resp = await fetchJson('/alumno/horario/guardar', 'No se pudo guardar tu horario', { method: 'POST' });
        mostrarMensajeExito(resp?.mensaje || 'Horario guardado');
    } catch (err) {
        // manejo centralizado
    }
}

async function descargarHorarioPdf() {
    try {
        const resp = await fetch('/alumno/horario/pdf');
        if (!resp.ok) throw new Error('No se pudo generar el PDF');
        const blob = await resp.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'horario.pdf';
        a.click();
        window.URL.revokeObjectURL(url);
        mostrarMensajeExito('Descarga iniciada');
    } catch (err) {
        mostrarMensajeError(err.message || 'No se pudo descargar el PDF');
    }
}

/* ============================================================
   HORARIO
============================================================ */
function generarEstructuraHorario(tbody) {
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
    const tablas = document.querySelectorAll('.horario-completo tbody');
    if (!tablas.length) return;

    const mapaDias = {
        LUNES: 1,
        MARTES: 2,
        MIÉRCOLES: 3,
        MIERCOLES: 3,
        JUEVES: 4,
        VIERNES: 5,
        SÁBADO: 6,
        SABADO: 6,
        DOMINGO: 7
    };

    tablas.forEach(tbody => {
        Array.from(tbody.querySelectorAll('tr')).forEach(tr => {
            Array.from(tr.children).forEach((td, idx) => {
                if (idx === 0) return;
                td.classList.add('empty');
                td.innerHTML = '';
            });
        });

        state.horario.forEach(bloque => {
            const inicioHora = parseInt((bloque.horaInicio || '0').split(':')[0], 10);
            const finHora = parseInt((bloque.horaFin || '0').split(':')[0], 10);
            const columna = mapaDias[(bloque.dia || bloque.diaSemana || '').toUpperCase()];
            if (!columna) return;

            for (let h = inicioHora; h < finHora; h++) {
                const fila = tbody.children[h - 8];
                if (!fila) continue;
                const celda = fila.children[columna];
                if (!celda) continue;
                celda.classList.remove('empty');
                celda.innerHTML = `<strong>${bloque.curso || bloque.codigoCurso || ''}</strong><br>${bloque.horaInicio || ''} - ${bloque.horaFin || ''}<br>${bloque.docente || ''}`;
            }
        });
    });
}

function renderHorarioTablas() {
    const tablas = document.querySelectorAll('.horario-completo tbody');
    tablas.forEach(generarEstructuraHorario);
    pintarHorario();
}

async function actualizarHorario() {
    state.horario = await fetchJson('/alumno/horario', 'No se pudo cargar el horario') || [];
    renderHorarioTablas();
}

/* ============================================================
   TARJETAS Y RESUMEN
============================================================ */
function actualizarResumenMatricula() {
    const resumen = state.resumen || {};
    const creditos = resumen.totalCreditos ?? state.cursosMatriculados.reduce((acc, c) => acc + (c.creditos || 0), 0);
    const horas = resumen.totalHoras ?? state.cursosMatriculados.reduce((acc, c) => acc + (c.horasSemanales || 0), 0);
    const monto = Number(resumen.montoTotal || 0).toFixed(2);

    setText('#resumen-cursos', resumen.totalCursos ?? state.cursosMatriculados.length ?? 0);
    setText('#resumen-horas', horas || '—');
    setText('#resumen-creditos', creditos || '0');
    setText('#resumen-creditos-max', resumen.creditosMaximos ?? resumen.creditosMax ?? '—');

    const montoNodo = document.querySelector('#alumno-estado-financiero');
    if (montoNodo) montoNodo.title = `Monto estimado: S/ ${monto}`;
}

function actualizarFichaAlumno() {
    const perfil = state.perfil || {};
    const resumen = state.resumen || {};
    const pendiente = state.pagos.filter(p => p.estado !== 'PAGADO');
    const deudaTotal = pendiente.reduce((acc, p) => acc + (p.monto || 0), 0);
    const estadoFinanciero = deudaTotal > 0 ? `Pendiente: S/ ${deudaTotal.toFixed(2)}` : 'Al día';

    setText('#alumno-nombre', `${perfil.nombres || ''} ${perfil.apellidos || ''}`.trim() || 'Alumno');
    setText('#alumno-codigo', perfil.codigo || '—');
    setText('#alumno-carrera', perfil.carrera || '—');
    setText('#alumno-ciclo', perfil.cicloActual || resumen.ciclo || '—');
    setText('#alumno-orden', perfil.ordenMerito || perfil.orden || '—');

    setText('#resumen-creditos', resumen.totalCreditos ?? '0');
    setText('#resumen-creditos-max', resumen.creditosMaximos ?? resumen.creditosMax ?? '—');
    setText('#resumen-cursos', resumen.totalCursos ?? state.cursosMatriculados.length ?? 0);
    setText('#resumen-horas', resumen.totalHoras ?? '—');

    setText('#alumno-estado-financiero', estadoFinanciero);
    setText('#estado-pagos', deudaTotal > 0 ? 'Pendiente' : 'Al día');

    const fechas = document.getElementById('fechas-matricula');
    if (fechas) {
        const inicio = resumen.inicio || resumen.fechaInicio || '—';
        const fin = resumen.fin || resumen.fechaFin || '—';
        fechas.innerHTML = `<span class="badge badge--soft">Inicio: ${inicio}</span><span class="badge badge--soft">Fin: ${fin}</span>`;
    }

    const contactoInfo = document.querySelector('.contact__info');
    if (contactoInfo && perfil.correoInstitucional) {
        contactoInfo.textContent = `Correo institucional: ${perfil.correoInstitucional}`;
    }
}

function renderCursosInscritos() {
    const tablaBody = document.getElementById('tabla-inscritos-body');
    if (tablaBody) {
        tablaBody.innerHTML = '';
        if (!state.cursosMatriculados.length) {
            tablaBody.innerHTML = '<tr><td colspan="6" class="estado-vacio">Aún no tienes cursos matriculados.</td></tr>';
        } else {
            state.cursosMatriculados.forEach(curso => {
                const tr = document.createElement('tr');
                const seccionId = curso.seccionId;
                tr.innerHTML = `
                    <td>${curso.codigoSeccion || curso.codigoCurso || '-'}</td>
                    <td>${curso.nombreCurso || '-'}</td>
                    <td>${curso.creditos ?? '-'}</td>
                    <td>${curso.modalidad || '-'}</td>
                    <td>${curso.docente || 'Por asignar'}</td>
                    <td class="acciones-col"></td>
                `;
                const acciones = tr.querySelector('.acciones-col');
                const btn = document.createElement('button');
                btn.className = 'btn-outline btn-sm';
                btn.type = 'button';
                btn.textContent = 'Retirar';
                btn.addEventListener('click', () => retirar(seccionId));
                acciones.appendChild(btn);
                tablaBody.appendChild(tr);
            });
        }
    }

    const contenedor = document.querySelector('.cursos-inscritos');
    if (contenedor) {
        contenedor.innerHTML = '';
        if (!state.cursosMatriculados.length) {
            contenedor.innerHTML = '<div class="estado-vacio">Aún no tienes cursos matriculados.</div>';
            return;
        }

        state.cursosMatriculados.forEach(curso => {
            const card = document.createElement('div');
            card.classList.add('curso-card');
            card.innerHTML = `
                <h3>${curso.codigoSeccion || curso.codigoCurso || ''} — ${curso.nombreCurso || ''}</h3>
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
const badgeSolicitudesAlumno = document.getElementById('badge-solicitudes-alumno');
const estadoActualSolicitud = document.getElementById('solicitud-estado-actual');
const btnLimpiarSolicitud = document.getElementById('btn-limpiar-solicitud');

function estadoChipClass(estado) {
    const normalizado = (estado || 'PENDIENTE').toUpperCase();
    if (normalizado === 'SOLUCIONADA') return 'solucionada';
    if (normalizado === 'RECHAZADA') return 'rechazada';
    return 'pendiente';
}

function actualizarIndicadoresSolicitud() {
    const total = state.solicitudes?.length || 0;
    if (badgeSolicitudesAlumno) {
        badgeSolicitudesAlumno.textContent = total;
        badgeSolicitudesAlumno.hidden = total === 0;
    }

    if (estadoActualSolicitud) {
        if (!total) {
            estadoActualSolicitud.textContent = 'Pendiente de envío';
            return;
        }
        const ultima = state.solicitudes[0];
        const estado = (ultima.estado || 'PENDIENTE').toLowerCase();
        const mensajes = {
            pendiente: 'Pendiente de revisión',
            solucionada: 'Sección solucionada',
            rechazada: 'Solicitud rechazada'
        };
        estadoActualSolicitud.textContent = mensajes[estado] || ultima.estado || 'Pendiente';
    }
}

function actualizarSnapshotSolicitudes(solicitudes) {
    state.solicitudesSnapshot = new Map((solicitudes || []).map(s => [s.id, {
        estado: s.estado,
        mensajeAdmin: s.mensajeAdmin
    }]));
}

function notificarCambiosSolicitudes(nuevasSolicitudes) {
    if (!state.solicitudesCargadas) {
        actualizarSnapshotSolicitudes(nuevasSolicitudes);
        state.solicitudesCargadas = true;
        return;
    }

    const anterior = state.solicitudesSnapshot || new Map();
    (nuevasSolicitudes || []).forEach(s => {
        if (!s?.id) return;
        const previo = anterior.get(s.id);
        if (!previo) {
            mostrarToast('Registraste una nueva solicitud de sección', 'info');
            return;
        }
        if (previo.estado !== s.estado && s.estado) {
            const tipo = s.estado === 'SOLUCIONADA' ? 'success' : (s.estado === 'RECHAZADA' ? 'error' : 'info');
            mostrarToast(`Tu solicitud de ${s.codigoCurso || 'curso'} ahora está ${s.estado.toLowerCase()}`, tipo);
        } else if (previo.mensajeAdmin !== s.mensajeAdmin && s.mensajeAdmin) {
            mostrarToast('El administrador dejó un nuevo mensaje en tu solicitud', 'info');
        }
    });

    actualizarSnapshotSolicitudes(nuevasSolicitudes);
}

async function cargarSolicitudesAlumno() {
    try {
        const solicitudes = await fetchJson('/alumno/solicitudes', 'No se pudieron cargar tus solicitudes');
        notificarCambiosSolicitudes(solicitudes || []);
        state.solicitudes = solicitudes || [];
        renderHistorialSolicitudes();
    } catch (err) {
        console.error(err);
    }
}

async function cargarCursosSolicitables() {
    try {
        const cursos = await fetchJson('/alumno/solicitudes/cursos', 'No se pudieron cargar los cursos solicitables');
        state.cursosSolicitables = Array.isArray(cursos) ? cursos : [];
        poblarCursosSolicitud();
    } catch (err) {
        console.error(err);
        state.cursosSolicitables = [];
        poblarCursosSolicitud();
    }
}

function descargarEvidencia(solicitud) {
    if (!solicitud?.evidenciaBase64 || !solicitud?.evidenciaNombreArchivo) return;
    const link = document.createElement('a');
    const tipo = solicitud.evidenciaContentType || 'application/octet-stream';
    link.href = `data:${tipo};base64,${solicitud.evidenciaBase64}`;
    link.download = solicitud.evidenciaNombreArchivo;
    document.body.appendChild(link);
    link.click();
    link.remove();
}

function renderHistorialSolicitudes() {
    if (!historialLista) return;
    historialLista.innerHTML = '';

    if (!state.solicitudes || !state.solicitudes.length) {
        if (historialVacio) historialVacio.style.display = 'block';
        actualizarIndicadoresSolicitud();
        return;
    }
    if (historialVacio) historialVacio.style.display = 'none';

    state.solicitudes.forEach(s => {
        const card = document.createElement('article');
        card.classList.add('solicitud-item');

        const estadoClass = estadoChipClass(s.estado);
        const fechaSolicitud = formatearFechaCorta(s.fechaSolicitud);
        const fechaActualizacion = formatearFechaCorta(s.fechaActualizacion || s.fechaSolicitud);

        card.innerHTML = `
            <div class="solicitud-item__header">
                <div>
                    <p class="muted">${s.codigoCurso || ''}</p>
                    <h4 class="solicitud-item__title">${s.curso || 'Curso'}</h4>
                </div>
                <div class="estado-chip ${estadoClass}">${s.estado || 'PENDIENTE'}</div>
            </div>
            <div class="solicitud-meta">
                ${s.ciclo ? `<span class="badge-light">Ciclo ${s.ciclo}</span>` : ''}
                ${s.modalidad ? `<span class="meta-tag"><strong>Modalidad:</strong> ${s.modalidad}</span>` : ''}
                ${s.turno ? `<span class="meta-tag"><strong>Turno:</strong> ${s.turno}</span>` : ''}
                ${typeof s.solicitantes === 'number' ? `<span class="meta-tag"><strong>Solicitudes:</strong> ${s.solicitantes}</span>` : ''}
            </div>
            <div class="solicitud-body">
                <div>
                    <p class="label">Motivo</p>
                    <p>${s.motivo || '—'}</p>
                </div>
                <div>
                    <p class="label">Mensaje del administrador</p>
                    <p>${s.mensajeAdmin || 'Aún no hay comentarios del administrador'}</p>
                </div>
            </div>
            <div class="solicitud-meta">
                <span class="meta-tag"><strong>Solicitud:</strong> ${fechaSolicitud}</span>
                <span class="meta-tag"><strong>Última actualización:</strong> ${fechaActualizacion}</span>
            </div>
        `;

        const actions = document.createElement('div');
        actions.className = 'solicitud-actions';
        if (s.evidenciaNombreArchivo && s.evidenciaBase64) {
            const btnEvidencia = document.createElement('button');
            btnEvidencia.type = 'button';
            btnEvidencia.className = 'btn-evidencia';
            btnEvidencia.textContent = `Descargar ${s.evidenciaNombreArchivo}`;
            btnEvidencia.addEventListener('click', () => descargarEvidencia(s));
            actions.appendChild(btnEvidencia);
        }

        card.appendChild(actions);
        historialLista.appendChild(card);
    });

    actualizarIndicadoresSolicitud();
}

async function archivoABase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => {
            const result = reader.result || '';
            const base64 = result.toString().split(',')[1];
            resolve(base64);
        };
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(file);
    });
}

async function registrarSolicitudSeccion(e) {
    e.preventDefault();
    const cursoId = document.getElementById('curso')?.value;
    const turno = document.getElementById('turno')?.value;
    const modalidad = document.getElementById('modalidad')?.value;
    const correo = document.getElementById('correo')?.value;
    const telefono = document.getElementById('telefono')?.value;
    const motivo = document.getElementById('motivo')?.value?.trim();
    const evidenciaInput = document.getElementById('evidencia');

    if (!cursoId || !motivo || !turno) {
        mostrarMensajeError('Completa los campos obligatorios');
        return;
    }

    if (motivo.length < 8) {
        mostrarMensajeError('Describe mejor el motivo de tu solicitud');
        return;
    }

    if (tieneSolicitudPendiente(cursoId)) {
        mostrarMensajeError('Ya registraste una solicitud pendiente para este curso');
        return;
    }

    let evidenciaNombreArchivo = null;
    let evidenciaBase64 = null;
    let evidenciaContentType = null;
    if (evidenciaInput?.files?.length) {
        const archivo = evidenciaInput.files[0];
        const maxSize = 5 * 1024 * 1024;
        if (archivo.size > maxSize) {
            mostrarMensajeError('La evidencia no debe superar los 5 MB');
            return;
        }
        evidenciaNombreArchivo = archivo.name;
        evidenciaContentType = archivo.type || 'application/octet-stream';
        evidenciaBase64 = await archivoABase64(archivo);
    }

    await fetchJson('/alumno/solicitudes', 'No se pudo registrar la solicitud', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            cursoId,
            turno,
            modalidad,
            correo,
            telefono,
            motivo,
            evidenciaNombreArchivo,
            evidenciaContentType,
            evidenciaBase64
        })
    });

    mostrarMensajeExito('Solicitud registrada correctamente');
    form.reset();
    await cargarSolicitudesAlumno();
    await cargarCursosSolicitables();
}

function limpiarFormularioSolicitud() {
    form?.reset();
    if (estadoActualSolicitud) estadoActualSolicitud.textContent = 'Pendiente de envío';
}

function poblarCursosSolicitud() {
    const select = document.getElementById('curso');
    if (!select) return;
    const opciones = Array.isArray(state.cursosSolicitables) ? state.cursosSolicitables : [];

    if (!opciones.length) {
        select.innerHTML = '<option value="" disabled selected>Sin cursos disponibles</option>';
        return;
    }

    const opcionesHtml = opciones
        .filter(c => c && c.id)
        .map(c => {
            const carrera = c.carrera ? ` - ${c.carrera}` : '';
            const ciclo = c.ciclo ? ` | Ciclo ${c.ciclo}` : '';
            const modalidad = c.modalidad ? ` | ${c.modalidad}` : '';
            const etiqueta = `${c.nombre || 'Curso'} (${c.codigo || 'S/C'})${carrera}${ciclo}${modalidad}`;
            const disabled = c.pendiente ? 'disabled' : '';
            const etiquetaFinal = c.pendiente ? `${etiqueta} - pendiente` : etiqueta;
            return `<option value="${c.id}" ${disabled}>${etiquetaFinal}</option>`;
        })
        .join('');

    select.innerHTML = '<option value="" disabled selected>Seleccione un curso</option>' + opcionesHtml;
}

if (form) {
    form.addEventListener('submit', registrarSolicitudSeccion);
}
btnLimpiarSolicitud?.addEventListener('click', limpiarFormularioSolicitud);

/* ============================================================
   INICIALIZACIÓN
============================================================ */
async function refrescarEstadoMatricula() {
    const [resumen, cursos] = await Promise.all([
        fetchJson('/alumno/matricula/actual', 'No se pudo obtener la matrícula actual'),
        fetchJson('/alumno/matricula/cursos', 'No se pudieron cargar los cursos')
    ]);

    state.resumen = resumen;
    state.cursosMatriculados = Array.isArray(cursos) ? cursos : [];
    actualizarResumenMatricula();
    actualizarFichaAlumno();
    renderCursosInscritos();
    await actualizarHorario();
}

async function cargarDatosIniciales() {
    try {
        const [perfil, resumen, cursos, horario, pagos] = await Promise.all([
            fetchJson('/alumno/info', 'No se pudo obtener la información del alumno'),
            fetchJson('/alumno/matricula/actual', 'No se pudo obtener la matrícula actual'),
            fetchJson('/alumno/matricula/cursos', 'No se pudieron cargar los cursos'),
            fetchJson('/alumno/horario', 'No se pudo cargar el horario'),
            fetchJson('/alumno/pagos', 'No se pudieron cargar las pensiones')
        ]);

        state.perfil = perfil;
        state.resumen = resumen;
        state.cursosMatriculados = Array.isArray(cursos) ? cursos : [];
        state.horario = Array.isArray(horario) ? horario : [];
        state.pagos = Array.isArray(pagos) ? pagos : [];

        actualizarResumenMatricula();
        actualizarDatosPensiones();
        actualizarFichaAlumno();
        renderCursosInscritos();
        renderHorarioTablas();
        await cargarSolicitudesAlumno();
        await cargarCursosSolicitables();
        poblarCursosSolicitud();
        await cargarPeriodos();
        await cargarCursosDisponibles();
    } catch (err) {
        // Errores ya manejados en fetchJson
    }
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDatosIniciales();
    document.getElementById('btn-filtrar')?.addEventListener('click', () => cargarCursosDisponibles());

    const formFiltros = document.getElementById('form-filtros');
    formFiltros?.addEventListener('submit', (e) => {
        e.preventDefault();
        cargarCursosDisponibles();
    });

    document.getElementById('btn-limpiar-filtros')?.addEventListener('click', () => {
        document.getElementById('filtro-texto').value = '';
        document.getElementById('filtro-ciclo').value = '';
        document.getElementById('filtro-modalidad').value = '';
        cargarCursosDisponibles();
    });

    document.getElementById('btn-guardar-borrador')?.addEventListener('click', guardarHorario);
    const btnPdf = document.getElementById('btn-descargar-resumen');
    if (btnPdf) {
        btnPdf.disabled = false;
        btnPdf.title = '';
        btnPdf.addEventListener('click', descargarHorarioPdf);
    }
});
