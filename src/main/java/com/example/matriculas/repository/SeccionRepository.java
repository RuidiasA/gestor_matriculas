package com.example.matriculas.repository;

import com.example.matriculas.model.Seccion;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeccionRepository extends JpaRepository<Seccion, Long>, JpaSpecificationExecutor<Seccion> {

    boolean existsByCodigo(String codigo);

    List<Seccion> findByDocenteId(Long docenteId);

    @Query("SELECT s FROM Seccion s LEFT JOIN FETCH s.horarios WHERE s.docente.id = :docenteId")
    List<Seccion> findWithHorariosByDocente(@Param("docenteId") Long docenteId);

    @EntityGraph(attributePaths = {"curso", "docente", "horarios"})
    List<Seccion> findAll(Specification<Seccion> specification, Sort sort);

    @EntityGraph(attributePaths = {"curso", "docente", "horarios"})
    @Query("SELECT s FROM Seccion s WHERE s.id = :id")
    Optional<Seccion> findDetalleById(@Param("id") Long id);

    @Query("SELECT DISTINCT s.periodoAcademico FROM Seccion s WHERE s.periodoAcademico IS NOT NULL ORDER BY s.periodoAcademico DESC")
    List<String> findDistinctPeriodos();

}
