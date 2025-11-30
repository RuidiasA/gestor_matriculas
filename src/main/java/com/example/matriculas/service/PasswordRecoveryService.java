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

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private final AlumnoRepository alumnoRepository;
    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Usuario> validarIdentidad(VerificacionDTO dto) {

        String dni = dto.getDni();
        String codigo = dto.getCodigo();
        String correo = dto.getCorreo();
        String nombreCompletoInput = dto.getNombresCompletos();

        // ==========================
        // 1) Intentar como ALUMNO
        // ==========================
        Optional<Alumno> alumnoOpt =
                alumnoRepository.findByDniAndCodigoAlumnoAndCorreoInstitucional(dni, codigo, correo);

        if (alumnoOpt.isPresent()) {
            Alumno alumno = alumnoOpt.get();

            if (coincideNombreCompleto(nombreCompletoInput,
                    alumno.getNombres(), alumno.getApellidos())) {

                return Optional.ofNullable(alumno.getUsuario());
            }
        }

        // ==========================
        // 2) Intentar como ADMIN
        // ==========================
        Optional<Administrador> adminOpt =
                administradorRepository.findByDniAndCodigoAdminAndCorreoInstitucional(dni, codigo, correo);

        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();

            if (coincideNombreCompleto(nombreCompletoInput,
                    admin.getNombres(), admin.getApellidos())) {

                return Optional.ofNullable(admin.getUsuario());
            }
        }

        // ==========================
        // 3) Nadie coincide
        // ==========================
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



    // ==========================================
    // VALIDACIÓN ROBUSTA DE NOMBRE COMPLETO
    // ==========================================

    private boolean coincideNombreCompleto(String inputNombreCompleto,
                                           String nombresBD,
                                           String apellidosBD) {

        // Si el input está vacío, no bloqueamos (como versión original)
        if (inputNombreCompleto == null || inputNombreCompleto.isBlank()) {
            return true;
        }

        // Normalizamos todo
        String inputNorm = normalizar(inputNombreCompleto);
        String bdNorm = normalizar(nombresBD + " " + apellidosBD);

        // Separamos el input por palabras
        String[] palabrasInput = inputNorm.split(" ");

        // Cada palabra del input debe existir en el nombre+apellido BD
        for (String palabra : palabrasInput) {
            if (!bdNorm.contains(palabra)) {
                return false; // si alguna palabra no coincide, falla
            }
        }

        return true; // todas las palabras coinciden
    }

    private String normalizar(String texto) {
        if (texto == null) return "";

        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", ""); // Quita acentos

        return sinTildes
                .replaceAll("\\s+", " ")    // varios espacios → uno
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}
