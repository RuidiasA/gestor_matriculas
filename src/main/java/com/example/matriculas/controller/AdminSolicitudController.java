package com.example.matriculas.controller;

import com.example.matriculas.dto.request.SolicitudSeccionRequest;
import com.example.matriculas.dto.response.SolicitudSeccionResponse;
import com.example.matriculas.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/solicitudes")
@RequiredArgsConstructor
public class AdminSolicitudController {

    private final SolicitudService solicitudService;

    @GetMapping
    public List<SolicitudSeccionResponse> listar() { return solicitudService.listar(); }

    @PostMapping
    public SolicitudSeccionResponse crear(@Valid @RequestBody SolicitudSeccionRequest request) { return solicitudService.crear(request); }
}
