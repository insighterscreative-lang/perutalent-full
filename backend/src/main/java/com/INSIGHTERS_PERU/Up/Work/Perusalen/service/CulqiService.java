package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CulqiService {

    @Value("${CULQI_SECRET_KEY:}")
    private String culqiSecretKey;

    @Value("${CULQI_API_URL:https://api.culqi.com/v2}")
    private String culqiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CulqiService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> crearCargo(
            Integer montoCentimos,
            String moneda,
            String email,
            String tokenId,
            String descripcion
    ) {
        if (culqiSecretKey == null || culqiSecretKey.isBlank()) {
            throw new RuntimeException("No se configuró CULQI_SECRET_KEY en el backend.");
        }

        if (tokenId == null || tokenId.isBlank()) {
            throw new RuntimeException("El token de Culqi es obligatorio.");
        }

        if (montoCentimos == null || montoCentimos <= 0) {
            throw new RuntimeException("El monto del cargo no es válido.");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("El correo del usuario es obligatorio para Culqi.");
        }

        String url = culqiApiUrl + "/charges";

        Map<String, Object> body = new HashMap<>();
        body.put("amount", montoCentimos);
        body.put("currency_code", moneda);

        String emailCargo = culqiSecretKey.startsWith("sk_test")
                ? "review@culqi.com"
                : email;

        body.put("email", emailCargo);
        body.put("source_id", tokenId);
        body.put("description", descripcion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(culqiSecretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            System.out.println("===== CULQI REQUEST =====");
            System.out.println(convertirRespuestaAJson(body));

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            System.out.println("===== CULQI RESPONSE OK =====");
            System.out.println(convertirRespuestaAJson(response.getBody()));

            return response.getBody();

        } catch (HttpStatusCodeException e) {
            String bodyError = e.getResponseBodyAsString();

            System.out.println("===== CULQI RESPONSE ERROR =====");
            System.out.println("HTTP STATUS: " + e.getStatusCode());
            System.out.println(bodyError);

            Map<String, Object> errorMap = convertirJsonAMap(bodyError);
            String mensaje = extraerMensajeCulqi(errorMap);

            throw new RuntimeException(mensaje != null ? mensaje : "Culqi rechazó el pago.");

        } catch (Exception e) {
            System.out.println("===== ERROR GENERAL CULQI =====");
            e.printStackTrace();

            throw new RuntimeException("No se pudo procesar el pago con Culqi: " + e.getMessage());
        }
    }

    public boolean cargoAprobado(Map<String, Object> respuesta) {
        if (respuesta == null) {
            return false;
        }

        Object object = respuesta.get("object");
        Object id = respuesta.get("id");

        boolean esCargo = object != null
                && object.toString().equalsIgnoreCase("charge");

        boolean tieneId = id != null
                && id.toString().startsWith("chr_");

        boolean aprobadoPorOutcome = false;

        Object outcomeObject = respuesta.get("outcome");
        if (outcomeObject instanceof Map<?, ?> outcome) {
            Object type = outcome.get("type");
            Object code = outcome.get("code");

            boolean ventaExitosa = type != null
                    && type.toString().equalsIgnoreCase("venta_exitosa");

            boolean autorizado = code != null
                    && code.toString().equalsIgnoreCase("AUT0000");

            aprobadoPorOutcome = ventaExitosa || autorizado;
        }

        Object responseCode = respuesta.get("response_code");
        Object state = respuesta.get("state");

        boolean aprobadoPorResponseCode = responseCode != null
                && responseCode.toString().equalsIgnoreCase("venta_exitosa");

        boolean aprobadoPorState = state != null
                && state.toString().equalsIgnoreCase("Exitosa");

        return esCargo && tieneId && (
                aprobadoPorOutcome ||
                aprobadoPorResponseCode ||
                aprobadoPorState
        );
    }

    public String obtenerChargeId(Map<String, Object> respuesta) {
        Object id = respuesta != null ? respuesta.get("id") : null;
        return id != null ? id.toString() : null;
    }

    public String convertirRespuestaAJson(Map<String, Object> respuesta) {
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> convertirJsonAMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("raw_error", json);
        }
    }

    private String extraerMensajeCulqi(Map<String, Object> errorMap) {
        if (errorMap == null) {
            return null;
        }

        Object userMessage = errorMap.get("user_message");
        if (userMessage != null) {
            return userMessage.toString();
        }

        Object merchantMessage = errorMap.get("merchant_message");
        if (merchantMessage != null) {
            return merchantMessage.toString();
        }

        Object message = errorMap.get("message");
        if (message != null) {
            return message.toString();
        }

        Object rawError = errorMap.get("raw_error");
        if (rawError != null) {
            return rawError.toString();
        }

        return null;
    }
}