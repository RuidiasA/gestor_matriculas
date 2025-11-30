package com.example.matriculas.repository;

import com.example.matriculas.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT a FROM Alumno a
            WHERE (:filtro IS NULL OR :filtro = ''
                OR LOWER(a.dni) LIKE LOWER(CONCAT('%', :filtro, '%'))
                OR LOWER(a.codigoAlumno) LIKE LOWER(CONCAT('%', :filtro, '%'))
                OR LOWER(a.nombres) LIKE LOWER(CONCAT('%', :filtro, '%'))
                OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%'))
                OR LOWER(a.correoInstitucional) LIKE LOWER(CONCAT('%', :filtro, '%')))
            ORDER BY a.apellidos ASC, a.nombres ASC
            """)
    Page<Alumno> buscarPorFiltro(@Param("filtro") String filtro, Pageable pageable);
}
