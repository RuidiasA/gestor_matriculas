const state = {
    perfil: null,
    resumen: null,
    cursosMatriculados: [],
    horario: [],
    pagos: [],
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
        if (!resp.ok) throw new Error(errorMessage || 'Error de servidor');
        const contentType = resp.headers.get('content-type') || '';
        if (contentType.includes('application/json')) {
            return await resp.json();
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

function mostrarToast(mensaje, tipo = 'error') {
    const toast = document.createElement('div');
    toast.className = `toast-banner toast-${tipo}`;
    toast.textContent = mensaje || '';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

function mostrarMensajeError(msg) {
    mostrarToast(msg || 'Ocurrió un error inesperado', 'error');
}

function mostrarMensajeExito(msg) {
    mostrarToast(msg || 'Operación exitosa', 'success');
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

/* ============================================================
   RENDER DE CURSOS Y SECCIONES
============================================================ */
async function cargarCursosDisponibles() {
    const url = `/alumno/cursos/disponibles${construirQueryCursos()}`;
    state.cursosDisponibles = await fetchJson(url, 'No se pudo cargar el catálogo de cursos') || [];
    renderizarCursosTabla();
}

async function cargarDetalleCurso(cursoId) {
    if (!cursoId) return [];
    if (!state.seccionesPorCurso[cursoId]) {
        const detalle = await fetchJson(`/alumno/cursos/${cursoId}/detalle`, 'No se pudo cargar el detalle del curso');
        state.seccionesPorCurso[cursoId] = detalle;
    }
    const detalle = state.seccionesPorCurso[cursoId];
    const secciones = detalle?.secciones || detalle?.seccion || detalle || [];
    return Array.isArray(secciones) ? secciones : [];
}

function renderizarCursosTabla() {
    const tbody = document.getElementById('tabla-cursos-body');
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
        const cursoId = curso.id ?? curso.cursoId ?? curso.seccionId ?? curso.codigoCurso;
        tr.dataset.cursoId = cursoId || '';

        const datos = [
            curso.codigoCurso || curso.codigo || '—',
            curso.nombreCurso || curso.nombre || '—',
            curso.ciclo || curso.cicloAcademico || '—',
            curso.creditos ?? '—',
            curso.modalidad || '—',
            curso.cuposDisponibles ?? curso.cupos ?? '—',
            curso.docente || 'Por asignar'
        ];

        datos.forEach(valor => {
            const td = document.createElement('td');
            td.textContent = valor;
            tr.appendChild(td);
        });

        const acciones = document.createElement('td');
        acciones.className = 'col-acciones';
        const verBtn = document.createElement('button');
        verBtn.type = 'button';
        verBtn.className = 'btn-outline btn-sm';
        verBtn.textContent = 'Ver secciones';
        verBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            mostrarSecciones(curso, tr);
        });
        acciones.appendChild(verBtn);
        tr.appendChild(acciones);

        tr.addEventListener('click', () => mostrarSecciones(curso, tr));
        tbody.appendChild(tr);
    });
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

    const secciones = await cargarDetalleCurso(cursoId);
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
    cell.colSpan = 8;

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
                <th>Horario</th>
                <th>Modalidad</th>
                <th>Turno</th>
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
            const seccionId = seccion.id ?? seccion.seccionId ?? seccion.codigoSeccion;
            const matriculado = state.cursosMatriculados.some(c => String(c.seccionId ?? c.id ?? c.codigoSeccion) === String(seccionId));
            const tr = document.createElement('tr');
            tr.dataset.seccionId = seccionId;

            tr.innerHTML = `
                <td>${seccion.codigoSeccion || seccion.codigo || seccionId || '—'}</td>
                <td>${seccion.docente || 'Por asignar'}</td>
                <td>${formatearHorario(seccion.horario || seccion.horarios)}</td>
                <td>${seccion.modalidad || cursoPadre.modalidad || '—'}</td>
                <td>${seccion.turno || cursoPadre.turno || '—'}</td>
                <td>${seccion.cuposDisponibles ?? seccion.cupos ?? '—'}</td>
                <td class="acciones-col"></td>
            `;

            const acciones = tr.querySelector('.acciones-col');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.textContent = matriculado ? 'Retirar' : 'Matricular';
            btn.className = matriculado ? 'btn-secondary btn-sm' : 'btn-primary btn-sm';
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                seleccionarSeccion(seccionId, tr);
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
        const validacion = await fetchJson(`/alumno/secciones/${seccionId}/validar`, 'No se pudo validar la sección');
        if (validacion && validacion.valido === false) {
            mostrarMensajeError(validacion.mensaje || 'La sección no está disponible');
            return;
        }

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
        const resp = await fetch(`/alumno/matricula/${seccionId}`, { method: 'DELETE' });
        if (!resp.ok) throw new Error('No se pudo retirar la sección');
        await refrescarEstadoMatricula();
        await cargarCursosDisponibles();
        mostrarMensajeExito('Se retiró la sección de tu matrícula');
    } catch (err) {
        mostrarMensajeError(err.message || 'No se pudo retirar la sección');
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
            tablaBody.innerHTML = '<tr><td colspan="5" class="estado-vacio">Aún no tienes cursos matriculados.</td></tr>';
        } else {
            state.cursosMatriculados.forEach(curso => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${curso.codigoSeccion || curso.codigoCurso || '-'}</td>
                    <td>${curso.nombreCurso || '-'}</td>
                    <td>${curso.creditos ?? '-'}</td>
                    <td>${curso.docente || 'Por asignar'}</td>
                    <td>${curso.modalidad || '-'}</td>
                `;
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
        await cargarCursosDisponibles();
    } catch (err) {
        // Errores ya manejados en fetchJson
    }
}

document.addEventListener('DOMContentLoaded', () => {
    cargarDatosIniciales();
    document.getElementById('btn-filtrar')?.addEventListener('click', cargarCursosDisponibles);
    document.getElementById('btn-matricular')?.addEventListener('click', () => matricular(state.seccionSeleccionada));
    document.getElementById('btn-retirar')?.addEventListener('click', () => retirar(state.seccionSeleccionada));

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
});
