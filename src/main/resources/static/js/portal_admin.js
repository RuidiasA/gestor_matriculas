document.addEventListener("DOMContentLoaded", () => {

    // ============================================================
    // 🔥 VARIABLE GLOBAL (Debe estar arriba SIEMPRE)
    // ============================================================
    let alumnoSeleccionado = null;

    // ============================================================
    // 1) MENU LATERAL: CAMBIO DE SECCIONES
    // ============================================================
    const navLinks = document.querySelectorAll(".admin-nav a");
    const sections = document.querySelectorAll(".admin-section");

    navLinks.forEach(link => {
        link.addEventListener("click", function (e) {
            e.preventDefault();

            const target = this.dataset.section;

            // Ocultar todas las secciones
            sections.forEach(sec => sec.classList.remove("active"));

            // Mostrar solo la seleccionada
            const sec = document.getElementById(target);
            if (sec) sec.classList.add("active");

            // Activar item del menú
            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // ============================================================
    // 2) SELECT DE CICLO (Debe crearse DESPUÉS de alumnoSeleccionado)
    // ============================================================
    const selectCiclo = document.getElementById("selectCiclo");
    if (selectCiclo) {
        selectCiclo.addEventListener("change", () => {
            if (!alumnoSeleccionado) return;
            const ciclo = selectCiclo.value;
            if (ciclo) {
                cargarCursos(ciclo);
            }
        });
    }

    // ============================================================
    // 3) LÓGICA DE BÚSQUEDA DE ALUMNOS
    // ============================================================
    const inputBuscar = document.getElementById("busqueda");
    const btnBuscar = document.getElementById("btnBuscar");
    const btnLimpiar = document.getElementById("btnLimpiar");

    // -------------------------
    // LIMPIAR TODO
    // -------------------------
    if (btnLimpiar) {
        btnLimpiar.addEventListener("click", () => {
            inputBuscar.value = "";
            alumnoSeleccionado = null;

            limpiarAlumno();
            limpiarCursos();
            limpiarResumen();
            limpiarHistorial();
        });
    }

    // -------------------------
    // BUSCAR ALUMNO
    // -------------------------
    if (btnBuscar) {
        btnBuscar.addEventListener("click", () => {
            const f = inputBuscar.value.trim();
            if (f === "") return;

            fetch(`/admin/alumnos/buscar?filtro=${encodeURIComponent(f)}`)
                .then(r => r.json())
                .then(lista => {
                    if (!lista.length) {
                        alert("No se encontraron alumnos");
                        return;
                    }

                    if (lista.length === 1) {
                        cargarAlumnoCompleto(lista[0].id);
                    } else {
                        alert("Se encontraron varios alumnos, escribe más específico.");
                    }
                })
                .catch(() => alert("Error al buscar alumnos"));
        });
    }

    // ============================================================
    // 4) CARGA COMPLETA DEL ALUMNO
    // ============================================================
    function cargarAlumnoCompleto(id) {

        alumnoSeleccionado = id;

        // Datos del alumno
        fetch(`/admin/alumnos/${id}`)
            .then(r => r.json())
            .then(a => mostrarDatosAlumno(a))
            .catch(() => alert("Error al obtener datos del alumno"));

        // Ciclos disponibles
        fetch(`/admin/matriculas/${id}/ciclos`)
            .then(r => r.json())
            .then(ciclos => llenarSelectCiclo(ciclos))
            .catch(() => alert("Error al obtener ciclos de matrícula"));

        // Historial de matrículas
        fetch(`/admin/matriculas/${id}/historial`)
            .then(r => r.json())
            .then(hist => renderHistorial(hist))
            .catch(() => alert("Error al obtener historial de matrículas"));
    }

    // ============================================================
    // 5) MOSTRAR INFO DEL ALUMNO
    // ============================================================
    function mostrarDatosAlumno(a) {
        setSpanText("info_codigo", a.codigoAlumno);
        setSpanText("info_nombres", a.nombres);
        setSpanText("info_apellidos", a.apellidos);
        setSpanText("info_dni", a.dni);
        setSpanText("info_correo", a.correoInstitucional);
        setSpanText("info_carrera", a.carreraNombre);
        setSpanText("info_anio", a.anioIngreso);
        setSpanText("info_ciclo", a.cicloActual);
        setSpanText("info_estado", a.estado);
    }

    function setSpanText(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value ?? "";
    }

    // ============================================================
    // 6) LLENAR SELECT DE CICLOS
    // ============================================================
    function llenarSelectCiclo(ciclos) {

        const select = document.getElementById("selectCiclo");
        if (!select) return;

        const valorAnterior = select.value;

        select.innerHTML = "";

        ciclos.forEach(c => {
            let op = document.createElement("option");
            op.value = c;
            op.textContent = c;
            select.appendChild(op);
        });

        // Si el ciclo anterior sigue existiendo, mantenerlo
        if (ciclos.includes(valorAnterior)) {
            select.value = valorAnterior;
        } else {
            select.value = ciclos[0]; // Primer ciclo solo si antes no había nada
        }
    }


    // ============================================================
    // 7) CARGAR CURSOS POR CICLO
    // ============================================================
    function cargarCursos(ciclo) {
        if (!alumnoSeleccionado) return;

        fetch(`/admin/matriculas/${alumnoSeleccionado}/ciclo/${ciclo}`)
            .then(r => r.json())
            .then(lista => {

                const tb = document.querySelector("#tablaCursos tbody");
                if (!tb) return;

                tb.innerHTML = "";

                let totalCursos = 0, totalCred = 0, totalHoras = 0;

                lista.forEach(c => {
                    totalCursos++;
                    totalCred += c.creditos;
                    totalHoras += c.horas;

                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${c.seccion}</td>
                        <td>${c.curso}</td>
                        <td>${c.docente}</td>
                        <td>${c.aula}</td>
                        <td>${c.cicloCurso}</td>
                        <td>${c.creditos}</td>
                        <td>${c.horas}</td>
                        <td>${c.tipo}</td>
                        <td>${c.modalidad}</td>
                    `;
                    tb.appendChild(tr);
                });

                // Actualizar resumen
                setSpanText("resumen_cursos", totalCursos);
                setSpanText("resumen_creditos", totalCred);
                setSpanText("resumen_horas", totalHoras);
                setSpanText("resumen_monto", totalCred * 50);
            })
            .catch(() => alert("Error al cargar cursos del ciclo"));
    }

    // ============================================================
    // 8) HISTORIAL DE MATRÍCULAS
    // ============================================================
    function renderHistorial(lista) {

        const tbody = document.querySelector("#tablaHistorial tbody");
        if (!tbody) return;

        tbody.innerHTML = "";

        lista.forEach(m => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${m.ciclo}</td>
                <td>${m.cursos}</td>
                <td>${m.creditos}</td>
                <td>${m.horas}</td>
                <td>S/ ${m.monto}</td>
                <td>${m.fecha}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    // ============================================================
    // 9) LIMPIADORES
    // ============================================================
    function limpiarAlumno() {
        [
            "info_codigo","info_nombres","info_apellidos","info_dni",
            "info_correo","info_carrera","info_anio","info_ciclo","info_estado"
        ].forEach(id => setSpanText(id, ""));
    }

    function limpiarCursos() {
        const tb = document.querySelector("#tablaCursos tbody");
        if (tb) tb.innerHTML = "";
    }

    function limpiarResumen() {
        ["resumen_cursos","resumen_creditos","resumen_horas","resumen_monto"]
            .forEach(id => setSpanText(id, ""));
        const select = document.getElementById("selectCiclo");
        if (select) select.innerHTML = "";
    }

    function limpiarHistorial() {
        const tb = document.querySelector("#tablaHistorial tbody");
        if (tb) tb.innerHTML = "";
    }

});
