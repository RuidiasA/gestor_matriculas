package com.example.matriculas.repository;

import com.example.matriculas.model.Docente;
import com.example.matriculas.model.enums.EstadoDocente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    // Buscar docente por DNI
    Optional<Docente> findByDni(String dni);

    // Buscar docente por código
    Optional<Docente> findByCodigoDocente(String codigoDocente);

    boolean existsByCodigoDocente(String codigoDocente);

    @Query("""
            SELECT DISTINCT d FROM Docente d
            LEFT JOIN d.cursosDictados c
            WHERE 
                (:filtro = '' 
                    OR LOWER(d.codigoDocente) LIKE LOWER(CONCAT('%', :filtro, '%'))
                    OR LOWER(d.dni) LIKE LOWER(CONCAT('%', :filtro, '%'))
                    OR LOWER(d.nombres) LIKE LOWER(CONCAT('%', :filtro, '%'))
                    OR LOWER(d.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%'))
                    OR LOWER(d.correoInstitucional) LIKE LOWER(CONCAT('%', :filtro, '%'))
                )
                AND (:estado IS NULL OR d.estado = :estado)
                AND (:cursoId IS NULL OR c.id = :cursoId)
            ORDER BY d.apellidos ASC, d.nombres ASC
            """)
    Page<Docente> buscar(@Param("filtro") String filtro,
                         @Param("estado") EstadoDocente estado,
                         @Param("cursoId") Long cursoId,
                         Pageable pageable);
}
