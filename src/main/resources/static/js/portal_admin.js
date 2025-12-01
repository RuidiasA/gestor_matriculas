// Herramientas comunes para todos los módulos
function createTools() {
    const toastContainer = ensureToastContainer();

    function ensureToastContainer() {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.textContent = message;
        toastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    }

    function showStatus(element, message, isError = false) {
        if (!element) return;
        element.textContent = message;
        element.hidden = false;
        element.style.color = isError ? '#c0392b' : 'var(--color-primario)';
    }

    function clearStatus(element) {
        if (!element) return;
        element.hidden = true;
        element.textContent = '';
    }

    function fillSelect(select, items, placeholder, valueGetter, labelGetter) {
        if (!select) return;
        select.innerHTML = '';
        const baseOption = document.createElement('option');
        baseOption.value = '';
        baseOption.textContent = placeholder;
        select.appendChild(baseOption);
        (items || []).forEach(item => {
            const option = document.createElement('option');
            option.value = valueGetter(item);
            option.textContent = labelGetter(item);
            select.appendChild(option);
        });
    }

    function renderEmptyRow(tbody, columns, message) {
        if (!tbody) return;
        tbody.innerHTML = `<tr><td colspan="${columns}" class="muted">${message}</td></tr>`;
    }

    function markSelectedRow(tbody, row) {
        if (!tbody) return;
        tbody.querySelectorAll('tr').forEach(r => r.classList.remove('selected'));
        row.classList.add('selected');
    }

    return { showToast, showStatus, clearStatus, fillSelect, renderEmptyRow, markSelectedRow };
}

// Navegación principal del panel
function setupNavigation(onSeccionesFocus) {
    const navLinks = document.querySelectorAll('.admin-nav a');
    const sections = document.querySelectorAll('.admin-section');

    navLinks.forEach(link => {
        link.addEventListener('click', evt => {
            evt.preventDefault();
            const target = link.getAttribute('data-section');
            if (!target) return;

            sections.forEach(sec => sec.classList.remove('active'));
            document.getElementById(target)?.classList.add('active');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            if (target === 'secciones' && typeof onSeccionesFocus === 'function') {
                onSeccionesFocus();
            }
        });
    });
}

// =====================
// MÓDULO ALUMNOS
// =====================
function createAlumnosModule(tools) {
    const tablaAlumnos = document.querySelector('#tablaAlumnos tbody');
    const formBusqueda = document.getElementById('formBusquedaAlumnos');
    const filtroAlumno = document.getElementById('filtroAlumno');
    const btnLimpiarBusqueda = document.getElementById('btnLimpiarBusqueda');
    const btnRefrescar = document.getElementById('btnRefrescarAlumnos');
    const estadoBusqueda = document.getElementById('estadoBusqueda');

    const fichaIds = {
        nombre: document.getElementById('nombreAlumno'),
        codigo: document.getElementById('codigoAlumno'),
        carrera: document.getElementById('carreraAlumno'),
        ciclo: document.getElementById('cicloAlumno'),
        ingreso: document.getElementById('ingresoAlumno'),
        correoInst: document.getElementById('correoInstitucionalAlumno'),
        correoPer: document.getElementById('correoPersonalAlumno'),
        telefono: document.getElementById('telefonoAlumno'),
        estado: document.getElementById('estadoAlumno'),
    };

    const contactoForm = document.getElementById('formContactoAlumno');
    const correoPersonalInput = document.getElementById('correoPersonalInput');
    const telefonoInput = document.getElementById('telefonoInput');
    const direccionInput = document.getElementById('direccionInput');
    const btnEditarContacto = document.getElementById('btnEditarContacto');
    const btnGuardarContacto = document.getElementById('btnGuardarContacto');
    const estadoContacto = document.getElementById('estadoContacto');

    const selectorCiclo = document.getElementById('selectorCiclo');
    const tablaCursos = document.querySelector('#tablaCursosMatriculados tbody');
    const totalCursos = document.getElementById('totalCursos');
    const totalCreditos = document.getElementById('totalCreditos');
    const totalHoras = document.getElementById('totalHoras');
    const montoEstimado = document.getElementById('montoEstimado');
    const contenedorHistorial = document.getElementById('contenedorHistorial');
    const subtituloHistorial = document.getElementById('subtituloHistorial');

    let alumnoSeleccionado = null;
    let historialMatriculasCache = [];
    let cursosPorCicloCache = {};

    function init() {
        if (formBusqueda) {
            formBusqueda.addEventListener('submit', e => {
                e.preventDefault();
                buscarAlumnos();
            });
        }

        if (btnLimpiarBusqueda) {
            btnLimpiarBusqueda.addEventListener('click', () => {
                filtroAlumno.value = '';
                limpiarSeleccion();
                buscarAlumnos();
            });
        }

        if (btnRefrescar) {
            btnRefrescar.addEventListener('click', buscarAlumnos);
        }

        if (selectorCiclo) {
            selectorCiclo.addEventListener('change', () => {
                if (alumnoSeleccionado) {
                    cargarDetalleAcademico(alumnoSeleccionado.id, selectorCiclo.value);
                }
            });
        }

        if (btnEditarContacto) {
            btnEditarContacto.addEventListener('click', () => habilitarEdicion(true));
        }

        if (contactoForm) {
            contactoForm.addEventListener('submit', e => {
                e.preventDefault();
                if (!alumnoSeleccionado) {
                    tools.showToast('Selecciona un alumno primero', 'info');
                    return;
                }
                if (validarContacto()) {
                    actualizarContacto(alumnoSeleccionado.id);
                }
            });
        }

        buscarAlumnos();
    }

    async function buscarAlumnos() {
        tools.showStatus(estadoBusqueda, 'Cargando alumnos...', false);
        const filtro = filtroAlumno?.value?.trim() || '';
        try {
            const response = await fetch(`/admin/alumnos/buscar?filtro=${encodeURIComponent(filtro)}`);
            if (!response.ok) throw new Error('Error al buscar alumnos');
            const alumnos = await response.json();
            renderizarAlumnos(alumnos);
            if (!alumnos.length) {
                tools.showStatus(estadoBusqueda, 'Alumno no encontrado', true);
            } else {
                estadoBusqueda.hidden = true;
            }
        } catch (err) {
            renderizarAlumnos([]);
            tools.showStatus(estadoBusqueda, err.message || 'No se pudo cargar la lista', true);
            tools.showToast('No se pudo obtener la lista de alumnos', 'error');
        }
    }

    function renderizarAlumnos(alumnos) {
        tablaAlumnos.innerHTML = '';
        if (!alumnos.length) {
            tools.renderEmptyRow(tablaAlumnos, 9, 'Sin resultados');
            return;
        }

        alumnos.forEach(alumno => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${alumno.codigo || '-'}</td>
                <td>${alumno.nombreCompleto || '-'}</td>
                <td>${alumno.dni || '-'}</td>
                <td>${alumno.correoInstitucional || '-'}</td>
                <td>${alumno.correoPersonal || '-'}</td>
                <td>${alumno.telefono || '-'}</td>
                <td>${alumno.anioIngreso || '-'}</td>
                <td>${alumno.cicloActual || '-'}</td>
                <td>${alumno.ordenMerito ?? '-'}</td>
            `;
            tr.addEventListener('click', () => {
                tools.markSelectedRow(tablaAlumnos, tr);
                historialMatriculasCache = [];
                cursosPorCicloCache = {};
                renderizarHistorial([], null);
                alumnoSeleccionado = alumno;
                cargarFicha(alumno);
                cargarPeriodos(alumno);
                if (selectorCiclo.value) cargarDetalleAcademico(alumno.id, selectorCiclo.value);
            });
            tablaAlumnos.appendChild(tr);
        });
    }

    function cargarFicha(alumno) {
        fichaIds.nombre.textContent = alumno.nombreCompleto || '-';
        fichaIds.codigo.textContent = alumno.codigo || '-';
        fichaIds.carrera.textContent = alumno.carrera || '-';
        fichaIds.ciclo.textContent = alumno.cicloActual || '-';
        fichaIds.ingreso.textContent = alumno.anioIngreso || '-';
        fichaIds.correoInst.textContent = alumno.correoInstitucional || '-';
        fichaIds.correoPer.textContent = alumno.correoPersonal || '-';
        fichaIds.telefono.textContent = alumno.telefono || '-';
        fichaIds.estado.textContent = alumno.estado || '-';
        fichaIds.estado.className = 'badge';
        fichaIds.estado.classList.add(alumno.estado === 'Activo' ? 'badge--success' : 'badge--info');

        correoPersonalInput.value = alumno.correoPersonal || '';
        telefonoInput.value = alumno.telefono || '';
        direccionInput.value = alumno.direccion || '';
        habilitarEdicion(false);
        limpiarEstadoContacto();
    }

    function cargarPeriodos(alumno) {
        selectorCiclo.innerHTML = '';
        const periodos = alumno.periodos || [];
        if (!periodos.length) {
            selectorCiclo.innerHTML = '<option value="">Sin ciclos</option>';
            return;
        }
        periodos.forEach(p => {
            const opt = document.createElement('option');
            opt.value = p;
            opt.textContent = p;
            selectorCiclo.appendChild(opt);
        });
    }

    async function cargarDetalleAcademico(idAlumno, ciclo) {
        if (!ciclo) return;
        mostrarSkeletonCursos('Cargando cursos...');
        try {
            const [cursosRes, resumenRes, historialRes] = await Promise.all([
                fetch(`/admin/alumnos/${idAlumno}/matriculas?ciclo=${encodeURIComponent(ciclo)}`),
                fetch(`/admin/alumnos/${idAlumno}/resumen?ciclo=${encodeURIComponent(ciclo)}`),
                fetch(`/admin/alumnos/${idAlumno}/historial`)
            ]);

            if (!cursosRes.ok || !resumenRes.ok || !historialRes.ok) {
                throw new Error('No se pudo obtener el detalle académico');
            }

            const cursos = await cursosRes.json();
            const resumen = await resumenRes.json();
            const historial = await historialRes.json();

            historialMatriculasCache = historial;
            renderizarCursos(cursos);
            renderizarResumen(resumen);
            renderizarHistorial(historial, idAlumno);
        } catch (err) {
            mostrarSkeletonCursos(err.message || 'Sin información');
            tools.showToast('No se pudo cargar el detalle académico', 'error');
        }
    }

    function renderizarCursos(cursos) {
        tablaCursos.innerHTML = '';
        if (!cursos || !cursos.length) {
            tablaCursos.innerHTML = '<tr class="skeleton-row"><td colspan="7">Sin cursos para este ciclo</td></tr>';
            return;
        }
        cursos.forEach(curso => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${curso.codigoSeccion || '-'}</td>
                <td>${curso.nombreCurso || '-'}</td>
                <td>${curso.docente || '-'}</td>
                <td>${curso.creditos ?? '-'}</td>
                <td>${curso.horasSemanales ?? '-'}</td>
                <td>${curso.modalidad || '-'}</td>
                <td>${curso.aula || '-'}</td>
            `;
            tablaCursos.appendChild(tr);
        });
    }

    function renderizarResumen(resumen) {
        totalCursos.textContent = resumen.totalCursos ?? '-';
        totalCreditos.textContent = resumen.totalCreditos ?? '-';
        totalHoras.textContent = resumen.totalHoras ?? '-';
        montoEstimado.textContent = resumen.montoTotal ? `S/ ${resumen.montoTotal}` : '-';
    }

    function renderizarHistorial(historial, alumnoId) {
        if (!historial || !historial.length) {
            contenedorHistorial.innerHTML = '<p class="muted">Sin historial</p>';
            subtituloHistorial.textContent = 'Historial de matrícula';
            return;
        }

        contenedorHistorial.innerHTML = historial.map(h => `
            <article class="historial-card">
                <header>
                    <div>
                        <p class="label">Ciclo</p>
                        <p><strong>${h.ciclo || '-'}</strong></p>
                    </div>
                    <span class="badge">${h.estado || '-'}</span>
                </header>
                <div class="historial-content">
                    ${crearTablaCursosHTML(h.cursos)}
                </div>
            </article>
        `).join('');
        subtituloHistorial.textContent = `Historial de matrícula (${historial.length})`;
        historialMatriculasCache = historial;
        cursosPorCicloCache[alumnoId] = historial;
    }

    function crearTablaCursosHTML(cursos) {
        if (!cursos || !cursos.length) {
            return '<p class="muted">Sin cursos para este ciclo</p>';
        }

        const rows = cursos.map(c => `
        <tr>
            <td>${c.codigoSeccion || '-'}</td>
            <td>${c.nombreCurso || '-'}</td>
            <td>${c.docente || '-'}</td>
            <td>${c.creditos ?? '-'}</td>
            <td>${c.horasSemanales ?? '-'}</td>
            <td>${c.modalidad || '-'}</td>
            <td>${c.aula || '-'}</td>
        </tr>
    `).join('');

        return `
        <div class="historial-table-wrapper">
            <table class="historial-table">
                <thead>
                    <tr>
                        <th>Código sección</th>
                        <th>Curso</th>
                        <th>Docente</th>
                        <th>Créditos</th>
                        <th>Horas</th>
                        <th>Modalidad</th>
                        <th>Aula</th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>
    `;
    }

    function mostrarSkeletonCursos(texto) {
        tablaCursos.innerHTML = `<tr class="skeleton-row"><td colspan="7">${texto}</td></tr>`;
    }

    function habilitarEdicion(valor) {
        correoPersonalInput.disabled = !valor;
        telefonoInput.disabled = !valor;
        direccionInput.disabled = !valor;
        btnGuardarContacto.disabled = !valor;
    }

    function limpiarSeleccion() {
        alumnoSeleccionado = null;
        historialMatriculasCache = [];
        cursosPorCicloCache = {};
        tablaAlumnos.querySelectorAll('tr').forEach(fila => fila.classList.remove('selected'));
        cargarFicha({});
        selectorCiclo.innerHTML = '';
        renderizarCursos([]);
        renderizarResumen({});
        renderizarHistorial([], null);
    }

    function validarContacto() {
        limpiarEstadoContacto();
        const correo = correoPersonalInput.value.trim();
        const telefono = telefonoInput.value.trim();
        const direccion = direccionInput.value.trim();

        const errores = [];
        if (correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
            errores.push('Correo personal inválido');
        }
        if (telefono && !/^\d{9}$/.test(telefono)) {
            errores.push('El teléfono debe tener 9 dígitos');
        }
        if (errores.length) {
            tools.showStatus(estadoContacto, errores.join('. '), true);
            return false;
        }
        if (!direccion && !telefono && !correo) {
            tools.showStatus(estadoContacto, 'Ingresa al menos un dato de contacto', true);
            return false;
        }
        return true;
    }

    async function actualizarContacto(idAlumno) {
        try {
            const body = {
                correoPersonal: correoPersonalInput.value.trim(),
                telefono: telefonoInput.value.trim(),
                direccion: direccionInput.value.trim()
            };
            const response = await fetch(`/admin/alumnos/${idAlumno}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (!response.ok) throw new Error('No se pudo actualizar, revise los datos');
            tools.showToast('Datos de contacto actualizados', 'success');
            habilitarEdicion(false);
            fichaIds.correoPer.textContent = body.correoPersonal || '-';
            fichaIds.telefono.textContent = body.telefono || '-';
            tools.showStatus(estadoContacto, 'Guardado correctamente', false);
        } catch (err) {
            tools.showStatus(estadoContacto, err.message, true);
            tools.showToast(err.message, 'error');
        }
    }

    function limpiarEstadoContacto() {
        tools.clearStatus(estadoContacto);
    }

    return { init };
}

// =====================
// MÓDULO DOCENTES
// =====================
function createDocentesModule(tools) {
    const tablaDocentes = document.querySelector('#tablaDocentes tbody');
    const formBusquedaDocentes = document.getElementById('formBusquedaDocentes');
    const filtroDocente = document.getElementById('filtroDocente');
    const filtroCursoDictable = document.getElementById('filtroCursoDictable');
    const filtroEstadoDocente = document.getElementById('filtroEstadoDocente');
    const estadoBusquedaDocentes = document.getElementById('estadoBusquedaDocentes');
    const selectCursoDictable = document.getElementById('selectCursoDictable');
    const btnAgregarCursoDictable = document.getElementById('btnAgregarCursoDictable');
    const tablaCursosDictables = document.querySelector('#tablaCursosDictables tbody');
    const tablaSeccionesDocente = document.querySelector('#tablaSeccionesDocente tbody');
    const resumenSeccionesDocente = document.getElementById('resumenSeccionesDocente');
    const tablaHistorialDocente = document.querySelector('#tablaHistorialDocente tbody');
    const btnEditarDocente = document.getElementById('btnEditarDocente');
    const btnEditarContactoDocente = document.getElementById('btnEditarContactoDocente');
    const formDatosDocente = document.getElementById('formDatosDocente');
    const formContactoDocente = document.getElementById('formContactoDocente');
    const estadoDatosDocente = document.getElementById('estadoDatosDocente');
    const estadoContactoDocente = document.getElementById('estadoContactoDocente');
    const btnCancelarDatosDocente = document.getElementById('btnCancelarDatosDocente');
    const btnCancelarContactoDocente = document.getElementById('btnCancelarContactoDocente');
    const btnLimpiarDocentes = document.getElementById('btnLimpiarDocentes');

    const inputsDocente = {
        codigo: document.getElementById('docCodigo'),
        estado: document.getElementById('docEstado'),
        apellidos: document.getElementById('docApellidos'),
        nombres: document.getElementById('docNombres'),
        dni: document.getElementById('docDni'),
        especialidad: document.getElementById('docEspecialidad'),
        correoInst: document.getElementById('docCorreoInst'),
        correoPer: document.getElementById('docCorreoPer'),
        telefono: document.getElementById('docTelefono'),
        direccion: document.getElementById('docDireccion'),
    };

    const formInputsDocente = {
        apellidos: document.getElementById('inputDocApellidos'),
        nombres: document.getElementById('inputDocNombres'),
        dni: document.getElementById('inputDocDni'),
        especialidad: document.getElementById('inputDocEspecialidad'),
        correoInst: document.getElementById('inputDocCorreoInst'),
        estado: document.getElementById('inputDocEstado'),
    };

    const formContactoInputs = {
        correoInst: document.getElementById('inputDocCorreoInstContacto'),
        correoPer: document.getElementById('inputDocCorreoPer'),
        telefono: document.getElementById('inputDocTelefono'),
        direccion: document.getElementById('inputDocDireccion'),
    };

    let docenteSeleccionado = null;

    function init() {
        cargarCatalogoCursos();
        buscarDocentes();

        if (formBusquedaDocentes) {
            formBusquedaDocentes.addEventListener('submit', e => {
                e.preventDefault();
                buscarDocentes();
            });
        }

        if (btnAgregarCursoDictable) {
            btnAgregarCursoDictable.addEventListener('click', agregarCursoDictable);
        }

        if (btnEditarDocente) {
            btnEditarDocente.addEventListener('click', () => {
                if (!docenteSeleccionado) return;
                mostrarFormularioDatos(true);
            });
        }

        if (btnEditarContactoDocente) {
            btnEditarContactoDocente.addEventListener('click', () => {
                if (!docenteSeleccionado) return;
                mostrarFormularioContacto(true);
            });
        }

        if (btnCancelarDatosDocente) {
            btnCancelarDatosDocente.addEventListener('click', () => mostrarFormularioDatos(false));
        }

        if (btnCancelarContactoDocente) {
            btnCancelarContactoDocente.addEventListener('click', () => mostrarFormularioContacto(false));
        }

        if (formDatosDocente) {
            formDatosDocente.addEventListener('submit', e => {
                e.preventDefault();
                if (!docenteSeleccionado) return;
                guardarDatosDocente();
            });
        }

        if (formContactoDocente) {
            formContactoDocente.addEventListener('submit', e => {
                e.preventDefault();
                if (!docenteSeleccionado) return;
                guardarContactoDocente();
            });
        }

        if (btnLimpiarDocentes) {
            btnLimpiarDocentes.addEventListener('click', limpiarBusquedaDocentes);
        }
    }

    async function cargarCatalogoCursos() {
        try {
            const resp = await fetch('/admin/docentes/cursos');
            if (!resp.ok) throw new Error();
            const cursos = await resp.json();
            [selectCursoDictable, filtroCursoDictable].forEach(select => {
                if (!select) return;
                select.innerHTML = '';
                const baseOption = document.createElement('option');
                baseOption.value = '';
                baseOption.textContent = select === selectCursoDictable ? 'Selecciona curso' : 'Todos';
                select.appendChild(baseOption);
                cursos.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.idCurso;
                    opt.textContent = `${c.codigo} - ${c.nombre}`;
                    select.appendChild(opt);
                });
            });
        } catch (e) {
            tools.showToast('No se pudo cargar el catálogo de cursos', 'error');
        }
    }

    function limpiarBusquedaDocentes() {
        if (filtroDocente) filtroDocente.value = '';
        if (filtroCursoDictable) filtroCursoDictable.value = '';
        if (filtroEstadoDocente) filtroEstadoDocente.value = '';
        limpiarSeleccionDocente();
        buscarDocentes();
    }

    async function buscarDocentes() {
        if (!tablaDocentes) return;
        tools.showStatus(estadoBusquedaDocentes, 'Buscando docentes...', false);
        const params = new URLSearchParams();
        params.append('filtro', filtroDocente?.value?.trim() || '');
        if (filtroCursoDictable?.value) params.append('cursoId', filtroCursoDictable.value);
        if (filtroEstadoDocente?.value) params.append('estado', filtroEstadoDocente.value);
        try {
            const resp = await fetch(`/admin/docentes/buscar?${params.toString()}`);
            if (!resp.ok) throw new Error('No se pudo buscar docentes');
            const docentes = await resp.json();
            renderizarDocentes(docentes);
            if (!docentes.length) tools.showStatus(estadoBusquedaDocentes, 'Sin resultados', true);
            else estadoBusquedaDocentes.hidden = true;
        } catch (err) {
            renderizarDocentes([]);
            tools.showStatus(estadoBusquedaDocentes, err.message, true);
        }
    }

    function renderizarDocentes(docentes) {
        tablaDocentes.innerHTML = '';
        if (!docentes.length) {
            tools.renderEmptyRow(tablaDocentes, 4, 'Sin resultados');
            return;
        }
        docentes.forEach(doc => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${doc.codigo || '-'}</td>
                <td>${doc.nombreCompleto || '-'}</td>
                <td>${doc.dni || '-'}</td>
                <td>${doc.estado || '-'}</td>
            `;
            tr.addEventListener('click', () => {
                tools.markSelectedRow(tablaDocentes, tr);
                cargarDetalleDocente(doc.id);
            });
            tablaDocentes.appendChild(tr);
        });
    }

    async function cargarDetalleDocente(id) {
        limpiarSeleccionDocente();
        try {
            const resp = await fetch(`/admin/docentes/${id}`);
            if (!resp.ok) throw new Error('No se pudo obtener el detalle del docente');
            const detalle = await resp.json();
            docenteSeleccionado = detalle;
            renderizarFichaDocente(detalle);
            renderizarCursosDictables(detalle.cursosDictables || []);
            renderizarSeccionesDocente(detalle);
            renderizarHistorialDocente(detalle.historial || []);
        } catch (err) {
            tools.showToast(err.message, 'error');
        }
    }

    function renderizarFichaDocente(detalle) {
        inputsDocente.codigo.textContent = detalle.codigo || '-';
        inputsDocente.estado.textContent = detalle.estado || '-';
        inputsDocente.estado.className = 'badge';
        inputsDocente.estado.classList.add(detalle.estado === 'ACTIVO' || detalle.estado === 'Activo' ? 'badge--success' : 'badge--info');
        inputsDocente.apellidos.textContent = detalle.apellidos || '-';
        inputsDocente.nombres.textContent = detalle.nombres || '-';
        inputsDocente.dni.textContent = detalle.dni || '-';
        inputsDocente.especialidad.textContent = detalle.especialidad || '-';
        inputsDocente.correoInst.textContent = detalle.correoInstitucional || '-';
        inputsDocente.correoPer.textContent = detalle.correoPersonal || '-';
        inputsDocente.telefono.textContent = detalle.telefono || '-';
        inputsDocente.direccion.textContent = detalle.direccion || '-';

        formInputsDocente.apellidos.value = detalle.apellidos || '';
        formInputsDocente.nombres.value = detalle.nombres || '';
        formInputsDocente.dni.value = detalle.dni || '';
        formInputsDocente.especialidad.value = detalle.especialidad || '';
        formInputsDocente.correoInst.value = detalle.correoInstitucional || '';
        formInputsDocente.estado.value = detalle.estado || 'ACTIVO';

        formContactoInputs.correoInst.value = detalle.correoInstitucional || '';
        formContactoInputs.correoPer.value = detalle.correoPersonal || '';
        formContactoInputs.telefono.value = detalle.telefono || '';
        formContactoInputs.direccion.value = detalle.direccion || '';

        mostrarFormularioDatos(false);
        mostrarFormularioContacto(false);
    }

    function renderizarCursosDictables(cursos) {
        tablaCursosDictables.innerHTML = '';
        if (!cursos.length) {
            tools.renderEmptyRow(tablaCursosDictables, 5, 'Sin cursos asignados');
            return;
        }
        cursos.forEach(curso => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${curso.codigo || '-'}</td>
                <td>${curso.nombre || '-'}</td>
                <td>${curso.creditos ?? '-'}</td>
                <td>${curso.horasSemanales ?? '-'}</td>
                <td>${curso.ciclo ?? '-'}</td>
            `;
            tr.addEventListener('click', () => eliminarCursoDictable(curso.idCurso));
            tablaCursosDictables.appendChild(tr);
        });
    }

    function renderizarSeccionesDocente(detalle) {
        const secciones = detalle.seccionesActuales || [];
        tablaSeccionesDocente.innerHTML = '';
        if (!secciones.length) {
            tools.renderEmptyRow(tablaSeccionesDocente, 5, 'Sin secciones');
            resumenSeccionesDocente.textContent = '0 secciones asignadas';
            return;
        }

        secciones.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${s.curso || '-'}</td>
                <td>${s.codigo || '-'}</td>
                <td>${s.modalidad || '-'}</td>
                <td>${s.horario || '-'}</td>
                <td>${s.aula || '-'}</td>
            `;
            tablaSeccionesDocente.appendChild(tr);
        });

        resumenSeccionesDocente.textContent = `${detalle.totalSeccionesActuales || 0} secciones`;
    }

    function renderizarHistorialDocente(historial) {
        tablaHistorialDocente.innerHTML = '';
        if (!historial.length) {
            tools.renderEmptyRow(tablaHistorialDocente, 5, 'Sin historial');
            return;
        }
        historial.forEach(h => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${h.periodo || '-'}</td>
                <td>${h.curso || '-'}</td>
                <td>${h.seccion || '-'}</td>
                <td>${h.modalidad || '-'}</td>
                <td>${h.horario || '-'}</td>
            `;
            tablaHistorialDocente.appendChild(tr);
        });
    }

    async function agregarCursoDictable() {
        if (!docenteSeleccionado) {
            tools.showToast('Selecciona un docente', 'info');
            return;
        }
        const cursoId = selectCursoDictable.value;
        if (!cursoId) return;
        try {
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/cursos?cursoId=${cursoId}`, { method: 'POST' });
            if (!resp.ok) throw new Error('No se pudo agregar el curso');
            tools.showToast('Curso añadido', 'success');
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showToast(err.message, 'error');
        }
    }

    async function eliminarCursoDictable(cursoId) {
        if (!docenteSeleccionado) return;
        try {
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/cursos/${cursoId}`, { method: 'DELETE' });
            if (!resp.ok) throw new Error('No se pudo eliminar el curso');
            tools.showToast('Curso removido', 'info');
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showToast(err.message, 'error');
        }
    }

    function mostrarFormularioDatos(valor) {
        if (formDatosDocente) formDatosDocente.hidden = !valor;
        tools.clearStatus(estadoDatosDocente);
    }

    function mostrarFormularioContacto(valor) {
        if (formContactoDocente) formContactoDocente.hidden = !valor;
        tools.clearStatus(estadoContactoDocente);
    }

    function limpiarSeleccionDocente() {
        docenteSeleccionado = null;
        [inputsDocente.codigo, inputsDocente.apellidos, inputsDocente.nombres, inputsDocente.dni, inputsDocente.especialidad,
            inputsDocente.correoInst, inputsDocente.correoPer, inputsDocente.telefono, inputsDocente.direccion].forEach(el => {
            if (el) el.textContent = '-';
        });
        if (inputsDocente.estado) inputsDocente.estado.textContent = '-';
        renderizarCursosDictables([]);
        renderizarSeccionesDocente({ seccionesActuales: [] });
        renderizarHistorialDocente([]);
        mostrarFormularioContacto(false);
        mostrarFormularioDatos(false);
    }

    async function guardarDatosDocente() {
        try {
            const body = {
                apellidos: formInputsDocente.apellidos.value.trim(),
                nombres: formInputsDocente.nombres.value.trim(),
                dni: formInputsDocente.dni.value.trim(),
                especialidad: formInputsDocente.especialidad.value.trim(),
                correoInstitucional: formInputsDocente.correoInst.value.trim(),
                estado: formInputsDocente.estado.value
            };
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (!resp.ok) throw new Error('No se pudo actualizar los datos');
            tools.showToast('Datos guardados', 'success');
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showStatus(estadoDatosDocente, err.message, true);
        }
    }

    async function guardarContactoDocente() {
        try {
            const body = {
                correoPersonal: formContactoInputs.correoPer.value.trim(),
                telefono: formContactoInputs.telefono.value.trim(),
                direccion: formContactoInputs.direccion.value.trim(),
                correoInstitucional: formContactoInputs.correoInst.value.trim(),
            };
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/contacto`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            if (!resp.ok) throw new Error('No se pudo actualizar el contacto');
            tools.showToast('Contacto actualizado', 'success');
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showStatus(estadoContactoDocente, err.message, true);
        }
    }

    return { init };
}

// =====================
// MÓDULO SECCIONES
// =====================
function createSeccionesModule(tools) {
    const formBusquedaSecciones = document.getElementById('formBusquedaSecciones');
    const tablaSecciones = document.querySelector('#tablaSecciones tbody');
    const tablaEstudiantesSeccion = document.querySelector('#tablaEstudiantesSeccion tbody');
    const tablaHistorialSeccion = document.querySelector('#tablaHistorialSeccion tbody');
    const filtroCursoSeccion = document.getElementById('filtroCursoSeccion');
    const filtroPeriodoSeccion = document.getElementById('filtroPeriodoSeccion');
    const filtroDocenteSeccion = document.getElementById('filtroDocenteSeccion');
    const filtroModalidadSeccion = document.getElementById('filtroModalidadSeccion');
    const filtroCodigoSeccion = document.getElementById('filtroCodigoSeccion');
    const btnLimpiarSecciones = document.getElementById('btnLimpiarSecciones');

    const fichaSeccionCampos = {
        curso: document.getElementById('seccionCurso'),
        estado: document.getElementById('seccionEstado'),
        codigo: document.getElementById('seccionCodigo'),
        docente: document.getElementById('seccionDocente'),
        periodo: document.getElementById('seccionPeriodo'),
        modalidad: document.getElementById('seccionModalidad'),
        horario: document.getElementById('seccionHorario'),
        aula: document.getElementById('seccionAula'),
        cupos: document.getElementById('seccionCupos'),
    };

    const btnEditarSeccion = document.getElementById('btnEditarSeccion');
    const btnGestionarHorarios = document.getElementById('btnGestionarHorarios');
    const btnAnularSeccion = document.getElementById('btnAnularSeccion');

    let seccionSeleccionada = null;
    let detalleSeccionActual = null;
    const catalogosSeccion = { docentes: [], modalidades: [] };

    function init() {
        cargarCatalogos();
        buscarSecciones();
        limpiarFichaSeccion();

        if (formBusquedaSecciones) {
            formBusquedaSecciones.addEventListener('submit', e => {
                e.preventDefault();
                buscarSecciones();
            });
        }

        if (btnLimpiarSecciones) {
            btnLimpiarSecciones.addEventListener('click', limpiarFiltrosSecciones);
        }

        if (btnEditarSeccion) btnEditarSeccion.onclick = () => abrirModalEdicionSeccion();
        if (btnGestionarHorarios) btnGestionarHorarios.onclick = () => abrirModalGestionHorarios();
        if (btnAnularSeccion) btnAnularSeccion.onclick = () => anularSeccion(seccionSeleccionada);
    }

    function resetFicha() {
        limpiarFichaSeccion();
    }

    async function cargarCatalogos() {
        try {
            const resp = await fetch('/admin/secciones/catalogos');
            if (!resp.ok) throw new Error('No se pudo cargar los catálogos');
            const data = await resp.json();
            catalogosSeccion.docentes = data.docentes || [];
            catalogosSeccion.modalidades = data.modalidades || [];
            tools.fillSelect(filtroCursoSeccion, data.cursos, 'Curso...', item => item.idCurso, item => `${item.codigo} - ${item.nombre}`);
            tools.fillSelect(filtroPeriodoSeccion, data.periodos, 'Seleccione', item => item, item => item);
            tools.fillSelect(filtroDocenteSeccion, data.docentes, 'Seleccione', item => item.idDocente, item => item.nombreCompleto);
            tools.fillSelect(filtroModalidadSeccion, data.modalidades, 'Todas', item => item, item => item);
        } catch (e) {
            tools.showToast(e.message, 'error');
        }
    }

    function limpiarFiltrosSecciones() {
        [filtroCursoSeccion, filtroPeriodoSeccion, filtroDocenteSeccion, filtroModalidadSeccion].forEach(sel => {
            if (sel) sel.value = '';
        });
        if (filtroCodigoSeccion) filtroCodigoSeccion.value = '';
        seccionSeleccionada = null;
        detalleSeccionActual = null;
        tools.renderEmptyRow(tablaSecciones, 9, 'Realiza una búsqueda para ver resultados');
        tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'Sin estudiantes');
        tools.renderEmptyRow(tablaHistorialSeccion, 5, 'Selecciona una sección');
        limpiarFichaSeccion();
        buscarSecciones();
    }

    async function buscarSecciones(preservarSeleccion = false) {
        if (!tablaSecciones) return;
        tools.renderEmptyRow(tablaSecciones, 9, 'Cargando secciones...');
        const params = new URLSearchParams();
        if (filtroCursoSeccion?.value) params.append('cursoId', filtroCursoSeccion.value);
        if (filtroPeriodoSeccion?.value) params.append('periodo', filtroPeriodoSeccion.value);
        if (filtroDocenteSeccion?.value) params.append('docenteId', filtroDocenteSeccion.value);
        if (filtroModalidadSeccion?.value) params.append('modalidad', filtroModalidadSeccion.value);
        if (filtroCodigoSeccion?.value?.trim()) params.append('codigo', filtroCodigoSeccion.value.trim());

        try {
            const url = params.toString() ? `/admin/secciones/buscar?${params.toString()}` : '/admin/secciones/buscar';
            const resp = await fetch(url);
            if (!resp.ok) throw new Error('No se pudo buscar secciones');
            const secciones = await resp.json();
            renderizarSecciones(secciones || [], preservarSeleccion);
        } catch (e) {
            tools.renderEmptyRow(tablaSecciones, 9, e.message || 'No se pudo cargar');
            tools.showToast(e.message || 'Error al buscar secciones', 'error');
        }
    }

    function renderizarSecciones(secciones, preservarSeleccion) {
        tablaSecciones.innerHTML = '';
        if (!secciones.length) {
            tools.renderEmptyRow(tablaSecciones, 9, 'Sin resultados');
            return;
        }
        secciones.forEach(sec => {
            const tr = document.createElement('tr');
            const idSeccion = sec.idSeccion ?? sec.id ?? sec.seccionId;
            const estado = (sec.estado || '').toLowerCase();
            tr.innerHTML = `
            <td>${sec.curso || '-'}</td>
            <td>${sec.codigoSeccion || sec.codigo || '-'}</td>
            <td>${sec.docente || '-'}</td>
            <td>${sec.periodo || '-'}</td>
            <td>${sec.modalidad || '-'}</td>
            <td>${sec.horario || '-'}</td>
            <td>${sec.aula || '-'}</td>
            <td>${sec.cupos ?? '-'} / ${(sec.matriculados ?? sec.estudiantes ?? 0)}</td>
            <td><span class="badge">${sec.estado || '-'}</span></td>
        `;
            if (estado === 'anulada') {
                tr.classList.add('anulada');
            }
            tr.addEventListener('click', () => {
                seccionSeleccionada = idSeccion;
                tools.markSelectedRow(tablaSecciones, tr);
                cargarFichaSeccion(idSeccion);
            });
            if (preservarSeleccion && seccionSeleccionada && `${seccionSeleccionada}` === `${idSeccion}`) {
                tools.markSelectedRow(tablaSecciones, tr);
            }
            tablaSecciones.appendChild(tr);
        });
    }

    async function cargarFichaSeccion(idSeccion) {
        if (!idSeccion) return;

        actualizarFichaSeccion({
            curso: 'Cargando...',
            estado: '',
            codigo: '-',
            docente: '-',
            periodo: '-',
            modalidad: '-',
            horario: '-',
            aula: '-',
            cupos: '-',
        });
        tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'Cargando estudiantes...');
        tools.renderEmptyRow(tablaHistorialSeccion, 5, 'Cargando historial...');

        try {
            const [detalleResp, estudiantesResp, historialResp] = await Promise.all([
                fetch(`/admin/secciones/${idSeccion}`),
                fetch(`/admin/secciones/${idSeccion}/estudiantes`),
                fetch(`/admin/secciones/${idSeccion}/historial`)
            ]);

            if (!detalleResp.ok || !estudiantesResp.ok || !historialResp.ok) throw new Error('No se pudo cargar la ficha');

            const detalle = await detalleResp.json();
            const estudiantes = await estudiantesResp.json();
            const historial = await historialResp.json();

            detalleSeccionActual = detalle;
            renderizarFichaSeccion(detalle, estudiantes);
            renderizarHistorial(historial);
            activarBotonesFicha();
        } catch (e) {
            tools.showToast(e.message || 'No se pudo cargar la ficha de sección', 'error');
            tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'No fue posible cargar los estudiantes');
            tools.renderEmptyRow(tablaHistorialSeccion, 5, 'No se pudo cargar el historial');
            detalleSeccionActual = null;
        }
    }

    function renderizarHistorial(historial) {
        tablaHistorialSeccion.innerHTML = '';
        const registrosHistorial = Array.isArray(historial) ? historial : [];
        if (!registrosHistorial.length) {
            tools.renderEmptyRow(tablaHistorialSeccion, 5, 'Sin registros');
            return;
        }
        registrosHistorial.forEach(reg => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${reg.periodo || '-'}</td>
                <td>${reg.alumnoNombre || '-'}<br><span class="muted">${reg.alumnoCodigo || ''}</span></td>
                <td>${reg.estadoMatricula || '-'}</td>
                <td>${reg.fechaMatricula ? new Date(reg.fechaMatricula).toLocaleString() : '-'}</td>
                <td>${reg.observacion || '-'}</td>
            `;
            tablaHistorialSeccion.appendChild(tr);
        });
    }

    function renderizarFichaSeccion(detalle, estudiantes) {
        actualizarFichaSeccion({
            curso: detalle.curso || '-',
            estado: detalle.estado || '-',
            codigo: detalle.codigoSeccion || '-',
            docente: detalle.docente || '-',
            periodo: detalle.periodo || '-',
            modalidad: detalle.modalidad || '-',
            horario: detalle.horario || '-',
            aula: detalle.aula || '-',
            cupos: `${detalle.cupos ?? '-'} / ${detalle.matriculados ?? detalle.estudiantes ?? 0}`,
        });

        tablaEstudiantesSeccion.innerHTML = '';
        const lista = estudiantes || [];
        if (!lista.length) {
            tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'Sin estudiantes');
            return;
        }
        lista.forEach(est => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${est.codigo || '-'}</td>
                <td>${est.nombre || '-'}</td>
                <td>${est.estado || '-'}</td>
                <td><button class="btn-outline" type="button">Ver</button></td>
            `;
            tablaEstudiantesSeccion.appendChild(tr);
        });
    }

    function actualizarFichaSeccion(valores) {
        if (!fichaSeccionCampos) return;

        fichaSeccionCampos.curso.textContent = valores.curso;
        fichaSeccionCampos.estado.textContent = valores.estado;
        fichaSeccionCampos.codigo.textContent = valores.codigo;
        fichaSeccionCampos.docente.textContent = valores.docente;
        fichaSeccionCampos.periodo.textContent = valores.periodo;
        fichaSeccionCampos.modalidad.textContent = valores.modalidad;
        fichaSeccionCampos.horario.textContent = valores.horario;
        fichaSeccionCampos.aula.textContent = valores.aula;
        fichaSeccionCampos.cupos.textContent = valores.cupos;

        // ------------ Badge dinámico ------------
        const badge = fichaSeccionCampos.estado;
        badge.className = "badge"; // reset
        const est = valores.estado?.toUpperCase() || '';

        if (est.includes("ANULADA") || est.includes("ANULADO")) badge.classList.add("badge--danger");
        else if (est.includes("ACTIVA") || est.includes("ACTIVO")) badge.classList.add("badge--success");
        else badge.classList.add("badge--info");

        // ------------ Control de botones ------------
        const anulada = est.includes("ANULADA") || est.includes("ANULADO");

        btnEditarSeccion.disabled = anulada;
        btnGestionarHorarios.disabled = anulada;
        btnAnularSeccion.disabled = anulada;

        // estilito visual
        [btnEditarSeccion, btnGestionarHorarios, btnAnularSeccion].forEach(btn => {
            if (anulada) btn.classList.add("btn-disabled");
            else btn.classList.remove("btn-disabled");
        });
    }


    function limpiarFichaSeccion() {
        actualizarFichaSeccion({
            curso: 'Selecciona una sección',
            estado: '-',
            codigo: '-',
            docente: '-',
            periodo: '-',
            modalidad: '-',
            horario: '-',
            aula: '-',
            cupos: '-',
        });
        tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'Selecciona una sección para ver estudiantes');
        tools.renderEmptyRow(tablaHistorialSeccion, 5, 'Selecciona una sección');
    }

    function abrirModalEdicionSeccion() {
        if (!seccionSeleccionada || !detalleSeccionActual) {
            tools.showToast('Selecciona una sección primero', 'info');
            return;
        }

        const docentesOpciones = (catalogosSeccion.docentes || []).map(doc => {
            const seleccionado = doc.idDocente === detalleSeccionActual.docenteId ? 'selected' : '';
            return `<option value="${doc.idDocente}" ${seleccionado}>${doc.nombreCompleto}</option>`;
        }).join('');

        const modalidadesOpciones = (catalogosSeccion.modalidades || []).map(mod => {
            const seleccionado = (detalleSeccionActual.modalidad || '').toLowerCase() === (mod || '').toLowerCase() ? 'selected' : '';
            return `<option value="${mod}" ${seleccionado}>${mod}</option>`;
        }).join('');

        Swal.fire({
            title: 'Editar sección',
            html: `
                <div class="swal-form-grid">
                    <div>
                        <label>Docente</label>
                        <select id="swalDocente">${docentesOpciones}</select>
                    </div>
                    <div>
                        <label>Modalidad</label>
                        <select id="swalModalidad">${modalidadesOpciones}</select>
                    </div>
                    <div>
                        <label>Aula</label>
                        <input id="swalAula" type="text" value="${detalleSeccionActual.aula || ''}" />
                    </div>
                    <div>
                        <label>Cupos</label>
                        <input id="swalCupos" type="number" min="1" value="${detalleSeccionActual.cupos || ''}" />
                    </div>
                </div>
            `,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: 'Guardar cambios',
            cancelButtonText: 'Cancelar',
            preConfirm: () => {
                const docenteId = document.getElementById('swalDocente')?.value;
                const modalidad = document.getElementById('swalModalidad')?.value;
                const aula = document.getElementById('swalAula')?.value?.trim();
                const cupos = parseInt(document.getElementById('swalCupos')?.value, 10);

                if (!docenteId) {
                    Swal.showValidationMessage('Selecciona un docente');
                    return false;
                }
                if (!aula) {
                    Swal.showValidationMessage('Ingresa el aula');
                    return false;
                }
                if (!cupos || cupos < 1) {
                    Swal.showValidationMessage('Los cupos deben ser mayores a 0');
                    return false;
                }
                if (!modalidad) {
                    Swal.showValidationMessage('Selecciona una modalidad');
                    return false;
                }

                return {
                    docenteId: Number(docenteId),
                    modalidad,
                    aula,
                    cupos,
                    horarios: Array.isArray(detalleSeccionActual.horarios)
                        ? detalleSeccionActual.horarios.map(h => ({ dia: h.dia, horaInicio: h.horaInicio, horaFin: h.horaFin }))
                        : []
                };
            }
        }).then(async result => {
            if (!result.isConfirmed || !result.value) return;
            try {
                await actualizarSeccion(seccionSeleccionada, result.value);
                Swal.fire('Guardado', 'La sección se actualizó correctamente', 'success');
                await cargarFichaSeccion(seccionSeleccionada);
                buscarSecciones();
            } catch (err) {
                Swal.fire('Error', err.message || 'No se pudo actualizar la sección', 'error');
            }
        });
    }

    function abrirModalGestionHorarios() {
        if (!seccionSeleccionada || !detalleSeccionActual) {
            tools.showToast('Selecciona una sección primero', 'info');
            return;
        }

        const diasSemana = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];
        let horariosTemp = Array.isArray(detalleSeccionActual.horarios) && detalleSeccionActual.horarios.length
            ? [...detalleSeccionActual.horarios]
            : [{ dia: 'LUNES', horaInicio: '08:00', horaFin: '10:00' }];

        const optionDia = (diaActual) => diasSemana.map(dia => {
            const label = dia.charAt(0) + dia.slice(1).toLowerCase();
            const selected = diaActual === dia ? 'selected' : '';
            return `<option value="${dia}" ${selected}>${label}</option>`;
        }).join('');

        const renderFila = (horario, idx) => `
            <div class="horario-row" data-idx="${idx}">
                <div class="horario-field">
                    <label>Día</label>
                    <select class="horario-dia">${optionDia(horario.dia || 'LUNES')}</select>
                </div>
                <div class="horario-field">
                    <label>Inicio</label>
                    <input type="time" class="horario-inicio" value="${horario.horaInicio || ''}" />
                </div>
                <div class="horario-field">
                    <label>Fin</label>
                    <input type="time" class="horario-fin" value="${horario.horaFin || ''}" />
                </div>
                <div class="horario-actions-inline">
                    <button type="button" class="btn-link btn-remove-horario">Quitar</button>
                </div>
            </div>`;

        const buildHtml = () => `
            <div class="horarios-list">
                ${horariosTemp.map(renderFila).join('')}
            </div>
            <div class="horarios-actions">
                <button type="button" class="btn-secondary" id="btnAgregarHorario">+ Añadir horario</button>
            </div>
        `;

        const sincronizarHorariosDesdePopup = () => {
            const popup = Swal.getPopup();
            if (!popup) return;
            horariosTemp = Array.from(popup.querySelectorAll('.horario-row')).map(row => ({
                dia: row.querySelector('.horario-dia')?.value || 'LUNES',
                horaInicio: row.querySelector('.horario-inicio')?.value || '',
                horaFin: row.querySelector('.horario-fin')?.value || ''
            }));
        };

        const enlazarEventos = () => {
            const popup = Swal.getPopup();
            popup?.querySelector('#btnAgregarHorario')?.addEventListener('click', () => {
                sincronizarHorariosDesdePopup();
                horariosTemp.push({ dia: 'LUNES', horaInicio: '08:00', horaFin: '10:00' });
                refrescarHtml();
            });
            popup?.querySelectorAll('.btn-remove-horario').forEach((btn, index) => {
                btn.addEventListener('click', () => eliminarHorario(index));
            });
        };

        const refrescarHtml = () => {
            Swal.update({ html: buildHtml() });
            enlazarEventos();
        };

        Swal.fire({
            title: 'Gestionar horarios',
            html: buildHtml(),
            width: 700,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: 'Guardar horarios',
            cancelButtonText: 'Cancelar',
            showLoaderOnConfirm: true,
            allowOutsideClick: () => !Swal.isLoading(),
            didOpen: enlazarEventos,
            preConfirm: async () => {
                const popup = Swal.getPopup();
                const rows = Array.from(popup.querySelectorAll('.horario-row'));
                const horarios = rows.map(row => ({
                    dia: row.querySelector('.horario-dia')?.value,
                    horaInicio: row.querySelector('.horario-inicio')?.value,
                    horaFin: row.querySelector('.horario-fin')?.value
                })).filter(h => h.dia || h.horaInicio || h.horaFin);

                if (!horarios.length) {
                    Swal.showValidationMessage('Agrega al menos un horario');
                    return false;
                }

                for (const horario of horarios) {
                    if (!horario.dia || !horario.horaInicio || !horario.horaFin) {
                        Swal.showValidationMessage('Completa todos los campos de horario');
                        return false;
                    }
                    if (horario.horaFin <= horario.horaInicio) {
                        Swal.showValidationMessage('La hora fin debe ser mayor a la hora inicio');
                        return false;
                    }
                }

                const repetidos = new Set();
                for (const h of horarios) {
                    const clave = `${h.dia}|${h.horaInicio}|${h.horaFin}`;
                    if (repetidos.has(clave)) {
                        Swal.showValidationMessage('Hay horarios duplicados, revisa las filas');
                        return false;
                    }
                    repetidos.add(clave);
                }

                try {
                    await actualizarHorarios(seccionSeleccionada, horarios);
                    detalleSeccionActual.horarios = horarios;
                    return horarios;
                } catch (err) {
                    Swal.showValidationMessage(err.message || 'No se pudo actualizar los horarios');
                    return false;
                }
            }
        }).then(async result => {
            if (!result.isConfirmed || !result.value) return;
            tools.showToast('Horarios actualizados', 'success');
            await cargarFichaSeccion(seccionSeleccionada);
            await buscarSecciones(true);
        });

        function eliminarHorario(idx) {
            sincronizarHorariosDesdePopup();
            if (horariosTemp.length <= 1) {
                tools.showToast('Debe existir al menos un horario', 'info');
                return;
            }
            horariosTemp.splice(idx, 1);
            refrescarHtml();
        }
    }

    async function actualizarHorarios(id, horarios) {
        const resp = await fetch(`/admin/secciones/${id}/horarios`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ horarios })
        });

        if (!resp.ok) {
            const mensaje = await resp.text();
            throw new Error(mensaje || 'No se pudieron guardar los horarios');
        }
    }

    async function actualizarSeccion(id, payload) {
        const resp = await fetch(`/admin/secciones/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!resp.ok) {
            const mensaje = await resp.text();
            throw new Error(mensaje || 'No se pudo guardar la sección');
        }
    }

    async function anularSeccion(id) {
        if (!id) return tools.showToast('Selecciona una sección', 'error');

        const confirm = await Swal.fire({
            title: '¿Anular sección?',
            text: 'Esta acción no puede revertirse.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, anular',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d33',
        });

        if (!confirm.isConfirmed) return;

        try {
            const resp = await fetch(`/admin/secciones/${id}/anular`, { method: 'PUT' });

            if (resp.status === 409) {
                Swal.fire('Atención', 'La sección ya estaba anulada.', 'info');
                return;
            }
            if (!resp.ok) {
                Swal.fire('Error', 'No se pudo anular la sección.', 'error');
                return;
            }

            Swal.fire('Listo', 'La sección fue anulada correctamente.', 'success');

            buscarSecciones();      // refrescar tabla
            cargarFichaSeccion(id); // refrescar ficha

        } catch (err) {
            Swal.fire('Error', 'Ocurrió un error inesperado.', 'error');
        }
    }



    function activarBotonesFicha() {
        if (!seccionSeleccionada) {
            btnEditarSeccion.disabled = true;
            btnGestionarHorarios.disabled = true;
            btnAnularSeccion.disabled = true;
            return;
        }

        btnEditarSeccion.disabled = false;
        btnGestionarHorarios.disabled = false;
        btnAnularSeccion.disabled = false;

        btnEditarSeccion.onclick = () => abrirModalEdicionSeccion();
        btnGestionarHorarios.onclick = () => abrirModalGestionHorarios();
        btnAnularSeccion.onclick = () => anularSeccion(seccionSeleccionada);
    }

    return { init, resetFicha };
}

// =====================
// Bootstrap general
// =====================
document.addEventListener('DOMContentLoaded', () => {
    const tools = createTools();
    const seccionesModule = createSeccionesModule(tools);
    const alumnosModule = createAlumnosModule(tools);
    const docentesModule = createDocentesModule(tools);

    setupNavigation(() => seccionesModule.resetFicha());

    alumnosModule.init();
    docentesModule.init();
    seccionesModule.init();
});
