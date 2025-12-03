package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seccion_cambios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    private LocalDateTime fecha;
    private String usuario;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;

    @Column(columnDefinition = "TEXT")
    private String observacion;
}
