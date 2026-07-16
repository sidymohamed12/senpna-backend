package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.stock.application.facade.MouvementStockFacade;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementDetail;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Mouvements de stock — vérifient le
 * câblage route/statut/sérialisation et la délégation à
 * {@link MouvementStockFacade}, pas les règles métier (déjà couvertes par
 * les tests de use case).
 */
@WebMvcTest(controllers = MouvementsStockController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("MouvementsStockController — HTTP")
class MouvementsStockControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MouvementStockFacade mouvementStockFacade;

    private MouvementDetail detail(UUID id) {
        return new MouvementDetail(id, "ENTREE_ACHAT", "ENTREE", null, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, Instant.now(), "REF-1", null,
                UUID.randomUUID(), Instant.now());
    }

    @Test
    @DisplayName("GET /api/mouvements-stock/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(mouvementStockFacade.obtenirMouvement(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/mouvements-stock/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("MOUVEMENT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/mouvements-stock → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(mouvementStockFacade.listerMouvements(any()))
                .thenReturn(new MouvementPage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/mouvements-stock").param("typeMouvement", "ENTREE_ACHAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("MOUVEMENTS_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }
}
