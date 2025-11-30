package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "dni"),
        @UniqueConstraint(columnNames = "correoInstitucional")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código institucional único del alumno */
    @Column(nullable = false, unique = true, length = 20)
    private String codigoAlumno;


    /* Nombres y apellidos */
    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    /* DNI único */
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    /* Información de contacto */
    private String telefonoPersonal;

    private String correoPersonal;

    @Column(nullable = false, unique = true)
    private String correoInstitucional;

    /* Año en que inició la carrera */
    @Column(nullable = false)
    private Integer anioIngreso;

    /* Ciclo actual del alumno (2–10) */
    @Column(nullable = false)
    private Integer cicloActual;

    /* Relación con carrera */
    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    /* Relación con usuario (login) */
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    /* Matrículas hechas por el alumno */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Matricula> matriculas = new ArrayList<>();
}
