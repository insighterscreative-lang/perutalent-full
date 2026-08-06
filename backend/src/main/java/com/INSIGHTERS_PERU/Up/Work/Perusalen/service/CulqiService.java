package com.INSIGHTERS_PERU.Up.Work.Perusalen.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CulqiService {

    private static final Logger log = LoggerFactory.getLogger(CulqiService.class);
    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");

    private static final Set<String> CAMPOS_SENSIBLES = Set.of(
            "token_id",
            "source_id",
            "card_number",
            "cardnumber",
            "cvv",
            "cvv2",
            "email",
            "phone_number",
            "first_name",
            "last_name",
            "address",
            "address_city"
    );

    @Value("${culqi.secret-key:}")
    private String culqiSecretKey;

    @Value("${culqi.api-url:https://api.culqi.com/v2}")
    private String culqiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CulqiService(ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(15_000);
        requestFactory.setReadTimeout(35_000);

        this.restTemplate = new RestTemplate(requestFactory);
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> crearCliente(
            Long idUsuario,
            String email,
            String firstName,
            String lastName,
            String address,
            String addressCity,
            String countryCode,
            String phoneNumber
    ) {
        validarConfiguracionSecreta();

        String correoNormalizado = validarYNormalizarEmail(email);
        String nombresNormalizados = validarYNormalizarNombre(firstName, "nombres");
        String apellidosNormalizados = validarYNormalizarNombre(lastName, "apellidos");
        String direccionNormalizada = validarYNormalizarLongitud(
                address,
                5,
                100,
                "La dirección"
        );
        String ciudadNormalizada = validarYNormalizarLongitud(
                addressCity,
                2,
                30,
                "La ciudad"
        );
        String codigoPaisNormalizado = validarYNormalizarCodigoPais(countryCode);
        String telefonoNormalizado = validarYNormalizarTelefono(phoneNumber);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("id_usuario", idUsuario);
        metadata.put("origen", "perutalent");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("first_name", nombresNormalizados);
        body.put("last_name", apellidosNormalizados);
        body.put("email", correoNormalizado);
        body.put("address", direccionNormalizada);
        body.put("address_city", ciudadNormalizada);
        body.put("country_code", codigoPaisNormalizado);
        body.put("phone_number", telefonoNormalizado);
        body.put("metadata", metadata);

        Map<String, Object> respuesta = post(
                "/customers",
                body,
                "No se pudo crear el cliente en Culqi."
        );

        String customerId = obtenerCustomerId(respuesta);
        validarIdAmbiente(customerId, "cus_", "cliente");
        return respuesta;
    }

    public Map<String, Object> crearTarjeta(String customerId, String tokenId) {
        validarConfiguracionSecreta();
        validarToken(tokenId);
        validarIdAmbiente(customerId, "cus_", "cliente");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customer_id", customerId);
        body.put("token_id", tokenId);

        Map<String, Object> respuesta = post(
                "/cards",
                body,
                "No se pudo crear la tarjeta en Culqi."
        );

        String cardId = obtenerCardId(respuesta);
        validarIdAmbiente(cardId, "crd_", "tarjeta");
        return respuesta;
    }

    public Map<String, Object> crearSuscripcion(
            String cardId,
            String planId,
            Long idUsuario,
            Long idPlanLocal
    ) {
        validarConfiguracionSecreta();
        validarIdAmbiente(cardId, "crd_", "tarjeta");
        validarPlanId(planId);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("id_usuario", idUsuario);
        metadata.put("id_plan_local", idPlanLocal);
        metadata.put("origen", "perutalent");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("card_id", cardId);
        body.put("plan_id", planId);
        body.put("tyc", true);
        body.put("metadata", metadata);

        return post(
                "/recurrent/subscriptions/create",
                body,
                "No se pudo crear la suscripción recurrente en Culqi."
        );
    }

    public Map<String, Object> cancelarSuscripcion(String culqiSubscriptionId) {
        validarConfiguracionSecreta();
        validarIdAmbiente(culqiSubscriptionId, "sxn_", "suscripción");

        return delete(
                "/recurrent/subscriptions/" + culqiSubscriptionId,
                "No se pudo cancelar la suscripción en Culqi."
        );
    }

    public void validarAmbientePlanYToken(String planId, String tokenId) {
        validarConfiguracionSecreta();
        validarPlanId(planId);
        validarToken(tokenId);
    }

    public boolean suscripcionActivaYCorrespondeAlPlan(
            Map<String, Object> respuesta,
            String planIdEsperado
    ) {
        if (respuesta == null || respuesta.isEmpty()) {
            return false;
        }

        String subscriptionId = obtenerSubscriptionId(respuesta);
        if (subscriptionId == null || subscriptionId.isBlank()) {
            return false;
        }

        try {
            validarIdAmbiente(subscriptionId, "sxn_", "suscripción");
        } catch (RuntimeException ex) {
            return false;
        }

        String planIdRespuesta = obtenerPlanId(respuesta);
        if (planIdRespuesta == null || !planIdRespuesta.equals(planIdEsperado)) {
            return false;
        }

        Object status = buscarObjetoPorClave(respuesta, "status");
        if (status instanceof Number numero) {
            return numero.intValue() == 1 || numero.intValue() == 3;
        }

        if (status != null) {
            String estado = status.toString().trim().toLowerCase(Locale.ROOT);
            return estado.equals("active")
                    || estado.equals("activa")
                    || estado.equals("1");
        }

        Object state = buscarObjetoPorClave(respuesta, "state");
        if (state != null) {
            String estado = state.toString().trim().toLowerCase(Locale.ROOT);
            return estado.equals("active") || estado.equals("activa");
        }

        return false;
    }

    public String obtenerId(Map<String, Object> respuesta) {
        Object id = respuesta != null ? respuesta.get("id") : null;
        return id != null ? id.toString() : null;
    }

    public String obtenerTextoRaiz(Map<String, Object> map, String clave) {
        if (map == null || clave == null) {
            return null;
        }

        Object valor = map.get(clave);
        return valor == null ? null : valor.toString();
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

    public String obtenerPlanId(Map<String, Object> respuesta) {
        String directo = buscarTextoPorClave(respuesta, "plan_id");
        if (directo != null && directo.startsWith("pln_")) {
            return directo;
        }

        return buscarIdPorPrefijo(respuesta, "pln_");
    }

    public Long obtenerMetadataLong(Map<String, Object> respuesta, String clave) {
        if (respuesta == null || clave == null) {
            return null;
        }

        Object valor = buscarValorEnMetadata(respuesta, clave);
        if (valor == null) {
            return null;
        }

        try {
            return valor instanceof Number numero
                    ? numero.longValue()
                    : Long.parseLong(valor.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
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
            return objectMapper.writeValueAsString(sanitizarValor(respuesta));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertirJsonAMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("raw_error", json == null ? "" : json);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> convertirJsonAMapEstricto(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("El webhook de Culqi llegó sin contenido.");
        }

        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("El webhook de Culqi no contiene un JSON válido.");
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
            if (value instanceof String texto && texto.startsWith(prefijo)) {
                return texto;
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
                    } else if (item instanceof String texto && texto.startsWith(prefijo)) {
                        return texto;
                    }
                }
            }
        }

        return null;
    }

    private Map<String, Object> post(String path, Map<String, Object> body, String mensajeErrorGeneral) {
        String url = normalizarApiUrl() + path;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, crearHeaders());

        try {
            log.info("Enviando solicitud POST a Culqi: {}", path);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> respuesta = convertirMapaRespuesta(response.getBody());
            log.info("Culqi respondió correctamente en {} con objeto {} e id {}",
                    path,
                    respuesta.get("object"),
                    respuesta.get("id"));
            return respuesta;
        } catch (HttpStatusCodeException e) {
            manejarErrorHttpCulqi(path, e);
            return Map.of();
        } catch (Exception e) {
            log.error("Error de comunicación con Culqi en {}: {}", path, e.getMessage());
            throw new RuntimeException(mensajeErrorGeneral + " Inténtalo nuevamente.");
        }
    }

    private Map<String, Object> delete(String path, String mensajeErrorGeneral) {
        String url = normalizarApiUrl() + path;
        HttpEntity<Void> request = new HttpEntity<>(crearHeaders());

        try {
            log.info("Enviando solicitud DELETE a Culqi: {}", path);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.DELETE, request, Map.class);

            HttpStatusCode statusCode = response.getStatusCode();
            if (!statusCode.is2xxSuccessful()) {
                throw new RuntimeException("Culqi respondió con estado " + statusCode.value());
            }

            Map<String, Object> respuesta = convertirMapaRespuesta(response.getBody());
            log.info("Culqi confirmó la cancelación de la suscripción solicitada.");
            return respuesta;
        } catch (HttpStatusCodeException e) {
            manejarErrorHttpCulqi(path, e);
            return Map.of();
        } catch (Exception e) {
            log.error("Error de comunicación con Culqi en {}: {}", path, e.getMessage());
            throw new RuntimeException(mensajeErrorGeneral + " Inténtalo nuevamente.");
        }
    }

    private void manejarErrorHttpCulqi(String path, HttpStatusCodeException e) {
        Map<String, Object> errorMap = convertirJsonAMap(e.getResponseBodyAsString());
        String mensaje = extraerMensajeCulqi(errorMap);

        log.warn("Culqi rechazó {} con HTTP {}: {}",
                path,
                e.getStatusCode().value(),
                mensaje == null ? "sin detalle público" : mensaje);

        throw new RuntimeException(mensaje != null ? mensaje : "Culqi rechazó la operación.");
    }

    private HttpHeaders crearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(culqiSecretKey.trim());
        return headers;
    }

    private void validarConfiguracionSecreta() {
        if (culqiSecretKey == null || culqiSecretKey.isBlank()) {
            throw new RuntimeException("No se configuró la llave privada de Culqi en el backend.");
        }

        String key = culqiSecretKey.trim();
        if (!key.startsWith("sk_test_") && !key.startsWith("sk_live_")) {
            throw new RuntimeException("La llave privada de Culqi no tiene un formato válido.");
        }
    }

    private void validarToken(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new RuntimeException("El token de Culqi es obligatorio.");
        }

        String token = tokenId.trim();
        if (!token.startsWith("tkn_test_") && !token.startsWith("tkn_live_")) {
            throw new RuntimeException("El token de Culqi no tiene un formato válido.");
        }

        validarMismoAmbiente(token, "token");
    }

    private void validarPlanId(String planId) {
        if (planId == null || planId.isBlank()) {
            throw new RuntimeException("No se configuró el ID del plan recurrente de Culqi.");
        }

        String plan = planId.trim();
        if (!plan.startsWith("pln_test_") && !plan.startsWith("pln_live_")) {
            throw new RuntimeException("El ID del plan de Culqi no tiene un formato válido.");
        }

        validarMismoAmbiente(plan, "plan");
    }

    private void validarIdAmbiente(String id, String prefijoBase, String tipo) {
        if (id == null || id.isBlank()) {
            throw new RuntimeException("Culqi no devolvió el ID de " + tipo + ".");
        }

        if (!id.startsWith(prefijoBase + "test_") && !id.startsWith(prefijoBase + "live_")) {
            throw new RuntimeException("El ID de " + tipo + " devuelto por Culqi no tiene un formato válido.");
        }

        validarMismoAmbiente(id, tipo);
    }

    private void validarMismoAmbiente(String id, String tipo) {
        boolean llaveLive = culqiSecretKey != null && culqiSecretKey.trim().startsWith("sk_live_");
        boolean idLive = id.contains("_live_");
        boolean idTest = id.contains("_test_");

        if ((llaveLive && !idLive) || (!llaveLive && !idTest)) {
            throw new RuntimeException(
                    "La llave privada y el " + tipo + " de Culqi pertenecen a ambientes diferentes."
            );
        }
    }


    private Object buscarValorEnMetadata(Object nodo, String clave) {
        if (nodo instanceof Map<?, ?> map) {
            Object metadataObject = map.get("metadata");
            if (metadataObject instanceof Map<?, ?> metadata && metadata.containsKey(clave)) {
                return metadata.get(clave);
            }

            for (Object value : map.values()) {
                Object encontrado = buscarValorEnMetadata(value, clave);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }

        if (nodo instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                Object encontrado = buscarValorEnMetadata(item, clave);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }

        return null;
    }

    private String normalizarApiUrl() {
        String apiUrl = culqiApiUrl == null || culqiApiUrl.isBlank()
                ? "https://api.culqi.com/v2"
                : culqiApiUrl.trim();

        return apiUrl.endsWith("/")
                ? apiUrl.substring(0, apiUrl.length() - 1)
                : apiUrl;
    }

    private String validarYNormalizarEmail(String email) {
        String valor = normalizarEspacios(email);

        if (valor.length() < 5
                || valor.length() > 50
                || !valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException(
                    "El correo de la cuenta debe tener un formato válido y entre 5 y 50 caracteres para Culqi."
            );
        }

        return valor;
    }

    private String validarYNormalizarNombre(String valor, String nombreCampo) {
        String normalizado = normalizarEspacios(valor);

        if (!normalizado.matches("^[\\p{L} ]{2,49}$")) {
            throw new RuntimeException(
                    "Los " + nombreCampo + " deben tener entre 2 y 49 caracteres y contener solamente letras y espacios."
            );
        }

        return normalizado;
    }

    private String validarYNormalizarLongitud(
            String valor,
            int minimo,
            int maximo,
            String nombreCampo
    ) {
        String normalizado = normalizarEspacios(valor);

        if (normalizado.length() < minimo || normalizado.length() > maximo) {
            throw new RuntimeException(
                    nombreCampo + " debe tener entre " + minimo + " y " + maximo + " caracteres."
            );
        }

        return normalizado;
    }

    private String validarYNormalizarCodigoPais(String countryCode) {
        String normalizado = normalizarEspacios(countryCode).toUpperCase(Locale.ROOT);

        if (!"PE".equals(normalizado)) {
            throw new RuntimeException(
                    "El código de país debe ser PE para pagos realizados desde PeruTalent."
            );
        }

        return normalizado;
    }

    private String validarYNormalizarTelefono(String phoneNumber) {
        String normalizado = phoneNumber == null
                ? ""
                : phoneNumber.replaceAll("\\D", "");

        if (!normalizado.matches("^\\d{5,15}$")) {
            throw new RuntimeException(
                    "El teléfono debe contener entre 5 y 15 dígitos."
            );
        }

        return normalizado;
    }

    private String normalizarEspacios(String valor) {
        return valor == null
                ? ""
                : valor.trim().replaceAll("\\s+", " ");
    }

    private String extraerMensajeCulqi(Map<String, Object> errorMap) {
        if (errorMap == null) {
            return null;
        }

        for (String key : List.of("merchant_message", "user_message", "message")) {
            Object value = errorMap.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
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

    private Object sanitizarValor(Object valor) {
        if (valor instanceof Map<?, ?> map) {
            Map<String, Object> sanitizado = new LinkedHashMap<>();
            map.forEach((key, value) -> {
                String nombre = String.valueOf(key);
                if (CAMPOS_SENSIBLES.contains(nombre.toLowerCase(Locale.ROOT))) {
                    sanitizado.put(nombre, "***");
                } else {
                    sanitizado.put(nombre, sanitizarValor(value));
                }
            });
            return sanitizado;
        }

        if (valor instanceof Iterable<?> iterable) {
            List<Object> sanitizado = new ArrayList<>();
            iterable.forEach(item -> sanitizado.add(sanitizarValor(item)));
            return sanitizado;
        }

        return valor;
    }

    private Map<String, Object> convertirMapa(Map<?, ?> original) {
        Map<String, Object> convertido = new LinkedHashMap<>();
        original.forEach((key, value) -> convertido.put(String.valueOf(key), value));
        return convertido;
    }

    private Map<String, Object> convertirMapaRespuesta(Map<?, ?> original) {
        return original == null ? new LinkedHashMap<>() : convertirMapa(original);
    }
}
