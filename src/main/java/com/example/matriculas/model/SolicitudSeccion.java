package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_seccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Alumno que realizó la solicitud */
    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    /* Curso que el alumno quiere solicitar */
    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    /* Turno solicitado: Diurno / Nocturno */
    @Column(nullable = false)
    private String turno;

    /* Modalidad solicitada */
    @Column(nullable = false)
    private String modalidad;

    /* Datos de contacto */
    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String telefono;

    /* Motivo de la solicitud */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    /* Nombre del archivo adjunto (si lo hay) */
    private String evidenciaNombreArchivo;

    /* Fecha en la que se realizó la solicitud */
    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;
}
