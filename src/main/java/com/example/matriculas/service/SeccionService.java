package com.example.matriculas.service;

import com.example.matriculas.dto.EstudianteSeccionDTO;
import com.example.matriculas.dto.SeccionActualizarDTO;
import com.example.matriculas.dto.SeccionCatalogoDTO;
import com.example.matriculas.dto.SeccionDetalleDTO;
import com.example.matriculas.dto.SeccionHistorialDTO;
import com.example.matriculas.dto.SeccionListadoDTO;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.Docente;
import com.example.matriculas.model.Seccion;
import com.example.matriculas.model.SeccionHorario;
import com.example.matriculas.model.enums.DiaSemana;
import com.example.matriculas.model.enums.EstadoMatricula;
import com.example.matriculas.model.enums.EstadoSeccion;
import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.DetalleMatriculaRepository;
import com.example.matriculas.repository.DocenteRepository;
import com.example.matriculas.repository.SeccionRepository;
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
            seccion.setDocente(docente);
        }

        if (StringUtils.hasText(dto.getAula())) {
            seccion.setAula(dto.getAula().trim());
        }

        if (dto.getModalidad() != null) {
            seccion.setModalidad(parsearModalidad(dto.getModalidad()));
        }

        if (dto.getCupos() != null) {
            long matriculadosActivos = detalleMatriculaRepository.contarMatriculadosActivosPorSeccion(seccionId);
            if (dto.getCupos() < matriculadosActivos) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El cupo no puede ser menor a los estudiantes matriculados (" + matriculadosActivos + ")");
            }
            seccion.setCapacidad(dto.getCupos());
        }

        if (dto.getHorarios() != null) {
            List<SeccionHorario> horariosActualizados = dto.getHorarios().stream()
                    .map(horarioDTO -> SeccionHorario.builder()
                            .dia(parsearDia(horarioDTO.getDia()))
                            .horaInicio(validarHora(horarioDTO.getHoraInicio()))
                            .horaFin(validarHora(horarioDTO.getHoraFin()))
                            .seccion(seccion)
                            .build())
                    .toList();

            if (seccion.getHorarios() == null) {
                seccion.setHorarios(new ArrayList<>());
            }
            seccion.getHorarios().clear();
            seccion.getHorarios().addAll(horariosActualizados);
        }

        seccionRepository.save(seccion);
    }

    private SeccionCatalogoDTO.CursoCatalogoDTO mapearCursoCatalogo(Curso curso) {
        return SeccionCatalogoDTO.CursoCatalogoDTO.builder()
                .idCurso(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .build();
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
                        .horaInicio(h.getHoraInicio())
                        .horaFin(h.getHoraFin())
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
            case INACTIVA -> "Inactiva";
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
                .sorted(Comparator.comparing((SeccionHorario h) -> orden.getOrDefault(h.getDia(), 8))
                        .thenComparing(SeccionHorario::getHoraInicio, Comparator.nullsLast(String::compareTo)))
                .map(h -> String.format("%s %s-%s", abreviatura.apply(h.getDia()),
                        h.getHoraInicio(), h.getHoraFin()))
                .collect(Collectors.joining(" | "));
    }

    private String formatearEstadoMatricula(EstadoMatricula estado) {
        if (estado == null) return "-";
        return switch (estado) {
            case GENERADA -> "Generada";
            case PAGADA -> "Matriculado";
            case ANULADA -> "Anulada";
        };
    }

    private String formatearNombre(String apellidos, String nombres) {
        String apellidoTxt = apellidos != null ? apellidos.trim() : "";
        String nombreTxt = nombres != null ? nombres.trim() : "";
        return (apellidoTxt + " " + nombreTxt).trim();
    }
}
