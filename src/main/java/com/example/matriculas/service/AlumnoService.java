package com.example.matriculas.service;

import com.example.matriculas.dto.ActualizarAlumnoContactoDTO;
import com.example.matriculas.dto.AlumnoBusquedaDTO;
import com.example.matriculas.dto.AlumnoFichaDTO;
import com.example.matriculas.dto.CursoMatriculadoDTO;
import com.example.matriculas.dto.HistorialMatriculaDTO;
import com.example.matriculas.dto.ResumenMatriculaDTO;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.DetalleMatricula;
import com.example.matriculas.model.Matricula;
import com.example.matriculas.model.PensionCuota;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.model.enums.EstadoAlumno;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.MatriculaRepository;
import com.example.matriculas.repository.PensionCuotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final UsuarioService usuarioService;
    private final MatriculaRepository matriculaRepository;
    private final PensionCuotaRepository pensionCuotaRepository;

    private static final Pattern CORREO_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{9}$");

    // ===============================================================
    // 1. Registrar alumno (crea usuario + crea alumno)
    // ===============================================================
    @Transactional
    public Alumno registrarAlumno(Alumno alumno, String passwordPlano) {

        // Crear usuario base
        Usuario usuario = alumno.getUsuario();
        usuario.setPassword(passwordPlano);
        usuarioService.crearUsuario(usuario);

        // Guardar alumno (sin código aún)
        Alumno guardado = alumnoRepository.save(alumno);

        // Generar código institucional
        String codigo = "S" + alumno.getAnioIngreso() + String.format("%04d", guardado.getId());
        guardado.setCodigoAlumno(codigo);

        // Guardarlo nuevamente con el código
        return alumnoRepository.save(guardado);
    }


    // ===============================================================
    // 2. Buscar alumno por ID
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Alumno> obtenerPorId(Long id) {
        return alumnoRepository.findById(id);
    }

    // ===============================================================
    // Buscar alumno por Codigo Institucional
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Alumno> obtenerPorCodigo(String codigoAlumno) {
        return alumnoRepository.findByCodigoAlumno(codigoAlumno);
    }

    // ===============================================================
    // 3. Buscar alumno por email institucional
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Alumno> obtenerPorCorreoInstitucional(String correoInstitucional) {
        return alumnoRepository.findByCorreoInstitucional(correoInstitucional);
    }

    // ===============================================================
    // 4. Buscar alumno por email de usuario (login)
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Alumno> obtenerPorEmailLogin(String email) {
        return alumnoRepository.findByUsuarioId(
                usuarioService.obtenerPorCorreoInstitucional(email)
                        .map(Usuario::getId)
                        .orElse(null)
        );
    }

    // ===============================================================
    // 5. Listar todos los alumnos
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Alumno> listarTodos() {
        return alumnoRepository.findAll();
    }

    // ===============================================================
    // 6. Actualizar datos del alumno
    // ===============================================================
    @Transactional
    public Alumno actualizarAlumno(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    // ===============================================================
    // 7. Eliminar alumno
    // ===============================================================
    @Transactional
    public void eliminarAlumno(Long id) {
        alumnoRepository.deleteById(id);
    }

    // ===============================================================
    // 8. Verificar existencia por correo institucional del alumno
    // ===============================================================
    @Transactional(readOnly = true)
    public boolean existeAlumnoPorCorreoInstitucional(String correo) {
        return alumnoRepository.findByCorreoInstitucional(correo).isPresent();
    }

    // ===============================================================
    // ADMIN: Buscar y listar alumnos
    // ===============================================================
    @Transactional(readOnly = true)
    public List<AlumnoBusquedaDTO> buscar(String filtro) {
        String filtroLimpio = filtro != null ? filtro.trim().toLowerCase() : "";
        PageRequest pageable = PageRequest.of(0, 50);
        Page<Alumno> pagina = alumnoRepository.buscarPorFiltro(filtroLimpio, pageable);
        return pagina.getContent()
                .stream()
                .map(this::mapearBusqueda)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // ADMIN: Obtener ficha de alumno
    // ===============================================================
    @Transactional(readOnly = true)
    public AlumnoFichaDTO obtenerFicha(Long id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));
        return mapearFicha(alumno, obtenerPeriodos(id));
    }

    // ===============================================================
    // ADMIN: Actualizar contacto de alumno
    // ===============================================================
    @Transactional
    public void actualizarContacto(Long id, ActualizarAlumnoContactoDTO dto) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));

        if (alumno.getEstado() == EstadoAlumno.INACTIVO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar un alumno inactivo");
        }

        validarContacto(dto);

        alumno.setCorreoPersonal(trimOrNull(dto.getCorreoPersonal()));
        alumno.setTelefonoPersonal(trimOrNull(dto.getTelefono()));
        alumno.setDireccion(trimOrNull(dto.getDireccion()));
        alumnoRepository.save(alumno);
    }

    // ===============================================================
    // ADMIN: Obtener periodos de matrícula del alumno
    // ===============================================================
    @Transactional(readOnly = true)
    public List<String> obtenerPeriodos(Long id) {
        asegurarExistenciaAlumno(id);
        return matriculaRepository.findDistinctCiclosByAlumnoId(id);
    }

    // ===============================================================
    // ADMIN: Cursos matriculados por ciclo
    // ===============================================================
    @Transactional(readOnly = true)
    public List<CursoMatriculadoDTO> obtenerCursos(Long id, String ciclo) {
        if (!StringUtils.hasText(ciclo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ciclo es obligatorio");
        }
        asegurarExistenciaAlumno(id);
        Matricula matricula = matriculaRepository.findWithDetallesByAlumnoAndCiclo(id, ciclo)
                .orElse(null);
        if (matricula == null) {
            return List.of();
        }
        List<DetalleMatricula> detalles = matricula.getDetalles();
        if (detalles == null || detalles.isEmpty()) {
            return List.of();
        }
        return detalles.stream()
                .map(this::mapearCurso)
                .collect(Collectors.toList());
    }

    // ===============================================================
    // ADMIN: Resumen de matrícula por ciclo
    // ===============================================================
    @Transactional(readOnly = true)
    public ResumenMatriculaDTO obtenerResumen(Long id, String ciclo) {
        if (!StringUtils.hasText(ciclo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ciclo es obligatorio");
        }
        asegurarExistenciaAlumno(id);
        Matricula matricula = matriculaRepository.findByAlumnoIdAndCicloAcademico(id, ciclo)
                .orElse(null);

        ResumenMontos montos = calcularMontos(id, ciclo, matricula);

        return ResumenMatriculaDTO.builder()
                .totalCursos(matricula != null && matricula.getDetalles() != null ? matricula.getDetalles().size() : 0)
                .totalCreditos(matricula != null && matricula.getTotalCreditos() != null ? matricula.getTotalCreditos() : 0)
                .totalHoras(matricula != null && matricula.getTotalHoras() != null ? matricula.getTotalHoras() : 0)
                .matricula(montos.matricula())
                .pension(montos.pension())
                .mora(montos.mora())
                .descuentos(montos.descuentos())
                .montoTotal(montos.montoTotal())
                .build();
    }

    // ===============================================================
    // ADMIN: Historial de matrícula
    // ===============================================================
    @Transactional(readOnly = true)
    public List<HistorialMatriculaDTO> obtenerHistorial(Long id) {
        asegurarExistenciaAlumno(id);
        List<Matricula> matriculas = matriculaRepository.findByAlumnoIdOrderByFechaMatriculaDesc(id);
        if (matriculas.isEmpty()) {
            return List.of();
        }

        String cicloActual = matriculas.get(0).getCicloAcademico();

        return matriculas.stream()
                .filter(m -> !m.getCicloAcademico().equalsIgnoreCase(cicloActual))
                .map(m -> {
                    ResumenMontos montos = calcularMontos(id, m.getCicloAcademico(), m);
                    List<CursoMatriculadoDTO> cursos = m.getDetalles() == null ? List.of() : m.getDetalles()
                            .stream()
                            .sorted(Comparator.comparing(d -> d.getSeccion() != null ? d.getSeccion().getCodigo() : ""))
                            .map(this::mapearCurso)
                            .collect(Collectors.toList());
                    return HistorialMatriculaDTO.builder()
                            .ciclo(m.getCicloAcademico())
                            .estado(m.getEstado() != null ? m.getEstado().name() : null)
                            .totalCursos(cursos.size())
                            .totalCreditos(m.getTotalCreditos() != null ? m.getTotalCreditos() : 0)
                            .totalHoras(m.getTotalHoras() != null ? m.getTotalHoras() : 0)
                            .matricula(montos.matricula())
                            .pension(montos.pension())
                            .mora(montos.mora())
                            .descuentos(montos.descuentos())
                            .montoTotal(montos.montoTotal())
                            .cursos(cursos)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ===============================================================
    // Helpers
    // ===============================================================
    private ResumenMontos calcularMontos(Long alumnoId, String ciclo, Matricula matricula) {

        List<PensionCuota> pagos = pensionCuotaRepository.findByAlumnoAndPeriodo(alumnoId, ciclo);

        BigDecimal montoMatricula = BigDecimal.ZERO;
        BigDecimal montoPensionPromedio = BigDecimal.ZERO;
        BigDecimal moraTotal = BigDecimal.ZERO;
        BigDecimal descuentos = BigDecimal.ZERO;
        BigDecimal sumaPensiones = BigDecimal.ZERO;

        int conteoPensiones = 0;

        for (PensionCuota p : pagos) {

            String tipo = p.getTipoConcepto() != null ? p.getTipoConcepto().name() : "";

            BigDecimal monto = (p.getImporteFinal() != null)
                    ? BigDecimal.valueOf(p.getImporteFinal())
                    : BigDecimal.ZERO;

            switch (tipo) {
                case "MATRICULA" -> montoMatricula = montoMatricula.add(monto);
                case "PENSION" -> {
                    sumaPensiones = sumaPensiones.add(monto);
                    conteoPensiones++;
                }
            }

            if (p.getMora() != null) {
                moraTotal = moraTotal.add(BigDecimal.valueOf(p.getMora()));
            }
            if (p.getDescuento() != null) {
                descuentos = descuentos.add(BigDecimal.valueOf(p.getDescuento()));
            }
        }

        if (conteoPensiones > 0) {
            montoPensionPromedio = sumaPensiones.divide(
                    BigDecimal.valueOf(conteoPensiones),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal totalCalculado =
                (matricula != null && matricula.getMontoTotal() != null)
                        ? BigDecimal.valueOf(matricula.getMontoTotal())
                        : montoMatricula.add(sumaPensiones).add(moraTotal).subtract(descuentos);

        return new ResumenMontos(
                montoMatricula.doubleValue(),
                montoPensionPromedio.doubleValue(),
                moraTotal.doubleValue(),
                descuentos.doubleValue(),
                totalCalculado.doubleValue()
        );
    }


    private CursoMatriculadoDTO mapearCurso(DetalleMatricula detalle) {
        return CursoMatriculadoDTO.builder()
                .seccionId(detalle.getSeccion() != null ? detalle.getSeccion().getId() : null)
                .codigoSeccion(detalle.getSeccion() != null ? detalle.getSeccion().getCodigo() : null)
                .nombreCurso(detalle.getSeccion() != null && detalle.getSeccion().getCurso() != null ?
                        detalle.getSeccion().getCurso().getNombre() : null)
                .docente(
                        detalle.getSeccion() != null && detalle.getSeccion().getDocente() != null
                                ? (detalle.getSeccion().getDocente().getNombres() + " "
                                + detalle.getSeccion().getDocente().getApellidos()).trim()
                                : null
                )
                .creditos(detalle.getCreditos())
                .horasSemanales(detalle.getHorasSemanales())
                .modalidad(detalle.getModalidad() != null ? detalle.getModalidad().name() : null)
                .aula(detalle.getAula())
                .build();
    }


    private record ResumenMontos(double matricula, double pension, double mora, double descuentos, double montoTotal) {}
    private AlumnoBusquedaDTO mapearBusqueda(Alumno alumno) {
        return AlumnoBusquedaDTO.builder()
                .id(alumno.getId())
                .codigo(alumno.getCodigoAlumno())
                .nombreCompleto((alumno.getNombres() + " " + alumno.getApellidos()).trim())
                .dni(alumno.getDni())
                .correoInstitucional(alumno.getCorreoInstitucional())
                .correoPersonal(alumno.getCorreoPersonal())
                .telefono(alumno.getTelefonoPersonal())
                .anioIngreso(alumno.getAnioIngreso() != null ? alumno.getAnioIngreso().toString() : null)
                .cicloActual(alumno.getCicloActual() != null ? alumno.getCicloActual().toString() : null)
                .turno(alumno.getTurno() != null ? alumno.getTurno().name() : null)
                .ordenMerito(alumno.getOrdenMerito())
                .carrera(alumno.getCarrera() != null ? alumno.getCarrera().getNombre() : null)
                .direccion(alumno.getDireccion())
                .estado(alumno.getEstado() != null ?
                        (alumno.getEstado() == EstadoAlumno.ACTIVO ? "Activo" : "Inactivo") : null)
                .periodos(obtenerPeriodos(alumno.getId()))
                .build();
    }

    private AlumnoFichaDTO mapearFicha(Alumno alumno, List<String> periodos) {
        return AlumnoFichaDTO.builder()
                .id(alumno.getId())
                .codigoAlumno(alumno.getCodigoAlumno())
                .nombreCompleto((alumno.getNombres() + " " + alumno.getApellidos()).trim())
                .carrera(alumno.getCarrera() != null ? alumno.getCarrera().getNombre() : null)
                .cicloActual(alumno.getCicloActual() != null ? alumno.getCicloActual().toString() : null)
                .anioIngreso(alumno.getAnioIngreso())
                .correoInstitucional(alumno.getCorreoInstitucional())
                .correoPersonal(alumno.getCorreoPersonal())
                .telefono(alumno.getTelefonoPersonal())
                .direccion(alumno.getDireccion())
                .estado(alumno.getEstado() != null ?
                        (alumno.getEstado() == EstadoAlumno.ACTIVO ? "Activo" : "Inactivo") : null)
                .periodos(periodos)
                .build();
    }

    private void validarContacto(ActualizarAlumnoContactoDTO dto) {
        String correo = trimOrNull(dto.getCorreoPersonal());
        String telefono = trimOrNull(dto.getTelefono());
        if (StringUtils.hasText(correo) && !CORREO_PATTERN.matcher(correo).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo personal inválido");
        }
        if (StringUtils.hasText(telefono) && !TELEFONO_PATTERN.matcher(telefono).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El teléfono debe tener 9 dígitos");
        }
    }

    private void asegurarExistenciaAlumno(Long id) {
        if (id == null || !alumnoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado");
        }
    }

    private String trimOrNull(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
