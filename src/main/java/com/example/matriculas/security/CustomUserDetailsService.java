package com.example.matriculas.security;

import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correoInstitucional)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreoInstitucional(correoInstitucional)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado: " + correoInstitucional));

        return new CustomUserDetails(usuario);
    }
}
