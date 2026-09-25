package com.javalive.backend.config;

import com.javalive.backend.security.JwtAuthenticationFilter;
import com.javalive.backend.security.ipblock.IpBlacklistFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final IpBlacklistFilter ipBlacklistFilter;

    @Value("${javalive.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, IpBlacklistFilter ipBlacklistFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.ipBlacklistFilter = ipBlacklistFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public: marketing site content, the actual login/register/2FA-submit steps, actuator health.
                // Note "/me" endpoints are deliberately NOT in this list — they require a real token.
                .requestMatchers("/api/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/2fa/verify",
                        "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/admin/auth/login", "/api/admin/auth/2fa",
                        "/api/admin/auth/forgot-password", "/api/admin/auth/reset-password").permitAll()
                // Admin guard: everything else under /api/admin/**, including /api/admin/auth/me.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // User guard: everything else under /api/**, including /api/auth/me.
                .requestMatchers("/api/**").hasRole("USER")
                .anyRequest().permitAll()
            )
            // Without this, Spring Security's default entry point (Http403ForbiddenEntryPoint) returns
            // 403 for a missing/invalid/expired token too, indistinguishable from a real role mismatch —
            // the frontend needs 401 specifically to tell "your session is gone, log in again" apart from
            // "you're logged in but not allowed here", so it can auto-redirect only on the former.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(HttpServletResponse.SC_FORBIDDEN))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(ipBlacklistFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
