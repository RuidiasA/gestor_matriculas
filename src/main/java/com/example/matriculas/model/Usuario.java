package com.example.matriculas.model;
import com.example.matriculas.model.enums.Rol;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToOne(mappedBy = "usuario")
    private Alumno alumno;

    @OneToOne(mappedBy = "usuario")
    private Docente docente;

    @OneToOne(mappedBy = "usuario")
    private Administrador administrador;

    public Usuario() {}
}
