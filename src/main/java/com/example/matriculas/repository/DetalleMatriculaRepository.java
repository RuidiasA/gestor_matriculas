package com.example.matriculas.repository;

import com.example.matriculas.model.DetalleMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, Long> {
}
