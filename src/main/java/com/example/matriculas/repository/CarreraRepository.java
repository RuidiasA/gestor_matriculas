package com.example.matriculas.repository;

import com.example.matriculas.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {
    Optional<Carrera> findByCodigo(String codigo);
}
