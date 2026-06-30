package ministere.sante.senpna.config;

import ministere.sante.senpna.shared.domain.port.out.RateLimitPort;
import ministere.sante.senpna.shared.infrastructure.web.filter.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * Configuration Spring Security — JWT stateless + RBAC par annotations.
 *
 * <h3>Ordre des filtres</h3>
 * <ol>
 * <li>{@link RateLimitFilter} — protection DDoS/bruteforce avant tout</li>
 * <li>{@link JwtAuthenticationFilter} — validation token + chargement
 * SecurityContext</li>
 * <li>Spring Security — autorisation par règle</li>
 * </ol>
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitPort rateLimitPort;
    private final Environment environment;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitPort rateLimitPort,
            Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitPort = rateLimitPort;
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        boolean isDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    // Routes d'authentification publiques
                    auth.requestMatchers(HttpMethod.POST,
                            "/api/auth/login",
                            "/api/auth/verify",
                            "/api/auth/resend-otp",
                            "/api/auth/refresh")
                            .permitAll();

                    // Healthcheck monitoring
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health")
                            .permitAll();

                    // Swagger — uniquement en dev
                    if (isDevProfile) {
                        auth.requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml")
                                .permitAll();
                    }

                    // Tout le reste exige une authentification
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt strength=12 — ~250ms/hash, conforme OWASP 2024.
     * Intentionnellement lent pour ralentir les attaques offline sur la base
     * compromise.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Déclaré en @Bean explicitement pour éviter le double-enregistrement
     * automatique de Spring Boot dans la chaîne Servlet.
     */
    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(rateLimitPort);
    }
}
