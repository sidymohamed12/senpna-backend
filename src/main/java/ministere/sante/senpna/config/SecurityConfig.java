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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

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

    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        boolean isDevProfile = Arrays.asList(environment.getActiveProfiles()).contains("dev");

        http
                /**
                 * CSRF désactivé car cette API est stateless :
                 * - Authentification via JWT Bearer Token dans Authorization header
                 * - Aucun cookie d'authentification
                 * - Aucune session HTTP serveur
                 *
                 * CSRF n'apporte donc pas de protection supplémentaire ici.
                 */
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                 * En-têtes de sécurité HTTP — OWASP Secure Headers Project.
                 * API JSON pure (aucune vue HTML servie par le backend en prod),
                 * d'où une CSP restrictive par défaut : default-src 'none'.
                 */
                .headers(headers -> headers
                        .contentTypeOptions(withDefaults -> {
                        })
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Content-Security-Policy",
                                isDevProfile
                                        // Swagger UI (dev uniquement) a besoin de charger ses propres assets.
                                        ? "default-src 'self'; script-src 'self' 'unsafe-inline'; "
                                                + "style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
                                                + "frame-ancestors 'none'; base-uri 'none'"
                                        // Prod/test : API JSON pure, aucune vue HTML servie.
                                        : "default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy",
                                "geolocation=(), camera=(), microphone=(), payment=()"))
                        .addHeaderWriter(new StaticHeadersWriter(
                                "X-Permitted-Cross-Domain-Policies", "none"))
                        .cacheControl(withDefaults -> {
                        }))
                .authorizeHttpRequests(auth -> {

                    // Routes d'authentification publiques
                    auth.requestMatchers(HttpMethod.POST,
                            "/api/auth/login",
                            "/api/auth/forgot-password",
                            "/api/auth/verify",
                            "/api/auth/resend-otp",
                            "/api/auth/reset-password",
                            "/api/auth/refresh")
                            .permitAll();

                    auth.requestMatchers(HttpMethod.GET,
                            "/api/actualites/public",
                            "/api/actualites/public/{id}",
                            "/api/projets/public",
                            "/api/projets/public/{id}",
                            "/api/opportunites/public",
                            "/api/opportunites/public/{id}")
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
