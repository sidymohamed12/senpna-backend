package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.stock.application.facade.LotFacade;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.CreerLotRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ModifierPrixLotRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Lots — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link LotFacade}, pas les
 * règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = LotsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("LotsController — HTTP")
class LotsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    LotFacade lotFacade;

    private LotDetail detail(UUID id) {
        return new LotDetail(id, "LOT-001", UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6), BigDecimal.TEN, new BigDecimal("15"), "ACTIF", false, 180,
                Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/lots → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.creerLot(any())).thenReturn(detail(id));

        CreerLotRequest request = new CreerLotRequest("LOT-001", UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), BigDecimal.TEN, new BigDecimal("15"));

        mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("LOT_CREATED"));

        verify(lotFacade).creerLot(any());
    }

    @Test
    @DisplayName("PATCH /api/lots/{id}/bloquer → 200")
    void bloquer_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.bloquerLot(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/lots/{id}/bloquer", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOT_BLOCKED"));
    }

    @Test
    @DisplayName("PATCH /api/lots/{id}/debloquer → 200")
    void debloquer_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.debloquerLot(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/lots/{id}/debloquer", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOT_UNBLOCKED"));
    }

    @Test
    @DisplayName("PATCH /api/lots/{id}/prix → 200")
    void modifierPrix_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.modifierPrixLot(any())).thenReturn(detail(id));

        ModifierPrixLotRequest request = new ModifierPrixLotRequest(BigDecimal.TEN, new BigDecimal("15"));

        mockMvc.perform(patch("/api/lots/{id}/prix", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOT_PRICE_UPDATED"));
    }

    @Test
    @DisplayName("GET /api/lots/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.obtenirLot(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/lots/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("LOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/lots → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.listerLots(any())).thenReturn(new LotPage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/lots").param("q", "lot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].numeroLot").value("LOT-001"))
                .andExpect(jsonPath("$.type").value("LOTS_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }

    @Test
    @DisplayName("GET /api/lots/alertes/peremption → 200, page paginée")
    void alertesPeremption_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(lotFacade.listerAlertesPeremption(any())).thenReturn(new LotPage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/lots/alertes/peremption").param("horizonJours", "90"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].numeroLot").value("LOT-001"))
                .andExpect(jsonPath("$.type").value("LOT_ALERTES_PEREMPTION_LISTED"));
    }
}
