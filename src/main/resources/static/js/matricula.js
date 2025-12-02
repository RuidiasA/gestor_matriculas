/* ============================================================
   CAMBIO DE VISTAS (MATRÍCULA / SOLICITUD / PENSIONES)
============================================================ */

function mostrarVista(vista, event) {
    if (event) event.preventDefault();

    const body = document.body;
    const asideDerecho = document.getElementById('aside-derecho');

    const vistas = {
        matricula: document.getElementById('vista-matricula'),
        horario: document.getElementById('vista-horario'),
        pensiones: document.getElementById('vista-pensiones'),
        solicitud: document.getElementById('vista-solicitud')
    };

    Object.values(vistas).forEach(div => {
        if (div) div.classList.add('hidden');
    });

    if (vista === 'matricula') {
        vistas.matricula.classList.remove('hidden');
        asideDerecho.classList.remove('hidden');
        body.classList.remove('layout-2-cols');
        inicializarHorario();

    } else if (vista === 'solicitud') {
        vistas.solicitud.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');

    } else if (vista === 'pensiones') {
        vistas.pensiones.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');

    } else if (vista === 'horario') {
        vistas.horario.classList.remove('hidden');
        asideDerecho.classList.add('hidden');
        body.classList.add('layout-2-cols');
        inicializarHorario(); // opcional si luego rellenamos horario dinámico
    }

    const linksMenu = document.querySelectorAll('.aside-left__container a');
    linksMenu.forEach(link => link.classList.remove('active'));

    if (event && event.currentTarget) {
        event.currentTarget.classList.add('active');
    }
}



/* ============================================================
   DATA DE CURSOS MATRICULADOS
============================================================ */

const cursosMatriculados = [
    {
        codigo: "IA401",
        nombre: "Inteligencia Artificial",
        seccion: "A1",
        aula: "302",
        horas: 4,
        creditos: 4,
        ciclo: 9,
        tipo: "Electivo",
        modalidad: "Virtual",
        descripcion:
            "La asignatura introduce los fundamentos de la IA moderna, agentes inteligentes y búsqueda.",
        horario: [
            { dia: "Martes", inicio: "08:00", fin: "10:00" }
        ]
    },
    {
        codigo: "ML402",
        nombre: "Machine Learning",
        seccion: "B2",
        aula: "205",
        horas: 4,
        creditos: 4,
        ciclo: 8,
        tipo: "Obligatorio",
        modalidad: "Presencial",
        descripcion:
            "Diseño, entrenamiento y evaluación de modelos supervisados y no supervisados.",
        horario: [
            { dia: "Jueves", inicio: "08:00", fin: "10:00" }
        ]
    },
    {
        codigo: "AS301",
        nombre: "Arquitectura de Software",
        seccion: "C1",
        aula: "501",
        horas: 3,
        creditos: 3,
        ciclo: 7,
        tipo: "Obligatorio",
        modalidad: "Presencial",
        descripcion:
            "Principios, patrones y estilos arquitectónicos aplicados a sistemas empresariales.",
        horario: [
            { dia: "Lunes", inicio: "10:00", fin: "12:00" }
        ]
    }
];



/* ============================================================
   GENERACIÓN DE TARJETAS DE CURSOS
============================================================ */

function generarTarjetasCursos() {
    const contenedor = document.querySelector(".cursos-inscritos");
    if (!contenedor) return;

    contenedor.innerHTML = "";

    cursosMatriculados.forEach(curso => {
        const card = document.createElement("div");
        card.classList.add("curso-card");

        card.innerHTML = `
            <h3>${curso.codigo} — ${curso.nombre}</h3>
            <p class="descripcion">${curso.descripcion}</p>

            <div class="curso-grid">
                <span><strong>Sección:</strong> ${curso.seccion}</span>
                <span><strong>Aula:</strong> ${curso.aula}</span>
                <span><strong>Horas semanales:</strong> ${curso.horas}</span>
                <span><strong>Créditos:</strong> ${curso.creditos}</span>
                <span><strong>Ciclo:</strong> ${curso.ciclo}</span>
                <span><strong>Tipo:</strong> ${curso.tipo}</span>
                <span><strong>Modalidad:</strong> ${curso.modalidad}</span>
            </div>
        `;

        contenedor.appendChild(card);
    });
}



/* ============================================================
   GENERADOR COMPLETO DE TABLA DE HORARIO
   (Lunes a Domingo • 8am a 10pm)
============================================================ */

function generarTablaHorario() {
    const tbody = document.querySelector("#vista-horario .horario-completo tbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    const horaInicio = 8;
    const horaFin = 22;

    const dias = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"];

    for (let h = horaInicio; h < horaFin; h++) {
        const fila = document.createElement("tr");

        const horaInicioStr = String(h).padStart(2, "0") + ":00";
        const horaFinStr = String(h + 1).padStart(2, "0") + ":00";

        const celdaHora = document.createElement("td");
        celdaHora.textContent = `${horaInicioStr} - ${horaFinStr}`;
        fila.appendChild(celdaHora);

        dias.forEach(() => {
            const celda = document.createElement("td");
            celda.classList.add("empty");
            fila.appendChild(celda);
        });

        tbody.appendChild(fila);
    }
}

/* ============================================================
   INICIALIZACIÓN GENERAL
============================================================ */
function inicializarHorario() {
    generarTarjetasCursos();
    generarTablaHorario();
}
document.addEventListener("DOMContentLoaded", inicializarHorario);



// ===============================
//  MANEJO DE SOLICITUD DE SECCIÓN
// ===============================

const form = document.querySelector(".solicitud-container");
const historialVacio = document.querySelector(".historial-vacio");
const historialLista = document.querySelector(".historial-lista");

form.addEventListener("submit", function(e){
    e.preventDefault();

    const curso = document.getElementById("curso").value;
    const turno = document.getElementById("turno").value;
    const modalidad = document.getElementById("modalidad").value;
    const correo = document.getElementById("correo").value;
    const telefono = document.getElementById("telefono").value;
    const motivo = document.getElementById("motivo").value;
    const evidencia = document.getElementById("evidencia").files[0];

    const fecha = new Date().toLocaleDateString("es-PE", {
        day: "numeric",
        month: "short",
        year: "numeric"
    });

    // Ocultar mensaje vacío
    historialVacio.style.display = "none";

    // Crear tarjeta
    const card = document.createElement("div");
    card.classList.add("solicitud-card");

    card.innerHTML = `
        <div class="curso-titulo">${curso}</div>
        <strong>Turno:</strong> ${turno}<br>
        <strong>Modalidad:</strong> ${modalidad}<br>
        <strong>Correo:</strong> ${correo}<br>
        <strong>Teléfono:</strong> ${telefono}<br>
        <strong>Motivo:</strong> ${motivo}<br>
        <strong>Fecha de solicitud:</strong> ${fecha}<br>
    `;

    // Si hay evidencia, crear botón de descarga
    if (evidencia) {
        const link = document.createElement("a");
        link.classList.add("btn-descargar");
        link.textContent = `Descargar evidencia (${evidencia.name})`;

        const url = URL.createObjectURL(evidencia);
        link.href = url;
        link.download = evidencia.name;

        card.appendChild(link);
    }

    // Agregar al historial
    historialLista.appendChild(card);

    // Reset form
    form.reset();
});
