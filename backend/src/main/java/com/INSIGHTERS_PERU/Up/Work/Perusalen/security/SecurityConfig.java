package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource
    ) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .requestMatchers("/usuarios/registro", "/usuarios/login").permitAll()

                .requestMatchers(HttpMethod.GET, "/catalogos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/filtros/ofertas").permitAll()
                .requestMatchers(HttpMethod.GET, "/filtros/ofertas/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/suscripciones/planes").permitAll()
                .requestMatchers("/suscripciones/**").authenticated()

                .requestMatchers(HttpMethod.GET, "/empleados/perfil-publico/*/foto").permitAll()
                .requestMatchers(HttpMethod.GET, "/empleadores/perfil-publico/*/logo").permitAll()
                .requestMatchers(HttpMethod.GET, "/empleados/perfil-publico/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/empleadores/perfil-publico/*").hasAuthority("ROLE_EMPLEADO")

                .requestMatchers(HttpMethod.POST, "/empleados/perfil").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/empleados/perfil").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.PUT, "/empleados/perfil").hasAuthority("ROLE_EMPLEADO")

                .requestMatchers(HttpMethod.POST, "/empleadores/perfil").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.GET, "/empleadores/perfil").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.PUT, "/empleadores/perfil").hasAuthority("ROLE_EMPLEADOR")

                .requestMatchers(HttpMethod.POST, "/ofertas-laborales").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.PUT, "/ofertas-laborales/*").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.PATCH, "/ofertas-laborales/*/finalizar").hasAuthority("ROLE_EMPLEADOR")

                .requestMatchers(HttpMethod.GET, "/ofertas-laborales").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/ofertas-laborales/filtrar").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/ofertas-laborales/para-ti").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/ofertas-laborales/*").authenticated()

                .requestMatchers(HttpMethod.GET, "/postulaciones/mis-ofertas").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.POST, "/postulaciones/ofertas/*").hasAuthority("ROLE_EMPLEADO")
                .requestMatchers(HttpMethod.GET, "/postulaciones/ofertas/*/postulantes").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.PUT, "/postulaciones/*/aceptar").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.PUT, "/postulaciones/*/rechazar").hasAuthority("ROLE_EMPLEADOR")
                .requestMatchers(HttpMethod.GET, "/postulaciones/*/cv").authenticated()

                .requestMatchers(HttpMethod.GET, "/pagos/culqi/config").permitAll()
                .requestMatchers(HttpMethod.POST, "/pagos/culqi/webhook").permitAll()
                .requestMatchers(HttpMethod.POST, "/pagos/culqi/premium").authenticated()
                .requestMatchers(HttpMethod.POST, "/pagos/culqi/premium/cancelar").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}