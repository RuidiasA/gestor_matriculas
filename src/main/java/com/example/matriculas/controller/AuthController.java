package com.example.matriculas.controller;

import com.example.matriculas.model.Usuario;
import com.example.matriculas.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/redireccion")
    public String redireccionSegunRol() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Usuario usuario = userDetails.getUsuario();

        switch (usuario.getRol()) {

            case ADMIN:
                return "redirect:/portal_admin";

            case ALUMNO:
                return "redirect:/matricula";

            default:
                return "redirect:/login?error=true";
        }
    }
}
