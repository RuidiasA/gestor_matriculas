package com.example.matriculas.controller;

import com.example.matriculas.dto.ActualizarAlumnoDTO;
import com.example.matriculas.dto.AlumnoDTO;
import com.example.matriculas.service.AdminAlumnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/admin/alumnos")
@RequiredArgsConstructor
public class AdminAlumnoController {

    private final AdminAlumnoService adminAlumnoService;

    // ====================================================================
    // 1. LISTAR TODOS
    // ====================================================================
    @GetMapping("/listar")
    public List<AlumnoDTO> listar() {
        return adminAlumnoService.listar();
    }

    // ====================================================================
    // 2. BUSCAR (nombre, apellido o código)
    // ====================================================================
    @GetMapping("/buscar")
    public List<AlumnoDTO> buscar(@RequestParam String filtro) {
        return adminAlumnoService.buscar(filtro);
    }


    // ====================================================================
    // 3. OBTENER POR ID
    // ====================================================================
    @GetMapping("/{id}")
    public AlumnoDTO obtener(@PathVariable Long id) {
        return adminAlumnoService.obtener(id);
    }

// ====================================================================
// 4. ACTUALIZAR DATOS EDITABLES: nombres, apellidos, correo y teléfono
// ====================================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAlumnoDTO dto
    ) {
        if (dto.getNombres() == null && dto.getApellidos() == null
                && dto.getCorreoPersonal() == null && dto.getTelefonoPersonal() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se enviaron campos para actualizar");
        }
        adminAlumnoService.actualizar(id, dto);
    }
}
