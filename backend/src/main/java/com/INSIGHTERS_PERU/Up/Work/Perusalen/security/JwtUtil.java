package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.INSIGHTERS_PERU.Up.Work.Perusalen.exception.UnauthorizedException;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Convierte la clave secreta en una Key válida para JWT
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generar token JWT
     */
    public String generateToken(String email, Long id, String rol) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        List<String> roles = new ArrayList<>();

        if ("EMPLEADO".equals(rol)) {
            roles.add("ROLE_EMPLEADO");
        }

        if ("EMPLEADOR".equals(rol)) {
            roles.add("ROLE_EMPLEADOR");
        }

        return Jwts.builder()
                .setSubject(email)
                .claim("id", id)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtener email desde token
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Obtener ID desde token
     */
    public Long getIdFromToken(String token) {
        return ((Number) getClaims(token).get("id")).longValue();
    }

    /**
     * Obtener roles como lista de Strings
     */
    public List<String> getRolesFromToken(String token) {

        Object rolesObject = getClaims(token).get("roles");

        if (rolesObject instanceof List<?> rolesList) {
            return rolesList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }

    /**
     * Obtener roles como GrantedAuthority
     */
    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {

        List<String> roles = getRolesFromToken(token);

        if (roles == null || roles.isEmpty()) {
            throw new IllegalStateException("El token no contiene roles");
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    /**
     * Obtener ID directamente desde request HTTP
     */
    public Long getIdFromRequest(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token no proporcionado");
        }

        String token = authHeader.substring(7);

        return getIdFromToken(token);
    }

    /**
     * Validar token
     */
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Método reutilizable para obtener claims
     */
    private Claims getClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}