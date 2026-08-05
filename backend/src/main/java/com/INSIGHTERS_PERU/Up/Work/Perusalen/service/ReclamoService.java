package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReclamoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Reclamo;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReclamoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;


@Service
public class ReclamoService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final ReclamoRepository reclamoRepository;
    private final EmailService emailService;

    @Value("${app.support-email:insighters.creative@gmail.com}")
    private String correoSoporte;

    public ReclamoService(
            ReclamoRepository reclamoRepository,
            EmailService emailService
    ) {
        this.reclamoRepository = reclamoRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ReclamoResponseDTO registrar(ReclamoRequestDTO request) {
        Reclamo reclamo = new Reclamo();
        reclamo.setCodigoReclamo(generarCodigoUnico());
        reclamo.setNombreCompleto(limpiar(request.getNombreCompleto()));
        reclamo.setEmail(limpiar(request.getEmail()).toLowerCase(Locale.ROOT));
        reclamo.setTelefono(limpiarOpcional(request.getTelefono()));
        reclamo.setTipoDocumento(request.getTipoDocumento());
        reclamo.setNumeroDocumento(limpiar(request.getNumeroDocumento()).toUpperCase(Locale.ROOT));
        reclamo.setServicioRelacionado(request.getServicioRelacionado());
        reclamo.setMontoReclamado(request.getMontoReclamado());
        reclamo.setTipoSolicitud(request.getTipoSolicitud());
        reclamo.setAsunto(limpiar(request.getAsunto()));
        reclamo.setDetalle(limpiar(request.getDetalle()));
        reclamo.setPedido(limpiar(request.getPedido()));
        reclamo.setFechaCreacion(FechaPeru.ahora());
        reclamo.setEstado(ESTADO_PENDIENTE);

        Reclamo guardado = reclamoRepository.save(reclamo);

        enviarCorreosSinInterrumpirRegistro(guardado);

        return new ReclamoResponseDTO(
                guardado.getCodigoReclamo(),
                guardado.getEstado(),
                guardado.getFechaCreacion()
        );
    }


    private void enviarCorreosSinInterrumpirRegistro(Reclamo reclamo) {
        try {
            emailService.enviarReclamoAlEquipo(correoSoporte, reclamo);
        } catch (Exception ex) {
            System.out.println("El reclamo se guardó, pero no se pudo enviar el correo al equipo: " + ex.getMessage());
        }

        try {
            emailService.enviarConfirmacionReclamo(reclamo.getEmail(), reclamo);
        } catch (Exception ex) {
            System.out.println("El reclamo se guardó, pero no se pudo enviar la confirmación: " + ex.getMessage());
        }
    }

    private String generarCodigoUnico() {
        String codigo;
        do {
            String sufijo = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
            codigo = "REC-%d-%s".formatted(FechaPeru.hoy().getYear(), sufijo);
        } while (reclamoRepository.existsByCodigoReclamo(codigo));

        return codigo;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String limpiarOpcional(String valor) {
        String limpio = limpiar(valor);
        return limpio.isBlank() ? null : limpio;
    }
}
