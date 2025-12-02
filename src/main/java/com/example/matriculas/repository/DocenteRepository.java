package com.example.matriculas.repository;

import com.example.matriculas.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Long> {
    Optional<Docente> findByCodigoDocente(String codigoDocente);
}
