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
            document.querySelectorAll(".admin-form.docentes input, .admin-form.docentes select").forEach(el => {
                if (el.type !== "hidden") el.value = "";
            });
        });
    }

    let alumnoSeleccionado = null;

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

    async function buscarAlumnos() {
        mostrarEstado(estadoBusqueda, "Cargando alumnos...", false);
        const filtro = filtroAlumno?.value?.trim() || "";
        try {
            const response = await fetch(`/admin/alumnos/buscar?filtro=${encodeURIComponent(filtro)}`);
            if (!response.ok) throw new Error("Error al buscar alumnos");
            const alumnos = await response.json();
            renderizarAlumnos(alumnos);
            if (!alumnos.length) mostrarEstado(estadoBusqueda, "Alumno no encontrado", true);
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
                <td>${alumno.turno || alumno.prioridad || "-"}</td>
            `;
            tr.addEventListener("click", () => {
                document.querySelectorAll("#tablaAlumnos tbody tr").forEach(fila => fila.classList.remove("selected"));
                tr.classList.add("selected");
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

            renderizarCursos(cursos);
            renderizarResumen(resumen);
            renderizarHistorial(historial);
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

    function renderizarHistorial(historial) {
        contenedorHistorial.innerHTML = "";
        if (!historial?.length) {
            contenedorHistorial.innerHTML = `<p class="muted">Sin registros</p>`;
            subtituloHistorial.textContent = "-";
            return;
        }
        subtituloHistorial.textContent = `${historial.length} ciclo(s) previos`;
        historial.forEach(registro => {
            const item = document.createElement("div");
            item.className = "historial-item";
            item.innerHTML = `
                <strong>${registro.ciclo || "-"}</strong>
                <span class="muted">${registro.detalle || ""}</span>
            `;
            contenedorHistorial.appendChild(item);
        });
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
        tablaAlumnos.querySelectorAll("tr").forEach(fila => fila.classList.remove("selected"));
        cargarFicha({});
        selectorCiclo.innerHTML = "";
        renderizarCursos([]);
        renderizarResumen({});
        renderizarHistorial([]);
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
});
