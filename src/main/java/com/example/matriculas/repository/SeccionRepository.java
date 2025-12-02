package com.example.matriculas.repository;

import com.example.matriculas.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {
    Optional<Seccion> findByCodigo(String codigo);
    List<Seccion> findByCursoId(Long cursoId);
    List<Seccion> findByDocenteId(Long docenteId);
}
