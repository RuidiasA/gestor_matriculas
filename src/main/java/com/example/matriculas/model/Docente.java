package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoUsuario;
import com.example.matriculas.model.Curso;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "docentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código institucional del docente */
    @Column(name = "codigo_docente", nullable = false, unique = true)
    private String codigoDocente;

    /* Nombres y apellidos del docente */
    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    /* DNI (único) */
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    /* Información de contacto */
    private String telefonoPersonal;

    @Column(nullable = false, unique = true)
    private String correoPersonal;

    @Column(nullable = false, unique = true)
    private String correoInstitucional;

    private String direccion;

    /* Año en que ingresó a la universidad */
    private Integer anioIngreso;

    /* Especialidad del docente — obligatorio */
    @Column(nullable = false)
    private String especialidad;

    /* Estado del docente: ACTIVO / INACTIVO */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado;

    /* Relación con usuario para login */
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    /* Cursos que el docente puede dictar */
    @ManyToMany
    @JoinTable(name = "docente_cursos",
            joinColumns = @JoinColumn(name = "docente_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id"))
    private Set<Curso> cursosDictables = new HashSet<>();

    /* Relación con las secciones que este docente dicta */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Seccion> secciones = new ArrayList<>();
}
