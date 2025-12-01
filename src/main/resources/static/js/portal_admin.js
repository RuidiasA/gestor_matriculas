document.addEventListener("DOMContentLoaded", () => {
    const navLinks = document.querySelectorAll(".admin-nav a");
    const sections = document.querySelectorAll(".admin-section");

    navLinks.forEach(link => {
        link.addEventListener("click", function (e) {
            e.preventDefault();
            const target = this.getAttribute("data-section");

            sections.forEach(sec => sec.classList.remove("active"));
            document.getElementById(target).classList.add("active");

            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });

    const toastContainer = createToastContainer();
    const tablaAlumnos = document.querySelector("#tablaAlumnos tbody");
    const formBusqueda = document.getElementById("formBusquedaAlumnos");
    const filtroAlumno = document.getElementById("filtroAlumno");
    const btnLimpiarBusqueda = document.getElementById("btnLimpiarBusqueda");
    const btnRefrescar = document.getElementById("btnRefrescarAlumnos");
    const estadoBusqueda = document.getElementById("estadoBusqueda");

    const fichaIds = {
        nombre: document.getElementById("nombreAlumno"),
        codigo: document.getElementById("codigoAlumno"),
        carrera: document.getElementById("carreraAlumno"),
        ciclo: document.getElementById("cicloAlumno"),
        ingreso: document.getElementById("ingresoAlumno"),
        correoInst: document.getElementById("correoInstitucionalAlumno"),
        correoPer: document.getElementById("correoPersonalAlumno"),
        telefono: document.getElementById("telefonoAlumno"),
        estado: document.getElementById("estadoAlumno"),
    };

    const contactoForm = document.getElementById("formContactoAlumno");
    const correoPersonalInput = document.getElementById("correoPersonalInput");
    const telefonoInput = document.getElementById("telefonoInput");
    const direccionInput = document.getElementById("direccionInput");
    const btnEditarContacto = document.getElementById("btnEditarContacto");
    const btnGuardarContacto = document.getElementById("btnGuardarContacto");
    const estadoContacto = document.getElementById("estadoContacto");

    const selectorCiclo = document.getElementById("selectorCiclo");
    const tablaCursos = document.querySelector("#tablaCursosMatriculados tbody");
    const totalCursos = document.getElementById("totalCursos");
    const totalCreditos = document.getElementById("totalCreditos");
    const totalHoras = document.getElementById("totalHoras");
    const montoEstimado = document.getElementById("montoEstimado");
    const contenedorHistorial = document.getElementById("contenedorHistorial");
    const subtituloHistorial = document.getElementById("subtituloHistorial");

    // DOCENTES
    const tablaDocentes = document.querySelector("#tablaDocentes tbody");
    const formBusquedaDocentes = document.getElementById("formBusquedaDocentes");
    const filtroDocente = document.getElementById("filtroDocente");
    const filtroCursoDictable = document.getElementById("filtroCursoDictable");
    const filtroEstadoDocente = document.getElementById("filtroEstadoDocente");
    const estadoBusquedaDocentes = document.getElementById("estadoBusquedaDocentes");
    const selectCursoDictable = document.getElementById("selectCursoDictable");
    const btnAgregarCursoDictable = document.getElementById("btnAgregarCursoDictable");
    const tablaCursosDictables = document.querySelector("#tablaCursosDictables tbody");
    const tablaSeccionesDocente = document.querySelector("#tablaSeccionesDocente tbody");
    const resumenSeccionesDocente = document.getElementById("resumenSeccionesDocente");
    const tablaHistorialDocente = document.querySelector("#tablaHistorialDocente tbody");
    const btnEditarDocente = document.getElementById("btnEditarDocente");
    const btnEditarContactoDocente = document.getElementById("btnEditarContactoDocente");
    const formDatosDocente = document.getElementById("formDatosDocente");
    const formContactoDocente = document.getElementById("formContactoDocente");
    const estadoDatosDocente = document.getElementById("estadoDatosDocente");
    const estadoContactoDocente = document.getElementById("estadoContactoDocente");
    const btnCancelarDatosDocente = document.getElementById("btnCancelarDatosDocente");
    const btnCancelarContactoDocente = document.getElementById("btnCancelarContactoDocente");
    const inputsDocente = {
        codigo: document.getElementById("docCodigo"),
        estado: document.getElementById("docEstado"),
        apellidos: document.getElementById("docApellidos"),
        nombres: document.getElementById("docNombres"),
        dni: document.getElementById("docDni"),
        especialidad: document.getElementById("docEspecialidad"),
        correoInst: document.getElementById("docCorreoInst"),
        correoPer: document.getElementById("docCorreoPer"),
        telefono: document.getElementById("docTelefono"),
        direccion: document.getElementById("docDireccion"),
    };

    const formInputsDocente = {
        apellidos: document.getElementById("inputDocApellidos"),
        nombres: document.getElementById("inputDocNombres"),
        dni: document.getElementById("inputDocDni"),
        especialidad: document.getElementById("inputDocEspecialidad"),
        correoInst: document.getElementById("inputDocCorreoInst"),
        estado: document.getElementById("inputDocEstado"),
    };

    const formContactoInputs = {
        correoInst: document.getElementById("inputDocCorreoInstContacto"),
        correoPer: document.getElementById("inputDocCorreoPer"),
        telefono: document.getElementById("inputDocTelefono"),
        direccion: document.getElementById("inputDocDireccion"),
    };

    // SECCIONES
    const formBusquedaSecciones = document.getElementById("formBusquedaSecciones");
    const tablaSecciones = document.querySelector("#tablaSecciones tbody");
    const tablaEstudiantesSeccion = document.querySelector("#tablaEstudiantesSeccion tbody");
    const filtroCursoSeccion = document.getElementById("filtroCursoSeccion");
    const filtroPeriodoSeccion = document.getElementById("filtroPeriodoSeccion");
    const filtroDocenteSeccion = document.getElementById("filtroDocenteSeccion");
    const filtroModalidadSeccion = document.getElementById("filtroModalidadSeccion");
    const filtroCodigoSeccion = document.getElementById("filtroCodigoSeccion");
    const btnLimpiarSecciones = document.getElementById("btnLimpiarSecciones");
    const fichaSeccion = document.getElementById("fichaSeccion");
    const fichaSeccionCampos = {
        curso: document.getElementById("seccionCurso"),
        estado: document.getElementById("seccionEstado"),
        codigo: document.getElementById("seccionCodigo"),
        docente: document.getElementById("seccionDocente"),
        periodo: document.getElementById("seccionPeriodo"),
        modalidad: document.getElementById("seccionModalidad"),
        horario: document.getElementById("seccionHorario"),
        aula: document.getElementById("seccionAula"),
        cupos: document.getElementById("seccionCupos"),
    };

    const btnLimpiarAlumnos = document.getElementById("btnLimpiar");
    if (btnLimpiarAlumnos) {
        btnLimpiarAlumnos.addEventListener("click", () => {
            document.querySelectorAll(".admin-form.alumnos input, .admin-form.alumnos select").forEach(el => {
                if (el.type !== "hidden") el.value = "";
            });
        });
    }

    const btnLimpiarDocentes = document.getElementById("btnLimpiarDocentes");
    if (btnLimpiarDocentes) {
        btnLimpiarDocentes.addEventListener("click", () => {
            limpiarBusquedaDocentes();
        });
    }

    if (formBusquedaSecciones) {
        formBusquedaSecciones.addEventListener("submit", (e) => {
            e.preventDefault();
            buscarSecciones();
        });
    }

    if (btnLimpiarSecciones) {
        btnLimpiarSecciones.addEventListener("click", limpiarFiltrosSecciones);
    }

    let alumnoSeleccionado = null;
    let historialMatriculasCache = [];
    let cursosPorCicloCache = {};
    let docenteSeleccionado = null;
    let seccionSeleccionada = null;

    if (formBusqueda) {
        formBusqueda.addEventListener("submit", (e) => {
            e.preventDefault();
            buscarAlumnos();
        });
    }

    if (btnLimpiarBusqueda) {
        btnLimpiarBusqueda.addEventListener("click", () => {
            filtroAlumno.value = "";
            limpiarSeleccion();
            buscarAlumnos();
        });
    }

    if (btnRefrescar) {
        btnRefrescar.addEventListener("click", buscarAlumnos);
    }

    if (selectorCiclo) {
        selectorCiclo.addEventListener("change", () => {
            if (alumnoSeleccionado) {
                cargarDetalleAcademico(alumnoSeleccionado.id, selectorCiclo.value);
            }
        });
    }

    if (formBusquedaDocentes) {
        formBusquedaDocentes.addEventListener("submit", (e) => {
            e.preventDefault();
            buscarDocentes();
        });
    }

    if (btnAgregarCursoDictable) {
        btnAgregarCursoDictable.addEventListener("click", agregarCursoDictable);
    }

    if (btnEditarDocente) {
        btnEditarDocente.addEventListener("click", () => {
            if (!docenteSeleccionado) return;
            mostrarFormularioDatos(true);
        });
    }

    if (btnEditarContactoDocente) {
        btnEditarContactoDocente.addEventListener("click", () => {
            if (!docenteSeleccionado) return;
            mostrarFormularioContacto(true);
        });
    }

    if (btnCancelarDatosDocente) {
        btnCancelarDatosDocente.addEventListener("click", () => mostrarFormularioDatos(false));
    }

    if (btnCancelarContactoDocente) {
        btnCancelarContactoDocente.addEventListener("click", () => mostrarFormularioContacto(false));
    }

    if (formDatosDocente) {
        formDatosDocente.addEventListener("submit", (e) => {
            e.preventDefault();
            if (!docenteSeleccionado) return;
            guardarDatosDocente();
        });
    }

    if (formContactoDocente) {
        formContactoDocente.addEventListener("submit", (e) => {
            e.preventDefault();
            if (!docenteSeleccionado) return;
            guardarContactoDocente();
        });
    }

    if (btnEditarContacto) {
        btnEditarContacto.addEventListener("click", () => habilitarEdicion(true));
    }

    if (contactoForm) {
        contactoForm.addEventListener("submit", (e) => {
            e.preventDefault();
            if (!alumnoSeleccionado) {
                mostrarToast("Selecciona un alumno primero", "info");
                return;
            }
            if (validarContacto()) {
                actualizarContacto(alumnoSeleccionado.id);
            }
        });
    }

    buscarAlumnos();
    cargarCatalogoCursos();
    buscarDocentes();
    cargarFiltrosSecciones();
    buscarSecciones();

    async function buscarAlumnos() {
        mostrarEstado(estadoBusqueda, "Cargando alumnos...", false);
        const filtro = filtroAlumno?.value?.trim() || "";
        try {
            const response = await fetch(`/admin/alumnos/buscar?filtro=${encodeURIComponent(filtro)}`);
            if (!response.ok) throw new Error("Error al buscar alumnos");
            const alumnos = await response.json();
            renderizarAlumnos(alumnos);
            if (!alumnos.length) {
                mostrarEstado(estadoBusqueda, "Alumno no encontrado", true);
            } else {
                estadoBusqueda.hidden = true;
            }
        } catch (err) {
            renderizarAlumnos([]);
            mostrarEstado(estadoBusqueda, err.message || "No se pudo cargar la lista", true);
            mostrarToast("No se pudo obtener la lista de alumnos", "error");
        }
    }

    function renderizarAlumnos(alumnos) {
        tablaAlumnos.innerHTML = "";
        if (!alumnos.length) {
            tablaAlumnos.innerHTML = `<tr><td colspan="9" class="muted">Sin resultados</td></tr>`;
            return;
        }

        alumnos.forEach(alumno => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${alumno.codigo || "-"}</td>
                <td>${alumno.nombreCompleto || "-"}</td>
                <td>${alumno.dni || "-"}</td>
                <td>${alumno.correoInstitucional || "-"}</td>
                <td>${alumno.correoPersonal || "-"}</td>
                <td>${alumno.telefono || "-"}</td>
                <td>${alumno.anioIngreso || "-"}</td>
                <td>${alumno.cicloActual || "-"}</td>
                <td>${alumno.ordenMerito ?? "-"}</td>
            `;
            tr.addEventListener("click", () => {
                document.querySelectorAll("#tablaAlumnos tbody tr").forEach(fila => fila.classList.remove("selected"));
                tr.classList.add("selected");
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
        fichaIds.nombre.textContent = alumno.nombreCompleto || "-";
        fichaIds.codigo.textContent = alumno.codigo || "-";
        fichaIds.carrera.textContent = alumno.carrera || "-";
        fichaIds.ciclo.textContent = alumno.cicloActual || "-";
        fichaIds.ingreso.textContent = alumno.anioIngreso || "-";
        fichaIds.correoInst.textContent = alumno.correoInstitucional || "-";
        fichaIds.correoPer.textContent = alumno.correoPersonal || "-";
        fichaIds.telefono.textContent = alumno.telefono || "-";
        fichaIds.estado.textContent = alumno.estado || "-";
        fichaIds.estado.className = "badge";
        fichaIds.estado.classList.add(alumno.estado === "Activo" ? "badge--success" : "badge--info");

        correoPersonalInput.value = alumno.correoPersonal || "";
        telefonoInput.value = alumno.telefono || "";
        direccionInput.value = alumno.direccion || "";
        habilitarEdicion(false);
        limpiarEstadoContacto();
    }

    function cargarPeriodos(alumno) {
        selectorCiclo.innerHTML = "";
        const periodos = alumno.periodos || [];
        if (!periodos.length) {
            selectorCiclo.innerHTML = `<option value="">Sin ciclos</option>`;
            return;
        }
        periodos.forEach(p => {
            const opt = document.createElement("option");
            opt.value = p;
            opt.textContent = p;
            selectorCiclo.appendChild(opt);
        });
    }

    async function cargarDetalleAcademico(idAlumno, ciclo) {
        if (!ciclo) return;
        mostrarSkeletonCursos("Cargando cursos...");
        try {
            const [cursosRes, resumenRes, historialRes] = await Promise.all([
                fetch(`/admin/alumnos/${idAlumno}/matriculas?ciclo=${encodeURIComponent(ciclo)}`),
                fetch(`/admin/alumnos/${idAlumno}/resumen?ciclo=${encodeURIComponent(ciclo)}`),
                fetch(`/admin/alumnos/${idAlumno}/historial`)
            ]);

            if (!cursosRes.ok || !resumenRes.ok || !historialRes.ok) {
                throw new Error("No se pudo obtener el detalle académico");
            }

            const cursos = await cursosRes.json();
            const resumen = await resumenRes.json();
            const historial = await historialRes.json();

            historialMatriculasCache = historial;
            renderizarCursos(cursos);
            renderizarResumen(resumen);
            renderizarHistorial(historial, idAlumno);
        } catch (err) {
            mostrarSkeletonCursos(err.message || "Sin información");
            mostrarToast("No se pudo cargar el detalle académico", "error");
        }
    }

    function renderizarCursos(cursos) {
        tablaCursos.innerHTML = "";
        if (!cursos.length) {
            tablaCursos.innerHTML = `<tr><td colspan="7" class="muted">Sin cursos para este ciclo</td></tr>`;
            return;
        }
        cursos.forEach(curso => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${curso.codigoSeccion || "-"}</td>
                <td>${curso.nombreCurso || "-"}</td>
                <td>${curso.docente || "-"}</td>
                <td>${curso.creditos ?? "-"}</td>
                <td>${curso.horasSemanales ?? "-"}</td>
                <td>${curso.modalidad || "-"}</td>
                <td>${curso.aula || "-"}</td>
            `;
            tablaCursos.appendChild(tr);
        });
    }

    function renderizarResumen(resumen) {
        totalCursos.textContent = resumen?.totalCursos ?? "-";
        totalCreditos.textContent = resumen?.totalCreditos ?? "-";
        totalHoras.textContent = resumen?.totalHoras ?? "-";
        montoEstimado.textContent = resumen?.montoEstimado ? `S/. ${resumen.montoEstimado}` : "-";
    }

    function renderizarHistorial(historial, idAlumno) {
        contenedorHistorial.innerHTML = "";

        if (!historial?.length) {
            contenedorHistorial.innerHTML = `<p class="muted">Sin registros</p>`;
            subtituloHistorial.textContent = "-";
            return;
        }

        subtituloHistorial.textContent = `${historial.length} ciclo(s) previos`;

        historial.forEach(h => {
            const item = document.createElement("div");
            item.className = "historial-item";
            item.dataset.ciclo = h.ciclo;

            /* ==== CABECERA DEL CICLO ==== */
            const header = document.createElement("div");
            header.className = "historial-item__header";
            header.innerHTML = `
            <span class="historial-item__ciclo">${h.ciclo || "-"}</span>
            <span class="historial-item__estado badge--${(h.estado || "").toLowerCase()}">
                ${h.estado || "-"}
            </span>
        `;

            /* ==== BADGES RESUMEN ==== */
            const badges = document.createElement("div");
            badges.className = "historial-badges";
            badges.innerHTML = `
            <span class="historial-badge">Cursos: ${h.totalCursos ?? 0}</span>
            <span class="historial-badge">Créditos: ${h.totalCreditos ?? 0}</span>
            <span class="historial-badge">Horas: ${h.totalHoras ?? 0}</span>
            <span class="historial-badge">Pension: S/.${h.montoTotal ?? 0}</span>
        `;

            /* ==== BOTÓN DE VER CURSOS ==== */
            const toggle = document.createElement("button");
            toggle.type = "button";
            toggle.className = "historial-item__toggle btn-secondary";
            toggle.textContent = "Ver cursos";

            header.appendChild(toggle);

            /* ==== CONTENEDOR DE TABLA ==== */
            const coursesContainer = document.createElement("div");
            coursesContainer.className = "historial-item__courses";
            coursesContainer.hidden = true;

            /* ==== AGREGAR TODO AL ITEM ==== */
            item.appendChild(header);
            item.appendChild(badges);
            item.appendChild(coursesContainer);
            contenedorHistorial.appendChild(item);

            /* ==== EVENTO DE ABRIR / CERRAR ==== */
            toggle.addEventListener("click", async () => {
                const ciclo = h.ciclo;
                const abierto = !coursesContainer.hidden;

                if (abierto) {
                    coursesContainer.hidden = true;
                    toggle.textContent = "Ver cursos";
                    return;
                }

                if (!cursosPorCicloCache[ciclo]) {
                    try {
                        coursesContainer.innerHTML = `<p class="muted">Cargando cursos...</p>`;
                        const resp = await fetch(`/admin/alumnos/${idAlumno}/matriculas?ciclo=${encodeURIComponent(ciclo)}`);
                        if (!resp.ok) throw new Error();
                        const cursos = await resp.json();
                        cursosPorCicloCache[ciclo] = cursos;
                        coursesContainer.innerHTML = crearTablaCursosHTML(cursos);
                    } catch (e) {
                        coursesContainer.innerHTML = `<p class="muted">No se pudieron cargar los cursos</p>`;
                    }
                } else {
                    coursesContainer.innerHTML = crearTablaCursosHTML(cursosPorCicloCache[ciclo]);
                }

                coursesContainer.hidden = false;
                toggle.textContent = "Ocultar cursos";
            });
        });
    }

    function crearTablaCursosHTML(cursos) {
        if (!cursos || !cursos.length) {
            return `<p class="muted">Sin cursos para este ciclo</p>`;
        }

        const rows = cursos.map(c => `
        <tr>
            <td>${c.codigoSeccion || "-"}</td>
            <td>${c.nombreCurso || "-"}</td>
            <td>${c.docente || "-"}</td>
            <td>${c.creditos ?? "-"}</td>
            <td>${c.horasSemanales ?? "-"}</td>
            <td>${c.modalidad || "-"}</td>
            <td>${c.aula || "-"}</td>
        </tr>
    `).join("");

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
        tablaAlumnos.querySelectorAll("tr").forEach(fila => fila.classList.remove("selected"));
        cargarFicha({});
        selectorCiclo.innerHTML = "";
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
            errores.push("Correo personal inválido");
        }
        if (telefono && !/^\d{9}$/.test(telefono)) {
            errores.push("El teléfono debe tener 9 dígitos");
        }
        if (errores.length) {
            mostrarEstado(estadoContacto, errores.join(". "), true);
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
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });
            if (!response.ok) throw new Error("No se pudo actualizar, revise los datos");
            mostrarToast("Datos de contacto actualizados", "success");
            habilitarEdicion(false);
            fichaIds.correoPer.textContent = body.correoPersonal || "-";
            fichaIds.telefono.textContent = body.telefono || "-";
            mostrarEstado(estadoContacto, "Guardado correctamente", false);
        } catch (err) {
            mostrarEstado(estadoContacto, err.message, true);
            mostrarToast(err.message, "error");
        }
    }

    function mostrarEstado(elemento, mensaje, esError) {
        if (!elemento) return;
        elemento.textContent = mensaje;
        elemento.hidden = false;
        elemento.style.color = esError ? "#c0392b" : "var(--color-primario)";
    }

    function limpiarEstadoContacto() {
        estadoContacto.hidden = true;
        estadoContacto.textContent = "";
    }

    function mostrarToast(mensaje, tipo = "info") {
        const toast = document.createElement("div");
        toast.className = `toast toast--${tipo}`;
        toast.textContent = mensaje;
        toastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    }

    function createToastContainer() {
        let container = document.querySelector(".toast-container");
        if (!container) {
            container = document.createElement("div");
            container.className = "toast-container";
            document.body.appendChild(container);
        }
        return container;
    }

    // =====================
    // DOCENTES
    // =====================
    async function cargarCatalogoCursos() {
        try {
            const resp = await fetch("/admin/docentes/cursos");
            if (!resp.ok) throw new Error();
            const cursos = await resp.json();
            [selectCursoDictable, filtroCursoDictable].forEach(select => {
                if (!select) return;
                select.innerHTML = "";
                const baseOption = document.createElement("option");
                baseOption.value = "";
                baseOption.textContent = select === selectCursoDictable ? "Selecciona curso" : "Todos";
                select.appendChild(baseOption);
                cursos.forEach(c => {
                    const opt = document.createElement("option");
                    opt.value = c.idCurso;
                    opt.textContent = `${c.codigo} - ${c.nombre}`;
                    select.appendChild(opt);
                });
            });
        } catch (e) {
            mostrarToast("No se pudo cargar el catálogo de cursos", "error");
        }
    }

    function limpiarBusquedaDocentes() {
        filtroDocente.value = "";
        if (filtroCursoDictable) filtroCursoDictable.value = "";
        if (filtroEstadoDocente) filtroEstadoDocente.value = "";
        limpiarSeleccionDocente();
        buscarDocentes();
    }

    async function buscarDocentes() {
        if (!tablaDocentes) return;
        mostrarEstado(estadoBusquedaDocentes, "Buscando docentes...", false);
        const params = new URLSearchParams();
        params.append("filtro", filtroDocente?.value?.trim() || "");
        if (filtroCursoDictable?.value) params.append("cursoId", filtroCursoDictable.value);
        if (filtroEstadoDocente?.value) params.append("estado", filtroEstadoDocente.value);
        try {
            const resp = await fetch(`/admin/docentes/buscar?${params.toString()}`);
            if (!resp.ok) throw new Error("No se pudo buscar docentes");
            const docentes = await resp.json();
            renderizarDocentes(docentes);
            if (!docentes.length) mostrarEstado(estadoBusquedaDocentes, "Sin resultados", true);
            else estadoBusquedaDocentes.hidden = true;
        } catch (err) {
            renderizarDocentes([]);
            mostrarEstado(estadoBusquedaDocentes, err.message, true);
        }
    }

    function renderizarDocentes(docentes) {
        tablaDocentes.innerHTML = "";
        if (!docentes.length) {
            tablaDocentes.innerHTML = `<tr><td colspan="4" class="muted">Sin resultados</td></tr>`;
            return;
        }
        docentes.forEach(doc => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${doc.codigo || "-"}</td>
                <td>${doc.nombreCompleto || "-"}</td>
                <td>${doc.dni || "-"}</td>
                <td>${doc.estado || "-"}</td>
            `;
            tr.addEventListener("click", () => {
                tablaDocentes.querySelectorAll("tr").forEach(f => f.classList.remove("selected"));
                tr.classList.add("selected");
                cargarDetalleDocente(doc.id);
            });
            tablaDocentes.appendChild(tr);
        });
    }

    async function cargarDetalleDocente(id) {
        limpiarSeleccionDocente();
        try {
            const resp = await fetch(`/admin/docentes/${id}`);
            if (!resp.ok) throw new Error("No se pudo obtener el detalle del docente");
            const detalle = await resp.json();
            docenteSeleccionado = detalle;
            renderizarFichaDocente(detalle);
            renderizarCursosDictables(detalle.cursosDictables || []);
            renderizarSeccionesDocente(detalle);
            renderizarHistorialDocente(detalle.historial || []);
        } catch (err) {
            mostrarToast(err.message, "error");
        }
    }

    function renderizarFichaDocente(detalle) {
        inputsDocente.codigo.textContent = detalle.codigo || "-";
        inputsDocente.estado.textContent = detalle.estado || "-";
        inputsDocente.estado.className = "badge";
        inputsDocente.estado.classList.add(detalle.estado === "ACTIVO" || detalle.estado === "Activo" ? "badge--success" : "badge--info");
        inputsDocente.apellidos.textContent = detalle.apellidos || "-";
        inputsDocente.nombres.textContent = detalle.nombres || "-";
        inputsDocente.dni.textContent = detalle.dni || "-";
        inputsDocente.especialidad.textContent = detalle.especialidad || "-";
        inputsDocente.correoInst.textContent = detalle.correoInstitucional || "-";
        inputsDocente.correoPer.textContent = detalle.correoPersonal || "-";
        inputsDocente.telefono.textContent = detalle.telefono || "-";
        inputsDocente.direccion.textContent = detalle.direccion || "-";

        formInputsDocente.apellidos.value = detalle.apellidos || "";
        formInputsDocente.nombres.value = detalle.nombres || "";
        formInputsDocente.dni.value = detalle.dni || "";
        formInputsDocente.especialidad.value = detalle.especialidad || "";
        formInputsDocente.correoInst.value = detalle.correoInstitucional || "";
        formInputsDocente.estado.value = detalle.estado || "ACTIVO";

        formContactoInputs.correoInst.value = detalle.correoInstitucional || "";
        formContactoInputs.correoPer.value = detalle.correoPersonal || "";
        formContactoInputs.telefono.value = detalle.telefono || "";
        formContactoInputs.direccion.value = detalle.direccion || "";
        mostrarFormularioDatos(false);
        mostrarFormularioContacto(false);
    }

    function renderizarCursosDictables(cursos) {
        tablaCursosDictables.innerHTML = "";
        if (!cursos.length) {
            tablaCursosDictables.innerHTML = `<tr><td colspan="5" class="muted">Sin cursos asignados</td></tr>`;
            return;
        }
        cursos.forEach(c => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${c.nombre || "-"}</td>
                <td>${c.codigo || "-"}</td>
                <td>${c.creditos ?? "-"}</td>
                <td>${c.ciclo ?? "-"}</td>
                <td><button class="btn-outline" data-curso="${c.idCurso}">Eliminar</button></td>
            `;
            tr.querySelector("button").addEventListener("click", () => eliminarCursoDictable(c.idCurso));
            tablaCursosDictables.appendChild(tr);
        });
    }

    function renderizarSeccionesDocente(detalle) {
        const secciones = detalle.seccionesActuales || [];
        const tbody = tablaSeccionesDocente;
        tbody.innerHTML = "";

        // 1. Mostrar tabla
        if (!secciones.length) {
            tbody.innerHTML = `<tr><td colspan="9" class="muted">Sin secciones</td></tr>`;
        } else {
            secciones.forEach(s => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                <td>${s.curso || "-"}</td>
                <td>${s.codigoSeccion || "-"}</td>
                <td>${s.periodo || "-"}</td>
                <td>${s.modalidad || "-"}</td>
                <td>${s.creditos ?? "-"}</td>
                <td>${s.turno || "-"}</td>
                <td>${s.horario || "-"}</td>
                <td>${s.aula || "-"}</td>
                <td>${s.estudiantesInscritos ?? 0}</td>
            `;
                tbody.appendChild(tr);
            });
        }

        // 3. Generar pastillas sin duplicar
        const contenedorBadges = document.getElementById("contenedorSeccionesBadges");
        contenedorBadges.innerHTML = ""; // <-- evita duplicados

        const badges = [
            `${detalle.totalSeccionesActuales || 0} secciones`,
            `${detalle.totalCreditosActuales || 0} créditos`,
            `${detalle.totalHorasSemanalesActuales || 0} h/semana`,
            `${detalle.totalCursosActuales || 0} cursos`
        ];

        badges.forEach(texto => {
            const span = document.createElement("span");
            span.classList.add("secciones-badge");
            span.textContent = texto;
            contenedorBadges.appendChild(span);
        });
    }

    function renderizarHistorialDocente(historial) {
        tablaHistorialDocente.innerHTML = "";
        if (!historial.length) {
            tablaHistorialDocente.innerHTML = `<tr><td colspan="10" class="muted">Sin historial</td></tr>`;
            return;
        }
        historial.forEach(h => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${h.periodo || "-"}</td>
                <td>${h.curso || "-"}</td>
                <td>${h.seccion || "-"}</td>
                <td>${h.modalidad || "-"}</td>
                <td>${h.creditos ?? "-"}</td>
                <td>${h.horario || "-"}</td>
                <td>${h.estudiantesFinalizados ?? "-"}</td>
                <td>${h.notaPromedio ?? "-"}</td>
                <td>${h.porcentajeAprobacion ?? "-"}</td>
                <td>${h.observaciones || "-"}</td>
            `;
            tablaHistorialDocente.appendChild(tr);
        });
    }

    async function guardarDatosDocente() {
        try {
            const body = {
                apellidos: formInputsDocente.apellidos.value.trim(),
                nombres: formInputsDocente.nombres.value.trim(),
                dni: formInputsDocente.dni.value.trim(),
                especialidad: formInputsDocente.especialidad.value.trim(),
                correoInstitucional: formInputsDocente.correoInst.value.trim(),
                estado: formInputsDocente.estado.value,
            };
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });
            if (!resp.ok) throw new Error("No se pudo actualizar los datos del docente");
            mostrarToast("Datos personales actualizados", "success");
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            mostrarEstado(estadoDatosDocente, err.message, true);
        }
    }

    async function guardarContactoDocente() {
        try {
            const body = {
                correoInstitucional: formContactoInputs.correoInst.value.trim(),
                correoPersonal: formContactoInputs.correoPer.value.trim(),
                telefono: formContactoInputs.telefono.value.trim(),
                direccion: formContactoInputs.direccion.value.trim(),
            };
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/contacto`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });
            if (!resp.ok) throw new Error("No se pudo actualizar el contacto");
            mostrarToast("Contacto actualizado", "success");
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            mostrarEstado(estadoContactoDocente, err.message, true);
        }
    }

    // =====================
    // SECCIONES
    // =====================
    async function cargarFiltrosSecciones() {
        if (!filtroCursoSeccion) return;
        try {
            const resp = await fetch("/admin/secciones/catalogos");
            if (!resp.ok) throw new Error();
            const data = await resp.json();
            poblarSelectSeccion(filtroCursoSeccion, data.cursos || [], "Todos");
            poblarSelectSeccion(filtroPeriodoSeccion, data.periodos || [], "Seleccione");
            poblarSelectSeccion(filtroDocenteSeccion, data.docentes || [], "Seleccione");
            poblarSelectSeccion(filtroModalidadSeccion, data.modalidades || [], "Estado");
        } catch (e) {
            mostrarToast("No se pudieron cargar los filtros de secciones", "error");
        }
    }

    function poblarSelectSeccion(select, items, placeholder) {
        if (!select) return;
        select.innerHTML = "";
        const base = document.createElement("option");
        base.value = "";
        base.textContent = placeholder;
        select.appendChild(base);

        (items || []).forEach(item => {
            const option = document.createElement("option");
            if (typeof item === "string") {
                option.value = item;
                option.textContent = item;
            } else {
                const valor = item.id ?? item.idCurso ?? item.idDocente ?? item.codigo ?? item.periodo ?? item.valor ?? "";
                const nombreDocente = `${item.apellidos || ""} ${item.nombres || ""}`.trim();
                const texto = item.nombreCompleto || item.nombre || item.descripcion || item.periodo || nombreDocente || item.codigo || valor;
                option.value = valor;
                option.textContent = texto || "-";
            }
            select.appendChild(option);
        });
    }

    function limpiarFiltrosSecciones() {
        [filtroCursoSeccion, filtroPeriodoSeccion, filtroDocenteSeccion, filtroModalidadSeccion].forEach(sel => {
            if (sel) sel.value = "";
        });
        if (filtroCodigoSeccion) filtroCodigoSeccion.value = "";
        seccionSeleccionada = null;
        if (tablaSecciones) {
            tablaSecciones.innerHTML = `<tr><td colspan="9" class="muted">Realiza una búsqueda para ver resultados</td></tr>`;
        }
        if (tablaEstudiantesSeccion) {
            tablaEstudiantesSeccion.innerHTML = `<tr><td colspan="4" class="muted">Sin estudiantes</td></tr>`;
        }
        if (fichaSeccion) {
            fichaSeccion.hidden = true;
        }
    }

    async function buscarSecciones() {
        if (!tablaSecciones) return;
        tablaSecciones.innerHTML = `<tr><td colspan="9" class="muted">Cargando secciones...</td></tr>`;
        const params = new URLSearchParams();
        if (filtroCursoSeccion?.value) params.append("cursoId", filtroCursoSeccion.value);
        if (filtroPeriodoSeccion?.value) params.append("periodo", filtroPeriodoSeccion.value);
        if (filtroDocenteSeccion?.value) params.append("docenteId", filtroDocenteSeccion.value);
        if (filtroModalidadSeccion?.value) params.append("modalidad", filtroModalidadSeccion.value);
        if (filtroCodigoSeccion?.value?.trim()) params.append("codigo", filtroCodigoSeccion.value.trim());

        try {
            const url = params.toString() ? `/admin/secciones/buscar?${params.toString()}` : "/admin/secciones/buscar";
            const resp = await fetch(url);
            if (!resp.ok) throw new Error("No se pudo buscar secciones");
            const secciones = await resp.json();
            renderizarSecciones(secciones || []);
        } catch (e) {
            renderizarSecciones([]);
            mostrarToast(e.message || "Error al cargar secciones", "error");
        }
    }

    function renderizarSecciones(secciones) {
        tablaSecciones.innerHTML = "";
        if (!secciones.length) {
            tablaSecciones.innerHTML = `<tr><td colspan="9" class="muted">Sin resultados</td></tr>`;
            return;
        }
        secciones.forEach(sec => {
            const tr = document.createElement("tr");
            const idSeccion = sec.id ?? sec.idSeccion ?? sec.seccionId;
            tr.innerHTML = `
                <td>${sec.curso || "-"}</td>
                <td>${sec.codigoSeccion || sec.codigo || "-"}</td>
                <td>${sec.docente || "-"}</td>
                <td>${sec.periodo || "-"}</td>
                <td>${sec.modalidad || "-"}</td>
                <td>${sec.horario || "-"}</td>
                <td>${sec.aula || "-"}</td>
                <td>${(sec.cupos ?? "-")} / ${(sec.matriculados ?? sec.estudiantes || 0)}</td>
                <td><span class="badge">${sec.estado || "-"}</span></td>
            `;
            tr.addEventListener("click", () => cargarFichaSeccion(idSeccion));
            tablaSecciones.appendChild(tr);
        });
    }

    async function cargarFichaSeccion(idSeccion) {
        if (!idSeccion || !fichaSeccion) return;
        seccionSeleccionada = idSeccion;
        fichaSeccion.hidden = false;
        actualizarFichaSeccion({ curso: "Cargando...", estado: "", codigo: "-", docente: "-", periodo: "-", modalidad: "-", horario: "-", aula: "-", cupos: "-" });
        tablaEstudiantesSeccion.innerHTML = `<tr><td colspan="4" class="muted">Cargando estudiantes...</td></tr>`;
        try {
            const [detalleResp, estudiantesResp] = await Promise.all([
                fetch(`/admin/secciones/${idSeccion}`),
                fetch(`/admin/secciones/${idSeccion}/estudiantes`)
            ]);
            if (!detalleResp.ok || !estudiantesResp.ok) throw new Error("No se pudo cargar la ficha");
            const detalle = await detalleResp.json();
            const estudiantes = await estudiantesResp.json();
            renderizarFichaSeccion(detalle || {}, estudiantes || []);
        } catch (e) {
            mostrarToast(e.message || "Error al cargar la sección", "error");
        }
    }

    function renderizarFichaSeccion(detalle, estudiantes) {
        if (!fichaSeccion) return;
        fichaSeccion.hidden = false;
        actualizarFichaSeccion({
            curso: detalle.curso || "-",
            estado: detalle.estado || "-",
            codigo: detalle.codigoSeccion || detalle.codigo || "-",
            docente: detalle.docente || "-",
            periodo: detalle.periodo || "-",
            modalidad: detalle.modalidad || "-",
            horario: detalle.horario || "-",
            aula: detalle.aula || "-",
            cupos: `${detalle.cupos ?? "-"} / ${detalle.matriculados ?? detalle.estudiantes ?? 0}`,
        });

        tablaEstudiantesSeccion.innerHTML = "";
        if (!estudiantes.length) {
            tablaEstudiantesSeccion.innerHTML = `<tr><td colspan="4" class="muted">Sin estudiantes</td></tr>`;
            return;
        }
        estudiantes.forEach(est => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${est.codigo || "-"}</td>
                <td>${est.nombre || "-"}</td>
                <td>${est.estado || "-"}</td>
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
    }

    async function agregarCursoDictable() {
        if (!docenteSeleccionado) {
            mostrarToast("Selecciona un docente", "info");
            return;
        }
        const cursoId = selectCursoDictable.value;
        if (!cursoId) return;
        try {
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/cursos?cursoId=${cursoId}`, { method: "POST" });
            if (!resp.ok) throw new Error("No se pudo agregar el curso");
            mostrarToast("Curso añadido", "success");
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            mostrarToast(err.message, "error");
        }
    }

    async function eliminarCursoDictable(cursoId) {
        if (!docenteSeleccionado) return;
        try {
            const resp = await fetch(`/admin/docentes/${docenteSeleccionado.id}/cursos/${cursoId}`, { method: "DELETE" });
            if (!resp.ok) throw new Error("No se pudo eliminar el curso");
            mostrarToast("Curso removido", "info");
            cargarDetalleDocente(docenteSeleccionado.id);
        } catch (err) {
            mostrarToast(err.message, "error");
        }
    }

    function mostrarFormularioDatos(valor) {
        formDatosDocente.hidden = !valor;
        estadoDatosDocente.hidden = true;
    }

    function mostrarFormularioContacto(valor) {
        formContactoDocente.hidden = !valor;
        estadoContactoDocente.hidden = true;
    }

    function limpiarSeleccionDocente() {
        docenteSeleccionado = null;
        [inputsDocente.codigo, inputsDocente.apellidos, inputsDocente.nombres, inputsDocente.dni, inputsDocente.especialidad,
            inputsDocente.correoInst, inputsDocente.correoPer, inputsDocente.telefono, inputsDocente.direccion].forEach(el => {
            if (el) el.textContent = "-";
        });
        inputsDocente.estado.textContent = "-";
        renderizarCursosDictables([]);
        renderizarSeccionesDocente({ seccionesActuales: [] });
        renderizarHistorialDocente([]);
        mostrarFormularioContacto(false);
        mostrarFormularioDatos(false);
    }
});
