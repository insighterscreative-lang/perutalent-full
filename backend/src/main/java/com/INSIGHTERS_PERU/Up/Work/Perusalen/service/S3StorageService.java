package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.prefix:cvs}")
    private String prefix;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String subirCvPostulacion(MultipartFile archivo, Long idUsuario, Long idOferta) {
        validarArchivoCv(archivo);

        String nombreOriginal = limpiarNombreArchivo(archivo.getOriginalFilename());
        String extension = obtenerExtension(nombreOriginal);

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreFinal = fecha + "-" + UUID.randomUUID() + extension;

        String key = normalizarPrefix(prefix)
                + "/postulaciones"
                + "/usuario-" + idUsuario
                + "/oferta-" + idOferta
                + "/" + nombreFinal;

        return subirArchivoPdf(
                archivo,
                key,
                Map.of(
                        "nombre-original", nombreOriginal,
                        "id-usuario", String.valueOf(idUsuario),
                        "id-oferta", String.valueOf(idOferta),
                        "tipo", "cv-postulacion"
                ),
                "No se pudo subir el CV de la postulación a S3: "
        );
    }

    public String subirCvPerfil(MultipartFile archivo, Long idUsuario) {
        validarArchivoCv(archivo);

        String nombreOriginal = limpiarNombreArchivo(archivo.getOriginalFilename());
        String extension = obtenerExtension(nombreOriginal);

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreFinal = fecha + "-" + UUID.randomUUID() + extension;

        String key = normalizarPrefix(prefix)
                + "/perfiles"
                + "/usuario-" + idUsuario
                + "/" + nombreFinal;

        return subirArchivoPdf(
                archivo,
                key,
                Map.of(
                        "nombre-original", nombreOriginal,
                        "id-usuario", String.valueOf(idUsuario),
                        "tipo", "cv-perfil"
                ),
                "No se pudo subir el CV del perfil a S3: "
        );
    }

    public String copiarCvPerfilAPostulacion(String cvPerfilKey, Long idUsuario, Long idOferta) {
        if (cvPerfilKey == null || cvPerfilKey.isBlank()) {
            throw new RuntimeException("No tienes un CV cargado en tu perfil");
        }

        if (esUrlPublica(cvPerfilKey) || cvPerfilKey.trim().startsWith("/")) {
            throw new RuntimeException("Tu CV actual no está guardado en Amazon S3. Actualiza el CV de tu perfil o sube un CV nuevo para postular.");
        }

        String extension = obtenerExtension(cvPerfilKey);
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreFinal = fecha + "-" + UUID.randomUUID() + "-perfil" + extension;

        String nuevaKey = normalizarPrefix(prefix)
                + "/postulaciones"
                + "/usuario-" + idUsuario
                + "/oferta-" + idOferta
                + "/" + nombreFinal;

        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .copySource(bucketName + "/" + cvPerfilKey)
                    .bucket(bucketName)
                    .key(nuevaKey)
                    .contentType("application/pdf")
                    .metadata(Map.of(
                            "origen", "cv-perfil",
                            "ruta-original", cvPerfilKey,
                            "id-usuario", String.valueOf(idUsuario),
                            "id-oferta", String.valueOf(idOferta),
                            "tipo", "cv-postulacion"
                    ))
                    .metadataDirective(MetadataDirective.REPLACE)
                    .build();

            s3Client.copyObject(copyObjectRequest);

            return nuevaKey;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo copiar el CV del perfil para la postulación en S3: " + e.getMessage());
        }
    }


    public String subirFotoPerfilEmpleado(MultipartFile archivo, Long idUsuario) {
        return subirImagenPerfil(
                archivo,
                idUsuario,
                "empleados",
                "foto-perfil",
                "foto de perfil del empleado"
        );
    }

    public String subirLogoEmpleador(MultipartFile archivo, Long idUsuario) {
        return subirImagenPerfil(
                archivo,
                idUsuario,
                "empleadores",
                "logo-empleador",
                "logo del empleador"
        );
    }

    private String subirImagenPerfil(
            MultipartFile archivo,
            Long idUsuario,
            String carpeta,
            String tipo,
            String descripcionArchivo
    ) {
        validarArchivoImagenPerfil(archivo);

        String nombreOriginal = limpiarNombreArchivo(archivo.getOriginalFilename());
        String extension = obtenerExtensionImagen(nombreOriginal, archivo.getContentType());

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreFinal = fecha + "-" + UUID.randomUUID() + extension;

        String key = "imagenes"
                + "/" + carpeta
                + "/usuario-" + idUsuario
                + "/" + nombreFinal;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(obtenerContentTypeImagen(extension, archivo.getContentType()))
                    .contentLength(archivo.getSize())
                    .metadata(Map.of(
                            "nombre-original", nombreOriginal,
                            "id-usuario", String.valueOf(idUsuario),
                            "tipo", tipo
                    ))
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize())
            );

            return key;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la imagen para subirla a S3.");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo subir la " + descripcionArchivo + " a S3: " + e.getMessage());
        }
    }

    private String subirArchivoPdf(
            MultipartFile archivo,
            String key,
            Map<String, String> metadata,
            String mensajeError
    ) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(archivo.getContentType() != null ? archivo.getContentType() : "application/pdf")
                    .contentLength(archivo.getSize())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize())
            );

            return key;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el archivo CV para subirlo a S3.");
        } catch (Exception e) {
            throw new RuntimeException(mensajeError + e.getMessage());
        }
    }

    public ResponseInputStream<GetObjectResponse> descargarArchivo(String key) {
        if (key == null || key.isBlank()) {
            throw new RuntimeException("El archivo solicitado no tiene una ruta válida.");
        }

        if (esUrlPublica(key)) {
            throw new RuntimeException("El archivo solicitado no corresponde a una ruta privada de S3.");
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo descargar el archivo desde S3: " + e.getMessage());
        }
    }

    public void eliminarArchivo(String key) {
        if (key == null || key.isBlank() || esUrlPublica(key)) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo eliminar el archivo de S3: " + e.getMessage());
        }
    }

    public boolean esUrlPublica(String valor) {
        if (valor == null) {
            return false;
        }

        String limpio = valor.trim().toLowerCase();
        return limpio.startsWith("http://") || limpio.startsWith("https://");
    }

    private void validarArchivoImagenPerfil(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debes adjuntar una imagen de perfil.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();
        String nombreLower = nombreOriginal != null ? nombreOriginal.toLowerCase() : "";

        boolean extensionValida = nombreLower.endsWith(".jpg")
                || nombreLower.endsWith(".jpeg")
                || nombreLower.endsWith(".png")
                || nombreLower.endsWith(".webp");

        boolean contentTypeValido = contentType != null && (
                contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/jpg")
                        || contentType.equalsIgnoreCase("image/png")
                        || contentType.equalsIgnoreCase("image/webp")
        );

        if (!extensionValida && !contentTypeValido) {
            throw new RuntimeException("La imagen de perfil debe ser JPG, PNG o WEBP.");
        }

        long maxSizeBytes = 2 * 1024 * 1024;

        if (archivo.getSize() > maxSizeBytes) {
            throw new RuntimeException("La imagen de perfil no debe superar los 2 MB.");
        }
    }

    private void validarArchivoCv(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debes adjuntar un CV en formato PDF.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();

        boolean extensionPdf = nombreOriginal != null
                && nombreOriginal.toLowerCase().endsWith(".pdf");

        boolean contentTypePdf = contentType != null
                && contentType.equalsIgnoreCase("application/pdf");

        if (!extensionPdf && !contentTypePdf) {
            throw new RuntimeException("El CV debe estar en formato PDF.");
        }

        long maxSizeBytes = 5 * 1024 * 1024;

        if (archivo.getSize() > maxSizeBytes) {
            throw new RuntimeException("El CV no debe superar los 5 MB.");
        }
    }

    private String limpiarNombreArchivo(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "cv.pdf";
        }

        String nombre = Normalizer.normalize(nombreOriginal, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return nombre
                .replaceAll("[^a-zA-Z0-9.\\-_]", "-")
                .replaceAll("-+", "-")
                .toLowerCase();
    }

    private String obtenerExtensionImagen(String nombreArchivo, String contentType) {
        String nombre = nombreArchivo != null ? nombreArchivo.toLowerCase() : "";

        if (nombre.endsWith(".jpeg")) {
            return ".jpg";
        }

        if (nombre.endsWith(".jpg")) {
            return ".jpg";
        }

        if (nombre.endsWith(".png")) {
            return ".png";
        }

        if (nombre.endsWith(".webp")) {
            return ".webp";
        }

        if (contentType != null) {
            if (contentType.equalsIgnoreCase("image/png")) {
                return ".png";
            }

            if (contentType.equalsIgnoreCase("image/webp")) {
                return ".webp";
            }
        }

        return ".jpg";
    }

    private String obtenerContentTypeImagen(String extension, String contentTypeOriginal) {
        if (contentTypeOriginal != null && !contentTypeOriginal.isBlank()) {
            String limpio = contentTypeOriginal.trim().toLowerCase();

            if (limpio.equals("image/jpeg")
                    || limpio.equals("image/jpg")
                    || limpio.equals("image/png")
                    || limpio.equals("image/webp")) {
                return limpio.equals("image/jpg") ? "image/jpeg" : limpio;
            }
        }

        return switch (extension) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".pdf";
        }

        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).toLowerCase();

        if (!extension.equals(".pdf")) {
            return ".pdf";
        }

        return extension;
    }

    private String normalizarPrefix(String valor) {
        if (valor == null || valor.isBlank()) {
            return "cvs";
        }

        String limpio = valor.trim();

        while (limpio.startsWith("/")) {
            limpio = limpio.substring(1);
        }

        while (limpio.endsWith("/")) {
            limpio = limpio.substring(0, limpio.length() - 1);
        }

        return limpio;
    }
}
