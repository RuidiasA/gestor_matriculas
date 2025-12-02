package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "administradores",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "dni"),
                @UniqueConstraint(columnNames = "correo_institucional"),
                @UniqueConstraint(columnNames = "codigo_admin"),
                @UniqueConstraint(columnNames = "usuario_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Datos personales */
    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    /* Información de contacto */
    @Column(name = "correo_institucional", nullable = false, unique = true)
    private String correoInstitucional;

    @Column(name = "telefono_personal")
    private String telefonoPersonal;

    /* Código administrativo: A + añoIngreso + ID
       Ejemplo: A20250001
    */
    @Column(name = "codigo_admin", nullable = false, unique = true, length = 20)
    private String codigoAdmin;

    /* Usuario vinculado (login) */
    @OneToOne
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    /* Año de ingreso al área administrativa (opcional) */
    @Column(name = "anio_ingreso")
    private Integer anioIngreso;

}
