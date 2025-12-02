package com.example.matriculas.model;

import com.example.matriculas.model.enums.DiaSemana;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seccion_horarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Día de la semana (LUNES, MARTES, MIÉRCOLES...) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiaSemana dia;

    /* Hora de inicio en formato 24h (ej: "08:00") */
    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    /* Hora de fin en formato 24h (ej: "10:00") */
    @Column(name = "hora_fin", nullable = false)
    private String horaFin;

    /* La sección a la que pertenece este horario */
    @ManyToOne
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;
}
