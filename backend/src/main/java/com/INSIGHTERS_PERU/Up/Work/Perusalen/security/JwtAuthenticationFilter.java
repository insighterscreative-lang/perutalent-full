package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        return ("GET".equalsIgnoreCase(method) && path.equals("/actuator/health"))
                || ("POST".equalsIgnoreCase(method) && path.equals("/reclamos"))
                || ("POST".equalsIgnoreCase(method) && path.equals("/reportes-problemas"))
                || path.equals("/usuarios/registro")
                || path.equals("/usuarios/login")
                || path.equals("/usuarios/password/solicitar-codigo")
                || path.equals("/usuarios/password/verificar-codigo")
                || path.equals("/usuarios/password/restablecer")

                || ("GET".equalsIgnoreCase(method) && path.startsWith("/catalogos/"))

                || ("GET".equalsIgnoreCase(method) && path.equals("/filtros/ofertas"))
                || ("GET".equalsIgnoreCase(method) && path.startsWith("/filtros/ofertas/"))

                || ("GET".equalsIgnoreCase(method) && path.equals("/suscripciones/planes"))

                || ("GET".equalsIgnoreCase(method) && path.matches("^/empleados/perfil-publico/\\d+/foto$"))
                || ("GET".equalsIgnoreCase(method) && path.matches("^/empleadores/perfil-publico/\\d+/logo$"))

                || ("GET".equalsIgnoreCase(method) && path.equals("/pagos/culqi/config"))

                || ("POST".equalsIgnoreCase(method) && path.equals("/pagos/culqi/webhook"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);

        try {
            email = jwtUtil.getEmailFromToken(token);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            chain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    List<SimpleGrantedAuthority> authorities =
                            jwtUtil.getAuthoritiesFromToken(token);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}