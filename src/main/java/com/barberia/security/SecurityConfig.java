package com.barberia.security;

import com.barberia.config.CorsConfig;
import com.barberia.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CorsConfig corsConfig;
    private final ObjectMapper objectMapper;

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/api/public/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, e) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error("No autorizado")
                                .message("Se requiere autenticación para acceder a este recurso")
                                .path(request.getRequestURI())
                                .build();
                        response.getWriter().write(objectMapper.writeValueAsString(error));
                    })
                    .accessDeniedHandler((request, response, e) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        ErrorResponse error = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("Prohibido")
                                .message("No tiene permisos para realizar esta acción")
                                .path(request.getRequestURI())
                                .build();
                        response.getWriter().write(objectMapper.writeValueAsString(error));
                    })
            );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
