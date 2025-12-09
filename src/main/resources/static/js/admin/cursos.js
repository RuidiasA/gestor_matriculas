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
    const btnLimpiar = seccion.querySelector('#btnLimpiarCursos');
    const btnNuevo = seccion.querySelector('#btnNuevoCurso');
    const estadoBusqueda = seccion.querySelector('#estadoBusquedaCursos');
    const badgeTotalCursos = seccion.querySelector('#badgeTotalCursos');

    // Tabla
    const tablaCursosBody = seccion.querySelector('#tablaCursos tbody');

    // Detalle general
    const campoCodigo = seccion.querySelector('#cursoCodigo');
    const campoNombre = seccion.querySelector('#cursoNombre');
    const campoCarrera = seccion.querySelector('#cursoCarrera');
    const campoCiclo = seccion.querySelector('#cursoCiclo');
    const campoTipo = seccion.querySelector('#cursoTipo');
    const campoCreditos = seccion.querySelector('#cursoCreditos');
    const campoHoras = seccion.querySelector('#cursoHoras');
    const campoModalidad = seccion.querySelector('#cursoModalidad');
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
        modalidades: [],
        cursos: [],
        docentes: []
    };

    let cursosActuales = [];
    let cursoSeleccionado = null;

    let prerrequisitosActuales = [];
    let docentesDictablesActuales = [];

    // ======= HELPERS FETCH (CORREGIDOS) =======

    async function fetchJson(url, errorMsg, options = {}) {
        const resp = await fetch(url, options);

        if (!resp.ok) {
            const texto = await resp.text();
            throw new Error(texto || errorMsg);
        }

        // Si hay contenido → intentar json
        const text = await resp.text();
        if (!text) return null;

        try {
            return JSON.parse(text);
        } catch {
            return text;
        }
    }

    async function fetchVoid(url, errorMsg, options = {}) {
        const resp = await fetch(url, options);
        if (!resp.ok) {
            const texto = await resp.text();
            throw new Error(texto || errorMsg);
        }
        return true;
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
                filtroTexto.value = '';
                filtroCarrera.value = '';
                filtroCiclo.value = '';
                filtroTipo.value = '';
                cursoSeleccionado = null;
                limpiarDetalle();
                buscarCursos();
            });
        }

        btnNuevo?.addEventListener('click', () => abrirModalCursoNuevo());

        btnEditarCurso?.addEventListener('click', () => {
            if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');
            abrirModalEditarCurso(cursoSeleccionado);
        });

        btnEliminarCurso?.addEventListener('click', () => {
            if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');
            confirmarEliminarCurso(cursoSeleccionado);
        });

        btnAgregarPrerrequisito?.addEventListener('click', agregarPrerrequisito);
        btnGuardarPrerrequisitos?.addEventListener('click', guardarPrerrequisitos);

        btnAgregarDocenteCurso?.addEventListener('click', agregarDocenteDictable);
        btnGuardarDocentesCurso?.addEventListener('click', guardarDocentesDictables);
    }

    // ======= CARGA DE CATÁLOGOS =======

    async function cargarCatalogos() {
        const data = await fetchJson('/admin/cursos/catalogos', 'No se pudieron cargar catálogos');
        catalogos.carreras = data.carreras ?? [];
        catalogos.ciclos = data.ciclos ?? [];
        catalogos.tipos = data.tipos ?? [];
        catalogos.modalidades = data.modalidades ?? [];
        catalogos.cursos = data.cursos ?? [];
        catalogos.docentes = data.docentes ?? [];

        tools.fillSelect(filtroCarrera, catalogos.carreras, 'Todas', c => c.id, c => c.nombre);
        tools.fillSelect(filtroCiclo, catalogos.ciclos, 'Todos', c => c, c => c);
        tools.fillSelect(filtroTipo, catalogos.tipos, 'Todos', t => t, t => t);
        tools.fillSelect(selectPrerrequisito, catalogos.cursos, 'Seleccione', c => c.id, c => `${c.codigo} - ${c.nombre}`);
        tools.fillSelect(
            selectDocenteCurso,
            catalogos.docentes,
            'Seleccione',
            d => d.idDocente ?? d.id,
            d => d.nombreCompleto
        );
    }

    // ======= LISTADO =======

    async function buscarCursos() {
        tools.showStatus(estadoBusqueda, 'Buscando...', false);
        tools.renderEmptyRow(tablaCursosBody, 7, 'Cargando...');

        const params = new URLSearchParams();
        if (filtroTexto.value.trim()) params.append('filtro', filtroTexto.value.trim());
        if (filtroCarrera.value) params.append('carreraId', filtroCarrera.value);
        if (filtroCiclo.value) params.append('ciclo', filtroCiclo.value);
        if (filtroTipo.value) params.append('tipo', filtroTipo.value);

        let url = '/admin/cursos';
        if (params.toString()) url += `?${params.toString()}`;

        const cursos = await fetchJson(url, 'Error al listar cursos');
        cursosActuales = Array.isArray(cursos) ? cursos : [];

        renderizarListaCursos(cursosActuales);
        estadoBusqueda.hidden = true;
    }

    function renderizarListaCursos(lista) {
        tablaCursosBody.innerHTML = '';

        if (badgeTotalCursos) {
            badgeTotalCursos.textContent = `${lista.length} curso${lista.length === 1 ? '' : 's'}`;
        }

        if (!lista.length) {
            tools.renderEmptyRow(tablaCursosBody, 7, 'Sin resultados');
            deshabilitarDetalle();
            return;
        }

        lista.forEach(curso => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${curso.codigo}</td>
                <td>${curso.nombre}</td>
                <td>${curso.ciclo}</td>
                <td>${curso.creditos}</td>
                <td>${curso.horasSemanales}</td>
                <td>${curso.modalidad ?? '-'}</td>
                <td>${curso.tipo ?? '-'}</td>
            `;
            tr.addEventListener('click', () => {
                tools.markSelectedRow(tablaCursosBody, tr);
                cargarDetalleCurso(curso.id);
            });
            tablaCursosBody.appendChild(tr);
        });
    }

    // ======= DETALLE =======

    async function cargarDetalleCurso(id) {
        mostrarCargandoDetalle();
        const detalle = await fetchJson(`/admin/cursos/${id}`, 'Error al obtener detalle');

        cursoSeleccionado = detalle;

        prerrequisitosActuales = detalle.prerrequisitos || [];
        docentesDictablesActuales = detalle.docentesDictables || [];

        renderizarDetalleCurso(detalle);
        renderizarPrerrequisitos();
        renderizarDocentesDictables();
        habilitarDetalle(true);
    }

    function mostrarCargandoDetalle() {
        campoCodigo.textContent = 'Cargando...';
        campoNombre.textContent = 'Cargando...';
        campoCarrera.textContent = '-';
        campoCiclo.textContent = '-';
        campoTipo.textContent = '-';
        campoCreditos.textContent = '-';
        campoHoras.textContent = '-';
        campoModalidad.textContent = '-';
        campoDescripcion.textContent = 'Cargando...';

        tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Cargando...');
        tools.renderEmptyRow(tablaDocentesCursoBody, 3, 'Cargando...');

        habilitarDetalle(false);
    }

    function renderizarDetalleCurso(curso) {
        campoCodigo.textContent = curso.codigo;
        campoNombre.textContent = curso.nombre;
        campoCarrera.textContent = curso.carrera ?? '-';
        campoCiclo.textContent = curso.ciclo;
        campoTipo.textContent = curso.tipo;
        campoCreditos.textContent = curso.creditos;
        campoHoras.textContent = curso.horasSemanales;
        campoModalidad.textContent = curso.modalidad;
        campoDescripcion.textContent = curso.descripcion;
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
        campoDescripcion.textContent = '-';

        prerrequisitosActuales = [];
        docentesDictablesActuales = [];

        tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Selecciona un curso');
        tools.renderEmptyRow(tablaDocentesCursoBody, 3, 'Selecciona un curso');

        habilitarDetalle(false);
    }

    function habilitarDetalle(flag) {
        btnEditarCurso.disabled = !flag;
        btnEliminarCurso.disabled = !flag;
        btnAgregarPrerrequisito.disabled = !flag;
        btnGuardarPrerrequisitos.disabled = !flag;
        btnAgregarDocenteCurso.disabled = !flag;
        btnGuardarDocentesCurso.disabled = !flag;
    }

    function deshabilitarDetalle() {
        habilitarDetalle(false);
    }

    // ======= PRERREQUISITOS =======

    function renderizarPrerrequisitos() {
        tablaPrerrequisitosBody.innerHTML = '';

        if (!prerrequisitosActuales.length) {
            tools.renderEmptyRow(tablaPrerrequisitosBody, 3, 'Sin prerrequisitos');
            return;
        }

        prerrequisitosActuales.forEach(pr => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${pr.codigo}</td>
                <td>${pr.nombre}</td>
                <td class="text-right">
                    <button class="btn-link btn-sm">Quitar</button>
                </td>
            `;
            tr.querySelector('button').onclick = () => {
                prerrequisitosActuales = prerrequisitosActuales.filter(p => p.idCurso !== pr.idCurso);
                renderizarPrerrequisitos();
            };

            tablaPrerrequisitosBody.appendChild(tr);
        });
    }

    function agregarPrerrequisito() {
        if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');

        const idSel = Number(selectPrerrequisito.value);
        if (!idSel) return;

        if (idSel === cursoSeleccionado.id) {
            return tools.showToast('Un curso no puede ser prerrequisito de sí mismo', 'warning');
        }

        if (prerrequisitosActuales.some(p => p.idCurso === idSel)) {
            return tools.showToast('Ya está agregado', 'info');
        }

        const base = catalogos.cursos.find(c => c.id === idSel);
        if (!base) return;

        prerrequisitosActuales.push({
            idCurso: base.id,
            codigo: base.codigo,
            nombre: base.nombre
        });

        renderizarPrerrequisitos();
    }

    async function guardarPrerrequisitos() {
        if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');

        const body = {
            idsPrerrequisitos: prerrequisitosActuales.map(p => p.idCurso)
        };

        try {
            btnGuardarPrerrequisitos.disabled = true;

            await fetchVoid(`/admin/cursos/${cursoSeleccionado.id}/prerrequisitos`,
                'Error al guardar prerrequisitos',
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

            tools.showToast('Prerrequisitos actualizados', 'success');
        } catch (e) {
            tools.showToast(e.message, 'error');
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
                <td>${doc.nombreCompleto}</td>
                <td>${doc.dni ?? '-'}</td>
                <td class="text-right"><button class="btn-link btn-sm">Quitar</button></td>
            `;
            tr.querySelector('button').onclick = () => {
                docentesDictablesActuales = docentesDictablesActuales.filter(d => d.idDocente !== doc.idDocente);
                renderizarDocentesDictables();
            };

            tablaDocentesCursoBody.appendChild(tr);
        });
    }

    function agregarDocenteDictable() {
        if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');

        const idSel = Number(selectDocenteCurso.value);
        if (!idSel) return;

        if (docentesDictablesActuales.some(d => d.idDocente === idSel)) {
            return tools.showToast('Docente ya agregado', 'info');
        }

        const base = catalogos.docentes.find(d => d.idDocente === idSel);
        if (!base) return;

        docentesDictablesActuales.push({
            idDocente: base.idDocente,
            nombreCompleto: base.nombreCompleto,
            dni: base.dni
        });

        renderizarDocentesDictables();
    }

    async function guardarDocentesDictables() {
        if (!cursoSeleccionado) return tools.showToast('Selecciona un curso', 'info');

        const body = {
            idsDocentes: docentesDictablesActuales.map(p => p.idDocente)
        };

        try {
            btnGuardarDocentesCurso.disabled = true;

            await fetchVoid(`/admin/cursos/${cursoSeleccionado.id}/docentes`,
                'Error al guardar docentes',
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(body)
                });

            tools.showToast('Docentes actualizados', 'success');
        } catch (e) {
            tools.showToast(e.message, 'error');
        } finally {
            btnGuardarDocentesCurso.disabled = false;
        }
    }

    // ======= CRUD =======

    function construirHtmlFormCurso(curso = {}) {
        const tipos = catalogos.tipos ?? [];
        const modalidades = catalogos.modalidades ?? [];
        const ciclos = catalogos.ciclos ?? [];
        const carreras = catalogos.carreras ?? [];

        const buildOptions = (lista, val) => lista.map(x => {
            const id = x.id ?? x;
            const label = x.nombre ?? x.descripcion ?? x;
            return `<option value="${id}" ${String(val) === String(id) ? 'selected' : ''}>${label}</option>`;
        }).join('');

        return `
            <div class="swal-form-grid">
                <div><label>Código</label><input id="swalCodigo" value="${curso.codigo ?? ''}"></div>
                <div><label>Nombre</label><input id="swalNombre" value="${curso.nombre ?? ''}"></div>
                <div>
                    <label>Carrera</label>
                    <select id="swalCarrera"><option value="">Seleccione</option>${buildOptions(carreras, curso.carreraId)}</select>
                </div>
                <div>
                    <label>Ciclo</label>
                    <select id="swalCiclo"><option value="">Seleccione</option>${buildOptions(ciclos, curso.ciclo)}</select>
                </div>
                <div>
                    <label>Tipo</label>
                    <select id="swalTipo"><option value="">Seleccione</option>${buildOptions(tipos, curso.tipo)}</select>
                </div>
                <div>
                    <label>Modalidad</label>
                    <select id="swalModalidad"><option value="">Seleccione</option>${buildOptions(modalidades, curso.modalidad)}</select>
                </div>
                <div><label>Créditos</label><input id="swalCreditos" type="number" value="${curso.creditos ?? ''}"></div>
                <div><label>Horas semanales</label><input id="swalHoras" type="number" value="${curso.horasSemanales ?? ''}"></div>
                <div class="form-field--full">
                    <label>Descripción</label>
                    <textarea id="swalDescripcion">${curso.descripcion ?? ''}</textarea>
                </div>
            </div>
        `;
    }

    function leerFormCurso() {
        const codigo = Swal.getPopup().querySelector('#swalCodigo').value.trim();
        const nombre = Swal.getPopup().querySelector('#swalNombre').value.trim();
        const carreraId = Swal.getPopup().querySelector('#swalCarrera').value;
        const ciclo = Swal.getPopup().querySelector('#swalCiclo').value;
        const tipo = Swal.getPopup().querySelector('#swalTipo').value;
        const modalidad = Swal.getPopup().querySelector('#swalModalidad').value;
        const creditos = Swal.getPopup().querySelector('#swalCreditos').value;
        const horas = Swal.getPopup().querySelector('#swalHoras').value;
        const descripcion = Swal.getPopup().querySelector('#swalDescripcion').value;

        if (!codigo) return Swal.showValidationMessage('Código obligatorio');
        if (!nombre) return Swal.showValidationMessage('Nombre obligatorio');
        if (!carreraId) return Swal.showValidationMessage('Seleccione carrera');
        if (!ciclo) return Swal.showValidationMessage('Seleccione ciclo');
        if (!tipo) return Swal.showValidationMessage('Seleccione tipo');
        if (!modalidad) return Swal.showValidationMessage('Seleccione modalidad');
        if (!creditos || creditos <= 0) return Swal.showValidationMessage('Créditos inválidos');
        if (!horas || horas <= 0) return Swal.showValidationMessage('Horas inválidas');

        return {
            codigo, nombre,
            carreraId: Number(carreraId),
            ciclo: Number(ciclo),
            tipo, modalidad,
            creditos: Number(creditos),
            horasSemanales: Number(horas),
            descripcion
        };
    }

    function abrirModalCursoNuevo() {
        Swal.fire({
            title: 'Nuevo curso',
            html: construirHtmlFormCurso(),
            width: 700,
            showCancelButton: true,
            confirmButtonText: 'Guardar',
            preConfirm: async () => {
                const payload = leerFormCurso();
                if (!payload) return false;

                await fetchVoid('/admin/cursos', 'Error al crear curso', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                return payload;
            }
        }).then(async r => {
            if (!r.isConfirmed) return;
            tools.showToast('Curso creado correctamente', 'success');
            await cargarCatalogos();
            buscarCursos();
        });
    }

    function abrirModalEditarCurso(curso) {
        Swal.fire({
            title: 'Editar curso',
            html: construirHtmlFormCurso(curso),
            width: 700,
            showCancelButton: true,
            confirmButtonText: 'Guardar cambios',
            preConfirm: async () => {
                const payload = leerFormCurso();
                if (!payload) return false;

                await fetchVoid(`/admin/cursos/${cursoSeleccionado.id}`, 'Error al actualizar curso', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                return payload;
            }
        }).then(async r => {
            if (!r.isConfirmed) return;
            tools.showToast('Curso actualizado', 'success');
            await cargarCatalogos();
            buscarCursos();
        });
    }

    function confirmarEliminarCurso(curso) {
        Swal.fire({
            title: '¿Eliminar curso?',
            text: `Esto eliminará el curso ${curso.nombre} (${curso.codigo})`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Eliminar',
            confirmButtonColor: '#d33'
        }).then(async r => {
            if (!r.isConfirmed) return;

            await fetchVoid(`/admin/cursos/${curso.id}`, 'Error al eliminar curso', {
                method: 'DELETE'
            });

            tools.showToast('Curso eliminado', 'success');
            limpiarDetalle();
            await cargarCatalogos();
            buscarCursos();
        });
    }

    // ======= API PUBLICA =======

    function resetVista() {
        limpiarDetalle();
    }

    return { init, resetVista };
}
