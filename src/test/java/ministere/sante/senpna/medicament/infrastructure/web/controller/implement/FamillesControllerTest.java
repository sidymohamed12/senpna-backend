package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.medicament.application.facade.FamilleFacade;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamillePage;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFamilleRequest;

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
 * Tests HTTP (MockMvc) du contrôleur Familles thérapeutiques — vérifient le
 * câblage route/statut/sérialisation et la délégation à {@link FamilleFacade},
 * pas les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = FamillesController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("FamillesController — HTTP")
class FamillesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FamilleFacade familleFacade;

    private FamilleDetail detail(UUID id) {
        return new FamilleDetail(id, "ANTIBIO", "Antibiotiques", "Description", true, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/familles → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.creerFamille(any())).thenReturn(detail(id));

        CreateFamilleRequest request = new CreateFamilleRequest("ANTIBIO", "Antibiotiques", "Description");

        mockMvc.perform(post("/api/familles").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("FAMILLE_CREATED"));

        verify(familleFacade).creerFamille(any());
    }

    @Test
    @DisplayName("PUT /api/familles/{id} → 200")
    void modifier_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.modifierFamille(any())).thenReturn(detail(id));

        UpdateFamilleRequest request = new UpdateFamilleRequest("Antibiotiques", "Description");

        mockMvc.perform(put("/api/familles/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FAMILLE_UPDATED"));
    }

    @Test
    @DisplayName("PATCH /api/familles/{id}/archiver → 200")
    void archiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.archiverFamille(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/familles/{id}/archiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FAMILLE_ARCHIVED"));
    }

    @Test
    @DisplayName("PATCH /api/familles/{id}/desarchiver → 200")
    void desarchiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.desarchiverFamille(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/familles/{id}/desarchiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FAMILLE_DESARCHIVED"));
    }

    @Test
    @DisplayName("GET /api/familles/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.obtenirFamille(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/familles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("FAMILLE_FOUND"));
    }

    @Test
    @DisplayName("GET /api/familles → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(familleFacade.listerFamilles(any()))
                .thenReturn(new FamillePage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/familles").param("q", "Anti").param("actif", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("ANTIBIO"))
                .andExpect(jsonPath("$.type").value("FAMILLES_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }
}
