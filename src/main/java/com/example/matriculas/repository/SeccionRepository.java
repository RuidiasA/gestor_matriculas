package com.example.matriculas.repository;

import com.example.matriculas.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    boolean existsByCodigo(String codigo);

}
