package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoAlumno;
import com.example.matriculas.model.enums.Turno;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "alumnos")
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_alumno", nullable = false, unique = true)
    private String codigoAlumno;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    private String correoPersonal;
    private String telefonoPersonal;
    private String direccion;

    @Column(name = "anio_ingreso", nullable = false)
    private Integer anioIngreso;

    private Integer cicloActual;

    @Enumerated(EnumType.STRING)
    private Turno turno;

    @Enumerated(EnumType.STRING)
    private EstadoAlumno estado;

    private Integer ordenMerito;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "alumno")
    private List<Matricula> matriculas;

    @OneToMany(mappedBy = "alumno")
    private List<Pago> pagos;

    @OneToMany(mappedBy = "alumno")
    private List<SolicitudSeccion> solicitudes;

    public Alumno() {}
}
