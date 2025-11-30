package com.example.matriculas.repository;

import com.example.matriculas.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByCodigoAlumno(String codigoAlumno);

    Optional<Alumno> findByDni(String dni);

    Optional<Alumno> findByCorreoInstitucional(String correoInstitucional);

    Optional<Alumno> findByUsuarioId(Long usuarioId);

    Optional<Alumno> findByDniAndCodigoAlumnoAndCorreoInstitucional(String dni, String codigoAlumno, String correoInstitucional);
}
