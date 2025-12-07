// Módulo de gestión de secciones en el Portal Admin

export function createSeccionesModule(tools, alumnosModule) {
    const formBusquedaSecciones = document.getElementById('formBusquedaSecciones');
    const tablaSecciones = document.querySelector('#tablaSecciones tbody');
    const tablaEstudiantesSeccion = document.querySelector('#tablaEstudiantesSeccion tbody');
    const tablaHistorialSeccion = document.querySelector('#tablaHistorialSeccion tbody');
    const tablaCambiosSeccion = document.querySelector('#tablaCambiosSeccion tbody');
    const tablaEstadisticasSeccion = document.querySelector('#tablaEstadisticasSeccion tbody');
    const tabsHistorial = document.querySelectorAll('.tabs .tab');
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
        creditos: document.getElementById('seccionCreditos'),
        ciclo: document.getElementById('seccionCiclo'),
        turno: document.getElementById('seccionTurno'),
        horas: document.getElementById('seccionHoras'),
        cuposDisponibles: document.getElementById('seccionCuposDisponibles'),
        cantidadHorarios: document.getElementById('seccionCantidadHorarios'),
        estadoAcademico: document.getElementById('seccionEstadoAcademico')
    };

    const resumenMetricas = {
        matriculados: document.getElementById('metricMatriculados'),
        cuposLibres: document.getElementById('metricCuposLibres'),
        aprobados: document.getElementById('metricAprobados'),
        aprobacion: document.getElementById('metricAprobacion'),
        retiros: document.getElementById('metricRetiros'),
        horarios: document.getElementById('metricHorarios'),
    };

    const btnEditarSeccion = document.getElementById('btnEditarSeccion');
    const btnGestionarHorarios = document.getElementById('btnGestionarHorarios');
    const btnAnularSeccion = document.getElementById('btnAnularSeccion');
    const btnNuevaSeccion = document.getElementById('btnNuevaSeccion');

    const modalNuevaSeccion = document.getElementById('modalNuevaSeccion');
    const formNuevaSeccion = document.getElementById('formNuevaSeccion');
    const selectCursoNuevaSeccion = document.getElementById('selectCursoNuevaSeccion');
    const inputCodigoNuevaSeccion = document.getElementById('inputCodigoNuevaSeccion');
    const selectDocenteNuevaSeccion = document.getElementById('selectDocenteNuevaSeccion');
    const inputPeriodoNuevaSeccion = document.getElementById('inputPeriodoNuevaSeccion');
    const selectModalidadNuevaSeccion = document.getElementById('selectModalidadNuevaSeccion');
    const selectTurnoNuevaSeccion = document.getElementById('selectTurnoNuevaSeccion');
    const inputCapacidadNuevaSeccion = document.getElementById('inputCapacidadNuevaSeccion');
    const inputAulaNuevaSeccion = document.getElementById('inputAulaNuevaSeccion');
    const contenedorHorariosNuevaSeccion = document.getElementById('contenedorHorariosNuevaSeccion');
    const btnAgregarHorarioNuevaSeccion = document.getElementById('btnAgregarHorarioNuevaSeccion');

    let seccionSeleccionada = null;
    let detalleSeccionActual = null;
    const catalogosSeccion = { docentes: [], modalidades: [], cursos: [] };

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

        if (btnNuevaSeccion) btnNuevaSeccion.addEventListener('click', openNuevaSeccionModal);
        if (btnAgregarHorarioNuevaSeccion) btnAgregarHorarioNuevaSeccion.addEventListener('click', () => agregarFilaHorario());
        if (formNuevaSeccion) {
            formNuevaSeccion.addEventListener('submit', e => {
                e.preventDefault();
                submitNuevaSeccion();
            });
        }

        if (btnEditarSeccion) btnEditarSeccion.onclick = () => abrirModalEdicionSeccion();
        if (btnGestionarHorarios) btnGestionarHorarios.onclick = () => abrirModalGestionHorarios();
        if (btnAnularSeccion) btnAnularSeccion.onclick = () => anularSeccion(seccionSeleccionada);

        tabsHistorial?.forEach(tab => {
            tab.addEventListener('click', () => {
                const objetivo = tab.dataset.tab;
                document.querySelectorAll('.tab-content').forEach(p => p.classList.remove('active'));
                tabsHistorial.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                document.getElementById(objetivo)?.classList.add('active');
            });
        });

        if (modalNuevaSeccion) {
            modalNuevaSeccion.addEventListener('click', evt => {
                if (evt.target === modalNuevaSeccion) cerrarModalNuevaSeccion();
            });
            modalNuevaSeccion.querySelectorAll('[data-close]').forEach(btn => {
                btn.addEventListener('click', cerrarModalNuevaSeccion);
            });
        }
    }

    function openNuevaSeccionModal() {
        if (!modalNuevaSeccion) return;
        formNuevaSeccion?.reset();
        inputCapacidadNuevaSeccion.value = 30;
        renderHorariosNuevaSeccion([{ dia: 'LUNES', horaInicio: '08:00', horaFin: '10:00' }]);
        cargarCatalogosModal();
        modalNuevaSeccion.hidden = false;
    }

    function cerrarModalNuevaSeccion() {
        if (!modalNuevaSeccion) return;
        modalNuevaSeccion.hidden = true;
    }

    function cargarCatalogosModal() {
        if (catalogosSeccion?.cursos?.length) {
            tools.fillSelect(selectCursoNuevaSeccion, catalogosSeccion.cursos, 'Selecciona curso', c => c.idCurso, c => `${c.codigo} - ${c.nombre}`);
        }
        if (catalogosSeccion?.docentes?.length) {
            tools.fillSelect(selectDocenteNuevaSeccion, catalogosSeccion.docentes, 'Selecciona docente', d => d.idDocente, d => d.nombreCompleto);
        }
        if (catalogosSeccion?.modalidades?.length) {
            tools.fillSelect(selectModalidadNuevaSeccion, catalogosSeccion.modalidades, 'Selecciona', m => m, m => m);
        }
    }

    function renderHorariosNuevaSeccion(horarios = []) {
        if (!contenedorHorariosNuevaSeccion) return;
        contenedorHorariosNuevaSeccion.innerHTML = '';
        const lista = horarios.length ? horarios : [{ dia: 'LUNES', horaInicio: '', horaFin: '' }];
        lista.forEach(h => agregarFilaHorario(h));
    }

    function agregarFilaHorario(horario = { dia: 'LUNES', horaInicio: '', horaFin: '' }) {
        if (!contenedorHorariosNuevaSeccion) return;
        const row = document.createElement('div');
        row.className = 'horario-row';
        row.innerHTML = `
            <div class="horario-field">
                <label>Día</label>
                <select class="horario-dia">
                    ${['LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO'].map(d => `<option value="${d}" ${d === (horario.dia || 'LUNES') ? 'selected' : ''}>${d.charAt(0)}${d.slice(1).toLowerCase()}</option>`).join('')}
                </select>
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
        `;
        row.querySelector('.btn-remove-horario')?.addEventListener('click', () => eliminarFilaHorario(row));
        contenedorHorariosNuevaSeccion.appendChild(row);
    }

    function eliminarFilaHorario(row) {
        if (!contenedorHorariosNuevaSeccion) return;
        if (contenedorHorariosNuevaSeccion.children.length <= 1) {
            tools.showToast('Debe existir al menos un horario', 'info');
            return;
        }
        row.remove();
    }

    async function submitNuevaSeccion() {
        try {
            const dto = construirNuevaSeccionDTO();
            const resp = await fetch('/admin/secciones', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });

            if (!resp.ok) {
                const mensaje = await resp.text();
                throw new Error(mensaje || 'No se pudo registrar la sección');
            }

            const data = await resp.json();
            tools.showToast('Sección registrada correctamente', 'success');
            cerrarModalNuevaSeccion();
            seccionSeleccionada = data.idSeccion || data.id || null;
            await buscarSecciones(true);
            if (seccionSeleccionada) {
                cargarFichaSeccion(seccionSeleccionada);
            }
        } catch (err) {
            tools.showToast(err.message || 'No se pudo registrar la sección', 'error');
        }
    }

    function construirNuevaSeccionDTO() {
        const cursoId = selectCursoNuevaSeccion?.value;
        const docenteId = selectDocenteNuevaSeccion?.value;
        const modalidad = selectModalidadNuevaSeccion?.value;
        const turno = selectTurnoNuevaSeccion?.value;
        const codigo = (inputCodigoNuevaSeccion?.value || '').trim();
        const periodo = (inputPeriodoNuevaSeccion?.value || '').trim();
        const capacidad = Number(inputCapacidadNuevaSeccion?.value || 0);
        const aula = (inputAulaNuevaSeccion?.value || '').trim();
        const horarios = Array.from(contenedorHorariosNuevaSeccion?.querySelectorAll('.horario-row') || []).map(row => ({
            dia: row.querySelector('.horario-dia')?.value,
            horaInicio: row.querySelector('.horario-inicio')?.value,
            horaFin: row.querySelector('.horario-fin')?.value,
        })).filter(h => h.dia || h.horaInicio || h.horaFin);

        if (!cursoId || !docenteId || !modalidad || !turno || !codigo || !periodo || !aula) {
            throw new Error('Completa los campos obligatorios');
        }

        if (capacidad < 1) {
            throw new Error('La capacidad debe ser mayor a cero');
        }

        if (!horarios.length) {
            throw new Error('Agrega al menos un horario');
        }

        for (const h of horarios) {
            if (!h.dia || !h.horaInicio || !h.horaFin) {
                throw new Error('Completa todos los campos de horario');
            }
            if (h.horaFin <= h.horaInicio) {
                throw new Error('La hora fin debe ser mayor a la hora inicio');
            }
        }

        return {
            idCurso: Number(cursoId),
            codigoSeccion: codigo,
            docenteId: Number(docenteId),
            periodoAcademico: periodo,
            modalidad,
            turno,
            capacidad,
            aula,
            horarios,
        };
    }

    function resetFicha() {
        limpiarFichaSeccion();
    }

    async function cargarCatalogos() {
        try {
            const data = await fetchJson('/admin/secciones/catalogos', 'No se pudo cargar los catálogos');
            catalogosSeccion.cursos = data.cursos || [];
            catalogosSeccion.docentes = data.docentes || [];
            catalogosSeccion.modalidades = data.modalidades || [];
            tools.fillSelect(filtroCursoSeccion, data.cursos, 'Curso...', item => item.idCurso, item => `${item.codigo} - ${item.nombre}`);
            tools.fillSelect(filtroPeriodoSeccion, data.periodos, 'Seleccione', item => item, item => item);
            tools.fillSelect(filtroDocenteSeccion, data.docentes, 'Seleccione', item => item.idDocente, item => item.nombreCompleto);
            tools.fillSelect(filtroModalidadSeccion, data.modalidades, 'Todas', item => item, item => item);
            cargarCatalogosModal();
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
        tools.renderEmptyRow(tablaCambiosSeccion, 6, 'Selecciona una sección');
        tools.renderEmptyRow(tablaEstadisticasSeccion, 6, 'Selecciona una sección');
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
            const secciones = await fetchJson(url, 'No se pudo buscar secciones');
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
            const idSeccion = sec.idSeccion;
            const estado = (sec.estado || '').toLowerCase();
            tr.innerHTML = `
            <td>${sec.curso || '-'}</td>
            <td>${sec.codigoSeccion || '-'}</td>
            <td>${sec.docente || '-'}</td>
            <td>${sec.periodo || '-'}</td>
            <td>${sec.modalidad || '-'}</td>
            <td>${sec.horario || '-'}</td>
            <td>${sec.aula || '-'}</td>
            <td>${sec.cupos ?? '-'} / ${(sec.matriculados ?? 0)}</td>
            <td><span class="badge">${sec.estado || '-'}</span></td>
        `;
            if (estado === 'anulada') {
                tr.classList.add('anulada');
            }
            if (idSeccion) {
                tr.addEventListener('click', () => {
                    seccionSeleccionada = idSeccion;
                    tools.markSelectedRow(tablaSecciones, tr);
                    cargarFichaSeccion(idSeccion);
                });
                if (preservarSeleccion && seccionSeleccionada && `${seccionSeleccionada}` === `${idSeccion}`) {
                    tools.markSelectedRow(tablaSecciones, tr);
                }
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
        tools.renderEmptyRow(tablaCambiosSeccion, 6, 'Cargando historial...');
        tools.renderEmptyRow(tablaEstadisticasSeccion, 6, 'Cargando estadísticas...');

        try {
            const [detalle, estudiantes, historial, cambios, estadisticas] = await Promise.all([
                fetchJson(`/admin/secciones/${idSeccion}`, 'No se pudo obtener la sección'),
                fetchJson(`/admin/secciones/${idSeccion}/estudiantes`, 'No se pudieron cargar los estudiantes'),
                fetchJson(`/admin/secciones/${idSeccion}/historial`, 'No se pudo cargar el historial'),
                fetchJson(`/admin/secciones/${idSeccion}/cambios`, 'No se pudieron cargar los cambios'),
                fetchJson(`/admin/secciones/${idSeccion}/estadisticas`, 'No se pudieron cargar las estadísticas')
            ]);

            detalleSeccionActual = detalle;
            renderizarFichaSeccion(detalle, estudiantes);
            renderizarHistorial(historial);
            renderizarCambios(cambios);
            renderizarEstadisticas(estadisticas?.resumenPeriodos || []);
            renderizarDashboard(estadisticas);
            activarBotonesFicha();
        } catch (e) {
            tools.showToast(e.message || 'No se pudo cargar la ficha de sección', 'error');
            tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'No fue posible cargar los estudiantes');
            tools.renderEmptyRow(tablaHistorialSeccion, 5, 'No se pudo cargar el historial');
            tools.renderEmptyRow(tablaCambiosSeccion, 6, 'No se pudo cargar el historial');
            tools.renderEmptyRow(tablaEstadisticasSeccion, 6, 'No se pudo cargar las estadísticas');
            detalleSeccionActual = null;
            seccionSeleccionada = null;
            activarBotonesFicha();
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

    function renderizarCambios(cambios) {
        tablaCambiosSeccion.innerHTML = '';
        const registros = Array.isArray(cambios) ? cambios : [];
        if (!registros.length) {
            tools.renderEmptyRow(tablaCambiosSeccion, 6, 'Sin cambios registrados');
            return;
        }
        registros.forEach(reg => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${reg.fecha ? new Date(reg.fecha).toLocaleString() : '-'}</td>
                <td>${reg.usuario || '-'}</td>
                <td>${reg.campoModificado || '-'}</td>
                <td>${reg.valorAnterior || '-'}</td>
                <td>${reg.valorNuevo || '-'}</td>
                <td>${reg.observacion || '-'}</td>
            `;
            tablaCambiosSeccion.appendChild(tr);
        });
    }

    function renderizarEstadisticas(resumen) {
        tablaEstadisticasSeccion.innerHTML = '';
        const registros = Array.isArray(resumen) ? resumen : [];
        if (!registros.length) {
            tools.renderEmptyRow(tablaEstadisticasSeccion, 6, 'Sin datos estadísticos');
            return;
        }
        registros.forEach(reg => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${reg.periodo || '-'}</td>
                <td>${reg.matriculados ?? '-'}</td>
                <td>${reg.retirados ?? '-'}</td>
                <td>${reg.aprobados ?? '-'}</td>
                <td>${reg.desaprobados ?? '-'}</td>
                <td>${reg.porcentajeAprobacion != null ? reg.porcentajeAprobacion + '%' : '-'}</td>
            `;
            tablaEstadisticasSeccion.appendChild(tr);
        });
    }

    function renderizarDashboard(estadisticas) {
        if (!estadisticas || !resumenMetricas) return;
        resumenMetricas.matriculados.textContent = estadisticas.matriculadosActuales ?? '-';
        resumenMetricas.cuposLibres.textContent = estadisticas.cuposLibres ?? '-';
        resumenMetricas.aprobados.textContent = estadisticas.aprobadosUltimoPeriodo ?? '0';
        resumenMetricas.aprobacion.textContent = `${estadisticas.porcentajeAprobacion ?? 0}%`;
        resumenMetricas.retiros.textContent = estadisticas.retiros ?? '0';
        resumenMetricas.horarios.textContent = estadisticas.horariosProgramados ?? '-';

        fichaSeccionCampos.creditos.textContent = estadisticas.creditos ?? '-';
        fichaSeccionCampos.ciclo.textContent = estadisticas.ciclo ?? '-';
        fichaSeccionCampos.turno.textContent = estadisticas.turno ?? '-';
        fichaSeccionCampos.horas.textContent = estadisticas.horasSemanales ?? '-';
        fichaSeccionCampos.cuposDisponibles.textContent = estadisticas.cuposDisponibles ?? '-';
        fichaSeccionCampos.cantidadHorarios.textContent = estadisticas.cantidadHorarios ?? '-';
        fichaSeccionCampos.estadoAcademico.textContent = estadisticas.estadoAcademico || '-';
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
            tr.querySelector('button')?.addEventListener('click', () => alumnosModule?.enfocarAlumnoPorCodigo(est.codigo));
            tablaEstudiantesSeccion.appendChild(tr);
        });
    }

    async function fetchJson(url, errorMessage, options = {}) {
        const response = await fetch(url, options);
        if (!response.ok) {
            const texto = await response.text();
            throw new Error(texto || errorMessage || 'Error al conectar con el servidor');
        }
        return response.json();
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
        if (fichaSeccionCampos.creditos) fichaSeccionCampos.creditos.textContent = valores.creditos ?? '-';
        if (fichaSeccionCampos.ciclo) fichaSeccionCampos.ciclo.textContent = valores.ciclo ?? '-';
        if (fichaSeccionCampos.turno) fichaSeccionCampos.turno.textContent = valores.turno ?? '-';
        if (fichaSeccionCampos.horas) fichaSeccionCampos.horas.textContent = valores.horas ?? '-';
        if (fichaSeccionCampos.cuposDisponibles) fichaSeccionCampos.cuposDisponibles.textContent = valores.cuposDisponibles ?? '-';
        if (fichaSeccionCampos.cantidadHorarios) fichaSeccionCampos.cantidadHorarios.textContent = valores.cantidadHorarios ?? '-';
        if (fichaSeccionCampos.estadoAcademico) fichaSeccionCampos.estadoAcademico.textContent = valores.estadoAcademico ?? '-';

        const badge = fichaSeccionCampos.estado;
        badge.className = "badge";
        const est = valores.estado?.toUpperCase() || '';

        if (est.includes("ANULADA") || est.includes("ANULADO")) badge.classList.add("badge--danger");
        else if (est.includes("ACTIVA") || est.includes("ACTIVO")) badge.classList.add("badge--success");
        else badge.classList.add("badge--info");

        const anulada = est.includes("ANULADA") || est.includes("ANULADO");

        btnEditarSeccion.disabled = anulada;
        btnGestionarHorarios.disabled = anulada;
        btnAnularSeccion.disabled = anulada;

        [btnEditarSeccion, btnGestionarHorarios, btnAnularSeccion].forEach(btn => {
            if (!btn) return;
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
            creditos: '-',
            ciclo: '-',
            turno: '-',
            horas: '-',
            cuposDisponibles: '-',
            cantidadHorarios: '-',
            estadoAcademico: '-',
        });
        tools.renderEmptyRow(tablaEstudiantesSeccion, 4, 'Selecciona una sección para ver estudiantes');
        tools.renderEmptyRow(tablaHistorialSeccion, 5, 'Selecciona una sección');
        tools.renderEmptyRow(tablaCambiosSeccion, 6, 'Selecciona una sección');
        tools.renderEmptyRow(tablaEstadisticasSeccion, 6, 'Selecciona una sección');
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

            buscarSecciones();
            cargarFichaSeccion(id);

        } catch (err) {
            Swal.fire('Error', 'Ocurrió un error inesperado.', 'error');
        }
    }

    function activarBotonesFicha() {
        if (!seccionSeleccionada) {
            if (btnEditarSeccion) btnEditarSeccion.disabled = true;
            if (btnGestionarHorarios) btnGestionarHorarios.disabled = true;
            if (btnAnularSeccion) btnAnularSeccion.disabled = true;
            return;
        }

        if (btnEditarSeccion) btnEditarSeccion.disabled = false;
        if (btnGestionarHorarios) btnGestionarHorarios.disabled = false;
        if (btnAnularSeccion) btnAnularSeccion.disabled = false;

        if (btnEditarSeccion) btnEditarSeccion.onclick = () => abrirModalEdicionSeccion();
        if (btnGestionarHorarios) btnGestionarHorarios.onclick = () => abrirModalGestionHorarios();
        if (btnAnularSeccion) btnAnularSeccion.onclick = () => anularSeccion(seccionSeleccionada);
    }

    return { init, resetFicha };
}
