package com.example.matriculas.repository;

import com.example.matriculas.model.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Long> {
    Optional<PeriodoAcademico> findByNombre(String nombre);
}
