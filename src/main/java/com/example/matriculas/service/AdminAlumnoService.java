package com.example.matriculas.service;

import com.example.matriculas.dto.ActualizarAlumnoDTO;
import com.example.matriculas.dto.AlumnoDTO;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.repository.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAlumnoService {

    private final AlumnoRepository alumnoRepository;

    public List<AlumnoDTO> listar() {
        return alumnoRepository.findAllByOrderByApellidosAscNombresAsc()
                .stream()
                .map(AlumnoDTO::fromEntity)
                .toList();
    }

    public List<AlumnoDTO> buscar(String filtro) {
        if (!StringUtils.hasText(filtro)) {
            return List.of();
        }

        return alumnoRepository.buscarPorFiltro(
                        filtro.trim().toLowerCase(),
                        PageRequest.of(0, 50) // tamaño de página (ajustable)
                )
                .getContent()
                .stream()
                .map(AlumnoDTO::fromEntity)
                .toList();
    }


    public AlumnoDTO obtener(Long id) {
        return AlumnoDTO.fromEntity(obtenerAlumno(id));
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
            alumno.setCorreoPersonal(dto.getCorreoPersonal().trim().toLowerCase());
        }

        if (dto.getTelefonoPersonal() != null) {
            alumno.setTelefonoPersonal(dto.getTelefonoPersonal().trim());
        }

        alumnoRepository.save(alumno);
        return AlumnoDTO.fromEntity(alumno);
    }

    private Alumno obtenerAlumno(Long id) {
        return alumnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado"));
    }

    private String normalizarNombre(String valor) {
        String limpio = valor.trim().replaceAll("\\s+", " ");
        if (!StringUtils.hasText(limpio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre o apellido no puede estar vacío");
        }
        return StringUtils.capitalize(limpio.toLowerCase());
    }
}
