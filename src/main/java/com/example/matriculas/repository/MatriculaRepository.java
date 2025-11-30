package com.example.matriculas.repository;

import com.example.matriculas.model.Matricula;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    List<Matricula> findByAlumnoIdOrderByFechaMatriculaDesc(Long alumnoId);

    Optional<Matricula> findByAlumnoIdAndCicloAcademico(Long alumnoId, String cicloAcademico);

    @EntityGraph(attributePaths = {"detalles", "detalles.seccion", "detalles.seccion.curso", "detalles.docente"})
    @Query("SELECT DISTINCT m FROM Matricula m WHERE m.alumno.id = :alumnoId AND m.cicloAcademico = :ciclo")
    Optional<Matricula> findWithDetallesByAlumnoAndCiclo(@Param("alumnoId") Long alumnoId, @Param("ciclo") String ciclo);

    @Query("SELECT DISTINCT m.cicloAcademico FROM Matricula m WHERE m.alumno.id = :alumnoId ORDER BY m.cicloAcademico DESC")
    List<String> findDistinctCiclosByAlumnoId(@Param("alumnoId") Long alumnoId);
}
