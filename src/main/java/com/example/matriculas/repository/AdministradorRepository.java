package com.example.matriculas.repository;

import com.example.matriculas.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByCodigoAdmin(String codigoAdmin);
}
