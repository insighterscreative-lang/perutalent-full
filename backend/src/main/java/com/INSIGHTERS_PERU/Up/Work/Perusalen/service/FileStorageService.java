package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String guardarCvPostulacion(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debe seleccionar un archivo CV");
        }

        String contentType = archivo.getContentType();

        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("El CV debe ser un archivo PDF");
        }

        try {
            Path uploadPath = obtenerRutaUpload();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String nombreOriginal = archivo.getOriginalFilename();
            String extension = obtenerExtension(nombreOriginal);

            String nombreArchivo = UUID.randomUUID().toString() + extension;

            Path rutaDestino = uploadPath.resolve(nombreArchivo).normalize();

            Files.copy(archivo.getInputStream(), rutaDestino);

            return rutaDestino.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el CV", e);
        }
    }

    public Resource cargarArchivo(String rutaArchivo) {
        try {
            Path ruta = Paths.get(rutaArchivo).normalize();

            Resource resource = new UrlResource(ruta.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("No se pudo leer el archivo CV");
            }

            return resource;

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar el archivo CV", e);
        }
    }

    private Path obtenerRutaUpload() {
        return Paths.get(uploadDir).normalize();
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".pdf";
        }

        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}
