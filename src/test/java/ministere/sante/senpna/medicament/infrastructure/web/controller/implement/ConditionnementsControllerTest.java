package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.medicament.application.facade.ConditionnementFacade;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateConditionnementRequest;

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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Conditionnements — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link ConditionnementFacade},
 * pas les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = ConditionnementsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ConditionnementsController — HTTP")
class ConditionnementsControllerTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        ConditionnementFacade conditionnementFacade;

        private ConditionnementDetail detail(UUID id, UUID medicamentId) {
                return new ConditionnementDetail(id, medicamentId, "Boîte de 20", 1, BigDecimal.TEN, false,
                                BigDecimal.valueOf(500), BigDecimal.valueOf(750), true, Instant.now(), Instant.now());
        }

        @Test
        @DisplayName("POST /api/conditionnements → 201, délègue à la façade")
        void creer_201() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.creerConditionnement(any())).thenReturn(detail(id, medicamentId));

                CreateConditionnementRequest request = new CreateConditionnementRequest(medicamentId, "Boîte de 20", 1,
                                BigDecimal.TEN, false, BigDecimal.valueOf(500), BigDecimal.valueOf(750));

                mockMvc.perform(post("/api/conditionnements").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENT_CREATED"));

                verify(conditionnementFacade).creerConditionnement(any());
        }

        @Test
        @DisplayName("PUT /api/conditionnements/{id} → 200")
        void modifier_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.modifierConditionnement(any())).thenReturn(detail(id, medicamentId));

                UpdateConditionnementRequest request = new UpdateConditionnementRequest("Boîte de 20", 1,
                                BigDecimal.TEN,
                                false, BigDecimal.valueOf(500), BigDecimal.valueOf(750));

                mockMvc.perform(put("/api/conditionnements/{id}", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENT_UPDATED"));
        }

        @Test
        @DisplayName("PATCH /api/conditionnements/{id}/archiver → 200")
        void archiver_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.archiverConditionnement(any())).thenReturn(detail(id, medicamentId));

                mockMvc.perform(patch("/api/conditionnements/{id}/archiver", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENT_ARCHIVED"));
        }

        @Test
        @DisplayName("PATCH /api/conditionnements/{id}/desarchiver → 200")
        void desarchiver_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.desarchiverConditionnement(any())).thenReturn(detail(id, medicamentId));

                mockMvc.perform(patch("/api/conditionnements/{id}/desarchiver", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENT_DESARCHIVED"));
        }

        @Test
        @DisplayName("GET /api/conditionnements/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.obtenirConditionnement(any())).thenReturn(detail(id, medicamentId));

                mockMvc.perform(get("/api/conditionnements/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENT_FOUND"));
        }

        @Test
        @DisplayName("GET /api/conditionnements → 200, page paginée")
        void lister_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(conditionnementFacade.listerConditionnements(any()))
                                .thenReturn(new ConditionnementPage(List.of(detail(id, medicamentId)), 0, 20, 1, 1));

                mockMvc.perform(get("/api/conditionnements").param("medicamentId", medicamentId.toString())
                                .param("actif", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].nom").value("Boîte de 20"))
                                .andExpect(jsonPath("$.type").value("CONDITIONNEMENTS_LISTED"))
                                .andExpect(jsonPath("$.pagination.totalItems").value(1));
        }
}
