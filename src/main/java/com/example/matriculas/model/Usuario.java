package com.example.matriculas.model;

import com.example.matriculas.model.enums.Rol;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "correo_institucional")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correo_institucional", nullable = false)
    private String correoInstitucional;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    /* RELACIONES */

    // Si el usuario corresponde a un alumno
    @OneToOne(mappedBy = "usuario")
    private Alumno alumno;

    // Si el usuario corresponde a un docente
    @OneToOne(mappedBy = "usuario")
    private Docente docente;

    // Si el usuario corresponde a un administrador
    @OneToOne(mappedBy = "usuario")
    private Administrador administrador;
}
