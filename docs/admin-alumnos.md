# Portal Administrador: Gestión de Alumnos

## Objetivo
Consolidar los alcances y reglas de la sección **Alumnos** del portal administrador para evitar redundancias y ambigüedades antes de seguir desarrollando código.

## Alcance funcional
1. **Búsqueda y listado**
   - Cuadro de búsqueda único que filtre por DNI, código, nombres, apellidos o correo institucional.
   - Tabla resultado con columnas: Código, Nombre completo, DNI, Correos (institucional y personal), Teléfono personal, Año de ingreso, Ciclo actual y Turno/Prioridad si existe.
   - Paginación simple o scroll virtual (sin filtros adicionales duplicados).

2. **Ficha resumida del alumno seleccionado**
   - Mostrar al seleccionar una fila: foto/avatar genérico, código, nombre completo, carrera, estado, ciclo actual, año de ingreso y contacto.
   - Botón **"Editar datos de contacto"** que habilita edición sólo para: correo personal, teléfono personal y dirección (si existiera), respetando los campos permitidos en backend.

3. **Detalle académico**
   - Selector de periodo/ciclo que carga:
     - Cursos matriculados con sección, docente, créditos, horas y aula/modalidad.
     - Resumen de matrícula (créditos, horas, monto estimado) reutilizando la lógica existente.
     - Historial de matrículas previas para referencia.
   - No duplicar vistas: una sola tabla para cursos actuales y un panel de resumen/historial en columnas laterales.

4. **Operaciones CRUD expuestas**
   - **Listar** y **buscar** reutilizan el mismo dataset, evitando endpoints paralelos.
   - **Actualizar** sólo campos editables (nombres/apellidos opcional según política, correo/telefono personal). No se permite modificar código, DNI, año de ingreso ni relaciones de usuario desde esta vista.
   - **Registro** y **eliminación/inactivación** se documentan pero pueden quedar fuera del sprint si la UI aún no los soporta; el backend debe bloquear la eliminación si hay matrículas asociadas.

5. **Validaciones mínimas**
   - DNI: 8 dígitos; Código institucional: prefijo `S` + correlativo numérico; correo institucional termina en `@universidad.com.pe`.
   - Correos personales válidos según formato estándar; teléfono de 9 dígitos (Perú); nombres y apellidos no vacíos y sin espacios iniciales/finales.
   - Al actualizar, trim y normalizar mayúsculas/minúsculas sólo en campos de texto libre.

6. **Mensajería y errores**
   - Mensajes claros: "Alumno no encontrado", "Campos obligatorios incompletos" o "No se puede eliminar: el alumno tiene matrículas registradas".
   - Validaciones en frontend y backend; la API debe responder con códigos HTTP adecuados (400 para validaciones, 404 para inexistentes, 409 para conflictos de eliminación/duplicados).

7. **Seguridad y auditoría**
   - Rutas bajo `/admin/alumnos` protegidas para rol ADMIN.
   - Registrar `usuarioModificacion` y `fechaModificacion` cuando se editen datos sensibles (puede ser columna futura, pero reservar espacio en el DTO si se requiere trazabilidad).

## API recomendada (REST)
| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/admin/alumnos/listar` | Listado paginado/simple de alumnos. |
| GET | `/admin/alumnos/buscar?filtro=` | Búsqueda por DNI, código, nombre, apellido o correo. |
| GET | `/admin/alumnos/{id}` | Obtiene ficha completa del alumno seleccionado. |
| PUT | `/admin/alumnos/{id}` | Actualiza datos editables (correo/telefono personal y, opcional, nombres/apellidos). |
| POST | `/admin/alumnos` | Registra un alumno nuevo (si el front lo soporta). |
| DELETE | `/admin/alumnos/{id}` | Inactiva o elimina si no tiene matrículas asociadas; devolver 409 si no se puede. |

## Consideraciones de UX
- Reutilizar el estilo existente del portal admin (aside y tarjetas). No crear secciones duplicadas para búsqueda o detalle.
- Estado de carga uniforme: spinners o skeletons para lista y detalle; toasts para confirmaciones.
- Botón **"Limpiar"** que resetea búsqueda y selección para evitar confusión al cambiar de alumno.

## Dependencias de datos
- La ficha y los resúmenes consumen los DTO ya existentes (`AlumnoDTO`, `ResumenMatriculaDTO`, etc.).
- Para nuevas operaciones (registro/eliminación), preferir DTOs específicos para entrada y nunca exponer entidades completas.

## Fuera de alcance (para evitar dispersión)
- Modificar el front actual de login o matriculación de alumnos.
- Implementar matrícula desde el portal admin (se gestiona en módulo de alumno o en otra vista específica).
- Integrar pasarela de pagos o gestión de pensiones desde esta sección.
