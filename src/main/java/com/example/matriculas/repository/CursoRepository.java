package com.example.matriculas.repository;

import com.example.matriculas.model.Curso;
import com.example.matriculas.model.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

    Optional<Curso> findByCodigo(String codigo);

    List<Curso> findByCarrera(Carrera carrera);

    List<Curso> findByCiclo(int ciclo);

    List<Curso> findByNombreContainingIgnoreCase(String nombre);
}
