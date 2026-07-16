package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.organisation.application.facade.EntrepotFacade;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Entrepôts — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link EntrepotFacade}, pas
 * les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = EntrepotsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("EntrepotsController — HTTP")
class EntrepotsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EntrepotFacade entrepotFacade;

    private EntrepotDetail detail(UUID id) {
        return new EntrepotDetail(id, "PNA-CENTRAL", "Pharmacie Nationale", TypeEntrepot.PNA_CENTRAL,
                UUID.randomUUID(), "Dakar", "Adresse", "+221771234567", UUID.randomUUID(), true, Instant.now(),
                Instant.now());
    }

    @Test
    @DisplayName("GET /api/entrepots/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(entrepotFacade.obtenirEntrepot(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/entrepots/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("ENTREPOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/entrepots → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(entrepotFacade.listerEntrepots(any())).thenReturn(new EntrepotPage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/entrepots").param("q", "pna").param("actif", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("PNA-CENTRAL"))
                .andExpect(jsonPath("$.type").value("ENTREPOTS_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }
}
