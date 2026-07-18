package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public void enviarCorreoPostulacionAceptada(
            String destinatario,
            String nombreEmpleado,
            String tituloOferta
    ) {
        String asunto = "Tu postulación fue aceptada";

        String contenidoHtml = """
                <div style="font-family: Arial, sans-serif; color: #111827;">
                    <h2>¡Buenas noticias, %s!</h2>

                    <p>
                        Tu postulación para la oferta <strong>%s</strong> fue aceptada.
                    </p>

                    <p>
                        El empleador revisó tu perfil y CV. Te recomendamos estar atento a tu correo
                        o teléfono para los siguientes pasos.
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