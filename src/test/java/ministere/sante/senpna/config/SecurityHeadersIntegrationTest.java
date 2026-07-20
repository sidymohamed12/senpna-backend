package ministere.sante.senpna.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie, sur une vraie chaîne de filtres Spring Security (contrairement
 * aux {@code @WebMvcTest} de contrôleurs qui désactivent volontairement
 * les filtres via {@code @AutoConfigureMockMvc(addFilters = false)}), que
 * les en-têtes de sécurité HTTP OWASP sont bien présents sur les réponses.
 *
 * <p>
 * Utilise {@code GET /actuator/health}, seule route {@code permitAll()}
 * simple à atteindre sans authentification ni dépendance métier.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("integration")
@DisplayName("SecurityConfig — en-têtes de sécurité HTTP (OWASP Secure Headers)")
class SecurityHeadersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("la réponse contient une Content-Security-Policy restrictive (API JSON pure)")
    void reponse_contientContentSecurityPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("default-src 'none'")));
    }

    @Test
    @DisplayName("la réponse contient Strict-Transport-Security avec includeSubDomains et preload")
    void reponse_contientHsts() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Strict-Transport-Security",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("max-age=31536000"),
                                org.hamcrest.Matchers.containsString("includeSubDomains"),
                                org.hamcrest.Matchers.containsString("preload"))));
    }

    @Test
    @DisplayName("la réponse contient X-Content-Type-Options: nosniff")
    void reponse_contientXContentTypeOptions() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("la réponse contient X-Frame-Options: DENY")
    void reponse_contientXFrameOptions() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("la réponse contient Referrer-Policy: strict-origin-when-cross-origin")
    void reponse_contientReferrerPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("la réponse contient Permissions-Policy désactivant géoloc/caméra/micro/paiement")
    void reponse_contientPermissionsPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Permissions-Policy",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("geolocation=()"),
                                org.hamcrest.Matchers.containsString("camera=()"),
                                org.hamcrest.Matchers.containsString("microphone=()"),
                                org.hamcrest.Matchers.containsString("payment=()"))));
    }

    @Test
    @DisplayName("la réponse contient X-Permitted-Cross-Domain-Policies: none")
    void reponse_contientXPermittedCrossDomainPolicies() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Permitted-Cross-Domain-Policies", "none"));
    }
}
