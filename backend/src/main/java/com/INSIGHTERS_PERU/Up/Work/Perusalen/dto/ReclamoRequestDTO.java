package com.INSIGHTERS_PERU.Up.Work.Perusalen.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReclamoRequestDTO {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 120, message = "El nombre completo debe tener entre 3 y 120 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 150, message = "El correo electrónico no puede superar 150 caracteres")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "DNI|CE|PASAPORTE|OTRO", message = "El tipo de documento no es válido")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 30, message = "El número de documento debe tener entre 5 y 30 caracteres")
    @Pattern(regexp = "[A-Za-z0-9.-]+", message = "El número de documento contiene caracteres no permitidos")
    private String numeroDocumento;

    @NotBlank(message = "El servicio relacionado es obligatorio")
    @Pattern(regexp = "PLAN|POSTULACION|OFERTA|CUENTA|OTRO", message = "El servicio relacionado no es válido")
    private String servicioRelacionado;

    @DecimalMin(value = "0.00", inclusive = true, message = "El monto reclamado no puede ser negativo")
    private BigDecimal montoReclamado;

    @NotBlank(message = "El tipo de solicitud es obligatorio")
    @Pattern(regexp = "RECLAMO|QUEJA", message = "El tipo de solicitud no es válido")
    private String tipoSolicitud;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 5, max = 150, message = "El asunto debe tener entre 5 y 150 caracteres")
    private String asunto;

    @NotBlank(message = "El detalle es obligatorio")
    @Size(min = 10, max = 2500, message = "El detalle debe tener entre 10 y 2500 caracteres")
    private String detalle;

    @NotBlank(message = "El pedido del consumidor es obligatorio")
    @Size(min = 5, max = 1500, message = "El pedido debe tener entre 5 y 1500 caracteres")
    private String pedido;

    @AssertTrue(message = "Debes confirmar que la información proporcionada es correcta")
    private boolean aceptaDeclaracion;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getServicioRelacionado() { return servicioRelacionado; }
    public void setServicioRelacionado(String servicioRelacionado) { this.servicioRelacionado = servicioRelacionado; }
    public BigDecimal getMontoReclamado() { return montoReclamado; }
    public void setMontoReclamado(BigDecimal montoReclamado) { this.montoReclamado = montoReclamado; }
    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getPedido() { return pedido; }
    public void setPedido(String pedido) { this.pedido = pedido; }
    public boolean isAceptaDeclaracion() { return aceptaDeclaracion; }
    public void setAceptaDeclaracion(boolean aceptaDeclaracion) { this.aceptaDeclaracion = aceptaDeclaracion; }
}
