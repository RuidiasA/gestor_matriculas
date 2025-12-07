package com.example.matriculas.service;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.*;
import com.example.matriculas.model.enums.EstadoMatricula;
import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.DetalleMatriculaRepository;
import com.example.matriculas.repository.MatriculaRepository;
import com.example.matriculas.repository.PagoRepository;
import com.example.matriculas.repository.SeccionRepository;
import com.example.matriculas.repository.SolicitudSeccionRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlumnoPortalService {

    private final AlumnoRepository alumnoRepository;
    private final AlumnoService alumnoService;
    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pensionRepository;
    private final SeccionRepository seccionRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;
    private final SolicitudSeccionRepository solicitudSeccionRepository;

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

    @Transactional(readOnly = true)
    public List<CursoDisponibleDTO> buscarCursosDisponibles(String cicloFiltro, String modalidadFiltro, String texto) {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = cicloFiltro != null ? cicloFiltro : obtenerCicloActual(alumno);

        return seccionRepository.findAll()
                .stream()
                .filter(sec -> ciclo == null || ciclo.equalsIgnoreCase(sec.getPeriodoAcademico()))
                .filter(sec -> modalidadFiltro == null || sec.getModalidad() == null || sec.getModalidad().name().equalsIgnoreCase(modalidadFiltro))
                .filter(sec -> {
                    if (!StringUtils.hasText(texto)) return true;
                    String normalizado = texto.toLowerCase();
                    return (sec.getCurso() != null && sec.getCurso().getNombre().toLowerCase().contains(normalizado))
                            || (sec.getCurso() != null && sec.getCurso().getCodigo().toLowerCase().contains(normalizado))
                            || (sec.getCodigo() != null && sec.getCodigo().toLowerCase().contains(normalizado));
                })
                .map(sec -> CursoDisponibleDTO.builder()
                        .seccionId(sec.getId())
                        .codigoCurso(sec.getCurso() != null ? sec.getCurso().getCodigo() : null)
                        .nombreCurso(sec.getCurso() != null ? sec.getCurso().getNombre() : null)
                        .creditos(sec.getCurso() != null ? sec.getCurso().getCreditos() : null)
                        .horasSemanales(sec.getCurso() != null ? sec.getCurso().getHorasSemanales() : null)
                        .ciclo(sec.getPeriodoAcademico())
                        .docente(sec.getDocente() != null ? (sec.getDocente().getNombres() + " " + sec.getDocente().getApellidos()).trim() : null)
                        .modalidad(sec.getModalidad() != null ? sec.getModalidad().name() : null)
                        .cuposDisponibles(calcularCuposDisponibles(sec))
                        .matriculados(sec.getMatriculadosActuales())
                        .turno(sec.getTurno() != null ? sec.getTurno().name() : null)
                        .build())
                .sorted(Comparator.comparing(CursoDisponibleDTO::getNombreCurso, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoDetalleAlumnoDTO obtenerDetalleCurso(Long seccionId) {
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        List<String> prerrequisitos = Optional.ofNullable(seccion.getCurso())
                .map(Curso::getPrerrequisitos)
                .orElseGet(Set::of)
                .stream()
                .map(Curso::getNombre)
                .toList();

        List<HorarioDTO> horarios = Optional.ofNullable(seccion.getHorarios())
                .orElse(List.of())
                .stream()
                .map(h -> HorarioDTO.builder()
                        .dia(h.getDia() != null ? h.getDia().name() : null)
                        .horaInicio(h.getHoraInicio() != null ? h.getHoraInicio().toString() : null)
                        .horaFin(h.getHoraFin() != null ? h.getHoraFin().toString() : null)
                        .curso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                        .docente(seccion.getDocente() != null ? (seccion.getDocente().getNombres() + " " + seccion.getDocente().getApellidos()).trim() : null)
                        .aula(seccion.getAula())
                        .build())
                .toList();

        return CursoDetalleAlumnoDTO.builder()
                .seccionId(seccion.getId())
                .codigoSeccion(seccion.getCodigo())
                .codigoCurso(seccion.getCurso() != null ? seccion.getCurso().getCodigo() : null)
                .nombreCurso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .descripcion(seccion.getCurso() != null ? seccion.getCurso().getDescripcion() : null)
                .creditos(seccion.getCurso() != null ? seccion.getCurso().getCreditos() : null)
                .horasSemanales(seccion.getCurso() != null ? seccion.getCurso().getHorasSemanales() : null)
                .docente(seccion.getDocente() != null ? (seccion.getDocente().getNombres() + " " + seccion.getDocente().getApellidos()).trim() : null)
                .aula(seccion.getAula())
                .turno(seccion.getTurno() != null ? seccion.getTurno().name() : null)
                .modalidad(seccion.getModalidad() != null ? seccion.getModalidad().name() : null)
                .cuposDisponibles(calcularCuposDisponibles(seccion))
                .matriculados(seccion.getMatriculadosActuales())
                .prerrequisitos(prerrequisitos)
                .horarios(horarios)
                .build();
    }

    @Transactional(readOnly = true)
    public ValidacionMatriculaDTO validarMatricula(Long seccionId) {
        Alumno alumno = obtenerAlumnoActual();
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        List<String> mensajes = new ArrayList<>();
        boolean cupos = calcularCuposDisponibles(seccion) > 0;
        if (!cupos) mensajes.add("No hay cupos disponibles");

        boolean requisitos = cumplePrerrequisitos(alumno, seccion);
        if (!requisitos) mensajes.add("Faltan prerrequisitos del curso");

        boolean sinCruce = validarCruceHorario(alumno, seccion);
        if (!sinCruce) mensajes.add("Existe choque de horario con cursos ya inscritos");

        boolean periodo = seccion.getPeriodoAcademico() != null;
        if (!periodo) mensajes.add("La sección no tiene un periodo académico definido");

        boolean creditosOk = validarTopesCreditos(alumno, seccion);
        if (!creditosOk) mensajes.add("Se excede el máximo de créditos permitidos");

        boolean puede = cupos && requisitos && sinCruce && periodo && creditosOk;

        return ValidacionMatriculaDTO.builder()
                .puedeMatricular(puede)
                .tieneCupos(cupos)
                .prerrequisitosCumplidos(requisitos)
                .sinCruceHorario(sinCruce)
                .dentroDelPeriodo(periodo)
                .creditosDisponibles(creditosOk)
                .mensajes(mensajes)
                .build();
    }

    @Transactional
    public CursoMatriculadoDTO matricular(Long seccionId) {
        ValidacionMatriculaDTO validacion = validarMatricula(seccionId);
        if (!validacion.isPuedeMatricular()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.join("; ", validacion.getMensajes()));
        }

        Alumno alumno = obtenerAlumnoActual();
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        String ciclo = seccion.getPeriodoAcademico();
        Matricula matricula = matriculaRepository.findWithDetallesAndHorarioByAlumnoAndCiclo(alumno.getId(), ciclo)
                .orElseGet(() -> crearMatricula(alumno, ciclo));

        DetalleMatricula detalle = new DetalleMatricula();
        detalle.setMatricula(matricula);
        detalle.setSeccion(seccion);
        detalle.setDocente(seccion.getDocente());
        detalle.setCreditos(seccion.getCurso() != null ? seccion.getCurso().getCreditos() : null);
        detalle.setHorasSemanales(seccion.getCurso() != null ? seccion.getCurso().getHorasSemanales() : null);
        detalle.setModalidad(seccion.getModalidad());
        detalle.setAula(seccion.getAula());
        detalle.setHorarioTexto(seccion.getHorarios() != null ? seccion.getHorarios().toString() : null);

        detalleMatriculaRepository.save(detalle);

        actualizarTotales(matricula);

        return CursoMatriculadoDTO.builder()
                .codigoSeccion(seccion.getCodigo())
                .nombreCurso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .docente(seccion.getDocente() != null ? (seccion.getDocente().getNombres() + " " + seccion.getDocente().getApellidos()).trim() : null)
                .creditos(detalle.getCreditos())
                .horasSemanales(detalle.getHorasSemanales())
                .modalidad(seccion.getModalidad() != null ? seccion.getModalidad().name() : null)
                .aula(seccion.getAula())
                .build();
    }

    @Transactional
    public void retirar(Long seccionId) {
        Alumno alumno = obtenerAlumnoActual();
        Matricula matricula = matriculaRepository.findWithDetallesAndHorarioByAlumnoAndCiclo(alumno.getId(), obtenerCicloActual(alumno))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró matrícula activa"));

        DetalleMatricula detalle = detalleMatriculaRepository.findByMatriculaId(matricula.getId())
                .stream()
                .filter(d -> d.getSeccion() != null && seccionId.equals(d.getSeccion().getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El curso no está inscrito"));

        detalleMatriculaRepository.delete(detalle);
        actualizarTotales(matricula);
    }

    @Transactional(readOnly = true)
    public List<HistorialAlumnoDTO> obtenerHistorial() {
        Alumno alumno = obtenerAlumnoActual();
        return matriculaRepository.findByAlumnoId(alumno.getId())
                .stream()
                .sorted(Comparator.comparing(Matricula::getFechaMatricula, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(m -> HistorialAlumnoDTO.builder()
                        .ciclo(m.getCicloAcademico())
                        .estado(m.getEstado() != null ? m.getEstado().name() : null)
                        .totalCreditos(m.getTotalCreditos())
                        .totalHoras(m.getTotalHoras())
                        .cursos(Optional.ofNullable(m.getDetalles())
                                .orElse(List.of())
                                .stream()
                                .map(det -> CursoMatriculadoDTO.builder()
                                        .codigoSeccion(det.getSeccion() != null ? det.getSeccion().getCodigo() : null)
                                        .nombreCurso(det.getSeccion() != null && det.getSeccion().getCurso() != null ? det.getSeccion().getCurso().getNombre() : null)
                                        .docente(det.getSeccion() != null && det.getSeccion().getDocente() != null ? (det.getSeccion().getDocente().getNombres() + " " + det.getSeccion().getDocente().getApellidos()).trim() : null)
                                        .creditos(det.getCreditos())
                                        .horasSemanales(det.getHorasSemanales())
                                        .modalidad(det.getModalidad() != null ? det.getModalidad().name() : null)
                                        .aula(det.getAula())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    @Transactional
    public void registrarSolicitud(SolicitudSeccion solicitud) {
        if (solicitud == null || !StringUtils.hasText(solicitud.getCurso())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud está incompleta");
        }
        Alumno alumno = obtenerAlumnoActual();
        solicitud.setAlumno(alumno);
        solicitud.setFechaSolicitud(java.time.LocalDateTime.now());
        solicitudSeccionRepository.save(solicitud);
    }

    private int calcularCuposDisponibles(Seccion seccion) {
        Integer capacidad = seccion.getCapacidad();
        if (capacidad == null) return 0;
        Long matriculados = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(seccion.getId());
        return Math.max(0, capacidad - (matriculados != null ? matriculados.intValue() : 0));
    }

    private boolean validarCruceHorario(Alumno alumno, Seccion nuevaSeccion) {
        List<HorarioDTO> horarioActual = obtenerHorarioActual();
        List<SeccionHorario> horariosNuevos = Optional.ofNullable(nuevaSeccion.getHorarios()).orElse(List.of());

        for (SeccionHorario hNuevo : horariosNuevos) {
            int nuevoInicio = hNuevo.getHoraInicio() != null ? hNuevo.getHoraInicio().getHour() : -1;
            int nuevoFin = hNuevo.getHoraFin() != null ? hNuevo.getHoraFin().getHour() : -1;
            String dia = hNuevo.getDia() != null ? hNuevo.getDia().name() : null;

            for (HorarioDTO actual : horarioActual) {
                if (dia != null && dia.equalsIgnoreCase(actual.getDia())) {
                    int actualInicio = Integer.parseInt(actual.getHoraInicio().substring(0, 2));
                    int actualFin = Integer.parseInt(actual.getHoraFin().substring(0, 2));
                    boolean cruza = nuevoInicio < actualFin && actualInicio < nuevoFin;
                    if (cruza) return false;
                }
            }
        }
        return true;
    }

    private boolean cumplePrerrequisitos(Alumno alumno, Seccion seccion) {
        if (seccion.getCurso() == null || seccion.getCurso().getPrerrequisitos() == null || seccion.getCurso().getPrerrequisitos().isEmpty()) {
            return true;
        }
        List<String> cursosAprobados = matriculaRepository.findByAlumnoId(alumno.getId())
                .stream()
                .flatMap(m -> Optional.ofNullable(m.getDetalles()).orElse(List.of()).stream())
                .map(det -> det.getSeccion() != null && det.getSeccion().getCurso() != null ? det.getSeccion().getCurso().getCodigo() : null)
                .filter(Objects::nonNull)
                .toList();

        return seccion.getCurso().getPrerrequisitos()
                .stream()
                .allMatch(pr -> cursosAprobados.contains(pr.getCodigo()));
    }

    private boolean validarTopesCreditos(Alumno alumno, Seccion seccion) {
        Integer creditos = seccion.getCurso() != null ? seccion.getCurso().getCreditos() : 0;
        Matricula matriculaActual = matriculaRepository.findWithDetallesByAlumnoAndCiclo(alumno.getId(), obtenerCicloActual(alumno)).orElse(null);
        int actuales = matriculaActual != null && matriculaActual.getTotalCreditos() != null ? matriculaActual.getTotalCreditos() : 0;
        return actuales + creditos <= 24; // límite simple
    }

    private Matricula crearMatricula(Alumno alumno, String ciclo) {
        Matricula matricula = new Matricula();
        matricula.setAlumno(alumno);
        matricula.setCicloAcademico(ciclo);
        matricula.setFechaMatricula(LocalDate.now().atStartOfDay());
        matricula.setEstado(EstadoMatricula.ACTIVA);
        return matriculaRepository.save(matricula);
    }

    private void actualizarTotales(Matricula matricula) {
        List<DetalleMatricula> detalles = detalleMatriculaRepository.findByMatriculaId(matricula.getId());
        int creditos = detalles.stream().map(det -> Optional.ofNullable(det.getCreditos()).orElse(0)).mapToInt(Integer::intValue).sum();
        int horas = detalles.stream().map(det -> Optional.ofNullable(det.getHorasSemanales()).orElse(0)).mapToInt(Integer::intValue).sum();
        matricula.setTotalCreditos(creditos);
        matricula.setTotalHoras(horas);
        matricula.setMontoTotal((double) creditos * 100);
        matriculaRepository.save(matricula);
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
