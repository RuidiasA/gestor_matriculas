package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoUsuario;
import com.example.matriculas.model.enums.Turno;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo_alumno"),
        @UniqueConstraint(columnNames = "dni"),
        @UniqueConstraint(columnNames = "correo_institucional")
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
    @Column(name = "codigo_alumno", nullable = false, unique = true, length = 20)
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
    @Column(name = "telefono_personal", length = 9)
    private String telefonoPersonal;

    @Column(name = "correo_personal")
    private String correoPersonal;

    @Column(length = 150)
    private String direccion;

    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    /* Año en que inició la carrera */
    @Column(name = "anio_ingreso", nullable = false)
    private Integer anioIngreso;

    /* Ciclo actual del alumno (2–10) */
    @Column(name = "ciclo_actual", nullable = false)
    private Integer cicloActual;

    @Enumerated(EnumType.STRING)
    private Turno turno;

    /* Orden de mérito / prioridad del alumno */
    @Column(name = "orden_merito")
    private Integer ordenMerito;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

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
