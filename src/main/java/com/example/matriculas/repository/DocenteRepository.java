package com.example.matriculas.repository;

import com.example.matriculas.model.Docente;
import com.example.matriculas.model.enums.EstadoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    // Buscar por correo institucional del usuario vinculado
    Optional<Docente> findByUsuario_CorreoInstitucional(String correoInstitucional);

    // Buscar docente por DNI (opcional)
    Optional<Docente> findByDni(String dni);

    Optional<Docente> findByCodigoDocente(String codigoDocente);

    boolean existsByCodigoDocente(String codigoDocente);

    @Query("""
            SELECT d FROM Docente d
            LEFT JOIN d.cursosDictables c
            WHERE (:filtro = '' OR lower(d.codigoDocente) LIKE CONCAT('%', :filtro, '%')
                OR lower(d.dni) LIKE CONCAT('%', :filtro, '%')
                OR lower(d.nombres) LIKE CONCAT('%', :filtro, '%')
                OR lower(d.apellidos) LIKE CONCAT('%', :filtro, '%')
                OR lower(d.correoInstitucional) LIKE CONCAT('%', :filtro, '%'))
              AND (:estado IS NULL OR d.estado = :estado)
              AND (:cursoId IS NULL OR c.id = :cursoId)
            GROUP BY d
            ORDER BY d.apellidos ASC
            """)
    Page<Docente> buscar(@Param("filtro") String filtro,
                         @Param("estado") EstadoUsuario estado,
                         @Param("cursoId") Long cursoId,
                         Pageable pageable);
}
