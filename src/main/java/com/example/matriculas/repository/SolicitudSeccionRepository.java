package com.example.matriculas.repository;

import com.example.matriculas.model.SolicitudSeccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudSeccionRepository extends JpaRepository<SolicitudSeccion, Long> {

    List<SolicitudSeccion> findByAlumnoId(Long alumnoId);

    List<SolicitudSeccion> findByEstado(com.example.matriculas.model.enums.EstadoSolicitud estado);

    boolean existsByAlumnoIdAndCursoIdAndCicloAcademico(Long alumnoId, Long cursoId, String cicloAcademico);
}
