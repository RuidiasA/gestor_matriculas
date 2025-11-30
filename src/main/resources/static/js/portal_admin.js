document.addEventListener("DOMContentLoaded", function() {
    // Mostrar secciones según menú lateral
    const navLinks = document.querySelectorAll(".admin-nav a");
    const sections = document.querySelectorAll(".admin-section");

    navLinks.forEach(link => {
        link.addEventListener("click", function(e) {
            e.preventDefault();
            const target = this.getAttribute("data-section");

            // Activar sección
            sections.forEach(sec => sec.classList.remove("active"));
            document.getElementById(target).classList.add("active");

            // Activar menú
            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });

    // Limpiar formulario alumnos
    const btnLimpiarAlumnos = document.getElementById("btnLimpiar");
    if (btnLimpiarAlumnos) {
        btnLimpiarAlumnos.addEventListener("click", function() {
            document.querySelectorAll(".admin-form.alumnos input, .admin-form.alumnos select").forEach(el => {
                if(el.type !== "hidden") el.value = "";
            });
        });
    }

    // Limpiar formulario docentes
    const btnLimpiarDocentes = document.getElementById("btnLimpiarDocentes");
    if (btnLimpiarDocentes) {
        btnLimpiarDocentes.addEventListener("click", function() {
            document.querySelectorAll(".admin-form.docentes input, .admin-form.docentes select").forEach(el => {
                if(el.type !== "hidden") el.value = "";
            });
        });
    }
});
