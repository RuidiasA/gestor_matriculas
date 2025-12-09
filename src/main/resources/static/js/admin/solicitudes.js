// Gestión de solicitudes de apertura de secciones

export function createSolicitudesModule(tools) {
    const tablaSolicitudes = document.querySelector('#tablaSolicitudesBody');
    const estadoSolicitudes = document.getElementById('estadoSolicitudes');
    const formFiltros = document.getElementById('formFiltroSolicitudes');
    const btnLimpiar = document.getElementById('btnLimpiarSolicitudes');
    const btnExportar = document.getElementById('btnExportarSolicitudes');
    const selectCurso = document.getElementById('filtroSolicitudCurso');
    const selectCarrera = document.getElementById('filtroSolicitudCarrera');
    const selectCiclo = document.getElementById('filtroSolicitudCiclo');
    const selectEstado = document.getElementById('filtroSolicitudEstado');
    const inputDesde = document.getElementById('filtroSolicitudDesde');
    const inputHasta = document.getElementById('filtroSolicitudHasta');

    const detalleCampos = {
        curso: document.getElementById('solicitudCurso'),
        estado: document.getElementById('solicitudEstado'),
        chip: document.getElementById('solicitudEstadoChip'),
        alumno: document.getElementById('solicitudAlumno'),
        codigo: document.getElementById('solicitudCodigoAlumno'),
        carrera: document.getElementById('solicitudCarrera'),
        ciclo: document.getElementById('solicitudCiclo'),
        fecha: document.getElementById('solicitudFecha'),
        dia: document.getElementById('solicitudDia'),
        horario: document.getElementById('solicitudHorario'),
        modalidad: document.getElementById('solicitudModalidad'),
        turno: document.getElementById('solicitudTurno'),
        motivo: document.getElementById('solicitudMotivo'),
        mensajeAdmin: document.getElementById('solicitudMensajeAdmin'),
        evidenciaNombre: document.getElementById('solicitudEvidenciaNombre'),
        btnEvidencia: document.getElementById('btnDescargarEvidencia'),
        relacionados: document.getElementById('solicitudRelacionados'),
        btnSolucionar: document.getElementById('btnSolucionarSolicitud'),
        btnRechazar: document.getElementById('btnRechazarSolicitud')
    };

    let solicitudes = [];
    let solicitudSeleccionada = null;

    function init() {
        cargarCatalogos();
        cargarSolicitudes();
        if (formFiltros) {
            formFiltros.addEventListener('submit', e => {
                e.preventDefault();
                cargarSolicitudes();
            });
        }
        btnLimpiar?.addEventListener('click', limpiarFiltros);
        btnExportar?.addEventListener('click', exportarCsv);
        detalleCampos.btnSolucionar?.addEventListener('click', () => actualizarEstado('SOLUCIONADA'));
        detalleCampos.btnRechazar?.addEventListener('click', () => actualizarEstado('RECHAZADA'));
        detalleCampos.btnEvidencia?.addEventListener('click', descargarEvidencia);
    }

    function limpiarFiltros() {
        [selectCurso, selectCarrera, selectCiclo, selectEstado, inputDesde, inputHasta].forEach(sel => {
            if (sel?.tagName === 'SELECT') sel.value = '';
            if (sel?.tagName === 'INPUT') sel.value = '';
        });
        cargarSolicitudes();
    }

    async function cargarCatalogos() {
        try {
            const data = await fetchJson('/admin/solicitudes/catalogo', 'No se pudo cargar el catálogo');
            tools.fillSelect(selectCurso, data.cursos || [], 'Todos', c => c.id, c => `${c.codigo || ''} ${c.nombre || ''}`.trim());
            tools.fillSelect(selectCarrera, data.carreras || [], 'Todas', c => c.id, c => c.nombre);
            tools.fillSelect(selectCiclo, data.ciclos || [], 'Todos', c => c, c => c);
        } catch (e) {
            tools.showToast(e.message || 'Error al cargar catálogos', 'error');
        }
    }

    async function cargarSolicitudes() {
        if (!tablaSolicitudes) return;
        tools.renderEmptyRow(tablaSolicitudes, 9, 'Cargando solicitudes...');
        const params = new URLSearchParams();
        if (selectCurso?.value) params.append('cursoId', selectCurso.value);
        if (selectCarrera?.value) params.append('carreraId', selectCarrera.value);
        if (selectCiclo?.value) params.append('ciclo', selectCiclo.value);
        if (selectEstado?.value) params.append('estado', selectEstado.value);
        if (inputDesde?.value) params.append('desde', inputDesde.value);
        if (inputHasta?.value) params.append('hasta', inputHasta.value);
        const url = params.toString() ? `/admin/solicitudes?${params.toString()}` : '/admin/solicitudes';

        try {
            solicitudes = await fetchJson(url, 'No se pudieron cargar las solicitudes');
            renderTabla();
            actualizarBadge();
        } catch (e) {
            tools.renderEmptyRow(tablaSolicitudes, 9, e.message || 'No se pudo cargar');
        }
    }

    function renderTabla() {
        if (!tablaSolicitudes) return;
        tablaSolicitudes.innerHTML = '';
        if (!solicitudes?.length) {
            tools.renderEmptyRow(tablaSolicitudes, 9, 'No hay solicitudes con los filtros aplicados');
            limpiarDetalle();
            return;
        }
        solicitudes.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${s.curso || '-'}</td>
                <td>${s.carrera || '-'}</td>
                <td>${s.ciclo || '-'}</td>
                <td>${s.diaSolicitado || '-'}</td>
                <td>${formatearHorario(s.horaInicioSolicitada, s.horaFinSolicitada)}</td>
                <td>${s.modalidadSolicitada || '-'}</td>
                <td>${s.turnoSolicitado || '-'}</td>
                <td>${s.solicitantes ?? 1}</td>
                <td><span class="chip ${chipClass(s.estado)}">${s.estado || ''}</span></td>
                <td>${formatearFecha(s.fechaSolicitud)}</td>
            `;
            tr.addEventListener('click', () => seleccionarSolicitud(s.id, tr));
            tablaSolicitudes.appendChild(tr);
        });
    }

    function seleccionarSolicitud(id, row) {
        if (!id) return;
        if (tablaSolicitudes && row) {
            tools.markSelectedRow(tablaSolicitudes, row);
        }
        cargarDetalle(id);
    }

    async function cargarDetalle(id) {
        try {
            const detalle = await fetchJson(`/admin/solicitudes/${id}`, 'No se pudo obtener el detalle');
            solicitudSeleccionada = detalle;
            renderDetalle(detalle);
        } catch (e) {
            tools.showToast(e.message || 'No se pudo cargar el detalle', 'error');
        }
    }

    function renderDetalle(detalle) {
        if (!detalle) {
            limpiarDetalle();
            return;
        }
        detalleCampos.curso.textContent = `${detalle.curso || '-'} (${detalle.codigoCurso || ''})`;
        detalleCampos.estado.textContent = detalle.estado || '';
        detalleCampos.chip.textContent = detalle.estado || '—';
        detalleCampos.chip.className = `chip ${chipClass(detalle.estado)}`;
        detalleCampos.alumno.textContent = detalle.alumno || '—';
        detalleCampos.codigo.textContent = detalle.codigoAlumno || '—';
        detalleCampos.carrera.textContent = detalle.carrera || '—';
        detalleCampos.ciclo.textContent = detalle.ciclo || '—';
        detalleCampos.fecha.textContent = formatearFecha(detalle.fechaSolicitud);
        detalleCampos.dia.textContent = detalle.diaSolicitado || '—';
        detalleCampos.horario.textContent = formatearHorario(detalle.horaInicioSolicitada, detalle.horaFinSolicitada);
        detalleCampos.modalidad.textContent = detalle.modalidadSolicitada || detalle.modalidad || '—';
        detalleCampos.turno.textContent = detalle.turnoSolicitado || detalle.turno || '—';
        detalleCampos.motivo.textContent = detalle.motivo || '—';
        detalleCampos.mensajeAdmin.value = detalle.mensajeAdmin || '';

        if (detalleCampos.evidenciaNombre) {
            detalleCampos.evidenciaNombre.textContent = detalle.evidenciaNombreArchivo || 'Sin archivo adjunto';
        }
        if (detalleCampos.btnEvidencia) {
            detalleCampos.btnEvidencia.disabled = !detalle.evidenciaUrl;
            detalleCampos.btnEvidencia.dataset.url = detalle.evidenciaUrl || '';
        }

        if (detalleCampos.relacionados) {
            detalleCampos.relacionados.innerHTML = '';
            (detalle.relacionados || []).forEach(rel => {
                const li = document.createElement('li');
                li.innerHTML = `<span>${rel.alumno || 'Alumno'}</span><span class="chip ${chipClass(rel.estado)}">${rel.estado || ''}</span>`;
                detalleCampos.relacionados.appendChild(li);
            });
            if (!detalle.relacionados?.length) {
                const li = document.createElement('li');
                li.textContent = 'Sin más solicitudes para este curso';
                detalleCampos.relacionados.appendChild(li);
            }
        }

        const habilitar = !!detalle.id && detalle.estado === 'PENDIENTE';
        detalleCampos.btnSolucionar.disabled = !habilitar;
        detalleCampos.btnRechazar.disabled = !habilitar;
    }

    function descargarEvidencia() {
        if (!solicitudSeleccionada?.evidenciaUrl) return;
        const link = document.createElement('a');
        link.href = solicitudSeleccionada.evidenciaUrl;
        link.target = '_blank';
        link.rel = 'noopener';
        link.download = solicitudSeleccionada.evidenciaNombreArchivo || 'evidencia';
        document.body.appendChild(link);
        link.click();
        link.remove();
    }

    async function actualizarEstado(estado) {
        if (!solicitudSeleccionada?.id) return;
        try {
            await fetchJson(`/admin/solicitudes/${solicitudSeleccionada.id}/estado`, 'No se pudo actualizar la solicitud', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: solicitudSeleccionada.id,
                    estado,
                    mensajeAdmin: detalleCampos.mensajeAdmin?.value || ''
                })
            });

            tools.showToast('Solicitud actualizada', 'success');
            await cargarSolicitudes();
            await cargarDetalle(solicitudSeleccionada.id);
        } catch (e) {
            tools.showToast(e.message || 'No se pudo actualizar', 'error');
        }
    }

    async function exportarCsv() {
        const params = new URLSearchParams();
        if (selectCurso?.value) params.append('cursoId', selectCurso.value);
        if (selectCarrera?.value) params.append('carreraId', selectCarrera.value);
        if (selectCiclo?.value) params.append('ciclo', selectCiclo.value);
        if (selectEstado?.value) params.append('estado', selectEstado.value);
        if (inputDesde?.value) params.append('desde', inputDesde.value);
        if (inputHasta?.value) params.append('hasta', inputHasta.value);
        const url = params.toString() ? `/admin/solicitudes/export?${params.toString()}` : '/admin/solicitudes/export';
        try {
            const resp = await fetch(url);
            if (!resp.ok) throw new Error('No se pudo exportar');
            const texto = await resp.text();
            const blob = new Blob([texto], { type: 'text/csv;charset=utf-8;' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = 'solicitudes.csv';
            link.click();
        } catch (e) {
            tools.showToast(e.message || 'Error al exportar', 'error');
        }
    }

    function limpiarDetalle() {
        solicitudSeleccionada = null;
        if (detalleCampos) {
            detalleCampos.curso.textContent = 'Selecciona una solicitud';
            detalleCampos.estado.textContent = '';
            detalleCampos.chip.textContent = '—';
            detalleCampos.chip.className = 'chip';
            detalleCampos.alumno.textContent = '—';
            detalleCampos.codigo.textContent = '—';
            detalleCampos.carrera.textContent = '—';
            detalleCampos.ciclo.textContent = '—';
            detalleCampos.fecha.textContent = '—';
            detalleCampos.dia.textContent = '—';
            detalleCampos.horario.textContent = '—';
            detalleCampos.modalidad.textContent = '—';
            detalleCampos.turno.textContent = '—';
            detalleCampos.motivo.textContent = '—';
            detalleCampos.mensajeAdmin.value = '';
            if (detalleCampos.evidenciaNombre) detalleCampos.evidenciaNombre.textContent = '—';
            if (detalleCampos.btnEvidencia) {
                detalleCampos.btnEvidencia.disabled = true;
                detalleCampos.btnEvidencia.dataset.url = '';
            }
            detalleCampos.relacionados.innerHTML = '';
            detalleCampos.btnSolucionar.disabled = true;
            detalleCampos.btnRechazar.disabled = true;
        }
    }

    function chipClass(estado) {
        if (!estado) return '';
        const valor = estado.toLowerCase();
        if (valor.includes('pendiente')) return 'chip-pendiente';
        if (valor.includes('solucion')) return 'chip-solucionada';
        if (valor.includes('rechaz')) return 'chip-rechazada';
        return '';
    }

    function formatearFecha(fecha) {
        if (!fecha) return '—';
        const d = new Date(fecha);
        return d.toLocaleDateString('es-PE');
    }

    function formatearHorario(inicio, fin) {
        if (!inicio && !fin) return '—';
        if (!inicio || !fin) return inicio || fin;
        return `${inicio} - ${fin}`;
    }

    async function actualizarBadge() {
        try {
            const data = await fetchJson('/admin/solicitudes/count?estado=PENDIENTE', 'No se pudo actualizar el contador');
            const badge = document.getElementById('badge-solicitudes');
            if (badge && typeof data.total === 'number') {
                badge.textContent = data.total;
                badge.hidden = data.total === 0;
            }
        } catch (e) {
            // silencioso
        }
    }

    async function fetchJson(url, errorMessage, options = {}) {
        const resp = await fetch(url, options);
        if (!resp.ok) {
            const msg = await resp.text();
            throw new Error(msg || errorMessage || 'Error en la solicitud');
        }
        if (resp.status === 204) return null;
        return resp.json();
    }

    return { init, actualizarBadge, cargarSolicitudes };
}
