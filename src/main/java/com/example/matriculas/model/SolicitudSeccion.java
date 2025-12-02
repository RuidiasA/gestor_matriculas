package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_seccion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    private String modalidad;

    private String turno;

    private String telefono;

    private String correo;

    private String motivo;

    @Column(name = "evidencia_nombre_archivo")
    private String evidenciaNombreArchivo;
}
