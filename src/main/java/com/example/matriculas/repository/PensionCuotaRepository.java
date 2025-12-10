package com.example.matriculas.repository;

import com.example.matriculas.model.PensionCuota;
import com.example.matriculas.model.enums.TipoConcepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PensionCuotaRepository extends JpaRepository<PensionCuota, Long> {

    @Query("""
            SELECT pc FROM PensionCuota pc
            WHERE pc.alumno.id = :alumnoId
              AND pc.periodoAcademico = :periodo
            ORDER BY pc.fechaVencimiento ASC, pc.numeroCuota ASC
            """)
    List<PensionCuota> findByAlumnoAndPeriodo(@Param("alumnoId") Long alumnoId,
                                              @Param("periodo") String periodo);

    @Query("SELECT DISTINCT pc.periodoAcademico FROM PensionCuota pc WHERE pc.alumno.id = :alumnoId ORDER BY pc.periodoAcademico DESC")
    List<String> findPeriodosByAlumno(@Param("alumnoId") Long alumnoId);

    boolean existsByAlumnoIdAndPeriodoAcademicoAndTipoConceptoAndNumeroCuota(Long alumnoId, String periodoAcademico,
                                                                             TipoConcepto tipoConcepto, Integer numeroCuota);

    Optional<PensionCuota> findByIdAndAlumnoId(Long id, Long alumnoId);

    List<PensionCuota> findByAlumnoId(Long alumnoId);
}
