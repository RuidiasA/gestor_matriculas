package com.example.matriculas.controller;

import com.example.matriculas.dto.VerificacionDTO;
import com.example.matriculas.dto.CambiarPasswordDTO;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.service.PasswordRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordRecoveryService passwordService;

    // ==========================================
    // 1. VALIDAR IDENTIDAD
    // ==========================================
    @PostMapping("/verificar")
    public ResponseEntity<?> verificar(@RequestBody VerificacionDTO dto) {

        return passwordService.validarIdentidad(dto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().body("Datos incorrectos"));
    }


    // ==========================================
    // 2. CAMBIAR PASSWORD
    // ==========================================
    @PostMapping("/cambiar")
    public ResponseEntity<?> cambiarPassword(@RequestBody CambiarPasswordDTO dto) {

        boolean ok = passwordService.cambiarPassword(dto.getCorreo(), dto.getNuevaPassword());

        if (!ok) return ResponseEntity.badRequest().body("Usuario no encontrado");

        return ResponseEntity.ok("Contraseña actualizada");
    }
}
