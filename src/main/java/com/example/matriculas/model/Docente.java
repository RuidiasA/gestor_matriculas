package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    /* Año en que ingresó a la universidad */
    private Integer anioIngreso;

    /* Especialidad del docente — obligatorio */
    @Column(nullable = false)
    private String especialidad;

    /* Relación con usuario para login */
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    /* Relación con las secciones que este docente dicta */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Seccion> secciones = new ArrayList<>();
}
