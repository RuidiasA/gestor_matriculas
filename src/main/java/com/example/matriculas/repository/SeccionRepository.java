package com.example.matriculas.repository;

import com.example.matriculas.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    boolean existsByCodigo(String codigo);

    List<Seccion> findByDocenteId(Long docenteId);

    @Query("SELECT s FROM Seccion s LEFT JOIN FETCH s.horarios WHERE s.docente.id = :docenteId")
    List<Seccion> findWithHorariosByDocente(@Param("docenteId") Long docenteId);

}
