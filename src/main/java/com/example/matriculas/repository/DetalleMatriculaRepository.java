package com.example.matriculas.repository;

import com.example.matriculas.model.DetalleMatricula;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, Long> {

    @Query("""
            SELECT dm.seccion.id, COUNT(dm)
            FROM DetalleMatricula dm
            WHERE dm.seccion.id IN :ids AND dm.matricula.estado <> com.example.matriculas.model.enums.EstadoMatricula.ANULADA
            GROUP BY dm.seccion.id
            """)
    List<Object[]> contarMatriculadosActivosPorSeccion(@Param("ids") List<Long> ids);

    @Query("""
            SELECT COUNT(dm)
            FROM DetalleMatricula dm
            WHERE dm.seccion.id = :seccionId AND dm.matricula.estado <> com.example.matriculas.model.enums.EstadoMatricula.ANULADA
            """)
    Long contarMatriculadosActivosPorSeccion(@Param("seccionId") Long seccionId);

    @Query("""
            SELECT dm FROM DetalleMatricula dm
            JOIN FETCH dm.matricula m
            JOIN FETCH m.alumno a
            WHERE dm.seccion.id = :seccionId AND m.estado <> com.example.matriculas.model.enums.EstadoMatricula.ANULADA
            """)
    List<DetalleMatricula> findBySeccionIdWithAlumno(@Param("seccionId") Long seccionId);

    @Query("""
            SELECT dm FROM DetalleMatricula dm
            JOIN FETCH dm.matricula m
            JOIN FETCH m.alumno a
            WHERE dm.seccion.id = :seccionId
            ORDER BY m.fechaMatricula DESC
            """)
    List<DetalleMatricula> findHistorialBySeccion(@Param("seccionId") Long seccionId);

    @Query("""
            SELECT DISTINCT dm FROM DetalleMatricula dm
            LEFT JOIN FETCH dm.seccion s
            LEFT JOIN FETCH s.curso
            WHERE dm.matricula.id = :matriculaId
            """)
    List<DetalleMatricula> findByMatriculaId(@Param("matriculaId") Long matriculaId);
}
