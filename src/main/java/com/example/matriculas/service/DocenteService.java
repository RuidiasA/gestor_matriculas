package com.example.matriculas.service;

import com.example.matriculas.dto.DocenteActualizarContactoDTO;
import com.example.matriculas.dto.DocenteActualizarDatosDTO;
import com.example.matriculas.dto.DocenteBusquedaDTO;
import com.example.matriculas.dto.DocenteCursoDictableDTO;
import com.example.matriculas.dto.DocenteDetalleDTO;
import com.example.matriculas.dto.DocenteHistorialSeccionDTO;
import com.example.matriculas.dto.DocenteSeccionActualDTO;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.Docente;
import com.example.matriculas.model.Seccion;
import com.example.matriculas.model.SeccionHorario;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.model.enums.DiaSemana;
import com.example.matriculas.model.enums.EstadoDocente;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.DocenteRepository;
import com.example.matriculas.repository.SeccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioService usuarioService;
    private final CursoRepository cursoRepository;
    private final SeccionRepository seccionRepository;

    private static final Pattern CORREO_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{9}$");

    // ===============================================================
    // 1. Registrar Docente (crea usuario + crea docente)
    // ===============================================================
    @Transactional
    public Docente registrarDocente(Docente docente, String passwordPlano) {

        Usuario usuario = docente.getUsuario();
        usuario.setPassword(passwordPlano);

        usuarioService.crearUsuario(usuario);

        if (!StringUtils.hasText(docente.getCodigoDocente())) {
            docente.setCodigoDocente("D" + System.currentTimeMillis());
        }
        if (docente.getEstado() == null) {
            docente.setEstado(EstadoDocente.ACTIVO);
        }

        return docenteRepository.save(docente);
    }

    // ===============================================================
    // 2. Buscar por ID
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Docente> obtenerPorId(Long id) {
        return docenteRepository.findById(id);
    }

    // ===============================================================
    // 3. Buscar por email
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Docente> obtenerPorEmail(String email) {
        return docenteRepository.findByUsuario_CorreoInstitucional(email);
    }

    // ===============================================================
    // 4. Buscar por DNI
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Docente> obtenerPorDni(String dni) {
        return docenteRepository.findByDni(dni);
    }

    // ===============================================================
    // 5. Listar todos
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    // ===============================================================
    // ADMIN: Búsqueda flexible de docentes
    // ===============================================================
    @Transactional(readOnly = true)
    public List<DocenteBusquedaDTO> buscar(String filtro, Long cursoId, String estado) {
        String filtroLimpio = filtro != null ? filtro.trim().toLowerCase() : "";
        EstadoDocente estadoDocente = null;
        if (StringUtils.hasText(estado)) {
            try {
                estadoDocente = EstadoDocente.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                estadoDocente = null;
            }
        }
        PageRequest pageable = PageRequest.of(0, 50);
        Page<Docente> pagina = docenteRepository.buscar(filtroLimpio, estadoDocente, cursoId, pageable);
        return pagina.getContent().stream()
                .map(this::mapearBusqueda)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // ADMIN: Obtener detalle completo
    // ===============================================================
    @Transactional(readOnly = true)
    public DocenteDetalleDTO obtenerDetalle(Long id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));

        List<DocenteCursoDictableDTO> cursosDictables = docente.getCursosDictados()
                .stream()
                .map(this::mapearCursoDictable)
                .sorted(Comparator.comparing(DocenteCursoDictableDTO::getNombre))
                .collect(Collectors.toList());

        List<Seccion> secciones = seccionRepository.findWithHorariosByDocente(id);
        String periodoActual = secciones.stream()
                .map(Seccion::getPeriodoAcademico)
                .filter(StringUtils::hasText)
                .max(String::compareTo)
                .orElse(null);

        List<DocenteSeccionActualDTO> seccionesActuales = secciones.stream()
                .filter(s -> periodoActual == null || periodoActual.equalsIgnoreCase(s.getPeriodoAcademico()))
                .map(this::mapearSeccionActual)
                .collect(Collectors.toList());

        List<DocenteHistorialSeccionDTO> historial = secciones.stream()
                .filter(s -> periodoActual == null || !periodoActual.equalsIgnoreCase(s.getPeriodoAcademico()))
                .sorted(Comparator.comparing(Seccion::getPeriodoAcademico, Comparator.nullsLast(String::compareTo)).reversed())
                .map(this::mapearHistorial)
                .collect(Collectors.toList());

        return DocenteDetalleDTO.builder()
                .id(docente.getId())
                .codigo(docente.getCodigoDocente())
                .apellidos(docente.getApellidos())
                .nombres(docente.getNombres())
                .dni(docente.getDni())
                .estado(docente.getEstado() != null ? docente.getEstado().name() : null)
                .correoInstitucional(docente.getCorreoInstitucional())
                .correoPersonal(docente.getCorreoPersonal())
                .telefono(docente.getTelefonoPersonal())
                .direccion(docente.getDireccion())
                .especialidad(docente.getEspecialidad())
                .anioIngreso(docente.getAnioIngreso())
                .cursosDictables(cursosDictables)
                .seccionesActuales(seccionesActuales)
                .totalSeccionesActuales(seccionesActuales.size())
                .totalCreditosActuales(seccionesActuales.stream().map(DocenteSeccionActualDTO::getCreditos).filter(v -> v != null).mapToInt(Integer::intValue).sum())
                .totalHorasSemanalesActuales(seccionesActuales.stream().map(DocenteSeccionActualDTO::getHorario).mapToInt(this::contarHoras).sum())
                .totalCursosActuales((int) seccionesActuales.stream().map(DocenteSeccionActualDTO::getCurso).distinct().count())
                .historial(historial)
                .build();
    }

    // ===============================================================
    // ADMIN: Actualizar datos personales y estado
    // ===============================================================
    @Transactional
    public void actualizarDatos(Long id, DocenteActualizarDatosDTO dto) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));

        validarDatos(dto);

        docente.setApellidos(dto.getApellidos().trim());
        docente.setNombres(dto.getNombres().trim());
        docente.setDni(dto.getDni().trim());
        docente.setEspecialidad(trimOrNull(dto.getEspecialidad()));
        if (StringUtils.hasText(dto.getEstado())) {
            docente.setEstado(EstadoDocente.valueOf(dto.getEstado().toUpperCase()));
        }
        if (StringUtils.hasText(dto.getCorreoInstitucional())) {
            docente.setCorreoInstitucional(dto.getCorreoInstitucional().trim());
        }
        docenteRepository.save(docente);
    }

    // ===============================================================
    // ADMIN: Actualizar contacto
    // ===============================================================
    @Transactional
    public void actualizarContacto(Long id, DocenteActualizarContactoDTO dto) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));

        validarContacto(dto.getCorreoInstitucional(), dto.getCorreoPersonal(), dto.getTelefono());

        docente.setCorreoInstitucional(trimOrNull(dto.getCorreoInstitucional()));
        docente.setCorreoPersonal(trimOrNull(dto.getCorreoPersonal()));
        docente.setTelefonoPersonal(trimOrNull(dto.getTelefono()));
        docente.setDireccion(trimOrNull(dto.getDireccion()));
        docenteRepository.save(docente);
    }

    // ===============================================================
    // ADMIN: Gestionar cursos dictables
    // ===============================================================
    @Transactional
    public DocenteCursoDictableDTO agregarCursoDictado(Long docenteId, Long cursoId) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        // Aseguramos que exista la colección
        Set<Curso> cursos = docente.getCursosDictados();
        if (cursos == null) {
            cursos = new HashSet<>();
            docente.setCursosDictados(cursos);
        }

        // Validar si ya existe
        boolean yaDicta = cursos.stream().anyMatch(c -> c.getId().equals(cursoId));
        if (yaDicta) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El docente ya tiene este curso como dictado"
            );
        }

        cursos.add(curso);
        docenteRepository.save(docente);

        return mapearCursoDictado(curso);
    }


    @Transactional
    public void eliminarCursoDictado(Long docenteId, Long cursoId) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docente no encontrado"));

        Set<Curso> cursos = docente.getCursosDictados();
        if (cursos == null || cursos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El docente no tiene cursos asignados");
        }

        boolean removido = cursos.removeIf(c -> c.getId().equals(cursoId));

        if (!removido) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El curso no está asociado al docente");
        }

        docenteRepository.save(docente);
    }


    // ===============================================================
    // Métodos previos conservados
    // ===============================================================
    @Transactional
    public Docente actualizarDocente(Docente docente) {
        return docenteRepository.save(docente);
    }

    @Transactional
    public void eliminarDocente(Long id) {
        docenteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return docenteRepository.findByUsuario_CorreoInstitucional(email).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean docenteDisponible(Long docenteId, Integer diaSemana, String horaInicio, String horaFin) {
        return true;
    }

    @Transactional(readOnly = true)
    public List<Docente> buscar(String filtro, EstadoDocente estado, Long cursoId) {
        String filtroLimpio = filtro != null ? filtro.trim().toLowerCase() : "";

        Page<Docente> pagina = docenteRepository.buscar(
                filtroLimpio,
                estado,
                cursoId,
                PageRequest.of(0, 50) // o el tamaño que desees
        );

        return pagina.getContent();
    }

    // ===============================================================
    // Helpers
    // ===============================================================
    private DocenteBusquedaDTO mapearBusqueda(Docente docente) {
        return DocenteBusquedaDTO.builder()
                .id(docente.getId())
                .codigo(docente.getCodigoDocente())
                .nombreCompleto((docente.getApellidos() + " " + docente.getNombres()).trim())
                .dni(docente.getDni())
                .estado(docente.getEstado() != null ? docente.getEstado().name() : null)
                .build();
    }

    private DocenteCursoDictableDTO mapearCursoDictable(Curso curso) {
        return DocenteCursoDictableDTO.builder()
                .idCurso(curso.getId())
                .nombre(curso.getNombre())
                .codigo(curso.getCodigo())
                .creditos(curso.getCreditos())
                .ciclo(curso.getCiclo())
                .build();
    }

    private DocenteSeccionActualDTO mapearSeccionActual(Seccion seccion) {
        return DocenteSeccionActualDTO.builder()
                .curso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .codigoSeccion(seccion.getCodigo())
                .periodo(seccion.getPeriodoAcademico())
                .modalidad(seccion.getModalidad() != null ? seccion.getModalidad().name() : null)
                .creditos(seccion.getCurso() != null ? seccion.getCurso().getCreditos() : null)
                .turno(seccion.getTurno() != null ? seccion.getTurno().name() : null)
                .horario(formatearHorario(seccion.getHorarios()))
                .aula(seccion.getAula())
                .estudiantesInscritos(
                        seccion.getDetalles() != null ? seccion.getDetalles().size() : 0
                )
                .build();
    }

    private DocenteHistorialSeccionDTO mapearHistorial(Seccion seccion) {
        return DocenteHistorialSeccionDTO.builder()
                .periodo(seccion.getPeriodoAcademico())
                .curso(seccion.getCurso() != null ? seccion.getCurso().getNombre() : null)
                .seccion(seccion.getCodigo())
                .modalidad(seccion.getModalidad() != null ? seccion.getModalidad().name() : null)
                .creditos(seccion.getCurso() != null ? seccion.getCurso().getCreditos() : null)
                .turno(seccion.getTurno() != null ? seccion.getTurno().name() : null)
                .horario(formatearHorario(seccion.getHorarios()))
                .estudiantesFinalizados(
                        seccion.getDetalles() != null ? seccion.getDetalles().size() : 0
                )
                .notaPromedio(null)
                .porcentajeAprobacion(null)
                .observaciones(null)
                .build();
    }

    private String formatearHorario(List<SeccionHorario> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            return "-";
        }

        return horarios.stream()
                .sorted(
                        Comparator.comparing(
                                SeccionHorario::getDia,
                                Comparator.nullsLast(Comparator.comparing(DiaSemana::ordinal))
                        ).thenComparing(
                                SeccionHorario::getHoraInicio,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .map(h -> String.format("%s %s-%s",
                        h.getDia(),
                        h.getHoraInicio(),
                        h.getHoraFin()))
                .collect(Collectors.joining(" / "));
    }


    private int contarHoras(String horarioTexto) {
        if (!StringUtils.hasText(horarioTexto)) {
            return 0;
        }
        String[] partes = horarioTexto.split("/");
        return partes.length * 2;
    }

    private void validarDatos(DocenteActualizarDatosDTO dto) {
        if (!StringUtils.hasText(dto.getApellidos()) || !StringUtils.hasText(dto.getNombres())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campos obligatorios incompletos");
        }
        if (!DNI_PATTERN.matcher(dto.getDni() != null ? dto.getDni().trim() : "").matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI debe tener 8 dígitos");
        }
        if (StringUtils.hasText(dto.getCorreoInstitucional()) && !CORREO_PATTERN.matcher(dto.getCorreoInstitucional()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo institucional inválido");
        }
        if (StringUtils.hasText(dto.getEstado())) {
            try {
                EstadoDocente.valueOf(dto.getEstado().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
            }
        }
    }

    private void validarContacto(String correoInst, String correoPer, String telefono) {
        if (StringUtils.hasText(correoInst) && !CORREO_PATTERN.matcher(correoInst.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo institucional inválido");
        }
        if (StringUtils.hasText(correoPer) && !CORREO_PATTERN.matcher(correoPer.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo personal inválido");
        }
        if (StringUtils.hasText(telefono) && !TELEFONO_PATTERN.matcher(telefono.trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El teléfono debe tener 9 dígitos");
        }
    }

    private String trimOrNull(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
    private DocenteCursoDictableDTO mapearCursoDictado(Curso curso) {
        return DocenteCursoDictableDTO.builder()
                .idCurso(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .creditos(curso.getCreditos())
                .ciclo(curso.getCiclo())
                .build();
    }

}
