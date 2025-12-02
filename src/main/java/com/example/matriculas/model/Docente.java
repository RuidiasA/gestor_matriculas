package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoDocente;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "docentes")
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_docente", nullable = false, unique = true)
    private String codigoDocente;

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
    private String especialidad;

    @Enumerated(EnumType.STRING)
    private EstadoDocente estado;

    private Integer anioIngreso;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
            name = "docente_cursos",
            joinColumns = @JoinColumn(name = "docente_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private List<Curso> cursosDictados;

    @OneToMany(mappedBy = "docente")
    private List<Seccion> secciones;

    public Docente() {}
}
