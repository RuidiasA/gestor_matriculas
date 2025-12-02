package com.example.matriculas.repository;

import com.example.matriculas.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    Optional<Alumno> findByCodigoAlumno(String codigoAlumno);
    Optional<Alumno> findByCorreoInstitucional(String correoInstitucional);
}
