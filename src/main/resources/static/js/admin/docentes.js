// Módulo de gestión de docentes en el Portal Admin

export function createDocentesModule(tools) {
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
    const badgeSeccionesDocente = document.getElementById('badgeSeccionesDocente');
    const tablaHistorialDocente = document.querySelector('#tablaHistorialDocente tbody');
    const btnEditarDocente = document.getElementById('btnEditarDocente');
    const btnEditarContactoDocente = document.getElementById('btnEditarContactoDocente');
    const formDatosDocente = document.getElementById('formDatosDocente');
    const formContactoDocente = document.getElementById('formContactoDocente');
    const estadoDatosDocente = document.getElementById('estadoDatosDocente');
    const estadoContactoDocente = document.getElementById('estadoContactoDocente');
    const modalDatosDocente = document.getElementById('modalDatosDocente');
    const modalContactoDocente = document.getElementById('modalContactoDocente');
    const btnLimpiarDocentes = document.getElementById('btnLimpiarDocentes');

    const inputsDocente = {
        codigo: document.getElementById('docCodigo'),
        nombreCompleto: document.getElementById('docNombreCompleto'),
        estado: document.getElementById('docEstado'),
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
            btnEditarDocente.addEventListener('click', () => abrirModalDocente(modalDatosDocente));
        }

        if (btnEditarContactoDocente) {
            btnEditarContactoDocente.addEventListener('click', () => abrirModalDocente(modalContactoDocente));
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

        [modalDatosDocente, modalContactoDocente].forEach(modal => {
            if (!modal) return;
            modal.addEventListener('click', evt => {
                if (evt.target === modal) cerrarModalDocente(modal);
            });
            modal.querySelectorAll('[data-close]').forEach(btn => {
                btn.addEventListener('click', () => cerrarModalDocente(modal));
            });
        });
    }

    async function cargarCatalogoCursos() {
        try {
            const resp = await fetch('/admin/docentes/cursos');
            const contentType = resp.headers.get('content-type') || '';
            if (!resp.ok || !contentType.includes('application/json')) {
                throw new Error('Respuesta inválida del servidor');
            }
            const data = await resp.json();
            const cursos = Array.isArray(data) ? data : [];
            [selectCursoDictable, filtroCursoDictable].forEach(select => {
                if (!select) return;
                select.innerHTML = '';
                const baseOption = document.createElement('option');
                baseOption.value = '';
                baseOption.textContent = select === selectCursoDictable ? 'Selecciona curso' : 'Todos';
                select.appendChild(baseOption);
                cursos.forEach(c => {
                    const id = c.idCurso ?? c.id ?? c.codigo;
                    if (!id && !c.codigo && !c.codigoCurso && !c.nombre && !c.nombreCurso) return;
                    const opt = document.createElement('option');
                    opt.value = id;
                    opt.textContent = `${c.codigo || c.codigoCurso || '-'} - ${c.nombre || c.nombreCurso || 'Sin nombre'}`;
                    select.appendChild(opt);
                });
            });
            console.log('Cursos dictables cargados:', cursos);
        } catch (e) {
            console.error('No se pudo cargar el catálogo de cursos', e);
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
            const codigoDocente = doc.docCodigo ?? doc.codigo;
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${codigoDocente || '-'}</td>
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
        prepararCargaDocente();
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
            limpiarSeleccionDocente();
        }
    }

    function prepararCargaDocente() {
        cerrarModalDocente(modalDatosDocente);
        cerrarModalDocente(modalContactoDocente);
        docenteSeleccionado = null;
        [
            inputsDocente.codigo, inputsDocente.nombreCompleto,
            inputsDocente.dni, inputsDocente.especialidad, inputsDocente.correoInst,
            inputsDocente.correoPer, inputsDocente.telefono, inputsDocente.direccion
        ].forEach(el => {
            if (el) el.textContent = 'Cargando...';
        });
        Object.values(formInputsDocente).forEach(input => {
            if (input) input.value = '';
        });
        Object.values(formContactoInputs).forEach(input => {
            if (input) input.value = '';
        });
        if (inputsDocente.estado) {
            inputsDocente.estado.textContent = 'Cargando...';
            inputsDocente.estado.className = 'badge';
        }
        if (badgeSeccionesDocente) badgeSeccionesDocente.textContent = 'Cargando...';
        tools.renderEmptyRow(tablaCursosDictables, 5, 'Cargando...');
        tools.renderEmptyRow(tablaSeccionesDocente, 9, 'Cargando...');
        tools.renderEmptyRow(tablaHistorialDocente, 11, 'Cargando...');
    }


    function renderizarFichaDocente(detalle) {
        const nombreCompleto = (detalle.nombreCompleto || `${detalle.nombres || ''} ${detalle.apellidos || ''}`).trim();
        const codigoDocente = detalle.docCodigo || detalle.codigo || '-';
        inputsDocente.nombreCompleto.textContent = nombreCompleto || '-';
        inputsDocente.codigo.textContent = codigoDocente || '-';
        inputsDocente.estado.textContent = detalle.estado || '-';
        inputsDocente.estado.className = 'badge';
        inputsDocente.estado.classList.add(detalle.estado === 'ACTIVO' || detalle.estado === 'Activo' ? 'badge--success' : 'badge--info');
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
                <td>${curso.codigoDocente || '-'}</td>
                <td>${curso.nombreCompleto || '-'}</td>
                <td>${curso.creditosCurso ?? '-'}</td>
                <td>${curso.creditosCurso ? curso.creditosCurso * 1 : '-'}</td> 
                <td>${curso.cicloCurso ?? '-'}</td>
            `;
            tr.addEventListener('click', () => eliminarCursoDictable(curso.idCurso));
            tablaCursosDictables.appendChild(tr);
        });
    }

    function renderizarSeccionesDocente(detalle) {
        const secciones = detalle.seccionesActuales || [];
        tablaSeccionesDocente.innerHTML = '';
        if (!secciones.length) {
            tools.renderEmptyRow(tablaSeccionesDocente, 9, 'Sin secciones');
            if (resumenSeccionesDocente) resumenSeccionesDocente.textContent = '0 secciones asignadas';
            if (badgeSeccionesDocente) badgeSeccionesDocente.textContent = '0 secciones';
            return;
        }

        secciones.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${s.curso || '-'}</td>
                <td>${s.codigoSeccion || '-'}</td>
                <td>${s.periodo || '-'}</td>
                <td>${s.modalidad || '-'}</td>
                <td>${s.creditos ?? '-'}</td>
                <td>${s.turno || '-'}</td>
                <td>${s.horario || '-'}</td>
                <td>${s.aula || '-'}</td>
                <td>${s.estudiantesInscritos ?? '-'}</td>
            `;
            tablaSeccionesDocente.appendChild(tr);
        });

        const total = detalle.totalSeccionesActuales || secciones.length || 0;
        if (resumenSeccionesDocente) {
            resumenSeccionesDocente.textContent = `${total} secciones`;
        }
        if (badgeSeccionesDocente) {
            badgeSeccionesDocente.textContent = `${total} secciones`;
        }
    }

    function renderizarHistorialDocente(historial) {
        tablaHistorialDocente.innerHTML = '';
        if (!historial.length) {
            tools.renderEmptyRow(tablaHistorialDocente, 11, 'Sin historial');
            return;
        }
        historial.forEach(h => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${h.periodo || '-'}</td>
                <td>${h.curso || '-'}</td>
                <td>${h.seccion || '-'}</td>
                <td>${h.modalidad || '-'}</td>
                <td>${h.creditos ?? '-'}</td>
                <td>${h.turno || '-'}</td>
                <td>${h.horario || '-'}</td>
                <td>${h.estudiantesFinalizados ?? '-'}</td>
                <td>${h.notaPromedio ?? '-'}</td>
                <td>${h.porcentajeAprobacion != null ? `${h.porcentajeAprobacion}%` : '-'}</td>
                <td>${h.observaciones || '-'}</td>
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
        const confirm = await Swal.fire({
            title: '¿Quitar curso dictable?',
            text: 'El docente dejará de estar habilitado para este curso.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
        });

        if (!confirm.isConfirmed) return;

        try {
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/cursos/${cursoId}`, { method: 'DELETE' });
            if (!resp.ok) throw new Error('No se pudo eliminar el curso');
            tools.showToast('Curso removido', 'info');
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showToast(err.message, 'error');
        }
    }

    function abrirModalDocente(modal) {
        if (!docenteSeleccionado) {
            tools.showToast('Selecciona un docente', 'info');
            return;
        }
        if (!modal) return;
        modal.hidden = false;
        document.body.classList.add('modal-open');
        if (modal === modalDatosDocente) tools.clearStatus(estadoDatosDocente);
        if (modal === modalContactoDocente) tools.clearStatus(estadoContactoDocente);
    }

    function cerrarModalDocente(modal) {
        if (!modal) return;
        modal.hidden = true;
        if (![modalDatosDocente, modalContactoDocente].some(m => m && !m.hidden)) {
            document.body.classList.remove('modal-open');
        }
        if (modal === modalDatosDocente) tools.clearStatus(estadoDatosDocente);
        if (modal === modalContactoDocente) tools.clearStatus(estadoContactoDocente);
    }

    function limpiarSeleccionDocente() {
        docenteSeleccionado = null;
        [
            inputsDocente.codigo, inputsDocente.nombreCompleto,
            inputsDocente.dni, inputsDocente.especialidad, inputsDocente.correoInst,
            inputsDocente.correoPer, inputsDocente.telefono, inputsDocente.direccion
        ].forEach(el => {
            if (el) el.textContent = '-';
        });
        if (inputsDocente.estado) {
            inputsDocente.estado.textContent = '-';
            inputsDocente.estado.className = 'badge';
        }
        renderizarCursosDictables([]);
        renderizarSeccionesDocente({ seccionesActuales: [] });
        renderizarHistorialDocente([]);
        cerrarModalDocente(modalDatosDocente);
        cerrarModalDocente(modalContactoDocente);
    }

    async function guardarDatosDocente() {
        if (!docenteSeleccionado) {
            tools.showToast('Selecciona un docente', 'info');
            return;
        }
        try {
            tools.showStatus(estadoDatosDocente, 'Guardando datos...', false);
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
            cerrarModalDocente(modalDatosDocente);
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showStatus(estadoDatosDocente, err.message, true);
        }
    }

    async function guardarContactoDocente() {
        if (!docenteSeleccionado) {
            tools.showToast('Selecciona un docente', 'info');
            return;
        }
        try {
            tools.showStatus(estadoContactoDocente, 'Guardando contacto...', false);
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
            cerrarModalDocente(modalContactoDocente);
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            tools.showStatus(estadoContactoDocente, err.message, true);
        }
    }

    return { init };
}
