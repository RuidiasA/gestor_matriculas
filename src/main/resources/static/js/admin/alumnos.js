// Módulo de gestión de alumnos en el Portal Admin

export function createAlumnosModule(tools) {
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
    const montoMatricula = document.getElementById('montoMatricula');
    const montoPension = document.getElementById('montoPension');
    const montoTotal = document.getElementById('montoTotal');
    const contenedorHistorial = document.getElementById('contenedorHistorial');
    const subtituloHistorial = document.getElementById('subtituloHistorial');

    let alumnoSeleccionado = null;
    let historialMatriculasCache = [];
    let cursosPorCicloCache = {};
    let ultimoListadoAlumnos = [];

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
            ultimoListadoAlumnos = Array.isArray(alumnos) ? alumnos : [];
            renderizarAlumnos(ultimoListadoAlumnos);
            if (!alumnos.length) {
                tools.showStatus(estadoBusqueda, 'Alumno no encontrado', true);
            } else {
                estadoBusqueda.hidden = true;
            }
            return ultimoListadoAlumnos;
        } catch (err) {
            renderizarAlumnos([]);
            tools.showStatus(estadoBusqueda, err.message || 'No se pudo cargar la lista', true);
            tools.showToast('No se pudo obtener la lista de alumnos', 'error');
            return [];
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
                renderizarHistorial([], null, null);
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
        fichaIds.codigo.textContent = alumno.codigo || alumno.codigoAlumno || '-';
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
        const ordenados = [...periodos].sort((a, b) => b.localeCompare(a));
        const cicloActual = alumno.cicloActual || ordenados[0];
        selectorCiclo.disabled = false;

        if (!ordenados.length) {
            selectorCiclo.disabled = true;
            selectorCiclo.innerHTML = '<option value="">Sin ciclos</option>';
            renderizarCursos([]);
            renderizarResumen({});
            return;
        }

        ordenados.forEach(ciclo => {
            const opt = document.createElement('option');
            opt.value = ciclo;
            opt.textContent = ciclo;
            selectorCiclo.appendChild(opt);
        });

        selectorCiclo.value = cicloActual || ordenados[0];
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

            const historialNormalizado = Array.isArray(historial) ? historial : [];
            historialMatriculasCache = historialNormalizado;
            renderizarCursos(cursos);
            renderizarResumen(resumen);
            renderizarHistorial(historialNormalizado, idAlumno, ciclo);
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
        const tieneDatos = resumen && Object.keys(resumen).length;
        totalCursos.textContent = tieneDatos && resumen.totalCursos != null ? resumen.totalCursos : '-';
        totalCreditos.textContent = tieneDatos && resumen.totalCreditos != null ? resumen.totalCreditos : '-';
        totalHoras.textContent = tieneDatos && resumen.totalHoras != null ? resumen.totalHoras : '-';
        montoMatricula.textContent = tieneDatos && resumen.matricula != null ? formatoMoneda(resumen.matricula) : '-';
        montoPension.textContent = tieneDatos && resumen.pension != null ? formatoMoneda(resumen.pension) : '-';
        montoTotal.textContent = tieneDatos && resumen.montoTotal != null ? formatoMoneda(resumen.montoTotal) : '-';
    }

    function formatoMoneda(valor) {
        if (valor === null || valor === undefined || Number.isNaN(Number(valor))) return '-';
        return `S/ ${Number(valor).toFixed(2)}`;
    }

    function renderizarHistorial(historial, alumnoId, cicloActual) {
        const registros = Array.isArray(historial) ? historial : [];
        const filtrados = cicloActual
            ? registros.filter(h => h.ciclo !== cicloActual)
            : registros;

        filtrados.sort((a, b) => (b.ciclo || '').localeCompare(a.ciclo || ''));

        // Sin historial
        if (!filtrados.length) {
            contenedorHistorial.innerHTML = '<p class="muted">Sin historial</p>';
            subtituloHistorial.textContent = 'Historial de matrícula';
            historialMatriculasCache = [];
            if (alumnoId) cursosPorCicloCache[alumnoId] = [];
            return;
        }

        contenedorHistorial.innerHTML = '';

        filtrados.forEach((h) => {
            const card = document.createElement('article');
            card.className = 'historial-item'; // ahora usa el nuevo diseño CSS

            card.innerHTML = `
            <div class="historial-item__header">
                <div class="meta">
                    <p class="historial-item__ciclo">${h.ciclo || '-'}</p>
                    <span class="badge">${h.estado || '-'}</span>
                </div>

                <button type="button" class="historial-item__toggle">
                    Ver detalle
                    <span class="historial-item__toggle-icon">⌄</span>
                </button>
            </div>

            <div class="historial-badges">
                <span class="historial-badge">Cursos: ${h.totalCursos ?? 0}</span>
                <span class="historial-badge">Créditos: ${h.totalCreditos ?? 0}</span>
                <span class="historial-badge">Horas: ${h.totalHoras ?? 0}</span>
                <span class="historial-badge">Total: ${formatoMoneda(h.montoTotal)}</span>
            </div>

            <div class="historial-item__body">
                ${crearTablaCursosHTML(h.cursos)}
                <div class="historial-pagos">
                    ${crearPagoCard('Matrícula', h.matricula)}
                    ${crearPagoCard('Pensión', h.pension)}
                    ${crearPagoCard('Mora', h.mora)}
                    ${crearPagoCard('Descuentos', h.descuentos)}
                </div>
            </div>
        `;

            const toggle = card.querySelector('.historial-item__toggle');
            const body = card.querySelector('.historial-item__body');

            // SIEMPRE iniciar cerrado — NADIE se abre por defecto
            body.style.maxHeight = '0';
            body.style.opacity = '0';

            toggle.addEventListener('click', () => {
                const isOpen = card.classList.toggle('historial-item--open');

                // Animación usando max-height + opacity
                if (isOpen) {
                    body.style.maxHeight = body.scrollHeight + "px";
                    body.style.opacity = "1";
                    toggle.innerHTML = `Ocultar <span class="historial-item__toggle-icon">⌄</span>`;
                } else {
                    body.style.maxHeight = "0";
                    body.style.opacity = "0";
                    toggle.innerHTML = `Ver detalle <span class="historial-item__toggle-icon">⌄</span>`;
                }
            });

            contenedorHistorial.appendChild(card);
        });

        subtituloHistorial.textContent = `Historial de matrícula (${filtrados.length})`;
        historialMatriculasCache = filtrados;

        if (alumnoId) {
            cursosPorCicloCache[alumnoId] = filtrados;
        }
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

    function crearPagoCard(label, valor) {
        return `
            <div class="historial-pago__card">
                <p class="historial-pago__label">${label}</p>
                <p><strong>${formatoMoneda(valor)}</strong></p>
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
        selectorCiclo.disabled = true;
        renderizarCursos([]);
        renderizarResumen({});
        renderizarHistorial([], null, null);
    }

    function validarContacto() {
        limpiarEstadoContacto();
        const correo = correoPersonalInput.value.trim();
        const telefono = telefonoInput.value.trim();
        const direccion = direccionInput.value.trim();

        correoPersonalInput.setCustomValidity('');
        telefonoInput.setCustomValidity('');

        const errores = [];
        if (correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
            errores.push('Correo personal inválido');
            correoPersonalInput.setCustomValidity('Correo personal inválido');
        }
        if (telefono && !/^\d{9}$/.test(telefono)) {
            errores.push('El teléfono debe tener 9 dígitos');
            telefonoInput.setCustomValidity('Debe contener 9 dígitos');
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
            if (!response.ok) {
                const detalle = await response.text();
                throw new Error(detalle || 'No se pudo actualizar, revise los datos');
            }
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

    async function enfocarAlumnoPorCodigo(codigo) {
        if (!codigo) return;
        document.querySelector('.admin-nav a[data-section="alumnos"]')?.click();
        if (filtroAlumno) filtroAlumno.value = codigo;

        const alumnos = await buscarAlumnos();
        const coincide = alumnos.find(a => `${a.codigo}` === `${codigo}` || `${a.codigoAlumno}` === `${codigo}`);
        if (!coincide) {
            tools.showToast('No se encontró al alumno solicitado', 'info');
            return;
        }

        seleccionarAlumnoEnTabla(coincide.codigo || coincide.codigoAlumno);
    }

    function seleccionarAlumnoEnTabla(codigo) {
        const filas = tablaAlumnos.querySelectorAll('tr');
        filas.forEach(fila => {
            const codigoCelda = fila.querySelector('td')?.textContent;
            if (codigoCelda === codigo) {
                fila.scrollIntoView({ behavior: 'smooth', block: 'center' });
                fila.click();
            }
        });
    }

    return { init, enfocarAlumnoPorCodigo };

    /* ============================================================
       HISTORIAL DE MATRÍCULAS - ACORDEÓN
    ============================================================ */
    document.addEventListener("click", (e) => {
        const toggle = e.target.closest(".historial-item__toggle");
        if (!toggle) return;

        const item = toggle.closest(".historial-item");
        if (!item) return;

        item.classList.toggle("historial-item--open");
    });
}