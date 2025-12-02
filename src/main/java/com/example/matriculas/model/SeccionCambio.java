package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seccion_cambios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String usuario;

    @Column(name = "campo_modificado")
    private String campoModificado;

    @Column(name = "valor_anterior")
    private String valorAnterior;

    @Column(name = "valor_nuevo")
    private String valorNuevo;

    @Column(columnDefinition = "TEXT")
    private String observacion;
}
