package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.io.IOException;
import java.math.BigDecimal;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.Reclamo;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteOferta;
import com.INSIGHTERS_PERU.Up.Work.Perusalen.model.entity.ReporteProblemaTecnico;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    @Value("${sendgrid.from-email:}")
    private String fromEmail;

    @Value("${sendgrid.from-name:PeruTalent}")
    private String fromName;

    public void enviarCorreoBienvenidaEmpleado(String destinatario) {
        String asunto = "Bienvenido a PeruTalent";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>¡Bienvenido a PeruTalent!</h2>

                    <p>
                        Gracias por registrarte como empleado en nuestra plataforma.
                    </p>

                    <p>
                        Desde ahora podrás completar tu perfil, subir o actualizar tu CV,
                        buscar oportunidades laborales y postular a las ofertas disponibles.
                    </p>

                    <p>
                        Te recomendamos mantener tu información actualizada para mejorar tus
                        posibilidades de ser seleccionado por los empleadores.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """;

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCorreoBienvenidaEmpleador(String destinatario) {
        String asunto = "Bienvenido a PeruTalent";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>¡Bienvenido a PeruTalent!</h2>

                    <p>
                        Gracias por registrarte como empleador en nuestra plataforma.
                    </p>

                    <p>
                        Desde ahora podrás completar tu perfil de empleador, publicar ofertas
                        laborales y revisar las postulaciones de los candidatos interesados.
                    </p>

                    <p>
                        Te recomendamos mantener la información de tus ofertas clara y actualizada
                        para recibir postulaciones más adecuadas.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """;

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCorreoPostulacionEnviada(
            String destinatario,
            String nombreEmpleado,
            String tituloOferta,
            String nombreEmpresa
    ) {
        String asunto = "Postulación enviada correctamente";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>Tu postulación fue enviada, %s</h2>

                    <p>
                        Confirmamos que tu postulación para la oferta
                        <strong>%s</strong> fue registrada correctamente.
                    </p>

                    <p>
                        Empresa: <strong>%s</strong>
                    </p>

                    <p>
                        El empleador podrá revisar tu perfil y el CV que enviaste en la postulación.
                        Te recomendamos estar atento a tu correo y teléfono de contacto.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(nombreEmpleado),
                escaparHtml(tituloOferta),
                escaparHtml(nombreEmpresa)
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCorreoPerfilEmpleadorCreado(
            String destinatario,
            String nombreComercial
    ) {
        String asunto = "Perfil de empleador creado en PeruTalent";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>¡Tu perfil de empleador está listo!</h2>

                    <p>
                        El perfil de <strong>%s</strong> fue creado correctamente en PeruTalent.
                    </p>

                    <p>
                        Desde ahora puedes publicar ofertas laborales, revisar postulantes
                        y gestionar tus oportunidades desde tu cuenta de empleador.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(nombreComercial)
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCodigoRecuperacionPassword(String destinatario, String codigo) {
        String asunto = "Código para restablecer tu contraseña";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>Recuperación de contraseña</h2>

                    <p>
                        Recibimos una solicitud para restablecer la contraseña de tu cuenta en PeruTalent.
                    </p>

                    <p>
                        Ingresa el siguiente código en la plataforma:
                    </p>

                    <div style="font-size: 28px; font-weight: 800; letter-spacing: 6px; padding: 16px 20px; background: #f1f5f9; border-radius: 12px; display: inline-block; color: #0f172a;">
                        %s
                    </div>

                    <p>
                        Este código vence en <strong>2 minutos</strong>. Si solicitas un código nuevo,
                        el código anterior dejará de funcionar.
                    </p>

                    <p>
                        Si no solicitaste este cambio, puedes ignorar este correo.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(escaparHtml(codigo));

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCorreoPostulacionAceptada(
            String destinatario,
            String nombreEmpleado,
            String tituloOferta
    ) {
        String asunto = "Has sido preseleccionado para una oferta";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>¡Buenas noticias, %s!</h2>

                    <p>
                        El empleador te preseleccionó para la oferta <strong>%s</strong>.
                    </p>

                    <p>
                        Esto no representa una contratación dentro de PeruTalent. El empleador revisó
                        tu perfil y CV y podría comunicarse contigo por correo o teléfono para continuar
                        el proceso de selección de manera externa a la plataforma.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(nombreEmpleado),
                escaparHtml(tituloOferta)
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarCorreoPostulacionRechazada(
            String destinatario,
            String nombreEmpleado,
            String tituloOferta
    ) {
        String asunto = "Actualización sobre tu postulación";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>Hola, %s</h2>

                    <p>
                        Tu postulación para la oferta <strong>%s</strong> fue revisada.
                    </p>

                    <p>
                        En esta ocasión, el empleador decidió no continuar con tu postulación.
                    </p>

                    <p>
                        Gracias por participar. Puedes seguir revisando nuevas oportunidades
                        dentro de PeruTalent.
                    </p>

                    <br>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(nombreEmpleado),
                escaparHtml(tituloOferta)
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }


    public void enviarReclamoAlEquipo(String destinatario, Reclamo reclamo) {
        String asunto = "Nuevo %s - %s".formatted(
                textoTipoSolicitud(reclamo.getTipoSolicitud()),
                reclamo.getCodigoReclamo()
        );

        String monto = reclamo.getMontoReclamado() == null
                ? "No aplica"
                : "S/ " + formatearMonto(reclamo.getMontoReclamado());

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Nuevo registro en el Libro de Reclamaciones</h2>

                    <p><strong>Código:</strong> %s</p>
                    <p><strong>Tipo:</strong> %s</p>
                    <p><strong>Estado:</strong> %s</p>
                    <p><strong>Fecha:</strong> %s</p>

                    <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 20px 0;">

                    <h3>Datos del consumidor</h3>
                    <p><strong>Nombre:</strong> %s</p>
                    <p><strong>Correo:</strong> %s</p>
                    <p><strong>Teléfono:</strong> %s</p>
                    <p><strong>Documento:</strong> %s %s</p>

                    <h3>Datos de la solicitud</h3>
                    <p><strong>Servicio:</strong> %s</p>
                    <p><strong>Monto reclamado:</strong> %s</p>
                    <p><strong>Asunto:</strong> %s</p>
                    <p><strong>Detalle:</strong><br>%s</p>
                    <p><strong>Pedido:</strong><br>%s</p>

                    <p style="color: #6b7280; margin-top: 24px;">
                        Este registro quedó guardado en la base de datos con estado PENDIENTE.
                    </p>
                </div>
                """.formatted(
                escaparHtml(reclamo.getCodigoReclamo()),
                escaparHtml(textoTipoSolicitud(reclamo.getTipoSolicitud())),
                escaparHtml(reclamo.getEstado()),
                escaparHtml(String.valueOf(reclamo.getFechaCreacion())),
                escaparHtml(reclamo.getNombreCompleto()),
                escaparHtml(reclamo.getEmail()),
                escaparHtml(valorOpcional(reclamo.getTelefono())),
                escaparHtml(reclamo.getTipoDocumento()),
                escaparHtml(reclamo.getNumeroDocumento()),
                escaparHtml(reclamo.getServicioRelacionado()),
                escaparHtml(monto),
                escaparHtml(reclamo.getAsunto()),
                conSaltosHtml(reclamo.getDetalle()),
                conSaltosHtml(reclamo.getPedido())
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarConfirmacionReclamo(String destinatario, Reclamo reclamo) {
        String asunto = "Confirmación de registro - " + reclamo.getCodigoReclamo();

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Recibimos tu solicitud</h2>

                    <p>Hola, <strong>%s</strong>.</p>

                    <p>
                        Tu %s fue registrado correctamente en el Libro de Reclamaciones de PeruTalent.
                    </p>

                    <div style="padding: 16px 20px; background: #f1f5f9; border-radius: 12px; display: inline-block;">
                        <span style="color: #475569;">Código de seguimiento</span><br>
                        <strong style="font-size: 22px; color: #0f172a;">%s</strong>
                    </div>

                    <p>
                        Conserva este código como constancia. El registro quedó con estado
                        <strong>PENDIENTE</strong> para revisión del equipo.
                    </p>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(reclamo.getNombreCompleto()),
                escaparHtml(textoTipoSolicitud(reclamo.getTipoSolicitud()).toLowerCase()),
                escaparHtml(reclamo.getCodigoReclamo())
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarReporteOfertaAlEquipo(String destinatario, ReporteOferta reporte) {
        String tituloOferta = reporte.getOferta().getTitulo();
        String empresa = reporte.getOferta().getIdEmpleador() != null
                ? reporte.getOferta().getIdEmpleador().getNombreComercial()
                : "No disponible";

        String asunto = "Nueva oferta reportada - ID " + reporte.getOferta().getId();

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Nueva oferta reportada</h2>

                    <p><strong>ID del reporte:</strong> %s</p>
                    <p><strong>Estado:</strong> %s</p>
                    <p><strong>Fecha:</strong> %s</p>

                    <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 20px 0;">

                    <p><strong>ID de la oferta:</strong> %s</p>
                    <p><strong>Título:</strong> %s</p>
                    <p><strong>Empleador:</strong> %s</p>
                    <p><strong>Usuario reportante:</strong> %s</p>
                    <p><strong>Motivo:</strong> %s</p>
                    <p><strong>Descripción:</strong><br>%s</p>

                    <p style="color: #6b7280; margin-top: 24px;">
                        El empleador no fue notificado. El reporte debe ser revisado por el equipo de PeruTalent.
                    </p>
                </div>
                """.formatted(
                reporte.getId(),
                escaparHtml(reporte.getEstado()),
                escaparHtml(String.valueOf(reporte.getFechaCreacion())),
                reporte.getOferta().getId(),
                escaparHtml(tituloOferta),
                escaparHtml(empresa),
                escaparHtml(reporte.getUsuarioReportante().getEmail()),
                escaparHtml(textoMotivoReporte(reporte.getMotivo())),
                conSaltosHtml(reporte.getDescripcion())
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarConfirmacionReporteOferta(String destinatario, ReporteOferta reporte) {
        String asunto = "Confirmación de reporte de oferta";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Recibimos tu reporte</h2>

                    <p>
                        Registramos tu reporte sobre la oferta <strong>%s</strong>.
                    </p>

                    <p>
                        El equipo de PeruTalent revisará la información. El empleador no recibe tus datos
                        ni una notificación automática por este reporte.
                    </p>

                    <p><strong>Número de reporte:</strong> %s</p>
                    <p><strong>Estado:</strong> PENDIENTE</p>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(reporte.getOferta().getTitulo()),
                reporte.getId()
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarProblemaTecnicoAlEquipo(
            String destinatario,
            ReporteProblemaTecnico reporte
    ) {
        String asunto = "Nuevo problema técnico - " + reporte.getCodigoReporte();

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Nuevo problema técnico reportado</h2>

                    <p><strong>Código:</strong> %s</p>
                    <p><strong>Estado:</strong> %s</p>
                    <p><strong>Fecha:</strong> %s</p>

                    <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 20px 0;">

                    <p><strong>Nombre:</strong> %s</p>
                    <p><strong>Correo:</strong> %s</p>
                    <p><strong>Tipo de problema:</strong> %s</p>
                    <p><strong>Pantalla:</strong> %s</p>
                    <p><strong>Descripción:</strong><br>%s</p>
                    <p><strong>Pasos realizados:</strong><br>%s</p>
                    <p><strong>Información adicional:</strong><br>%s</p>

                    <p style="color: #6b7280; margin-top: 24px;">
                        El reporte quedó guardado en la base de datos con estado PENDIENTE.
                    </p>
                </div>
                """.formatted(
                escaparHtml(reporte.getCodigoReporte()),
                escaparHtml(reporte.getEstado()),
                escaparHtml(String.valueOf(reporte.getFechaCreacion())),
                escaparHtml(reporte.getNombreCompleto()),
                escaparHtml(reporte.getEmail()),
                escaparHtml(textoTipoProblema(reporte.getTipoProblema())),
                escaparHtml(reporte.getPantalla()),
                conSaltosHtml(reporte.getDescripcion()),
                conSaltosHtml(valorOpcional(reporte.getPasosReproducir())),
                conSaltosHtml(valorOpcional(reporte.getInformacionAdicional()))
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    public void enviarConfirmacionProblemaTecnico(
            String destinatario,
            ReporteProblemaTecnico reporte
    ) {
        String asunto = "Confirmación de problema técnico - " + reporte.getCodigoReporte();

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827; line-height: 1.6;">
                    <h2>Recibimos tu reporte</h2>

                    <p>Hola, <strong>%s</strong>.</p>

                    <p>
                        Registramos el problema técnico que encontraste en PeruTalent.
                        El equipo podrá revisarlo usando el siguiente código:
                    </p>

                    <div style="padding: 16px 20px; background: #f1f5f9; border-radius: 12px; display: inline-block;">
                        <span style="color: #475569;">Código de seguimiento</span><br>
                        <strong style="font-size: 22px; color: #0f172a;">%s</strong>
                    </div>

                    <p><strong>Estado inicial:</strong> PENDIENTE</p>

                    <p style="color: #6b7280;">
                        Atentamente,<br>
                        <strong>Equipo PeruTalent</strong>
                    </p>
                </div>
                """.formatted(
                escaparHtml(reporte.getNombreCompleto()),
                escaparHtml(reporte.getCodigoReporte())
        );

        enviarCorreo(destinatario, asunto, contenidoHtml);
    }

    private String textoTipoProblema(String tipo) {
        if (tipo == null) {
            return "Otro";
        }

        return switch (tipo) {
            case "ACCESO_CUENTA" -> "Acceso o cuenta";
            case "OFERTAS" -> "Ofertas laborales";
            case "POSTULACIONES" -> "Postulaciones";
            case "PERFIL" -> "Perfil";
            case "PAGOS_SUSCRIPCION" -> "Pagos o suscripción";
            case "ARCHIVOS_CV" -> "Archivos o CV";
            default -> "Otro";
        };
    }

    private String textoTipoSolicitud(String tipo) {
        return "QUEJA".equalsIgnoreCase(tipo) ? "Queja" : "Reclamo";
    }

    private String textoMotivoReporte(String motivo) {
        if (motivo == null) {
            return "Otro";
        }

        return switch (motivo) {
            case "POSIBLE_ESTAFA" -> "Posible estafa";
            case "INFORMACION_FALSA" -> "Información falsa";
            case "CONTENIDO_INAPROPIADO" -> "Contenido inapropiado";
            case "OFERTA_DUPLICADA" -> "Oferta duplicada";
            case "DATOS_CONTACTO_SOSPECHOSOS" -> "Datos de contacto sospechosos";
            case "DISCRIMINACION" -> "Discriminación";
            default -> "Otro";
        };
    }

    private String valorOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor : "No indicado";
    }

    private String formatearMonto(BigDecimal monto) {
        return monto.stripTrailingZeros().toPlainString();
    }

    private String conSaltosHtml(String valor) {
        return escaparHtml(valor).replace("\n", "<br>");
    }

    private void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        if (!StringUtils.hasText(apiKey)) {
            System.out.println("SENDGRID_API_KEY no configurado. No se envió el correo.");
            return;
        }

        if (!StringUtils.hasText(fromEmail)) {
            System.out.println("SENDGRID_FROM_EMAIL no configurado. No se envió el correo.");
            return;
        }

        if (!StringUtils.hasText(destinatario)) {
            System.out.println("Destinatario vacío. No se envió el correo.");
            return;
        }

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(destinatario);
        Content content = new Content("text/html", contenidoHtml);

        Mail mail = new Mail(from, asunto, to, content);

        SendGrid sg = new SendGrid(apiKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Correo enviado correctamente a: " + destinatario);
                System.out.println("SendGrid status: " + response.getStatusCode());
            } else {
                System.out.println("SendGrid no pudo enviar el correo.");
                System.out.println("Status: " + response.getStatusCode());
                System.out.println("Body: " + response.getBody());
            }

        } catch (IOException e) {
            System.out.println("Error enviando correo con SendGrid: " + e.getMessage());
        }
    }

    private String escaparHtml(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}