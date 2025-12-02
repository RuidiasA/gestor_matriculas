package com.example.matriculas.repository;

import com.example.matriculas.model.SeccionCambio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeccionCambioRepository extends JpaRepository<SeccionCambio, Long> {

    List<SeccionCambio> findBySeccionIdOrderByFechaDesc(Long seccionId);
}
