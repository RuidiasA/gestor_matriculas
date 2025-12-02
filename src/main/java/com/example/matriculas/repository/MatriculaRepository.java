package com.example.matriculas.repository;

import com.example.matriculas.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {
    List<Matricula> findByAlumnoId(Long alumnoId);
    List<Matricula> findByCicloAcademico(String cicloAcademico);
}
