package com.example.matriculas.repository;

import com.example.matriculas.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    // Para obtener TODOS los ciclos del alumno
    List<Matricula> findByAlumnoId(Long alumnoId);

    // Para obtener UNA matrícula según ciclo del alumno
    Optional<Matricula> findByAlumnoIdAndCicloAcademico(Long alumnoId, String cicloAcademico);

    // Para historial (ordenado por fecha)
    List<Matricula> findByAlumnoIdOrderByFechaMatriculaDesc(Long alumnoId);

}
