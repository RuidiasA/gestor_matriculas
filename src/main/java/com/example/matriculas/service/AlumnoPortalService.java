package com.example.matriculas.service;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Matricula;
import com.example.matriculas.model.Pago;
import com.example.matriculas.model.SeccionHorario;
import com.example.matriculas.model.enums.EstadoMatricula;
import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.MatriculaRepository;
import com.example.matriculas.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.matriculas.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlumnoPortalService {

    private final AlumnoRepository alumnoRepository;
    private final AlumnoService alumnoService;
    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pensionRepository;

    @Transactional(readOnly = true)
    public AlumnoPerfilDTO obtenerPerfil() {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = obtenerCicloActual(alumno);
        Matricula matriculaActual = ciclo != null
                ? matriculaRepository.findWithDetallesByAlumnoAndCiclo(alumno.getId(), ciclo).orElse(null)
                : null;

        ResumenMatriculaDTO resumen = ciclo != null ? alumnoService.obtenerResumen(alumno.getId(), ciclo) : null;

        return AlumnoPerfilDTO.builder()
                .codigo(alumno.getCodigoAlumno())
                .nombres(alumno.getNombres())
                .apellidos(alumno.getApellidos())
                .correoInstitucional(alumno.getCorreoInstitucional())
                .cicloActual(alumno.getCicloActual())
                .carrera(alumno.getCarrera() != null ? alumno.getCarrera().getNombre() : null)
                .ordenMerito(alumno.getOrdenMerito())
                .totalCreditosActuales(matriculaActual != null ? matriculaActual.getTotalCreditos() : null)
                .resumenMatricula(resumen)
                .build();
    }

    @Transactional(readOnly = true)
    public MatriculaActualDTO obtenerMatriculaActual() {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = obtenerCicloActual(alumno);
        if (ciclo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El alumno no tiene matrícula registrada");
        }

        Matricula matricula = matriculaRepository.findWithDetallesByAlumnoAndCiclo(alumno.getId(), ciclo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matrícula no encontrada"));

        ResumenMatriculaDTO resumen = alumnoService.obtenerResumen(alumno.getId(), ciclo);

        return MatriculaActualDTO.builder()
                .totalCreditos(matricula.getTotalCreditos())
                .totalHoras(matricula.getTotalHoras())
                .montoMatricula(resumen != null ? resumen.getMatricula() : null)
                .montoPensionMensual(resumen != null ? resumen.getPension() : null)
                .montoTotal(resumen != null ? resumen.getMontoTotal() : matricula.getMontoTotal())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CursoMatriculadoDTO> cursosMatriculadosActuales() {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = obtenerCicloActual(alumno);
        if (ciclo == null) {
            return List.of();
        }
        return alumnoService.obtenerCursos(alumno.getId(), ciclo);
    }

    @Transactional(readOnly = true)
    public List<HorarioDTO> obtenerHorarioActual() {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = obtenerCicloActual(alumno);
        if (ciclo == null) {
            return List.of();
        }

        Optional<Matricula> matriculaOpt = matriculaRepository.findWithDetallesAndHorarioByAlumnoAndCiclo(alumno.getId(), ciclo);
        Matricula matricula = matriculaOpt.orElse(null);
        if (matricula == null || matricula.getDetalles() == null) {
            return List.of();
        }

        return matricula.getDetalles().stream()
                .filter(det -> det.getMatricula() != null && det.getMatricula().getEstado() != EstadoMatricula.ANULADA)
                .sorted(Comparator.comparing(det ->
                        det.getSeccion() != null && det.getSeccion().getCurso() != null
                                ? det.getSeccion().getCurso().getNombre()
                                : ""
                ))
                .flatMap(det -> {
                    List<SeccionHorario> horarios =
                            det.getSeccion() != null ? det.getSeccion().getHorarios() : new ArrayList<>();

                    if (horarios == null || horarios.isEmpty()) {
                        return List.<HorarioDTO>of().stream();
                    }

                    return horarios.stream().map(h -> HorarioDTO.builder()
                            .curso(det.getSeccion() != null && det.getSeccion().getCurso() != null
                                    ? det.getSeccion().getCurso().getNombre()
                                    : null)
                            .dia(h.getDia() != null ? h.getDia().name() : null)
                            .horaInicio(h.getHoraInicio() != null ? h.getHoraInicio().toString() : null)
                            .horaFin(h.getHoraFin() != null ? h.getHoraFin().toString() : null)
                            .aula(det.getAula())
                            .docente(det.getSeccion().getDocente() != null
                                    ? (det.getSeccion().getDocente().getNombres() + " " + det.getSeccion().getDocente().getApellidos()).trim()
                                    : null)
                            .build());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoDTO> obtenerPagos(boolean soloPendientes) {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = obtenerCicloActual(alumno);
        if (ciclo == null) {
            return List.of();
        }

        List<Pago> pensiones = pensionRepository.findByAlumnoIdAndPeriodo(alumno.getId(), ciclo);
        return pensiones.stream()
                .filter(p -> !soloPendientes || EstadoPago.PAGADO != p.getEstado())
                .map(this::mapearPago)
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarPagoComoPagado(Long pagoId) {
        Alumno alumno = obtenerAlumnoActual();
        Pago pension = pensionRepository.findByIdAndAlumnoId(pagoId, alumno.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
        pension.setEstado(EstadoPago.PAGADO);
        pension.setFechaPago(LocalDate.now());
        pensionRepository.save(pension);
    }

    private PagoDTO mapearPago(Pago p) {
        return PagoDTO.builder()
                .id(p.getId())
                .concepto(p.getConcepto())
                .periodo(p.getPeriodo())
                .monto(p.getMonto() != null ? p.getMonto().doubleValue() : null)
                .vencimiento(p.getFechaVencimiento())
                .estado(p.getEstado() != null ? p.getEstado().name() : null)
                .fechaPago(p.getFechaPago())
                .build();
    }

    private Alumno obtenerAlumnoActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        Long usuarioId = null;
        if (auth.getPrincipal() instanceof CustomUserDetails cud) {
            usuarioId = cud.getUsuario().getId();
        }
        if (usuarioId == null) {
            usuarioId = alumnoService.obtenerPorEmailLogin(auth.getName())
                    .map(a -> a.getUsuario().getId())
                    .orElse(null);
        }
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inválido");
        }
        return alumnoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));
    }

    private String obtenerCicloActual(Alumno alumno) {
        List<Matricula> matriculas = matriculaRepository.findByAlumnoIdOrderByFechaMatriculaDesc(alumno.getId());
        return matriculas.isEmpty() ? null : matriculas.get(0).getCicloAcademico();
    }
}
