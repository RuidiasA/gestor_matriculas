package com.example.matriculas.service;

import com.example.matriculas.dto.SolicitudSeccionAdminDTO;
import com.example.matriculas.model.SolicitudSeccion;
import com.example.matriculas.model.enums.EstadoSolicitud;
import com.example.matriculas.repository.SolicitudSeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudSeccionService {

    private final SolicitudSeccionRepository solicitudSeccionRepository;

    @Transactional(readOnly = true)
    public List<SolicitudSeccionAdminDTO> listarTodas() {
        return solicitudSeccionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(SolicitudSeccion::getFechaSolicitud, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapearAdmin)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudSeccionAdminDTO> listarPendientes() {
        return solicitudSeccionRepository.findByEstado(EstadoSolicitud.PENDIENTE)
                .stream()
                .sorted(Comparator.comparing(SolicitudSeccion::getFechaSolicitud, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::mapearAdmin)
                .collect(Collectors.toList());
    }

    @Transactional
    public void aprobar(Long id) {
        SolicitudSeccion solicitud = obtenerPorId(id);
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitudSeccionRepository.save(solicitud);
    }

    @Transactional
    public void rechazar(Long id, String razon) {
        SolicitudSeccion solicitud = obtenerPorId(id);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setRespuesta(razon);
        solicitudSeccionRepository.save(solicitud);
    }

    private SolicitudSeccion obtenerPorId(Long id) {
        return solicitudSeccionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    private SolicitudSeccionAdminDTO mapearAdmin(SolicitudSeccion s) {
        String alumnoNombre = Optional.ofNullable(s.getAlumno())
                .map(a -> (a.getNombres() + " " + a.getApellidos()).trim())
                .orElse(null);
        return SolicitudSeccionAdminDTO.builder()
                .id(s.getId())
                .alumno(alumnoNombre)
                .codigoAlumno(Optional.ofNullable(s.getAlumno()).map(al -> al.getCodigoAlumno()).orElse(null))
                .curso(Optional.ofNullable(s.getCurso()).map(c -> c.getNombre()).orElse(null))
                .codigoCurso(Optional.ofNullable(s.getCurso()).map(c -> c.getCodigo()).orElse(null))
                .ciclo(s.getCicloAcademico())
                .motivo(s.getMotivo())
                .modalidad(s.getModalidad())
                .turno(s.getTurno())
                .telefono(s.getTelefono())
                .correo(s.getCorreo())
                .estado(s.getEstado() != null ? s.getEstado().name() : null)
                .respuesta(s.getRespuesta())
                .fechaSolicitud(s.getFechaSolicitud())
                .build();
    }
}
