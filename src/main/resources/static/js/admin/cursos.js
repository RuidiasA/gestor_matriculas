import { createTools } from './utils.js';

export function createCursosModule(globalTools) {
    const tools = globalTools || createTools();

    const seccion = document.getElementById('cursos');
    if (!seccion) {
        console.warn('Sección #cursos no encontrada en el DOM');
        return { init: () => {}, resetVista: () => {} };
    }

    // ======= ELEMENTOS DEL DOM =======
    // Filtros / búsqueda
    const formBusqueda = seccion.querySelector('#formBusquedaCursos');
    const filtroTexto = seccion.querySelector('#filtroCursoTexto');
    const filtroCarrera = seccion.querySelector('#filtroCursoCarrera');
    const filtroCiclo = seccion.querySelector('#filtroCursoCiclo');
    const filtroTipo = seccion.querySelector('#filtroCursoTipo');
    const filtroEstado = seccion.querySelector('#filtroCursoEstado');
    const btnLimpiar = seccion.querySelector('#btnLimpiarCursos');
    const btnNuevo = seccion.querySelector('#btnNuevoCurso');
    const estadoBusqueda = seccion.querySelector('#estadoBusquedaCursos');
    const badgeTotalCursos = seccion.querySelector('#badgeTotalCursos');
    // Tabla de cursos
    const tablaCursosBody = seccion.querySelector('#tablaCursos tbody');
    // Detalle datos generales
    const campoCodigo = seccion.querySelector('#cursoCodigo');
    const campoNombre = seccion.querySelector('#cursoNombre');
    const campoCarrera = seccion.querySelector('#cursoCarrera');
    const campoCiclo = seccion.querySelector('#cursoCiclo');
    const campoTipo = seccion.querySelector('#cursoTipo');
    const campoCreditos = seccion.querySelector('#cursoCreditos');
    const campoHoras = seccion.querySelector('#cursoHoras');
    const campoModalidad = seccion.querySelector('#cursoModalidad');
    const campoEstado = seccion.querySelector('#cursoEstado');
    const campoDescripcion = seccion.querySelector('#cursoDescripcion');

    const btnEditarCurso = seccion.querySelector('#btnEditarCurso');
    const btnEliminarCurso = seccion.querySelector('#btnEliminarCurso');

    // Prerrequisitos
    const selectPrerrequisito = seccion.querySelector('#selectPrerrequisito');
    const btnAgregarPrerrequisito = seccion.querySelector('#btnAgregarPrerrequisito');
    const btnGuardarPrerrequisitos = seccion.querySelector('#btnGuardarPrerrequisitos');
    const tablaPrerrequisitosBody = seccion.querySelector('#tablaPrerrequisitos tbody');

    // Docentes dictables
    const selectDocenteCurso = seccion.querySelector('#selectDocenteCurso');
    const btnAgregarDocenteCurso = seccion.querySelector('#btnAgregarDocenteCurso');
    const btnGuardarDocentesCurso = seccion.querySelector('#btnGuardarDocentesCurso');
    const tablaDocentesCursoBody = seccion.querySelector('#tablaDocentesCurso tbody');

    // ======= ESTADO INTERNO =======
    let catalogos = {
        carreras: [],
        ciclos: [],
        tipos: [],
        estados: [],
        modalidades: [],
        cursos: [],
        docentes: []
    };

    let cursosActuales = [];
    let cursoSeleccionado = null;

    // Mantendremos los cambios locales en memoria
    let prerrequisitosActuales = [];   // array de {idCurso, codigo, nombre}
    let docentesDictablesActuales = []; // array de {idDocente, nombreCompleto, dni}

    // ======= HELPERS FETCH =======

    async function fetchJson(url, errorMsg, options = {}) {
        const resp = await fetch(url, options);
        if (!resp.ok) {
            const texto = await resp.text();
            throw new Error(texto || errorMsg || 'Error al conectar con el servidor');
        }
        return resp.json();
    }

    // ======= INICIALIZACIÓN =======

    function init() {
        enlazarEventosBasicos();
        cargarCatalogos()
            .then(() => buscarCursos())
            .catch(err => {
                console.error(err);
                tools.showToast('No se pudieron cargar los catálogos de cursos', 'error');
            });

        limpiarDetalle();
    }

    function enlazarEventosBasicos() {
        if (formBusqueda) {
            formBusqueda.addEventListener('submit', e => {
                e.preventDefault();
                buscarCursos();
            });
        }

        if (btnLimpiar) {
            btnLimpiar.addEventListener('click', () => {
                if (filtroTexto) filtroTexto.value = '';
                if (filtroCarrera) filtroCarrera.value = '';
                if (filtroCiclo) filtroCiclo.value = '';
                if (filtroTipo) filtroTipo.value = '';
                if (filtroEstado) filtroEstado.value = '';
                cursoSeleccionado = null;
                cursosActuales = [];
                limpiarDetalle();
                buscarCursos();
            });
        }

        if (btnNuevo) {
            btnNuevo.addEventListener('click', () => abrirModalCursoNuevo());
        }

        if (btnEditarCurso) {
            btnEditarCurso.addEventListener('click', () => {
                if (!cursoSeleccionado) {
                    tools.showToast('Selecciona un curso primero', 'info');
                    return;
                }
                abrirModalEditarCurso(cursoSeleccionado);
            });
        }

        if (btnEliminarCurso) {
            btnEliminarCurso.addEventListener('click', () => {
                if (!cursoSeleccionado) {
                    tools.showToast('Selecciona un curso primero', 'info');
                    return;
                }
                confirmarEliminarCurso(cursoSeleccionado);
            });
        }

        if (btnAgregarPrerrequisito) {
            btnAgregarPrerrequisito.addEventListener('click', agregarPrerrequisito);
        }

        if (btnGuardarPrerrequisitos) {
            btnGuardarPrerrequisitos.addEventListener('click', guardarPrerrequisitos);
        }

        if (btnAgregarDocenteCurso) {
            btnAgregarDocenteCurso.addEventListener('click', agregarDocenteDictable);
        }

        if (btnGuardarDocentesCurso) {
            btnGuardarDocentesCurso.addEventListener('click', guardarDocentesDictables);
        }
    }

    // ======= CARGA DE CATÁLOGOS =======

    async function cargarCatalogos() {
        try {
            const data = await fetchJson('/admin/cursos/catalogos', 'No se pudieron cargar los catálogos');

            catalogos.carreras = data.carreras || [];
            catalogos.ciclos = data.ciclos || [];
            catalogos.tipos = data.tipos || [];
            catalogos.estados = data.estados || [];
            catalogos.modalidades = data.modalidades || [];
            catalogos.cursos = data.cursos || [];
            catalogos.docentes = data.docentes || [];

            // Filtros
            tools.fillSelect(filtroCarrera, catalogos.carreras, 'Todas', c => c.idCarrera, c => c.nombre);
            tools.fillSelect(filtroCiclo, catalogos.ciclos, 'Todos', c => c, c => c);
            tools.fillSelect(filtroTipo, catalogos.tipos, 'Todos', t => t, t => t);
            tools.fillSelect(filtroEstado, catalogos.estados, 'Todos', e => e, e => e);

            // Select para agregar prerrequisitos (se filtra luego según curso)
            tools.fillSelect(selectPrerrequisito, catalogos.cursos, 'Selecciona curso', c => c.idCurso, c => `${c.codigo} - ${c.nombre}`);

            // Select para docentes dictables
            tools.fillSelect(selectDocenteCurso, catalogos.docentes, 'Selecciona docente', d => d.idDocente, d => `${d.nombreCompleto} (${d.dni || ''})`);

        } catch (err) {
            console.error(err);
            throw err;
        }
    }

    // ======= BUSCAR LISTA DE CURSOS =======

    async function buscarCursos() {
        tools.showStatus(estadoBusqueda, 'Buscando cursos...', false);
        tools.renderEmptyRow(tablaCursosBody, 7, 'Cargando...');

        const params = new URLSearchParams();
        if (filtroTexto?.value?.trim()) params.append('filtro', filtroTexto.value.trim());
        if (filtroCarrera?.value) params.append('carreraId', filtroCarrera.value);
        if (filtroCiclo?.value) params.append('ciclo', filtroCiclo.value);
        if (filtroTipo?.value) params.append('tipo', filtroTipo.value);
        if (filtroEstado?.value) params.append('estado', filtroEstado.value);

        let url = '/admin/cursos';
        if ([...params.keys()].length) {
            url += `?${params.toString()}`;
        }

        try {
            const cursos = await fetchJson(url, 'No se pudo buscar cursos');

            cursosActuales = Array.isArray(cursos) ? cursos : [];
            renderizarListaCursos(cursosActuales);
            if (!cursosActuales.length) {
                tools.showStatus(estadoBusqueda, 'Sin resultados', true);
            } else {
                estadoBusqueda.hidden = true;
            }
        } catch (err) {
            console.error(err);
            cursosActuales = [];
            renderizarListaCursos([]);
            tools.showStatus(estadoBusqueda, err.message || 'No se pudo cargar la lista de cursos', true);
        }
    }

    function renderizarListaCursos(cursos) {
        tablaCursosBody.innerHTML = '';

        if (badgeTotalCursos) {
            const total = cursos.length || 0;
            badgeTotalCursos.textContent = `${total} curso${total === 1 ? '' : 's'}`;
        }

        if (!cursos || !cursos.length) {
            tools.renderEmptyRow(tablaCursosBody, 7, 'Sin resultados');
            deshabilitarDetalle();
            return;
        }

        cursos.forEach(curso => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${curso.codigo || '-'}</td>
                <td>${curso.nombre || '-'}</td>
                <td>${curso.ciclo || '-'}</td>
                <td>${curso.creditos ?? '-'}</td>
                <td>${curso.horasSemanales ?? '-'}</td>
                <td>${curso.modalidad || '-'}</td>
                <td>${curso.tipo || '-'}</td>
            `;
            tr.addEventListener('click', () => {
                tools.markSelectedRow(tablaCursosBody, tr);
                if (curso.idCurso || curso.id) {
                    cargarDetalleCurso(curso.idCurso ?? curso.id);
                }
            });
            tablaCursosBody.appendChild(tr);
        });
    }

    // ======= DETALLE DE CURSO =======

    async function cargarDetalleCurso(idCurso) {
        if (!idCurso) return;

        mostrarCargandoDetalle();

        try {
            const detalle = await fetchJson(`/admin/cursos/${idCurso}`, 'No se pudo obtener el detalle del curso');

            cursoSeleccionado = detalle;
            prerrequisitosActuales = (detalle.prerrequisitos || []).map(p => ({
                idCurso: p.idCurso,
                codigo: p.codigo,
                nombre: p.nombre
            }));
            docentesDictablesActuales = (detalle.docentesDictables || []).map(d => ({
                idDocente: d.idDocente,
                nombreCompleto: d.nombreCompleto,
                dni: d.dni
            }));

            renderizarDetalleCurso(detalle);
            renderizarPrerrequisitos();
            renderizarDocentesDictables();
            habilitarDetalle(true);
        } catch (err) {
            console.error(err);
            tools.showToast(err.message || 'No se pudo cargar el detalle', 'error');
            limpiarDetalle();
        }
    }

    function mostrarCargandoDetalle() {
        campoCodigo.textContent = 'Cargando...';
        campoNombre.textContent = 'Cargando...';
        campoCarrera.textContent = 'Cargando...';
        campoCiclo.textContent = 'Cargando...';
        campoTipo.textContent = 'Cargando...';
        campoCreditos.textContent = '...';
        campoHoras.textContent = '...';
        campoModalidad.textContent = '...';
        campoEstado.textContent = '...';
        campoEstado.className = 'detalle-valor badge';
        campoDescripcion.textContent = 'Cargando descripción...';

        tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Cargando...');
        tools.renderEmptyRow(tablaDocentesCursoBody, 3, 'Cargando...');

        habilitarDetalle(false);
    }

    function renderizarDetalleCurso(curso) {
        campoCodigo.textContent = curso.codigo || '-';
        campoNombre.textContent = curso.nombre || '-';
        campoCarrera.textContent = curso.carrera || '-';
        campoCiclo.textContent = curso.ciclo || '-';
        campoTipo.textContent = curso.tipo || '-';
        campoCreditos.textContent = curso.creditos ?? '-';
        campoHoras.textContent = curso.horasSemanales ?? '-';
        campoModalidad.textContent = curso.modalidad || '-';

        campoDescripcion.textContent = curso.descripcion || '-';

        // Badge estado
        campoEstado.textContent = curso.estado || '-';
        campoEstado.className = 'detalle-valor badge';
        const est = (curso.estado || '').toUpperCase();
        if (est.includes('INACTIVO')) campoEstado.classList.add('badge--info');
        else if (est.includes('SUSPEND')) campoEstado.classList.add('badge--warning');
        else campoEstado.classList.add('badge--success');
    }

    function limpiarDetalle() {
        campoCodigo.textContent = 'Selecciona un curso';
        campoNombre.textContent = '-';
        campoCarrera.textContent = '-';
        campoCiclo.textContent = '-';
        campoTipo.textContent = '-';
        campoCreditos.textContent = '-';
        campoHoras.textContent = '-';
        campoModalidad.textContent = '-';
        campoEstado.textContent = '-';
        campoEstado.className = 'detalle-valor badge';
        campoDescripcion.textContent = '-';

        tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Selecciona un curso para ver sus prerrequisitos');
        tools.renderEmptyRow(tablaDocentesCursoBody, 3, 'Selecciona un curso para ver sus docentes habilitados');

        prerrequisitosActuales = [];
        docentesDictablesActuales = [];
        habilitarDetalle(false);
    }

    function habilitarDetalle(habilitado) {
        btnEditarCurso.disabled = !habilitado;
        btnEliminarCurso.disabled = !habilitado;
        btnAgregarPrerrequisito.disabled = !habilitado;
        btnGuardarPrerrequisitos.disabled = !habilitado;
        btnAgregarDocenteCurso.disabled = !habilitado;
        btnGuardarDocentesCurso.disabled = !habilitado;
    }

    function deshabilitarDetalle() {
        habilitarDetalle(false);
    }

    // ======= PRERREQUISITOS =======

    function renderizarPrerrequisitos() {
        tablaPrerrequisitosBody.innerHTML = '';

        if (!prerrequisitosActuales.length) {
            tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Sin prerrequisitos registrados');
            return;
        }

        prerrequisitosActuales.forEach(pr => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${pr.codigo || '-'}</td>
                <td>${pr.nombre || '-'}</td>
                <td class="text-right">
                    <button type="button" class="btn-link btn-sm" data-id="${pr.idCurso}">Quitar</button>
                </td>
            `;
            tr.querySelector('button')?.addEventListener('click', () => {
                prerrequisitosActuales = prerrequisitosActuales.filter(p => p.idCurso !== pr.idCurso);
                renderizarPrerrequisitos();
            });
            tablaPrerrequisitosBody.appendChild(tr);
        });
    }

    function agregarPrerrequisito() {
        if (!cursoSeleccionado) {
            tools.showToast('Selecciona un curso primero', 'info');
            return;
        }
        const idSel = selectPrerrequisito?.value;
        if (!idSel) return;

        const idNum = Number(idSel);
        if (cursoSeleccionado.idCurso && idNum === cursoSeleccionado.idCurso) {
            tools.showToast('Un curso no puede ser prerrequisito de sí mismo', 'warning');
            return;
        }

        if (prerrequisitosActuales.some(p => p.idCurso === idNum)) {
            tools.showToast('El prerrequisito ya está agregado', 'info');
            return;
        }

        const cursoBase = (catalogos.cursos || []).find(c => c.idCurso === idNum);
        if (!cursoBase) {
            tools.showToast('Curso no encontrado en el catálogo', 'error');
            return;
        }

        prerrequisitosActuales.push({
            idCurso: cursoBase.idCurso,
            codigo: cursoBase.codigo,
            nombre: cursoBase.nombre
        });
        renderizarPrerrequisitos();
    }

    async function guardarPrerrequisitos() {
        if (!cursoSeleccionado) {
            tools.showToast('Selecciona un curso', 'info');
            return;
        }

        try {
            btnGuardarPrerrequisitos.disabled = true;
            const body = {
                idsPrerrequisitos: prerrequisitosActuales.map(p => p.idCurso)
            };
            await fetchJson(
                `/admin/cursos/${cursoSeleccionado.idCurso}/prerrequisitos`,

                'No se pudieron guardar los prerrequisitos',
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                }
            );
            tools.showToast('Prerrequisitos actualizados', 'success');
        } catch (err) {
            tools.showToast(err.message, 'error');
        } finally {
            btnGuardarPrerrequisitos.disabled = false;
        }
    }

    // ======= DOCENTES DICTABLES =======

    function renderizarDocentesDictables() {
        tablaDocentesCursoBody.innerHTML = '';

        if (!docentesDictablesActuales.length) {
            tools.renderEmptyRow(tablaDocentesCursoBody, 3, 'Sin docentes habilitados');
            return;
        }

        docentesDictablesActuales.forEach(doc => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${doc.nombreCompleto || '-'}</td>
                <td>${doc.dni || '-'}</td>
                <td class="text-right">
                    <button type="button" class="btn-link btn-sm" data-id="${doc.idDocente}">Quitar</button>
                </td>
            `;
            tr.querySelector('button')?.addEventListener('click', () => {
                docentesDictablesActuales = docentesDictablesActuales.filter(d => d.idDocente !== doc.idDocente);
                renderizarDocentesDictables();
            });
            tablaDocentesCursoBody.appendChild(tr);
        });
    }

    function agregarDocenteDictable() {
        if (!cursoSeleccionado) {
            tools.showToast('Selecciona un curso primero', 'info');
            return;
        }
        const idSel = selectDocenteCurso?.value;
        if (!idSel) return;

        const idNum = Number(idSel);
        if (docentesDictablesActuales.some(d => d.idDocente === idNum)) {
            tools.showToast('El docente ya está asignado', 'info');
            return;
        }

        const docenteBase = (catalogos.docentes || []).find(d => d.idDocente === idNum);
        if (!docenteBase) {
            tools.showToast('Docente no encontrado en el catálogo', 'error');
            return;
        }

        docentesDictablesActuales.push({
            idDocente: docenteBase.idDocente,
            nombreCompleto: docenteBase.nombreCompleto,
            dni: docenteBase.dni
        });
        renderizarDocentesDictables();
    }

    async function guardarDocentesDictables() {
        if (!cursoSeleccionado) {
            tools.showToast('Selecciona un curso', 'info');
            return;
        }

        try {
            btnGuardarDocentesCurso.disabled = true;
            const body = {
                idsDocentes: docentesDictablesActuales.map(d => d.idDocente)
            };
            await fetchJson(
                `/admin/cursos/${cursoSeleccionado.idCurso}/docentes`,
                'No se pudieron guardar los docentes',
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                }
            );
            tools.showToast('Docentes actualizados', 'success');
        } catch (err) {
            tools.showToast(err.message, 'error');
        } finally {
            btnGuardarDocentesCurso.disabled = false;
        }
    }

    // ======= CRUD CURSO (CREAR / EDITAR / ELIMINAR) =======

    function construirHtmlFormCurso(curso, tituloCarreraLabel = 'Carrera') {
        const estados = catalogos.estados || [];
        const tipos = catalogos.tipos || [];
        const modalidades = catalogos.modalidades || [];
        const ciclos = catalogos.ciclos || [];
        const carreras = catalogos.carreras || [];

        const selOptions = (lista, valorActual) => lista.map(v => {
            const code = typeof v === 'string' ? v : v.codigo || v.nombre || v.idCarrera;
            const label = typeof v === 'string' ? v : (v.nombre || v.descripcion || code);
            const value = typeof v === 'string' ? v : (v.idCarrera ?? v);
            const selected = (valorActual && String(valorActual) === String(value)) ? 'selected' : '';
            return `<option value="${value}" ${selected}>${label}</option>`;
        }).join('');

        return `
            <div class="swal-form-grid">
                <div class="form-field">
                    <label>Código</label>
                    <input id="swalCursoCodigo" type="text" value="${curso?.codigo || ''}" maxlength="20" />
                </div>
                <div class="form-field">
                    <label>Nombre</label>
                    <input id="swalCursoNombre" type="text" value="${curso?.nombre || ''}" />
                </div>
                <div class="form-field">
                    <label>${tituloCarreraLabel}</label>
                    <select id="swalCursoCarrera">
                        <option value="">Seleccione</option>
                        ${selOptions(carreras, curso?.idCarrera)}
                    </select>
                </div>
                <div class="form-field">
                    <label>Ciclo</label>
                    <select id="swalCursoCiclo">
                        <option value="">Seleccione</option>
                        ${selOptions(ciclos, curso?.ciclo)}
                    </select>
                </div>
                <div class="form-field">
                    <label>Tipo</label>
                    <select id="swalCursoTipo">
                        <option value="">Seleccione</option>
                        ${selOptions(tipos, curso?.tipo)}
                    </select>
                </div>
                <div class="form-field">
                    <label>Modalidad</label>
                    <select id="swalCursoModalidad">
                        <option value="">Seleccione</option>
                        ${selOptions(modalidades, curso?.modalidad)}
                    </select>
                </div>
                <div class="form-field">
                    <label>Créditos</label>
                    <input id="swalCursoCreditos" type="number" min="0" max="20" value="${curso?.creditos ?? ''}" />
                </div>
                <div class="form-field">
                    <label>Horas semanales</label>
                    <input id="swalCursoHoras" type="number" min="0" max="40" value="${curso?.horasSemanales ?? ''}" />
                </div>
                <div class="form-field">
                    <label>Estado</label>
                    <select id="swalCursoEstado">
                        <option value="">Seleccione</option>
                        ${selOptions(estados, curso?.estado)}
                    </select>
                </div>
                <div class="form-field form-field--full">
                    <label>Descripción</label>
                    <textarea id="swalCursoDescripcion" rows="3">${curso?.descripcion || ''}</textarea>
                </div>
            </div>
        `;
    }

    function leerDatosFormCursoDesdeSwal() {
        const popup = Swal.getPopup();
        const get = id => popup.querySelector(`#${id}`);

        const codigo = get('swalCursoCodigo')?.value.trim();
        const nombre = get('swalCursoNombre')?.value.trim();
        const carreraId = get('swalCursoCarrera')?.value;
        const ciclo = get('swalCursoCiclo')?.value;
        const tipo = get('swalCursoTipo')?.value;
        const modalidad = get('swalCursoModalidad')?.value;
        const creditos = get('swalCursoCreditos')?.value;
        const horas = get('swalCursoHoras')?.value;
        const estado = get('swalCursoEstado')?.value;
        const descripcion = get('swalCursoDescripcion')?.value.trim();

        if (!codigo) {
            Swal.showValidationMessage('El código es obligatorio');
            return null;
        }
        if (!nombre) {
            Swal.showValidationMessage('El nombre es obligatorio');
            return null;
        }
        if (!carreraId) {
            Swal.showValidationMessage('Selecciona una carrera');
            return null;
        }
        if (!ciclo) {
            Swal.showValidationMessage('Selecciona un ciclo');
            return null;
        }
        if (!tipo) {
            Swal.showValidationMessage('Selecciona un tipo de curso');
            return null;
        }
        if (!modalidad) {
            Swal.showValidationMessage('Selecciona una modalidad');
            return null;
        }
        if (!creditos || Number(creditos) <= 0) {
            Swal.showValidationMessage('Ingresa los créditos del curso');
            return null;
        }
        if (!horas || Number(horas) <= 0) {
            Swal.showValidationMessage('Ingresa las horas semanales');
            return null;
        }
        if (!estado) {
            Swal.showValidationMessage('Selecciona un estado');
            return null;
        }

        return {
            codigo,
            nombre,
            idCarrera: Number(carreraId),
            ciclo,
            tipo,
            modalidad,
            creditos: Number(creditos),
            horasSemanales: Number(horas),
            estado,
            descripcion
        };
    }

    function abrirModalCursoNuevo() {
        Swal.fire({
            title: 'Nuevo curso',
            html: construirHtmlFormCurso(null),
            width: 700,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: 'Guardar',
            cancelButtonText: 'Cancelar',
            preConfirm: async () => {
                const payload = leerDatosFormCursoDesdeSwal();
                if (!payload) return false;
                try {
                    await fetchJson('/admin/cursos', 'No se pudo crear el curso', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    return payload;
                } catch (err) {
                    Swal.showValidationMessage(err.message || 'Error al crear el curso');
                    return false;
                }
            }
        }).then(async result => {
            if (!result.isConfirmed) return;
            tools.showToast('Curso creado correctamente', 'success');
            await buscarCursos();
        });
    }

    function abrirModalEditarCurso(curso) {
        if (!curso || !cursoSeleccionado) return;

        const cursoForm = {
            codigo: curso.codigo,
            nombre: curso.nombre,
            idCarrera: curso.idCarrera,
            ciclo: curso.ciclo,
            tipo: curso.tipo,
            modalidad: curso.modalidad,
            creditos: curso.creditos,
            horasSemanales: curso.horasSemanales,
            estado: curso.estado,
            descripcion: curso.descripcion
        };

        Swal.fire({
            title: 'Editar curso',
            html: construirHtmlFormCurso(cursoForm),
            width: 700,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: 'Guardar',
            cancelButtonText: 'Cancelar',
            preConfirm: async () => {
                const payload = leerDatosFormCursoDesdeSwal();
                if (!payload) return false;
                try {
                    await fetchJson(`/admin/cursos/${cursoSeleccionado.idCurso}`, 'No se pudo actualizar el curso', {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    return payload;
                } catch (err) {
                    Swal.showValidationMessage(err.message || 'Error al actualizar el curso');
                    return false;
                }
            }
        }).then(async result => {
            if (!result.isConfirmed) return;
            tools.showToast('Curso actualizado correctamente', 'success');
            await buscarCursos();
        });
    }

    function confirmarEliminarCurso(curso) {
        Swal.fire({
            title: '¿Eliminar curso?',
            text: `Se eliminará el curso "${curso.nombre}" (${curso.codigo}). Esta acción puede afectar secciones y matrículas relacionadas.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
            confirmButtonColor: '#d33'
        }).then(async result => {
            if (!result.isConfirmed) return;
            try {
                await fetchJson(`/admin/cursos/${curso.idCurso}`, 'No se pudo eliminar el curso', {
                    method: 'DELETE'
                });
                tools.showToast('Curso eliminado', 'success');
                cursoSeleccionado = null;
                limpiarDetalle();
                await buscarCursos();
            } catch (err) {
                tools.showToast(err.message, 'error');
            }
        });
    }
    // ======= API PÚBLICA DEL MÓDULO =======
    function resetVista() {
        // se usa por si al cambiar de pestaña quieres limpiar estados
        limpiarDetalle();
    }

    return { init, resetVista };
}