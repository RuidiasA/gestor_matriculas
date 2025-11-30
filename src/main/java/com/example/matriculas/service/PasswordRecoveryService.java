package com.example.matriculas.service;

import com.example.matriculas.dto.VerificacionDTO;
import com.example.matriculas.model.Administrador;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.AdministradorRepository;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final AlumnoRepository alumnoRepository;
    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Usuario> validarIdentidad(VerificacionDTO dto) {

        // (Por ahora NO usamos nombres/apellidos, solo los 3 datos críticos)

        // 1) Intentar como alumno
        Optional<Alumno> alumno = alumnoRepository
                .findByDniAndCodigoAlumnoAndCorreoInstitucional(
                        dto.getDni(),
                        dto.getCodigo(),
                        dto.getCorreo()
                );

        if (alumno.isPresent()) {
            return Optional.ofNullable(alumno.get().getUsuario());
        }

        // 2) Intentar como administrador
        Optional<Administrador> admin = administradorRepository
                .findByDniAndCodigoAdminAndCorreoInstitucional(
                        dto.getDni(),
                        dto.getCodigo(),
                        dto.getCorreo()
                );

        if (admin.isPresent()) {
            return Optional.ofNullable(admin.get().getUsuario());
        }

        return Optional.empty();
    }

    public boolean cambiarPassword(String correo, String newPassword) {
        Optional<Usuario> userOpt = usuarioRepository.findByCorreoInstitucional(correo);

        if (userOpt.isEmpty()) return false;

        Usuario usuario = userOpt.get();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        return true;
    }
}

