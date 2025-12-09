ALTER TABLE solicitudes_seccion
    MODIFY evidencia_contenido LONGBLOB,
    ADD COLUMN evidencia_ruta VARCHAR(500) NULL AFTER evidencia_contenido;
