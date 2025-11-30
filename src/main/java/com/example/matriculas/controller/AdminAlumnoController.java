package com.example.matriculas.controller;

import com.example.matriculas.dto.ActualizarAlumnoDTO;
import com.example.matriculas.dto.AlumnoDTO;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.repository.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/alumnos")
@RequiredArgsConstructor
public class AdminAlumnoController {

    private final AlumnoRepository alumnoRepository;

    // ====================================================================
    // 1. LISTAR TODOS
    // ====================================================================
    @GetMapping("/listar")
    public List<AlumnoDTO> listar() {
        return alumnoRepository.findAll()
                .stream()
                .map(AlumnoDTO::fromEntity)
                .toList();
    }

    // ====================================================================
    // 2. BUSCAR (nombre, apellido o código)
    // ====================================================================
    @GetMapping("/buscar")
    public List<AlumnoDTO> buscar(@RequestParam String filtro) {

        String f = filtro.toLowerCase();

        return alumnoRepository.findAll()
                .stream()
                .filter(a ->
                        (a.getNombres() != null && a.getNombres().toLowerCase().contains(f)) ||
                                (a.getApellidos() != null && a.getApellidos().toLowerCase().contains(f)) ||
                                (a.getCodigoAlumno() != null && a.getCodigoAlumno().toLowerCase().contains(f)) ||
                                (a.getDni() != null && a.getDni().contains(f)) ||
                                (a.getCorreoInstitucional() != null && a.getCorreoInstitucional().toLowerCase().contains(f))
                )
                .map(AlumnoDTO::fromEntity)
                .toList();
    }


    // ====================================================================
    // 3. OBTENER POR ID
    // ====================================================================
    @GetMapping("/{id}")
    public AlumnoDTO obtener(@PathVariable Long id) {
        Alumno a = alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        return AlumnoDTO.fromEntity(a);
    }

    // ====================================================================
// 4. ACTUALIZAR DATOS EDITABLES: nombres, apellidos, correo y teléfono
// ====================================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarAlumnoDTO dto
    ) {
        Alumno a = alumnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        // Solo campos editables
        a.setNombres(dto.getNombres());
        a.setApellidos(dto.getApellidos());
        a.setCorreoPersonal(dto.getCorreoPersonal());
        a.setTelefonoPersonal(dto.getTelefonoPersonal());

        alumnoRepository.save(a);
    }
}
