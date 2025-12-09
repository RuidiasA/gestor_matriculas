package com.example.matriculas.repository;

import com.example.matriculas.model.SolicitudSeccion;
import com.example.matriculas.model.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SolicitudSeccionRepository extends JpaRepository<SolicitudSeccion, Long>, JpaSpecificationExecutor<SolicitudSeccion> {

    List<SolicitudSeccion> findByAlumnoId(Long alumnoId);

    List<SolicitudSeccion> findByEstado(EstadoSolicitud estado);

    boolean existsByAlumnoIdAndCursoIdAndEstado(Long alumnoId, Long cursoId, EstadoSolicitud estado);

    boolean existsByAlumnoIdAndCursoIdAndCicloAcademico(Long alumnoId, Long cursoId, String cicloAcademico);

    long countByEstado(EstadoSolicitud estado);

    long countByCursoId(Long cursoId);

    List<SolicitudSeccion> findByCursoIdAndEstado(Long cursoId, EstadoSolicitud estado);

    @Query("SELECT s FROM SolicitudSeccion s WHERE s.fechaSolicitud BETWEEN :inicio AND :fin")
    List<SolicitudSeccion> findByFechaSolicitudBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
