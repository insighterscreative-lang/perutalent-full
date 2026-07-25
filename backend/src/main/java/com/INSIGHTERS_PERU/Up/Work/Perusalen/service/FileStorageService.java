package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    public String guardarCvPostulacion(MultipartFile archivo) {
        throw new RuntimeException("El guardado local de CVs está deshabilitado. Debe usarse Amazon S3.");
    }

    public Resource cargarArchivo(String rutaArchivo) {
        throw new RuntimeException("La descarga local de CVs está deshabilitada. Debe usarse Amazon S3.");
    }
}