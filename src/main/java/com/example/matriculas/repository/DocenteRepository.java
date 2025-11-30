package com.example.matriculas.repository;

import com.example.matriculas.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    // Buscar por correo institucional del usuario vinculado
    Optional<Docente> findByUsuario_CorreoInstitucional(String correoInstitucional);

    // Buscar docente por DNI (opcional)
    Optional<Docente> findByDni(String dni);
}
