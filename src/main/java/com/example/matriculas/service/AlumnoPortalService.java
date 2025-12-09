package com.example.matriculas.service;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.*;
import com.example.matriculas.model.enums.EstadoSolicitud;
import com.example.matriculas.model.enums.EstadoSeccion;
import com.example.matriculas.model.enums.EstadoDetalleMatricula;
import com.example.matriculas.model.enums.EstadoMatricula;
import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.matriculas.security.CustomUserDetails;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

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
    private final CursoRepository cursoRepository;
    private final CursoService cursoService;


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

    @Transactional
    public Path guardarHorarioActual() {
        Alumno alumno = obtenerAlumnoActual();
        List<HorarioDTO> horario = obtenerHorarioActual();
        if (horario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay horario para guardar");
        }
        try {
            Path archivo = Files.createTempFile("horario-" + alumno.getCodigoAlumno(), ".json");
            String contenido = horario.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
            Files.writeString(archivo, contenido);
            return archivo;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el horario", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generarHorarioPdf() {
        Alumno alumno = obtenerAlumnoActual();
        List<HorarioDTO> horario = obtenerHorarioActual();
        if (horario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay cursos matriculados para generar PDF");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Horario del alumno", titulo));
            document.add(new Paragraph((alumno.getNombres() + " " + alumno.getApellidos()).trim()));
            document.add(new Paragraph("Ciclo actual: " + Optional.ofNullable(alumno.getCicloActual()).map(Object::toString).orElse("—")));
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            agregarCeldaCabecera(tabla, "Curso");
            agregarCeldaCabecera(tabla, "Día");
            agregarCeldaCabecera(tabla, "Inicio");
            agregarCeldaCabecera(tabla, "Fin");
            agregarCeldaCabecera(tabla, "Docente");

            horario.forEach(h -> {
                tabla.addCell(valorCelda(h.getCurso()));
                tabla.addCell(valorCelda(h.getDia()));
                tabla.addCell(valorCelda(h.getHoraInicio()));
                tabla.addCell(valorCelda(h.getHoraFin()));
                tabla.addCell(valorCelda(h.getDocente()));
            });

            document.add(tabla);
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el PDF", e);
        }
    }

    @Transactional(readOnly = true)
    public List<PagoDTO> obtenerPagos(boolean soloPendientes, String periodo) {
        Alumno alumno = obtenerAlumnoActual();
        String ciclo = StringUtils.hasText(periodo) ? periodo : obtenerCicloActual(alumno);
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
        Integer cicloFiltroNumero = parsearCicloNumerico(cicloFiltro);
        Integer cicloMinimo = alumno.getCicloActual();

        Set<Long> cursosAprobados = obtenerCursosAprobados(alumno);
        Set<Long> cursosMatriculadosActualmente = obtenerCursosMatriculados(alumno);

        List<Seccion> secciones = seccionRepository.findAll()
                .stream()
                .filter(sec -> sec.getCurso() != null)
                .filter(sec -> sec.getCurso().getCarrera() != null
                        && alumno.getCarrera() != null
                        && Objects.equals(sec.getCurso().getCarrera().getId(), alumno.getCarrera().getId()))
                .filter(sec -> cicloFiltroNumero == null || Objects.equals(sec.getCurso().getCiclo(), cicloFiltroNumero))
                .filter(sec -> cicloMinimo == null || (sec.getCurso().getCiclo() != null && sec.getCurso().getCiclo() >= cicloMinimo))
                .filter(sec -> !StringUtils.hasText(modalidadFiltro)
                        || (sec.getModalidad() != null && sec.getModalidad().name().equalsIgnoreCase(modalidadFiltro)))
                .filter(sec -> {
                    if (!StringUtils.hasText(texto)) return true;
                    String normalizado = texto.toLowerCase();
                    return (sec.getCurso().getNombre().toLowerCase().contains(normalizado))
                            || (sec.getCurso().getCodigo().toLowerCase().contains(normalizado))
                            || (sec.getCodigo() != null && sec.getCodigo().toLowerCase().contains(normalizado));
                })
                .filter(sec -> sec.getCurso().getId() != null
                        && !cursosAprobados.contains(sec.getCurso().getId())
                        && !cursosMatriculadosActualmente.contains(sec.getCurso().getId()))
                .toList();

        return secciones.stream()
                .collect(Collectors.groupingBy(sec -> sec.getCurso().getId()))
                .values()
                .stream()
                .map(lista -> {
                    Seccion principal = lista.get(0);
                    int cupos = lista.stream().mapToInt(this::calcularCuposDisponibles).sum();
                    int matriculados = lista.stream()
                            .mapToInt(sec -> Optional.ofNullable(sec.getMatriculadosActuales()).orElse(0))
                            .sum();
                    String docente = lista.stream()
                            .map(sec -> sec.getDocente() != null
                                    ? (sec.getDocente().getNombres() + " " + sec.getDocente().getApellidos()).trim()
                                    : null)
                            .filter(StringUtils::hasText)
                            .findFirst()
                            .orElse(null);

                    return CursoDisponibleDTO.builder()
                            .cursoId(principal.getCurso().getId())
                            .codigoCurso(principal.getCurso().getCodigo())
                            .nombreCurso(principal.getCurso().getNombre())
                            .creditos(principal.getCurso().getCreditos())
                            .horasSemanales(principal.getCurso().getHorasSemanales())
                            .ciclo(principal.getCurso().getCiclo() != null ? principal.getCurso().getCiclo().toString() : null)
                            .docente(docente)
                            .modalidad(resumenModalidad(lista))
                            .cuposDisponibles(cupos)
                            .matriculados(matriculados)
                            .turno(principal.getTurno() != null ? principal.getTurno().name() : null)
                            .build();
                })
                .sorted(Comparator.comparing(CursoDisponibleDTO::getNombreCurso, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoDetalleAlumnoDTO obtenerDetalleCurso(Long seccionId) {
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        return mapearDetalleSeccion(seccion);
    }

    @Transactional(readOnly = true)
    public List<CursoDetalleAlumnoDTO> listarSeccionesPorCurso(Long cursoId) {
        List<Seccion> secciones = seccionRepository.findByCursoId(cursoId);
        if (secciones.isEmpty()) {
            return List.of();
        }
        return secciones.stream()
                .map(this::mapearDetalleSeccion)
                .sorted(Comparator.comparing(CursoDetalleAlumnoDTO::getCodigoSeccion, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerPeriodosDisponibles() {
        return seccionRepository.findDistinctPeriodos();
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

        boolean yaInscrito = Optional.ofNullable(matricula.getDetalles())
                .orElse(List.of())
                .stream()
                .anyMatch(det -> det.getSeccion() != null && Objects.equals(det.getSeccion().getId(), seccionId));
        if (yaInscrito) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El curso ya está en tu matrícula");
        }

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
                .seccionId(seccion.getId())
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
        String cicloActual = obtenerCicloActual(alumno);
        Matricula matricula = matriculaRepository.findWithDetallesAndHorarioByAlumnoAndCiclo(alumno.getId(), cicloActual)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró matrícula activa"));

        DetalleMatricula detalle = Optional.ofNullable(matricula.getDetalles())
                .orElse(List.of())
                .stream()
                .filter(d -> d.getSeccion() != null && seccionId.equals(d.getSeccion().getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El curso no está inscrito"));

        detalleMatriculaRepository.delete(detalle);
        Optional.ofNullable(matricula.getDetalles()).ifPresent(lista -> lista.remove(detalle));
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
                                        .seccionId(det.getSeccion() != null ? det.getSeccion().getId() : null)
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
    public void registrarSolicitud(SolicitudSeccionCreateDTO solicitudDto) {
        if (solicitudDto == null || solicitudDto.getCursoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La solicitud está incompleta: curso inválido");
        }

        if (!StringUtils.hasText(solicitudDto.getMotivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo es obligatorio");
        }

        Alumno alumno = obtenerAlumnoActual();
        Carrera carreraAlumno = alumno.getCarrera();
        if (carreraAlumno == null || carreraAlumno.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tienes una carrera asignada");
        }
        String cicloActual = obtenerCicloActual(alumno);

        boolean pendiente = solicitudSeccionRepository.existsByAlumnoIdAndCursoIdAndEstado(
                alumno.getId(), solicitudDto.getCursoId(), EstadoSolicitud.PENDIENTE);
        if (pendiente) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya registraste una solicitud pendiente para este curso");
        }

        Curso curso = cursoRepository.findById(solicitudDto.getCursoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        if (curso.getCarrera() == null || !Objects.equals(curso.getCarrera().getId(), carreraAlumno.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes solicitar cursos fuera de tu carrera");
        }

        boolean tieneSeccionDisponible = seccionRepository.findByCursoId(curso.getId())
                .stream()
                .filter(sec -> sec.getEstado() != EstadoSeccion.ANULADA)
                .anyMatch(sec -> calcularCuposDisponibles(sec) > 0);
        if (tieneSeccionDisponible) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El curso ya tiene secciones disponibles");
        }

        SolicitudSeccion solicitud = new SolicitudSeccion();
        solicitud.setAlumno(alumno);
        solicitud.setCurso(curso);
        solicitud.setCicloAcademico(cicloActual);
        solicitud.setModalidad(StringUtils.hasText(solicitudDto.getModalidad()) ? solicitudDto.getModalidad() : null);
        solicitud.setTurno(solicitudDto.getTurno());
        solicitud.setCorreo(solicitudDto.getCorreo());
        solicitud.setTelefono(solicitudDto.getTelefono());
        solicitud.setMotivo(solicitudDto.getMotivo().trim());
        solicitud.setEvidenciaNombreArchivo(StringUtils.hasText(solicitudDto.getEvidenciaNombreArchivo())
                ? solicitudDto.getEvidenciaNombreArchivo().trim()
                : null);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setFechaActualizacion(solicitud.getFechaSolicitud());
        if (StringUtils.hasText(solicitudDto.getEvidenciaBase64()) && solicitudDto.getEvidenciaNombreArchivo() != null) {
            try {
                solicitud.setEvidenciaContenido(Base64.getDecoder().decode(solicitudDto.getEvidenciaBase64()));
                solicitud.setEvidenciaContentType(StringUtils.hasText(solicitudDto.getEvidenciaContentType())
                        ? solicitudDto.getEvidenciaContentType()
                        : "application/octet-stream");
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La evidencia adjunta no es válida");
            }
        }
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);

        solicitudSeccionRepository.save(solicitud);
    }

    @Transactional(readOnly = true)
    public List<SolicitudSeccionAlumnoDTO> listarSolicitudesAlumno() {
        Alumno alumno = obtenerAlumnoActual();
        List<SolicitudSeccion> solicitudes = solicitudSeccionRepository.findByAlumnoId(alumno.getId());

        Set<Long> cursosSolicitados = solicitudes.stream()
                .map(s -> Optional.ofNullable(s.getCurso()).map(Curso::getId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Long> solicitantesPorCurso = cursosSolicitados.stream()
                .collect(Collectors.toMap(id -> id, id -> solicitudSeccionRepository.countByCursoId(id)));

        return solicitudes.stream()
                .sorted(Comparator.comparing(SolicitudSeccion::getFechaSolicitud, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(s -> {
                    Long cursoId = Optional.ofNullable(s.getCurso()).map(Curso::getId).orElse(null);
                    return SolicitudSeccionAlumnoDTO.builder()
                            .id(s.getId())
                            .cursoId(cursoId)
                            .curso(Optional.ofNullable(s.getCurso()).map(Curso::getNombre).orElse(null))
                            .codigoCurso(Optional.ofNullable(s.getCurso()).map(Curso::getCodigo).orElse(null))
                            .motivo(s.getMotivo())
                            .modalidad(s.getModalidad())
                            .turno(s.getTurno())
                            .ciclo(s.getCicloAcademico())
                            .estado(s.getEstado() != null ? s.getEstado().name() : null)
                            .mensajeAdmin(s.getMensajeAdmin())
                            .fechaSolicitud(s.getFechaSolicitud())
                            .fechaActualizacion(s.getFechaActualizacion())
                            .evidenciaNombreArchivo(s.getEvidenciaNombreArchivo())
                            .evidenciaContentType(s.getEvidenciaContentType())
                            .evidenciaBase64(s.getEvidenciaContenido() != null
                                    ? Base64.getEncoder().encodeToString(s.getEvidenciaContenido())
                                    : null)
                            .solicitantes(solicitantesPorCurso.getOrDefault(cursoId, 0L))
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CursoSolicitudAlumnoDTO> listarCursosSolicitables(Long carreraId, Integer ciclo) {
        Alumno alumno = obtenerAlumnoActual();
        Carrera carreraAlumno = alumno.getCarrera();
        if (carreraAlumno == null || carreraAlumno.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No tienes una carrera asignada");
        }

        if (carreraId != null && !Objects.equals(carreraId, carreraAlumno.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes solicitar cursos de otra carrera");
        }

        Set<Long> pendientes = solicitudSeccionRepository.findByAlumnoId(alumno.getId())
                .stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE)
                .map(s -> Optional.ofNullable(s.getCurso()).map(Curso::getId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return cursoService.listarPorCarreraId(carreraAlumno.getId())
                .stream()
                .filter(c -> c.getCarrera() != null && Objects.equals(c.getCarrera().getId(), carreraAlumno.getId()))
                .filter(c -> StringUtils.hasText(c.getNombre()))
                .filter(c -> StringUtils.hasText(c.getCodigo()))
                .filter(c -> c.getCiclo() != null)
                .filter(c -> ciclo == null || (c.getCiclo() != null && Objects.equals(c.getCiclo(), ciclo)))
                .map(c -> CursoSolicitudAlumnoDTO.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .ciclo(c.getCiclo() != null ? c.getCiclo().toString() : null)
                        .carrera(c.getCarrera() != null ? c.getCarrera().getNombre() : null)
                        .carreraId(c.getCarrera() != null ? c.getCarrera().getId() : null)
                        .modalidad(c.getModalidad() != null ? c.getModalidad().name() : null)
                        .pendiente(pendientes.contains(c.getId()))
                        .build())
                .sorted(Comparator.comparing(CursoSolicitudAlumnoDTO::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }


    private String resumenModalidad(List<Seccion> secciones) {
        Set<String> modalidades = secciones.stream()
                .map(sec -> sec.getModalidad() != null ? sec.getModalidad().name() : null)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (modalidades.isEmpty()) {
            return null;
        }
        if (modalidades.size() == 1) {
            return modalidades.iterator().next();
        }
        return "VARIAS";
    }

    private CursoDetalleAlumnoDTO mapearDetalleSeccion(Seccion seccion) {
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

    private Set<Long> obtenerCursosMatriculados(Alumno alumno) {
        String ciclo = obtenerCicloActual(alumno);
        if (ciclo == null) {
            return Set.of();
        }
        return matriculaRepository.findWithDetallesByAlumnoAndCiclo(alumno.getId(), ciclo)
                .map(Matricula::getDetalles)
                .orElse(List.of())
                .stream()
                .map(DetalleMatricula::getSeccion)
                .filter(Objects::nonNull)
                .map(Seccion::getCurso)
                .filter(Objects::nonNull)
                .map(Curso::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> obtenerCursosAprobados(Alumno alumno) {
        return matriculaRepository.findByAlumnoId(alumno.getId())
                .stream()
                .filter(m -> m.getEstado() != EstadoMatricula.ANULADA)
                .flatMap(m -> Optional.ofNullable(m.getDetalles()).orElse(List.of()).stream())
                .filter(det -> det.getSeccion() != null && det.getSeccion().getCurso() != null)
                .filter(det -> alumnoYaAproboCurso(det))
                .map(det -> det.getSeccion().getCurso().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean alumnoYaAproboCurso(DetalleMatricula detalle) {
        if (detalle.getNotaFinal() != null && detalle.getNotaFinal() >= 11) {
            return true;
        }
        return detalle.getEstadoDetalle() != null && detalle.getEstadoDetalle() == EstadoDetalleMatricula.CONVALIDADO;
    }

    private Integer parsearCicloNumerico(String cicloFiltro) {
        try {
            return StringUtils.hasText(cicloFiltro) ? Integer.parseInt(cicloFiltro.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Matricula crearMatricula(Alumno alumno, String ciclo) {
        Matricula matricula = new Matricula();
        matricula.setAlumno(alumno);
        matricula.setCicloAcademico(ciclo);
        matricula.setFechaMatricula(LocalDate.now().atStartOfDay());
        matricula.setEstado(EstadoMatricula.GENERADA);
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

    private void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        tabla.addCell(celda);
    }

    private Phrase valorCelda(String texto) {
        return new Phrase(Optional.ofNullable(texto).orElse("—"));
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
