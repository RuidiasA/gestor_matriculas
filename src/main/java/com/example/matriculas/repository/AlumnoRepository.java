package com.example.matriculas.repository;

import com.example.matriculas.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByCodigoAlumno(String codigoAlumno);

    Optional<Alumno> findByDni(String dni);

    Optional<Alumno> findByCorreoInstitucional(String correoInstitucional);

    Optional<Alumno> findByUsuarioId(Long usuarioId);

    Optional<Alumno> findByDniAndCodigoAlumnoAndCorreoInstitucional(String dni, String codigoAlumno, String correoInstitucional);

    List<Alumno> findAllByOrderByApellidosAscNombresAsc();

    @Query("""
            SELECT a FROM Alumno a
            WHERE LOWER(a.nombres) LIKE LOWER(CONCAT('%', :filtro, '%'))
               OR LOWER(a.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%'))
               OR LOWER(a.codigoAlumno) LIKE LOWER(CONCAT('%', :filtro, '%'))
               OR a.dni LIKE CONCAT('%', :filtro, '%')
               OR LOWER(a.correoInstitucional) LIKE LOWER(CONCAT('%', :filtro, '%'))
               OR LOWER(a.correoPersonal) LIKE LOWER(CONCAT('%', :filtro, '%'))
            """)
    List<Alumno> buscarPorFiltro(@Param("filtro") String filtro);
}
