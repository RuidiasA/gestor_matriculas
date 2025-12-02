package com.example.matriculas.service;

import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================================
    // 1. Obtener usuario por email (para login)
    // =========================================
    public Optional<Usuario> obtenerPorCorreoInstitucional(String correo) {
        return usuarioRepository.findByCorreoInstitucional(correo);
    }


    // =========================================
    // 2. Crear usuario (encriptando contraseña)
    // =========================================
    public Usuario crearUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    // =========================================
    // 3. Cambiar contraseña
    // =========================================
    public void cambiarPassword(String email, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByCorreoInstitucional(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    // =========================================
    // 4. Validar existencia
    // =========================================
    public boolean existeEmail(String email) {
        return usuarioRepository.findByCorreoInstitucional(email).isPresent();
    }

    // =========================================
    // 5. Guardar cambios generales
    // =========================================
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
