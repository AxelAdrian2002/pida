package com.efectivale.centrocostos.config;

import com.efectivale.centrocostos.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/Login/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/solicitudes/**", "/pedidos/**").hasAnyRole("ADMIN", "CAPTURA", "AUTORIZADOR", "CONSULTA")
                .requestMatchers("/Administracion_Pedidos/**").hasAnyRole("ADMIN", "CAPTURA", "AUTORIZADOR")
                .requestMatchers("/credenciales/**", "/tarjetas/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/Administracion_Tarjetas/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/grupos/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/Administracion_de_grupos/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/empleados/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/Actualizar_Datos/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA")
                .requestMatchers("/Administracion_Login/**").hasAnyRole("ADMIN", "CAPTURA", "AUTORIZADOR", "CONSULTA")
                .requestMatchers("/bases/**").hasAnyRole("ADMIN", "CAPTURA", "CONSULTA", "AUTORIZADOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(
            Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList()
        );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
