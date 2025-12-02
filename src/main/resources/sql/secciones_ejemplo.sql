-- Datos de ejemplo para probar el módulo de secciones
-- Ajusta los IDs de carrera, usuarios y claves primarias según tu entorno.

-- Cursos base
INSERT INTO cursos (codigo, nombre, descripcion, creditos, horas_semanales, ciclo, tipo, modalidad, carrera_id)
VALUES
('INF101', 'Introducción a la Programación', 'Fundamentos de programación', 4, 6, 1, 'OBLIGATORIO', 'PRESENCIAL', 1),
('BD201', 'Bases de Datos I', 'Modelo relacional y SQL', 3, 4, 3, 'OBLIGATORIO', 'SEMIPRESENCIAL', 1);

-- Usuarios y docentes
INSERT INTO usuarios (correo_institucional, password, rol, activo) VALUES
('docente1@demo.pe', '{noop}demo123', 'DOCENTE', 1),
('docente2@demo.pe', '{noop}demo123', 'DOCENTE', 1);

INSERT INTO docentes (codigo_docente, nombres, apellidos, dni, telefono_personal, correo_personal, correo_institucional, direccion, anio_ingreso, especialidad, estado, usuario_id)
VALUES
('D001', 'Ana', 'Rojas Vega', '12345678', '999111222', 'ana.personal@demo.pe', 'docente1@demo.pe', 'Av. Principal 123', 2015, 'Ingeniería de Software', 'ACTIVO', 1),
('D002', 'Carlos', 'Pérez Soto', '23456789', '999333444', 'carlos.personal@demo.pe', 'docente2@demo.pe', 'Jr. Central 456', 2012, 'Base de Datos', 'ACTIVO', 2);

-- Secciones
INSERT INTO secciones (codigo, capacidad, aula, modalidad, turno, periodo_academico, curso_id, docente_id, estado)
VALUES
('A01', 40, 'D0204', 'PRESENCIAL', 'DIURNO', '2024-1', 1, 1, 'ACTIVA'),
('B02', 35, 'Zoom', 'SEMIPRESENCIAL', 'NOCTURNO', '2024-2', 2, 2, 'ACTIVA');

-- Horarios asociados
INSERT INTO seccion_horarios (dia, hora_inicio, hora_fin, seccion_id)
VALUES
('LUNES', '08:00', '10:00', 1),
('MIERCOLES', '08:00', '10:00', 1),
('MARTES', '19:00', '21:00', 2),
('JUEVES', '19:00', '21:00', 2);
