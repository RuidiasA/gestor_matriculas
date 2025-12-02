package com.example.matriculas.repository;

import com.example.matriculas.model.SeccionCambioLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeccionCambioLogRepository extends JpaRepository<SeccionCambioLog, Long> {

    List<SeccionCambioLog> findBySeccionIdOrderByFechaDesc(Long seccionId);
}
