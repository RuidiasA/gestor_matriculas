package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seccion_cambios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionCambioLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String campoModificado;

    @Column(nullable = false, length = 1000)
    private String valorAnterior;

    @Column(nullable = false, length = 1000)
    private String valorNuevo;

    @Column(length = 1000)
    private String observacion;
}
