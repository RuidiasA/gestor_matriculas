package com.example.matriculas.service;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.*;
import com.example.matriculas.model.enums.DiaSemana;
import com.example.matriculas.model.enums.EstadoMatricula;
import com.example.matriculas.model.enums.EstadoSeccion;
import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.DetalleMatriculaRepository;
import com.example.matriculas.repository.DocenteRepository;
import com.example.matriculas.repository.SeccionRepository;
import com.example.matriculas.repository.SeccionCambioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;
    private final SeccionCambioRepository seccionCambioLogRepository;

    @Transactional(readOnly = true)
    public SeccionCatalogoDTO obtenerCatalogos() {
        List<SeccionCatalogoDTO.CursoCatalogoDTO> cursos = cursoRepository.findAll(Sort.by("nombre"))
                .stream()
                .map(this::mapearCursoCatalogo)
                .toList();

        List<String> periodos = seccionRepository.findDistinctPeriodos();

        List<SeccionCatalogoDTO.DocenteCatalogoDTO> docentes = docenteRepository.findAll(Sort.by("apellidos"))
                .stream()
                .map(this::mapearDocenteCatalogo)
                .toList();

        List<String> modalidades = List.of("Presencial", "Virtual", "Híbrido");

        return SeccionCatalogoDTO.builder()
                .cursos(cursos)
                .periodos(periodos)
                .docentes(docentes)
                .modalidades(modalidades)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SeccionListadoDTO> buscar(Long cursoId, String periodo,
                                          Long docenteId, String modalidad,
                                          String codigo) {

        Modalidad modalidadEnum = parsearModalidad(modalidad);

        Specification<Seccion> spec = Specification.where((root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();
        });

        if (cursoId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("curso").get("id"), cursoId)
            );
        }
        if (StringUtils.hasText(periodo)) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("periodoAcademico")),
                            periodo.trim().toLowerCase())
            );
        }
        if (docenteId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("docente").get("id"), docenteId)
            );
        }
        if (modalidadEnum != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("modalidad"), modalidadEnum)
            );
        }
        if (StringUtils.hasText(codigo)) {
            String filtro = "%" + codigo.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("codigo")), filtro)
            );
        }

        // 🔥 Sin ORDER BY en SQL
        List<Seccion> secciones = seccionRepository.findAll(spec);

        Map<Long, Integer> matriculadosPorSeccion = obtenerMatriculados(secciones);

        List<SeccionListadoDTO> lista = new ArrayList<>(
                secciones.stream()
                        .map(seccion ->
                                mapearListado(
                                        seccion,
                                        matriculadosPorSeccion.getOrDefault(seccion.getId(), 0)
                                )
                        )
                        .toList()
        );

        // 🔥 Orden final en memoria
        lista.sort(Comparator
                .comparing(SeccionListadoDTO::getCurso,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(SeccionListadoDTO::getCodigoSeccion,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
        );

        return lista;
    }


    @Transactional(readOnly = true)
    public SeccionDetalleDTO obtenerDetalle(Long id) {
        Seccion seccion = seccionRepository.findDetalleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        int matriculados = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(id).intValue();
        return mapearDetalle(seccion, matriculados);
    }

    @Transactional(readOnly = true)
    public List<EstudianteSeccionDTO> listarEstudiantes(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
        }

        return detalleMatriculaRepository.findBySeccionIdWithAlumno(seccionId)
                .stream()
                .map(detalle -> EstudianteSeccionDTO.builder()
                        .codigo(detalle.getMatricula().getAlumno().getCodigoAlumno())
                        .nombre(formatearNombre(detalle.getMatricula().getAlumno().getApellidos(),
                                detalle.getMatricula().getAlumno().getNombres()))
                        .estado(formatearEstadoMatricula(detalle.getMatricula().getEstado()))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeccionHistorialDTO> obtenerHistorial(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
        }

        return detalleMatriculaRepository.findHistorialBySeccion(seccionId)
                .stream()
                .map(detalle -> SeccionHistorialDTO.builder()
                        .matriculaId(detalle.getMatricula().getId())
                        .alumnoCodigo(detalle.getMatricula().getAlumno().getCodigoAlumno())
                        .alumnoNombre(formatearNombre(detalle.getMatricula().getAlumno().getApellidos(),
                                detalle.getMatricula().getAlumno().getNombres()))
                        .estadoMatricula(formatearEstadoMatricula(detalle.getMatricula().getEstado()))
                        .periodo(detalle.getMatricula().getCicloAcademico())
                        .fechaMatricula(detalle.getMatricula().getFechaMatricula())
                        .observacion(detalle.getMatricula().getEstado() == EstadoMatricula.ANULADA
                                ? "Matrícula anulada"
                                : null)
                        .build())
                .toList();
    }

    @Transactional
    public void anular(Long seccionId) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        if (EstadoSeccion.ANULADA.equals(seccion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sección ya está anulada");
        }

        long matriculados = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(seccionId);
        if (matriculados > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede anular una sección con matrículas activas");
        }

        seccion.setEstado(EstadoSeccion.ANULADA);
        seccionRepository.save(seccion);
    }

    @Transactional
    public void actualizar(Long seccionId, SeccionActualizarDTO dto) {
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        if (EstadoSeccion.ANULADA.equals(seccion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar una sección anulada");
        }

        if (dto.getDocenteId() != null) {
            Docente docente = docenteRepository.findById(dto.getDocenteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Docente no encontrado"));
            registrarCambio(seccion, "docente", seccion.getDocente() != null ? seccion.getDocente().getId().toString() : "-",
                    docente.getId().toString(), "Actualización de docente");
            seccion.setDocente(docente);
        }

        if (StringUtils.hasText(dto.getAula())) {
            registrarCambio(seccion, "aula", seccion.getAula(), dto.getAula().trim(), "Actualización de aula");
            seccion.setAula(dto.getAula().trim());
        }

        if (dto.getModalidad() != null) {
            Modalidad nuevaModalidad = parsearModalidad(dto.getModalidad());
            registrarCambio(seccion, "modalidad", formatearModalidad(seccion.getModalidad()), formatearModalidad(nuevaModalidad),
                    "Actualización de modalidad");
            seccion.setModalidad(nuevaModalidad);
        }

        if (dto.getCupos() != null) {
            long matriculadosActivos = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(seccionId);
            if (dto.getCupos() < matriculadosActivos) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El cupo no puede ser menor a los estudiantes matriculados (" + matriculadosActivos + ")");
            }
            registrarCambio(seccion, "capacidad", String.valueOf(seccion.getCapacidad()), String.valueOf(dto.getCupos()),
                    "Actualización de cupos");
            seccion.setCapacidad(dto.getCupos());
        }

        if (dto.getHorarios() != null) {
            List<SeccionHorario> horariosActualizados = construirHorarios(dto.getHorarios(), seccion);
            reemplazarHorarios(seccion, horariosActualizados);
            registrarCambio(seccion, "horarios", formatearHorario(seccion.getHorarios()), formatearHorario(horariosActualizados),
                    "Actualización de horarios");
        }

        seccionRepository.save(seccion);
    }

    @Transactional
    public void actualizarHorarios(Long seccionId, SeccionHorariosActualizarDTO dto) {
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        if (EstadoSeccion.ANULADA.equals(seccion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar una sección anulada");
        }

        List<SeccionHorario> horariosActualizados = construirHorarios(dto.getHorarios(), seccion);
        reemplazarHorarios(seccion, horariosActualizados);

        registrarCambio(seccion, "horarios", formatearHorario(seccion.getHorarios()), formatearHorario(horariosActualizados),
                "Gestión de horarios");

        seccionRepository.save(seccion);
    }

    @Transactional(readOnly = true)
    public List<SeccionCambioDTO> obtenerCambios(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
        }
        return seccionCambioLogRepository.findBySeccionIdOrderByFechaDesc(seccionId)
                .stream()
                .map(log -> SeccionCambioDTO.builder()
                        .fecha(log.getFecha())
                        .usuario(log.getUsuario())
                        .campoModificado(log.getCampoModificado())
                        .valorAnterior(log.getValorAnterior())
                        .valorNuevo(log.getValorNuevo())
                        .observacion(log.getObservacion())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public SeccionEstadisticaDTO obtenerEstadisticas(Long seccionId) {
        Seccion seccion = seccionRepository.findDetalleById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));

        int matriculados = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(seccionId).intValue();
        int cuposLibres = Math.max(0, seccion.getCapacidad() - matriculados);
        int horariosProgramados = seccion.getHorarios() != null ? seccion.getHorarios().size() : 0;

        SeccionPeriodoResumenDTO periodoActual = SeccionPeriodoResumenDTO.builder()
                .periodo(seccion.getPeriodoAcademico())
                .matriculados(matriculados)
                .retirados(0)
                .aprobados(0)
                .desaprobados(0)
                .porcentajeAprobacion(0.0)
                .promedioNotas(0.0)
                .promedioAsistencia(0.0)
                .build();

        return SeccionEstadisticaDTO.builder()
                .matriculadosActuales(matriculados)
                .cuposLibres(cuposLibres)
                .aprobadosUltimoPeriodo(0)
                .porcentajeAprobacion(0.0)
                .retiros(0)
                .horariosProgramados(horariosProgramados)
                .creditos(seccion.getCurso() != null ? seccion.getCurso().getCreditos() : 0)
                .ciclo(seccion.getCurso() != null ? String.valueOf(seccion.getCurso().getCiclo()) : "-")
                .turno(seccion.getTurno() != null ? seccion.getTurno().name() : "-")
                .horasSemanales(seccion.getCurso() != null ? seccion.getCurso().getHorasSemanales() : 0)
                .cuposTotales(seccion.getCapacidad())
                .cuposDisponibles(cuposLibres)
                .cantidadHorarios(horariosProgramados)
                .estadoAcademico(formatearEstadoSeccion(seccion.getEstado()))
                .resumenPeriodos(List.of(periodoActual))
                .build();
    }

    @Transactional(readOnly = true)
    public SeccionHistorialCompletoDTO obtenerHistorialCompleto(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada");
        }
        List<MovimientoMatriculaDTO> movimientos = detalleMatriculaRepository.findHistorialBySeccion(seccionId)
                .stream()
                .map(detalle -> MovimientoMatriculaDTO.builder()
                        .codigo(detalle.getMatricula().getAlumno().getCodigoAlumno())
                        .nombre(formatearNombre(detalle.getMatricula().getAlumno().getApellidos(),
                                detalle.getMatricula().getAlumno().getNombres()))
                        .accion(formatearEstadoMatricula(detalle.getMatricula().getEstado()))
                        .fecha(detalle.getMatricula().getFechaMatricula())
                        .usuario("Sistema")
                        .notaFinal("-")
                        .observacion(detalle.getMatricula().getEstado() == EstadoMatricula.ANULADA ? "Matrícula anulada" : "")
                        .build())
                .toList();

        return SeccionHistorialCompletoDTO.builder()
                .cambios(obtenerCambios(seccionId))
                .movimientos(movimientos)
                .estadisticas(obtenerEstadisticas(seccionId))
                .build();
    }

    @Transactional
    public void registrarLogManual(Long seccionId, SeccionCambioDTO dto) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        SeccionCambio log = SeccionCambio.builder()
                .seccion(seccion)
                .fecha(Optional.ofNullable(dto.getFecha()).orElseGet(java.time.LocalDateTime::now))
                .usuario(Optional.ofNullable(dto.getUsuario()).orElse("Sistema"))
                .campoModificado(dto.getCampoModificado())
                .valorAnterior(Optional.ofNullable(dto.getValorAnterior()).orElse("-"))
                .valorNuevo(Optional.ofNullable(dto.getValorNuevo()).orElse("-"))
                .observacion(dto.getObservacion())
                .build();
        seccionCambioLogRepository.save(log);
    }

    @Transactional
    public void actualizarDocente(Long seccionId, Long docenteId) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Docente no encontrado"));
        registrarCambio(seccion, "docente", seccion.getDocente() != null ? seccion.getDocente().getId().toString() : "-",
                docente.getId().toString(), "Actualización directa de docente");
        seccion.setDocente(docente);
        seccionRepository.save(seccion);
    }

    @Transactional
    public void actualizarAula(Long seccionId, String aula) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        registrarCambio(seccion, "aula", seccion.getAula(), aula, "Actualización directa de aula");
        seccion.setAula(aula);
        seccionRepository.save(seccion);
    }

    @Transactional
    public void actualizarEstado(Long seccionId, EstadoSeccion estado) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        registrarCambio(seccion, "estado", formatearEstadoSeccion(seccion.getEstado()), formatearEstadoSeccion(estado),
                "Actualización de estado");
        seccion.setEstado(estado);
        seccionRepository.save(seccion);
    }

    @Transactional
    public void actualizarCupos(Long seccionId, int cupos) {
        Seccion seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sección no encontrada"));
        registrarCambio(seccion, "capacidad", String.valueOf(seccion.getCapacidad()), String.valueOf(cupos),
                "Actualización directa de cupos");
        seccion.setCapacidad(cupos);
        seccionRepository.save(seccion);
    }

    private void registrarCambio(Seccion seccion, String campo, String anterior, String nuevo, String observacion) {
        SeccionCambio log = SeccionCambio.builder()
                .seccion(seccion)
                .fecha(java.time.LocalDateTime.now())
                .usuario("Sistema")
                .campoModificado(campo)
                .valorAnterior(Optional.ofNullable(anterior).orElse("-"))
                .valorNuevo(Optional.ofNullable(nuevo).orElse("-"))
                .observacion(observacion)
                .build();
        seccionCambioLogRepository.save(log);
    }

    private SeccionCatalogoDTO.CursoCatalogoDTO mapearCursoCatalogo(Curso curso) {
        return SeccionCatalogoDTO.CursoCatalogoDTO.builder()
                .idCurso(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .build();
    }

    private List<SeccionHorario> construirHorarios(List<SeccionActualizarDTO.HorarioEdicionDTO> horariosDto, Seccion seccion) {
        if (horariosDto == null || horariosDto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes registrar al menos un horario");
        }

        List<SeccionHorario> horariosActualizados = horariosDto.stream()
                .map(horarioDTO -> {
                    DiaSemana dia = parsearDia(horarioDTO.getDia());
                    String horaInicio = validarHora(horarioDTO.getHoraInicio());
                    String horaFin = validarHora(horarioDTO.getHoraFin());

                    validarRangoHorario(horaInicio, horaFin, dia);

                    return SeccionHorario.builder()
                            .dia(dia)
                            .horaInicio(LocalTime.parse(horaInicio))
                            .horaFin(LocalTime.parse(horaFin))
                            .seccion(seccion)
                            .build();
                })
                .toList();

        validarCruceHorarios(horariosActualizados);
        return horariosActualizados;

    }

    private void reemplazarHorarios(Seccion seccion, List<SeccionHorario> horariosActualizados) {
        if (seccion.getHorarios() == null) {
            seccion.setHorarios(new ArrayList<>());
        }
        seccion.getHorarios().clear();
        seccion.getHorarios().addAll(horariosActualizados);
    }

    private SeccionCatalogoDTO.DocenteCatalogoDTO mapearDocenteCatalogo(Docente docente) {
        String nombreCompleto = formatearNombre(docente.getApellidos(), docente.getNombres());
        return SeccionCatalogoDTO.DocenteCatalogoDTO.builder()
                .idDocente(docente.getId())
                .apellidos(docente.getApellidos())
                .nombres(docente.getNombres())
                .nombreCompleto(nombreCompleto)
                .build();
    }

    private SeccionListadoDTO mapearListado(Seccion seccion, int matriculados) {
        return SeccionListadoDTO.builder()
                .idSeccion(seccion.getId())
                .curso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .codigoSeccion(seccion.getCodigo())
                .docente(seccion.getDocente() != null ? formatearNombre(seccion.getDocente().getApellidos(), seccion.getDocente().getNombres()) : null)
                .periodo(seccion.getPeriodoAcademico())
                .modalidad(formatearModalidad(seccion.getModalidad()))
                .horario(formatearHorario(seccion.getHorarios()))
                .aula(seccion.getAula())
                .cupos(seccion.getCapacidad())
                .matriculados(matriculados)
                .estado(formatearEstadoSeccion(seccion.getEstado()))
                .build();
    }

    private SeccionDetalleDTO mapearDetalle(Seccion seccion, int matriculados) {
        return SeccionDetalleDTO.builder()
                .idSeccion(seccion.getId())
                .curso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .codigoSeccion(seccion.getCodigo())
                .docenteId(seccion.getDocente() != null ? seccion.getDocente().getId() : null)
                .docente(seccion.getDocente() != null ? formatearNombre(seccion.getDocente().getApellidos(), seccion.getDocente().getNombres()) : null)
                .periodo(seccion.getPeriodoAcademico())
                .modalidad(formatearModalidad(seccion.getModalidad()))
                .horario(formatearHorario(seccion.getHorarios()))
                .aula(seccion.getAula())
                .cupos(seccion.getCapacidad())
                .matriculados(matriculados)
                .estado(formatearEstadoSeccion(seccion.getEstado()))
                .horarios(mapearHorarios(seccion.getHorarios()))
                .build();
    }

    private List<SeccionDetalleDTO.HorarioDTO> mapearHorarios(List<SeccionHorario> horarios) {
        if (horarios == null) return Collections.emptyList();
        Map<DiaSemana, Integer> orden = Map.of(
                DiaSemana.LUNES, 1,
                DiaSemana.MARTES, 2,
                DiaSemana.MIERCOLES, 3,
                DiaSemana.JUEVES, 4,
                DiaSemana.VIERNES, 5,
                DiaSemana.SABADO, 6,
                DiaSemana.DOMINGO, 7
        );
        return horarios.stream()
                .sorted(Comparator.comparing(h -> orden.getOrDefault(h.getDia(), 9)))
                .map(h -> SeccionDetalleDTO.HorarioDTO.builder()
                        .dia(h.getDia() != null ? h.getDia().name() : null)
                        .horaInicio(h.getHoraInicio() != null ? h.getHoraInicio().toString() : null)
                        .horaFin(h.getHoraFin() != null ? h.getHoraFin().toString() : null)
                        .build())
                .toList();
    }

    private Map<Long, Integer> obtenerMatriculados(List<Seccion> secciones) {
        if (secciones == null || secciones.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> ids = secciones.stream().map(Seccion::getId).toList();
        List<Object[]> resultados = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(ids);
        Map<Long, Integer> conteo = new HashMap<>();
        for (Object[] fila : resultados) {
            conteo.put((Long) fila[0], ((Long) fila[1]).intValue());
        }
        return conteo;
    }

    private Modalidad parsearModalidad(String modalidad) {
        if (!StringUtils.hasText(modalidad)) {
            return null;
        }
        String normalizado = Normalizer.normalize(modalidad, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase(Locale.ROOT);
        return switch (normalizado) {
            case "PRESENCIAL" -> Modalidad.PRESENCIAL;
            case "VIRTUAL" -> Modalidad.VIRTUAL;
            case "HIBRIDO", "HÍBRIDO", "SEMIPRESENCIAL" -> Modalidad.SEMIPRESENCIAL;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modalidad no soportada: " + modalidad);
        };
    }

    private DiaSemana parsearDia(String dia) {
        if (!StringUtils.hasText(dia)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Día de horario inválido");
        }
        try {
            return DiaSemana.valueOf(dia.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Día de horario no soportado: " + dia);
        }
    }

    private String validarHora(String valor) {
        if (!StringUtils.hasText(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hora de horario inválida");
        }
        try {
            return LocalTime.parse(valor.trim()).toString();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de hora inválido: " + valor);
        }
    }

    private void validarRangoHorario(String horaInicio, String horaFin, DiaSemana dia) {
        LocalTime inicio = LocalTime.parse(horaInicio);
        LocalTime fin = LocalTime.parse(horaFin);
        if (!fin.isAfter(inicio)) {
            String diaTexto = dia != null ? dia.name().toLowerCase(Locale.ROOT) : "el día seleccionado";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora fin debe ser mayor a la hora inicio para " + diaTexto);
        }
    }

    private void validarCruceHorarios(List<SeccionHorario> horarios) {

        Map<DiaSemana, List<SeccionHorario>> agrupados = horarios.stream()
                .collect(Collectors.groupingBy(SeccionHorario::getDia));

        for (Map.Entry<DiaSemana, List<SeccionHorario>> entry : agrupados.entrySet()) {

            List<SeccionHorario> delDia = entry.getValue().stream()
                    .sorted(Comparator.comparing(SeccionHorario::getHoraInicio)) // ✔ ordena por LocalTime
                    .toList();

            LocalTime ultimoFin = null;

            for (SeccionHorario horario : delDia) {

                LocalTime inicio = horario.getHoraInicio(); // ✔ ya es LocalTime
                LocalTime fin = horario.getHoraFin();       // ✔ ya es LocalTime

                if (ultimoFin != null && !inicio.isAfter(ultimoFin)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Los horarios se sobreponen para el día "
                                    + entry.getKey().name().toLowerCase(Locale.ROOT)
                    );
                }

                ultimoFin = fin;
            }
        }
    }


    private String formatearModalidad(Modalidad modalidad) {
        if (modalidad == null) return "-";
        return switch (modalidad) {
            case PRESENCIAL -> "Presencial";
            case VIRTUAL -> "Virtual";
            case SEMIPRESENCIAL -> "Híbrido";
        };
    }

    private String formatearEstadoSeccion(EstadoSeccion estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case ACTIVA -> "Activa";
            case ANULADA -> "Anulada";
        };
    }

    private String formatearHorario(List<SeccionHorario> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            return "-";
        }

        Map<DiaSemana, Integer> orden = Map.of(
                DiaSemana.LUNES, 1,
                DiaSemana.MARTES, 2,
                DiaSemana.MIERCOLES, 3,
                DiaSemana.JUEVES, 4,
                DiaSemana.VIERNES, 5,
                DiaSemana.SABADO, 6,
                DiaSemana.DOMINGO, 7
        );

        Function<DiaSemana, String> abreviatura = dia -> switch (dia) {
            case LUNES -> "Lun";
            case MARTES -> "Mar";
            case MIERCOLES -> "Mié";
            case JUEVES -> "Jue";
            case VIERNES -> "Vie";
            case SABADO -> "Sáb";
            case DOMINGO -> "Dom";
        };

        return horarios.stream()
                .filter(Objects::nonNull)
                .sorted(
                        Comparator.comparing((SeccionHorario h) -> orden.getOrDefault(h.getDia(), 8))
                                .thenComparing(SeccionHorario::getHoraInicio,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                )
                .map(h -> String.format("%s %s-%s",
                        abreviatura.apply(h.getDia()),
                        h.getHoraInicio(),
                        h.getHoraFin()))
                .collect(Collectors.joining(" | "));

    }

    private String formatearEstadoMatricula(EstadoMatricula estado) {
        if (estado == null) return "-";

        return switch (estado) {
            case PREREGISTRO -> "Pre-registro";
            case GENERADA -> "Generada";
            case OBSERVADA -> "Observada";
            case CONFIRMADA -> "Confirmada";
            case PAGADA -> "Matriculado";
            case RETIRADA -> "Retirada";
            case ANULADA -> "Anulada";
        };
    }


    private String formatearNombre(String apellidos, String nombres) {
        String apellidoTxt = apellidos != null ? apellidos.trim() : "";
        String nombreTxt = nombres != null ? nombres.trim() : "";
        return (apellidoTxt + " " + nombreTxt).trim();
    }
}
