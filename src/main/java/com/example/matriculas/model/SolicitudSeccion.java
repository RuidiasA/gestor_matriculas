package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_seccion")
@Getter
@Setter
public class SolicitudSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    private LocalDateTime fechaSolicitud;
    private String modalidad;
    private String turno;
    private String telefono;
    private String correo;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    private String evidenciaNombreArchivo;

    public SolicitudSeccion() {}
}
