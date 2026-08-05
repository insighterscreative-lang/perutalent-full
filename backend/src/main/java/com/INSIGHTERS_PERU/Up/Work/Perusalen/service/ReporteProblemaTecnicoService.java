package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoRequestDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.dto.ReporteProblemaTecnicoResponseDTO;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteProblemaTecnico;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.repository.ReporteProblemaTecnicoRepository;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.util.FechaPeru;

@Service
public class ReporteProblemaTecnicoService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";

    private final ReporteProblemaTecnicoRepository repository;
    private final EmailService emailService;

    @Value("${app.support-email:insighters.creative@gmail.com}")
    private String correoSoporte;

    public ReporteProblemaTecnicoService(
            ReporteProblemaTecnicoRepository repository,
            EmailService emailService
    ) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Transactional
    public ReporteProblemaTecnicoResponseDTO registrar(
            ReporteProblemaTecnicoRequestDTO request
    ) {
        ReporteProblemaTecnico reporte = new ReporteProblemaTecnico();
        reporte.setCodigoReporte(generarCodigoUnico());
        reporte.setNombreCompleto(request.getNombreCompleto().trim());
        reporte.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        reporte.setTipoProblema(request.getTipoProblema().toUpperCase(Locale.ROOT));
        reporte.setPantalla(request.getPantalla().trim());
        reporte.setDescripcion(request.getDescripcion().trim());
        reporte.setPasosReproducir(limpiarOpcional(request.getPasosReproducir()));
        reporte.setInformacionAdicional(limpiarOpcional(request.getInformacionAdicional()));
        reporte.setFechaCreacion(FechaPeru.ahora());
        reporte.setEstado(ESTADO_PENDIENTE);

        ReporteProblemaTecnico guardado = repository.save(reporte);
        enviarCorreosSinInterrumpirRegistro(guardado);

        return new ReporteProblemaTecnicoResponseDTO(
                guardado.getCodigoReporte(),
                guardado.getEstado(),
                guardado.getFechaCreacion()
        );
    }

    private String generarCodigoUnico() {
        String codigo;

        do {
            String aleatorio = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
            codigo = "TEC-" + FechaPeru.hoy().getYear() + "-" + aleatorio;
        } while (repository.existsByCodigoReporte(codigo));

        return codigo;
    }

    private String limpiarOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private void enviarCorreosSinInterrumpirRegistro(ReporteProblemaTecnico reporte) {
        try {
            emailService.enviarProblemaTecnicoAlEquipo(correoSoporte, reporte);
        } catch (Exception ex) {
            System.out.println(
                    "El problema técnico se guardó, pero no se pudo enviar al equipo: "
                            + ex.getMessage()
            );
        }

        try {
            emailService.enviarConfirmacionProblemaTecnico(reporte.getEmail(), reporte);
        } catch (Exception ex) {
            System.out.println(
                    "El problema técnico se guardó, pero no se pudo enviar la confirmación: "
                            + ex.getMessage()
            );
        }
    }
}
