package com.example.matriculas.service;

import com.example.matriculas.model.SolicitudSeccion;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class EvidenciaStorageService {

    private final Path rootLocation = Paths.get("uploads").resolve("solicitudes");

    public String guardarEvidencia(Long solicitudId, MultipartFile archivo) throws IOException {
        if (solicitudId == null || archivo == null || archivo.isEmpty()) {
            return null;
        }
        String nombreLimpio = Paths.get(archivo.getOriginalFilename()).getFileName().toString();
        Path directorio = rootLocation.resolve(String.valueOf(solicitudId));
        Files.createDirectories(directorio);
        Path destino = directorio.resolve(nombreLimpio);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return rootLocation.relativize(destino).toString().replace('\\', '/');
    }

    public Resource cargarEvidencia(SolicitudSeccion solicitud) throws IOException {
        if (solicitud == null) {
            return null;
        }
        if (StringUtils.hasText(solicitud.getEvidenciaRuta())) {
            Path archivo = rootLocation.resolve(solicitud.getEvidenciaRuta());
            if (Files.exists(archivo)) {
                return new UrlResource(archivo.toUri());
            }
        }
        if (solicitud.getEvidenciaContenido() != null) {
            return new ByteArrayResource(solicitud.getEvidenciaContenido());
        }
        return null;
    }
}
