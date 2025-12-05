// Orquestador del Portal Admin: inicializa módulos y navegación

import { createTools, setupNavigation } from './utils.js';
import { createAlumnosModule } from './alumnos.js';
import { createDocentesModule } from './docentes.js';
import { createSeccionesModule } from './secciones.js';

document.addEventListener('DOMContentLoaded', () => {
    const tools = createTools();

    const alumnosModule = createAlumnosModule(tools);
    const docentesModule = createDocentesModule(tools);
    const seccionesModule = createSeccionesModule(tools, alumnosModule);

    setupNavigation(() => seccionesModule.resetFicha());

    alumnosModule.init();
    docentesModule.init();
    seccionesModule.init();
});
