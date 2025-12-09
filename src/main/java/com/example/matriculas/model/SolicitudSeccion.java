package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "ciclo_academico")
    private String cicloAcademico;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    private String modalidad;
    private String turno;
    private String telefono;
    private String correo;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "mensaje_admin", length = 300)
    private String mensajeAdmin;

    private String evidenciaNombreArchivo;

    @Column(name = "evidencia_content_type", length = 150)
    private String evidenciaContentType;

    @Lob
    @Column(name = "evidencia_contenido", columnDefinition = "LONGBLOB")
    private byte[] evidenciaContenido;

    @Column(name = "evidencia_ruta", length = 500)
    private String evidenciaRuta;

    @Column(name = "dia_solicitado", length = 25)
    private String diaSolicitado;

    @Column(name = "hora_inicio_solicitada")
    private LocalTime horaInicioSolicitada;

    @Column(name = "hora_fin_solicitada")
    private LocalTime horaFinSolicitada;

    @Column(name = "modalidad_solicitada", length = 50)
    private String modalidadSolicitada;

    @Column(name = "turno_solicitado", length = 50)
    private String turnoSolicitado;

    public SolicitudSeccion() {}
}
