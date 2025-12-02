package com.example.matriculas.model;

import com.example.matriculas.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "seccion_horarios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;

    @Enumerated(EnumType.STRING)
    private DiaSemana dia;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
}
