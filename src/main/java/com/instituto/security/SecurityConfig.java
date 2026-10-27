package com.instituto.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String ADMINISTRATIVO = "ADMINISTRATIVO";
    private static final String DOCENTE = "DOCENTE";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Respuestas JSON (y sin el cartel de login del navegador)
        AuthenticationEntryPoint noAutenticado =
                (req, res, ex) -> escribir(res, 401, "Credenciales inválidas o ausentes");
        AccessDeniedHandler sinPermiso =
                (req, res, ex) -> escribir(res, 403, "No tenés permiso para realizar esta operación");

        http
            .csrf(csrf -> csrf.disable())              // API sin sesiones ni cookies
            .cors(Customizer.withDefaults())           // usa la configuración de CorsConfig
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b -> b.authenticationEntryPoint(noAutenticado))
            .exceptionHandling(e -> e.authenticationEntryPoint(noAutenticado).accessDeniedHandler(sinPermiso))
            .authorizeHttpRequests(a -> a
                // El orden importa: gana la primera regla que coincide.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").authenticated()

                // Gestión de usuarios: solo el administrador
                .requestMatchers("/api/usuarios/**").hasRole(ADMIN)

                // Datos económicos de un alumno: administrador y administrativo
                .requestMatchers(HttpMethod.GET, "/api/alumnos/*/cuotas",
                                 "/api/alumnos/*/situacion-administrativa")
                    .hasAnyRole(ADMIN, ADMINISTRATIVO)

                // Alumnos: todos consultan; solo administrador/administrativo modifican
                .requestMatchers(HttpMethod.GET, "/api/alumnos/**").hasAnyRole(ADMIN, ADMINISTRATIVO, DOCENTE)
                .requestMatchers("/api/alumnos/**").hasAnyRole(ADMIN, ADMINISTRATIVO)

                // Inscripciones: todos consultan; administrador/administrativo registran
                .requestMatchers(HttpMethod.GET, "/api/inscripciones/**").hasAnyRole(ADMIN, ADMINISTRATIVO, DOCENTE)
                .requestMatchers("/api/inscripciones/**").hasAnyRole(ADMIN, ADMINISTRATIVO)

                // Matrículas, cuotas y pagos: administrador y administrativo
                .requestMatchers("/api/matriculas/**", "/api/cuotas/**", "/api/pagos/**")
                    .hasAnyRole(ADMIN, ADMINISTRATIVO)

                // Cualquier otra ruta nueva exige estar autenticado
                .anyRequest().authenticated());

        return http.build();
    }

    private static void escribir(HttpServletResponse res, int estado, String mensaje) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"estado\":" + estado + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
