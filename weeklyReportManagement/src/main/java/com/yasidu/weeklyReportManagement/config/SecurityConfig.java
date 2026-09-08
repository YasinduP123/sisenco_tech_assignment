package com.yasidu.weeklyReportManagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize on controller methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API — no session, no CSRF token needed
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no token required
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Manager-only endpoints (also enforced via @PreAuthorize at method level,
                        // this is a second layer of defense at the URL level)
                        .requestMatchers("/api/users/**").hasRole("MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/projects").hasRole("MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/projects/**").hasRole("MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/projects/**").hasRole("MANAGER")
                        .requestMatchers("/api/reports/*/review").hasRole("MANAGER")
                        .requestMatchers("/api/dashboard/**").hasRole("MANAGER")

                        // Everything else under /api requires a valid, authenticated token
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Validate incoming JWTs and map Keycloak realm roles to Spring authorities
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    /**
     * Converts Keycloak's realm_access.roles claim into Spring Security
     * GrantedAuthority objects prefixed with "ROLE_" so hasRole("MANAGER") works.
     */
    @SuppressWarnings("unchecked")
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess == null || realmAccess.get("roles") == null) {
                return new ArrayList<>();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");

            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return authorities;
        });

        return converter;
    }

    /**
     * Allows the Next.js frontend (running on a different origin/port) to call this API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}