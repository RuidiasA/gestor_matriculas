package com.example.matriculas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlumnoController {

    @GetMapping("/matricula")
    public String matricula() {
        return "matricula"; // archivo matricula.html
    }
}
