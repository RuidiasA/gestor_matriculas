package com.example.matriculas.repository;

import com.example.matriculas.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByAlumnoId(Long alumnoId);
    List<Pago> findByMatriculaId(Long matriculaId);
}
