package com.example.matriculas.service;

import com.example.matriculas.dto.ActualizarAlumnoDTO;
import com.example.matriculas.dto.AlumnoDTO;
import com.example.matriculas.dto.RegistrarAlumnoDTO;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Carrera;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.CarreraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final CarreraRepository carreraRepository;

    public List<AlumnoDTO> listar() {
        return alumnoRepository.findAllByOrderByApellidosAscNombresAsc()
                .stream()
                .map(AlumnoDTO::fromEntity)
                .toList();
    }

    public List<AlumnoDTO> buscar(String filtro) {
        String filtroNormalizado = normalizarFiltro(filtro);
        if (!StringUtils.hasText(filtroNormalizado)) {
            return listar();
        }
        return alumnoRepository.buscarPorFiltro(filtroNormalizado)
                .stream()
                .map(AlumnoDTO::fromEntity)
                .toList();
    }

    public AlumnoDTO obtener(Long id) {
        return AlumnoDTO.fromEntity(obtenerAlumno(id));
    }

    public AlumnoDTO registrar(RegistrarAlumnoDTO dto) {
        validarUnicidad(dto.getCodigoAlumno(), dto.getDni(), dto.getCorreoInstitucional());

        Alumno alumno = Alumno.builder()
                .codigoAlumno(dto.getCodigoAlumno().trim().toUpperCase())
                .nombres(normalizarNombre(dto.getNombres()))
                .apellidos(normalizarNombre(dto.getApellidos()))
                .dni(dto.getDni().trim())
                .correoInstitucional(dto.getCorreoInstitucional().trim().toLowerCase())
                .correoPersonal(normalizarCorreoPersonal(dto.getCorreoPersonal()))
                .telefonoPersonal(normalizarTelefono(dto.getTelefonoPersonal()))
                .direccion(normalizarDireccion(dto.getDireccion()))
                .anioIngreso(dto.getAnioIngreso())
                .cicloActual(dto.getCicloActual())
                .estado("ACTIVO")
                .carrera(buscarCarrera(dto.getCarreraId()))
                .build();

        return AlumnoDTO.fromEntity(alumnoRepository.save(alumno));
    }

    public AlumnoDTO actualizar(Long id, ActualizarAlumnoDTO dto) {
        Alumno alumno = obtenerAlumno(id);

        if (StringUtils.hasText(dto.getNombres())) {
            alumno.setNombres(normalizarNombre(dto.getNombres()));
        }

        if (StringUtils.hasText(dto.getApellidos())) {
            alumno.setApellidos(normalizarNombre(dto.getApellidos()));
        }

        if (dto.getCorreoPersonal() != null) {
            alumno.setCorreoPersonal(normalizarCorreoPersonal(dto.getCorreoPersonal()));
        }

        if (dto.getTelefonoPersonal() != null) {
            alumno.setTelefonoPersonal(normalizarTelefono(dto.getTelefonoPersonal()));
        }

        alumnoRepository.save(alumno);
        return AlumnoDTO.fromEntity(alumno);
    }

    public void eliminar(Long id) {
        Alumno alumno = obtenerAlumno(id);
        if (alumno.getMatriculas() != null && !alumno.getMatriculas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar: el alumno tiene matrículas registradas");
        }
        alumnoRepository.delete(alumno);
    }

    private Alumno obtenerAlumno(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));
    }

    private String normalizarFiltro(String filtro) {
        return filtro == null ? "" : filtro.trim();
    }

    private String normalizarNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(limpio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre o apellido no puede estar vacío");
        }
        return Arrays.stream(limpio.toLowerCase().split(" "))
                .filter(StringUtils::hasText)
                .map(StringUtils::capitalize)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private String normalizarTelefono(String telefono) {
        return telefono == null ? null : telefono.trim();
    }

    private String normalizarCorreoPersonal(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }

    private String normalizarDireccion(String direccion) {
        return direccion == null ? null : direccion.trim();
    }

    private void validarUnicidad(String codigoAlumno, String dni, String correoInstitucional) {
        alumnoRepository.findByCodigoAlumno(codigoAlumno.trim().toUpperCase())
                .ifPresent(a -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un alumno con el mismo código");
                });

        alumnoRepository.findByDni(dni.trim())
                .ifPresent(a -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un alumno con el mismo DNI");
                });

        alumnoRepository.findByCorreoInstitucional(correoInstitucional.trim().toLowerCase())
                .ifPresent(a -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un alumno con el mismo correo institucional");
                });
    }

    private Carrera buscarCarrera(Long carreraId) {
        return carreraRepository.findById(carreraId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no encontrada"));
    }
}
