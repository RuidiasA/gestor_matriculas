package com.example.matriculas.repository;

import com.example.matriculas.model.DetalleMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, Long> {
    List<DetalleMatricula> findByMatriculaId(Long matriculaId);
}
