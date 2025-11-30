package com.example.matriculas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/portal_admin")
    public String portalAdmin() {
        return "portal_admin"; // archivo portal_admin.html
    }
}
