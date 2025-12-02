package com.example.matriculas.repository;

import com.example.matriculas.model.Pago;
import com.example.matriculas.model.enums.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("""
            SELECT p FROM Pago p
            WHERE p.alumno.id = :alumnoId
              AND p.periodo = :periodo
            ORDER BY p.concepto ASC
            """)
    List<Pago> findByAlumnoIdAndPeriodo(@Param("alumnoId") Long alumnoId,
                                        @Param("periodo") String periodo);

    @Query("""
            SELECT p FROM Pago p
            WHERE p.alumno.id = :alumnoId
              AND p.periodo = :periodo
              AND p.tipo = :tipo
            ORDER BY p.concepto ASC
            """)
    List<Pago> findByAlumnoIdAndPeriodoAndTipo(@Param("alumnoId") Long alumnoId,
                                               @Param("periodo") String periodo,
                                               @Param("tipo") TipoPago tipo);

    Optional<Pago> findByIdAndAlumnoId(Long id, Long alumnoId);
}
