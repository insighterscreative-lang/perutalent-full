package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReporteProblemaTecnicoRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre completo debe tener entre 3 y 120 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede superar 150 caracteres")
    private String email;

    @NotBlank(message = "El tipo de problema es obligatorio")
    @Pattern(
            regexp = "ACCESO_CUENTA|OFERTAS|POSTULACIONES|PERFIL|PAGOS_SUSCRIPCION|ARCHIVOS_CV|OTRO",
            message = "El tipo de problema no es válido"
    )
    private String tipoProblema;

    @NotBlank(message = "La pantalla donde ocurrió el problema es obligatoria")
    @Size(min = 2, max = 150, message = "La pantalla debe tener entre 2 y 150 caracteres")
    private String pantalla;

    @NotBlank(message = "La descripción del problema es obligatoria")
    @Size(min = 10, max = 2500, message = "La descripción debe tener entre 10 y 2500 caracteres")
    private String descripcion;

    @Size(max = 2000, message = "Los pasos para reproducir no pueden superar 2000 caracteres")
    private String pasosReproducir;

    @Size(max = 1500, message = "La información adicional no puede superar 1500 caracteres")
    private String informacionAdicional;

    @AssertTrue(message = "Debes confirmar que la información proporcionada es correcta")
    private boolean aceptaDeclaracion;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(String tipoProblema) { this.tipoProblema = tipoProblema; }
    public String getPantalla() { return pantalla; }
    public void setPantalla(String pantalla) { this.pantalla = pantalla; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPasosReproducir() { return pasosReproducir; }
    public void setPasosReproducir(String pasosReproducir) { this.pasosReproducir = pasosReproducir; }
    public String getInformacionAdicional() { return informacionAdicional; }
    public void setInformacionAdicional(String informacionAdicional) { this.informacionAdicional = informacionAdicional; }
    public boolean isAceptaDeclaracion() { return aceptaDeclaracion; }
    public void setAceptaDeclaracion(boolean aceptaDeclaracion) { this.aceptaDeclaracion = aceptaDeclaracion; }
}
