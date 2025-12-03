package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "administradores")
@Getter
@Setter
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_admin", nullable = false, unique = true)
    private String codigoAdmin;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    private String nombres;
    private String apellidos;

    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    private String telefonoPersonal;
    private Integer anioIngreso;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Administrador() {}
}
