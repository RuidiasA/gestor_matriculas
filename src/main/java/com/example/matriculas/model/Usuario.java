package com.example.matriculas.model;

import com.example.matriculas.enums.RolUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Entity
@Table(name = "usuarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(nullable = false)
    private boolean activo;

    @OneToOne(mappedBy = "usuario")
    private Alumno alumno;

    @OneToOne(mappedBy = "usuario")
    private Docente docente;

    @OneToOne(mappedBy = "usuario")
    private Administrador administrador;

    public Optional<Alumno> getAlumnoOptional() {
        return Optional.ofNullable(alumno);
    }
}
