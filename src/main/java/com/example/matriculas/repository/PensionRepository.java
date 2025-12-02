package com.example.matriculas.repository;

import com.example.matriculas.model.Pension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionRepository extends JpaRepository<Pension, Long> {

    @Query("SELECT p FROM Pension p WHERE p.alumno.id = :alumnoId AND p.periodo = :periodo ORDER BY p.concepto ASC")
    List<Pension> findByAlumnoIdAndPeriodo(@Param("alumnoId") Long alumnoId, @Param("periodo") String periodo);

    Optional<Pension> findByIdAndAlumnoId(Long id, Long alumnoId);
}
