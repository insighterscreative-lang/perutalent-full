package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteOfertaResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ConflictException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.ResourceNotFoundException;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.OfertaLaboral;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteOferta;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Usuario;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.OfertaLaboralRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReporteOfertaRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.UsuarioRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;


@Service
public class ReporteOfertaService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final ReporteOfertaRepository reporteOfertaRepository;
    private final OfertaLaboralRepository ofertaLaboralRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Value("${app.support-email:insighters.creative@gmail.com}")
    private String correoSoporte;

    public ReporteOfertaService(
            ReporteOfertaRepository reporteOfertaRepository,
            OfertaLaboralRepository ofertaLaboralRepository,
            UsuarioRepository usuarioRepository,
            EmailService emailService
    ) {
        this.reporteOfertaRepository = reporteOfertaRepository;
        this.ofertaLaboralRepository = ofertaLaboralRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ReporteOfertaResponseDTO reportar(
            Long idOferta,
            Long idUsuario,
            ReporteOfertaRequestDTO request
    ) {
        OfertaLaboral oferta = ofertaLaboralRepository.findById(idOferta)
                .orElseThrow(() -> new ResourceNotFoundException("La oferta que intentas reportar no existe"));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario autenticado no existe"));

        if (reporteOfertaRepository.existsByOfertaIdAndUsuarioReportanteId(idOferta, idUsuario)) {
            throw new ConflictException("Ya reportaste esta oferta anteriormente");
        }

        ReporteOferta reporte = new ReporteOferta();
        reporte.setOferta(oferta);
        reporte.setUsuarioReportante(usuario);
        reporte.setMotivo(request.getMotivo().toUpperCase(Locale.ROOT));
        reporte.setDescripcion(request.getDescripcion().trim());
        reporte.setFechaCreacion(FechaPeru.ahora());
        reporte.setEstado(ESTADO_PENDIENTE);

        try {
            reporte = reporteOfertaRepository.saveAndFlush(reporte);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Ya reportaste esta oferta anteriormente");
        }

        enviarCorreosSinInterrumpirRegistro(reporte, usuario.getEmail());

        return new ReporteOfertaResponseDTO(
                reporte.getId(),
                reporte.getEstado(),
                reporte.getFechaCreacion()
        );
    }
    private void enviarCorreosSinInterrumpirRegistro(ReporteOferta reporte, String correoUsuario) {
        try {
            emailService.enviarReporteOfertaAlEquipo(correoSoporte, reporte);
        } catch (Exception ex) {
            System.out.println("El reporte se guardó, pero no se pudo enviar el correo al equipo: " + ex.getMessage());
        }

        try {
            emailService.enviarConfirmacionReporteOferta(correoUsuario, reporte);
        } catch (Exception ex) {
            System.out.println("El reporte se guardó, pero no se pudo enviar la confirmación: " + ex.getMessage());
        }
    }

}
