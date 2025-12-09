// Orquestador del Portal Admin: inicializa módulos y navegación

import { createTools, setupNavigation } from './utils.js';
import { createAlumnosModule } from './alumnos.js';
import { createDocentesModule } from './docentes.js';
import { createSeccionesModule } from './secciones.js';
import { createCursosModule } from "./cursos.js";
import { createSolicitudesModule } from "./solicitudes.js";

document.addEventListener('DOMContentLoaded', () => {
    const tools = createTools();

    const alumnosModule = createAlumnosModule(tools);
    const docentesModule = createDocentesModule(tools);
    const seccionesModule = createSeccionesModule(tools, alumnosModule);
    const cursosModule = createCursosModule(tools);
    const solicitudesModule = createSolicitudesModule(tools);

    setupNavigation(() => seccionesModule.resetFicha());

    alumnosModule.init();
    docentesModule.init();
    seccionesModule.init();
    cursosModule.init();
    solicitudesModule.init();
    solicitudesModule.actualizarBadge();
});
