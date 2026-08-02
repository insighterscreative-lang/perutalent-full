package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CulqiService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

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
        validarConfiguracionSecreta();
        validarToken(tokenId);

        if (montoCentimos == null || montoCentimos <= 0) {
            throw new RuntimeException("El monto del cargo no es válido.");
        }

        if (email == null || email.isBlank()) {
            throw new RuntimeException("El correo del usuario es obligatorio para Culqi.");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("amount", montoCentimos);
        body.put("currency_code", moneda);

        String emailCargo = culqiSecretKey.startsWith("sk_test")
                ? "review@culqi.com"
                : email;

        body.put("email", emailCargo);
        body.put("source_id", tokenId);
        body.put("description", descripcion);

        return post("/charges", body, "No se pudo procesar el pago con Culqi.");
    }

    public Map<String, Object> crearCliente(Long idUsuario, String email) {
        validarConfiguracionSecreta();

        if (email == null || email.isBlank()) {
            throw new RuntimeException("El correo del usuario es obligatorio para crear el cliente en Culqi.");
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("id_usuario", idUsuario);
        metadata.put("origen", "perutalent");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("first_name", obtenerNombreDesdeEmail(email));
        body.put("last_name", "PeruTalent");
        body.put("email", email);
        body.put("address", "Lima");
        body.put("address_city", "Lima");
        body.put("country_code", "PE");
        body.put("phone_number", "999999999");
        body.put("metadata", metadata);

        return post("/customers", body, "No se pudo crear el cliente en Culqi.");
    }

    public Map<String, Object> crearTarjeta(String customerId, String tokenId) {
        validarConfiguracionSecreta();
        validarToken(tokenId);

        if (customerId == null || customerId.isBlank()) {
            throw new RuntimeException("No se recibió el ID del cliente de Culqi.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customer_id", customerId);
        body.put("token_id", tokenId);

        return post("/cards", body, "No se pudo crear la tarjeta en Culqi.");
    }

    public Map<String, Object> crearSuscripcion(
            String cardId,
            String planId,
            Long idUsuario,
            Long idPlanLocal
    ) {
        validarConfiguracionSecreta();

        if (cardId == null || cardId.isBlank()) {
            throw new RuntimeException("No se recibió el ID de tarjeta de Culqi.");
        }

        if (planId == null || planId.isBlank()) {
            throw new RuntimeException("No se configuró el ID del plan recurrente de Culqi.");
        }

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("id_usuario", idUsuario);
        metadata.put("id_plan_local", idPlanLocal);
        metadata.put("origen", "perutalent");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("card_id", cardId);
        body.put("plan_id", planId);
        body.put("tyc", true);
        body.put("metadata", metadata);

        return post("/recurrent/subscriptions/create", body, "No se pudo crear la suscripción recurrente en Culqi.");
    }

    public Map<String, Object> cancelarSuscripcion(String culqiSubscriptionId) {
        validarConfiguracionSecreta();

        if (culqiSubscriptionId == null || culqiSubscriptionId.isBlank()) {
            throw new RuntimeException("No se recibió el ID de suscripción de Culqi.");
        }

        return delete(
                "/recurrent/subscriptions/" + culqiSubscriptionId,
                "No se pudo cancelar la suscripción en Culqi."
        );
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

    public String obtenerId(Map<String, Object> respuesta) {
        Object id = respuesta != null ? respuesta.get("id") : null;
        return id != null ? id.toString() : null;
    }

    public String obtenerChargeId(Map<String, Object> respuesta) {
        String directo = buscarTextoPorClave(respuesta, "charge_id");
        if (directo != null && directo.startsWith("chr_")) {
            return directo;
        }

        String id = obtenerId(respuesta);
        return id != null && id.startsWith("chr_") ? id : buscarIdPorPrefijo(respuesta, "chr_");
    }

    public String obtenerCustomerId(Map<String, Object> respuesta) {
        String directo = buscarTextoPorClave(respuesta, "customer_id");
        if (directo != null && directo.startsWith("cus_")) {
            return directo;
        }

        String id = obtenerId(respuesta);
        return id != null && id.startsWith("cus_") ? id : buscarIdPorPrefijo(respuesta, "cus_");
    }

    public String obtenerCardId(Map<String, Object> respuesta) {
        String directo = buscarTextoPorClave(respuesta, "card_id");
        if (directo != null && directo.startsWith("crd_")) {
            return directo;
        }

        String id = obtenerId(respuesta);
        return id != null && id.startsWith("crd_") ? id : buscarIdPorPrefijo(respuesta, "crd_");
    }

    public String obtenerSubscriptionId(Map<String, Object> respuesta) {
        String directo = buscarTextoPorClave(respuesta, "subscription_id");
        if (directo != null && directo.startsWith("sxn_")) {
            return directo;
        }

        String id = obtenerId(respuesta);
        return id != null && id.startsWith("sxn_") ? id : buscarIdPorPrefijo(respuesta, "sxn_");
    }

    public Integer obtenerStatusSuscripcion(Map<String, Object> respuesta) {
        Object status = buscarObjetoPorClave(respuesta, "status");
        if (status instanceof Number numero) {
            return numero.intValue();
        }

        if (status != null) {
            try {
                return Integer.parseInt(status.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    public LocalDate obtenerFechaLocalDesdeTimestamp(Map<String, Object> respuesta, String nombreCampo) {
        Object valor = buscarObjetoPorClave(respuesta, nombreCampo);
        return convertirTimestampALocalDate(valor);
    }

    public LocalDateTime obtenerFechaHoraLocalDesdeTimestamp(Map<String, Object> respuesta, String nombreCampo) {
        Object valor = buscarObjetoPorClave(respuesta, nombreCampo);
        return convertirTimestampALocalDateTime(valor);
    }

    public String convertirRespuestaAJson(Map<String, Object> respuesta) {
        try {
            return objectMapper.writeValueAsString(respuesta);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public Map<String, Object> convertirJsonAMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("raw_error", json == null ? "" : json);
        }
    }

    public String buscarTextoPorClave(Map<String, Object> map, String clave) {
        Object valor = buscarObjetoPorClave(map, clave);
        return valor != null ? valor.toString() : null;
    }

    public Object buscarObjetoPorClave(Map<String, Object> map, String clave) {
        if (map == null || clave == null) {
            return null;
        }

        if (map.containsKey(clave)) {
            return map.get(clave);
        }

        for (Object value : map.values()) {
            if (value instanceof Map<?, ?> nested) {
                Object encontrado = buscarObjetoPorClave(convertirMapa(nested), clave);
                if (encontrado != null) {
                    return encontrado;
                }
            }

            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item instanceof Map<?, ?> nestedItem) {
                        Object encontrado = buscarObjetoPorClave(convertirMapa(nestedItem), clave);
                        if (encontrado != null) {
                            return encontrado;
                        }
                    }
                }
            }
        }

        return null;
    }

    public String buscarIdPorPrefijo(Map<String, Object> map, String prefijo) {
        if (map == null || prefijo == null) {
            return null;
        }

        for (Object value : map.values()) {
            if (value != null && value.toString().startsWith(prefijo)) {
                return value.toString();
            }

            if (value instanceof Map<?, ?> nested) {
                String encontrado = buscarIdPorPrefijo(convertirMapa(nested), prefijo);
                if (encontrado != null) {
                    return encontrado;
                }
            }

            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item instanceof Map<?, ?> nestedItem) {
                        String encontrado = buscarIdPorPrefijo(convertirMapa(nestedItem), prefijo);
                        if (encontrado != null) {
                            return encontrado;
                        }
                    } else if (item != null && item.toString().startsWith(prefijo)) {
                        return item.toString();
                    }
                }
            }
        }

        return null;
    }

    private Map<String, Object> post(String path, Map<String, Object> body, String mensajeErrorGeneral) {
        String url = culqiApiUrl + path;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, crearHeaders());

        try {
            System.out.println("===== CULQI REQUEST POST " + path + " =====");
            System.out.println(convertirRespuestaAJson(body));

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            System.out.println("===== CULQI RESPONSE OK " + path + " =====");
            System.out.println(convertirRespuestaAJson(response.getBody()));

            return response.getBody();

        } catch (HttpStatusCodeException e) {
            manejarErrorHttpCulqi(e);
            return Map.of();
        } catch (Exception e) {
            System.out.println("===== ERROR GENERAL CULQI " + path + " =====");
            e.printStackTrace();

            throw new RuntimeException(mensajeErrorGeneral + " " + e.getMessage());
        }
    }

    private Map<String, Object> delete(String path, String mensajeErrorGeneral) {
        String url = culqiApiUrl + path;
        HttpEntity<Void> request = new HttpEntity<>(crearHeaders());

        try {
            System.out.println("===== CULQI REQUEST DELETE " + path + " =====");

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Map.class);

            HttpStatusCode statusCode = response.getStatusCode();
            if (!statusCode.is2xxSuccessful()) {
                throw new RuntimeException("Culqi respondió con estado " + statusCode.value());
            }

            System.out.println("===== CULQI RESPONSE OK " + path + " =====");
            System.out.println(convertirRespuestaAJson(response.getBody()));

            return response.getBody();

        } catch (HttpStatusCodeException e) {
            manejarErrorHttpCulqi(e);
            return Map.of();
        } catch (Exception e) {
            System.out.println("===== ERROR GENERAL CULQI " + path + " =====");
            e.printStackTrace();

            throw new RuntimeException(mensajeErrorGeneral + " " + e.getMessage());
        }
    }

    private void manejarErrorHttpCulqi(HttpStatusCodeException e) {
        String bodyError = e.getResponseBodyAsString();

        System.out.println("===== CULQI RESPONSE ERROR =====");
        System.out.println("HTTP STATUS: " + e.getStatusCode());
        System.out.println(bodyError);

        Map<String, Object> errorMap = convertirJsonAMap(bodyError);
        String mensaje = extraerMensajeCulqi(errorMap);

        throw new RuntimeException(mensaje != null ? mensaje : "Culqi rechazó la operación.");
    }

    private HttpHeaders crearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(culqiSecretKey);
        return headers;
    }

    private void validarConfiguracionSecreta() {
        if (culqiSecretKey == null || culqiSecretKey.isBlank()) {
            throw new RuntimeException("No se configuró CULQI_SECRET_KEY en el backend.");
        }
    }

    private void validarToken(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new RuntimeException("El token de Culqi es obligatorio.");
        }
    }

    private String obtenerNombreDesdeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Usuario";
        }

        String nombre = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", " ")
                .trim();

        if (nombre.isBlank()) {
            return "Usuario";
        }

        return nombre.length() > 40 ? nombre.substring(0, 40) : nombre;
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

    private LocalDate convertirTimestampALocalDate(Object valor) {
        LocalDateTime fechaHora = convertirTimestampALocalDateTime(valor);
        return fechaHora != null ? fechaHora.toLocalDate() : null;
    }

    private LocalDateTime convertirTimestampALocalDateTime(Object valor) {
        if (valor == null) {
            return null;
        }

        try {
            long timestamp = valor instanceof Number
                    ? ((Number) valor).longValue()
                    : Long.parseLong(valor.toString());

            if (timestamp <= 0) {
                return null;
            }

            Instant instant = timestamp > 9_999_999_999L
                    ? Instant.ofEpochMilli(timestamp)
                    : Instant.ofEpochSecond(timestamp);

            return LocalDateTime.ofInstant(instant, ZONA_PERU);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> convertirMapa(Map<?, ?> map) {
        Map<String, Object> convertido = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                convertido.put(entry.getKey().toString(), entry.getValue());
            }
        }
        return convertido;
    }
}
