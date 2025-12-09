package com.example.matriculas.service;

import com.example.matriculas.dto.SolicitudSeccionDetalleDTO;
import com.example.matriculas.dto.SolicitudSeccionListadoDTO;
import com.example.matriculas.dto.SolicitudSeccionUpdateDTO;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.SolicitudSeccion;
import com.example.matriculas.model.enums.EstadoSolicitud;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.SolicitudSeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudSeccionService {

    private final SolicitudSeccionRepository solicitudSeccionRepository;
    private final CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public List<SolicitudSeccionListadoDTO> listar(
            String estadoFiltro,
            Long cursoId,
            Long carreraId,
            String ciclo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {

        // SOLUCIÓN: evitar ambiguity con Specification.where()
        Specification<SolicitudSeccion> spec = (root, query, cb) -> cb.conjunction();

        if (cursoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("curso").get("id"), cursoId)
            );
        }

        if (carreraId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("curso").get("carrera").get("id"), carreraId)
            );
        }

        if (StringUtils.hasText(ciclo)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("cicloAcademico"), ciclo)
            );
        }

        if (StringUtils.hasText(estadoFiltro)) {
            try {
                EstadoSolicitud estado = EstadoSolicitud.valueOf(estadoFiltro.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("estado"), estado)
                );
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
            }
        }

        if (fechaInicio != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaSolicitud"), inicio)
            );
        }

        if (fechaFin != null) {
            LocalDateTime fin = fechaFin.plusDays(1).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("fechaSolicitud"), fin)
            );
        }

        // OBTENER SOLICITUDES
        List<SolicitudSeccion> solicitudes = solicitudSeccionRepository.findAll(spec)
                .stream()
                .sorted(Comparator.comparing(
                        SolicitudSeccion::getFechaSolicitud,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();

        // CONTAR TOTAL POR CURSO
        Map<Long, Long> totalPorCurso = solicitudes.stream()
                .filter(s -> s.getCurso() != null && s.getCurso().getId() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getCurso().getId(),
                        Collectors.counting()
                ));

        return solicitudes.stream()
                .map(s -> mapearListado(
                        s,
                        totalPorCurso.getOrDefault(
                                Optional.ofNullable(s.getCurso())
                                        .map(Curso::getId)
                                        .orElse(null),
                                0L
                        )))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<SolicitudSeccionListadoDTO> listarPendientesPorCurso(Long cursoId) {
        List<SolicitudSeccion> solicitudes = cursoId == null
                ? List.of()
                : solicitudSeccionRepository.findByCursoIdAndEstado(cursoId, EstadoSolicitud.PENDIENTE);

        long total = solicitudes.size();
        return solicitudes.stream()
                .sorted(Comparator.comparing(SolicitudSeccion::getFechaSolicitud, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(s -> mapearListado(s, total))
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudSeccionDetalleDTO obtenerDetalle(Long id) {
        SolicitudSeccion solicitud = obtenerPorId(id);
        List<SolicitudSeccionListadoDTO> relacionados = solicitud.getCurso() == null ? List.of()
                : listarPendientesPorCurso(solicitud.getCurso().getId());

        return SolicitudSeccionDetalleDTO.builder()
                .id(solicitud.getId())
                .alumno(Optional.ofNullable(solicitud.getAlumno()).map(a -> (a.getNombres() + " " + a.getApellidos()).trim()).orElse(null))
                .codigoAlumno(Optional.ofNullable(solicitud.getAlumno()).map(a -> a.getCodigoAlumno()).orElse(null))
                .curso(Optional.ofNullable(solicitud.getCurso()).map(Curso::getNombre).orElse(null))
                .codigoCurso(Optional.ofNullable(solicitud.getCurso()).map(Curso::getCodigo).orElse(null))
                .carrera(Optional.ofNullable(solicitud.getCurso()).map(c -> c.getCarrera() != null ? c.getCarrera().getNombre() : null).orElse(null))
                .ciclo(solicitud.getCicloAcademico())
                .estado(solicitud.getEstado() != null ? solicitud.getEstado().name() : null)
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .fechaActualizacion(solicitud.getFechaActualizacion())
                .mensajeAdmin(solicitud.getMensajeAdmin())
                .motivo(solicitud.getMotivo())
                .diaSolicitado(solicitud.getDiaSolicitado())
                .horaInicioSolicitada(solicitud.getHoraInicioSolicitada() != null ? solicitud.getHoraInicioSolicitada().toString() : null)
                .horaFinSolicitada(solicitud.getHoraFinSolicitada() != null ? solicitud.getHoraFinSolicitada().toString() : null)
                .modalidadSolicitada(solicitud.getModalidadSolicitada())
                .turnoSolicitado(solicitud.getTurnoSolicitado())
                .evidenciaNombreArchivo(solicitud.getEvidenciaNombreArchivo())
                .evidenciaContentType(solicitud.getEvidenciaContentType())
                .evidenciaBase64(solicitud.getEvidenciaContenido() != null
                        ? Base64.getEncoder().encodeToString(solicitud.getEvidenciaContenido())
                        : null)
                .solicitantes(solicitud.getCurso() != null && solicitud.getCurso().getId() != null
                        ? solicitudSeccionRepository.countByCursoId(solicitud.getCurso().getId())
                        : null)
                .relacionados(relacionados)
                .build();
    }

    @Transactional
    public void actualizarEstado(Long id, SolicitudSeccionUpdateDTO dto) {
        SolicitudSeccion solicitud = obtenerPorId(id);
        EstadoSolicitud nuevoEstado = EstadoSolicitud.valueOf(dto.getEstado().toUpperCase());
        if (nuevoEstado == EstadoSolicitud.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado debe ser SOLUCIONADA o RECHAZADA");
        }
        solicitud.setEstado(nuevoEstado);
        solicitud.setMensajeAdmin(StringUtils.hasText(dto.getMensajeAdmin()) ? dto.getMensajeAdmin().trim() : null);
        solicitud.setFechaActualizacion(LocalDateTime.now());
        solicitudSeccionRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoSolicitud estado) {
        return solicitudSeccionRepository.countByEstado(estado);
    }

    @Transactional(readOnly = true)
    public List<Curso> catalogoCursos() {
        return cursoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<String> catalogoCiclos() {
        return cursoRepository.findAll().stream()
                .map(Curso::getCiclo)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .distinct()
                .sorted()
                .toList();
    }

    private SolicitudSeccion obtenerPorId(Long id) {
        return solicitudSeccionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    private SolicitudSeccionListadoDTO mapearListado(SolicitudSeccion s, Long totalCurso) {
        return SolicitudSeccionListadoDTO.builder()
                .id(s.getId())
                .curso(Optional.ofNullable(s.getCurso()).map(Curso::getNombre).orElse(null))
                .codigoCurso(Optional.ofNullable(s.getCurso()).map(Curso::getCodigo).orElse(null))
                .carrera(Optional.ofNullable(s.getCurso()).map(c -> c.getCarrera() != null ? c.getCarrera().getNombre() : null).orElse(null))
                .ciclo(s.getCicloAcademico())
                .solicitantes(totalCurso)
                .estado(s.getEstado() != null ? s.getEstado().name() : null)
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaActualizacion(s.getFechaActualizacion())
                .mensajeAdmin(s.getMensajeAdmin())
                .alumno(Optional.ofNullable(s.getAlumno()).map(a -> (a.getNombres() + " " + a.getApellidos()).trim()).orElse(null))
                .diaSolicitado(s.getDiaSolicitado())
                .horaInicioSolicitada(Optional.ofNullable(s.getHoraInicioSolicitada()).map(Object::toString).orElse(null))
                .horaFinSolicitada(Optional.ofNullable(s.getHoraFinSolicitada()).map(Object::toString).orElse(null))
                .modalidadSolicitada(s.getModalidadSolicitada())
                .turnoSolicitado(s.getTurnoSolicitado())
                .build();
    }
}
