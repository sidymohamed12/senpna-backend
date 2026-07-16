package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.organisation.application.facade.RegionFacade;
import ministere.sante.senpna.organisation.domain.command.RegionCommand.RegionDetail;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateRegionRequest;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Régions — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link RegionFacade}, pas
 * les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = RegionsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("RegionsController — HTTP")
class RegionsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RegionFacade regionFacade;

    private RegionDetail detail(UUID id) {
        return new RegionDetail(id, "DK", "Dakar", true, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/regions → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(regionFacade.creerRegion(any())).thenReturn(detail(id));

        CreateRegionRequest request = new CreateRegionRequest("DK", "Dakar");

        mockMvc.perform(post("/api/regions").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("REGION_CREATED"));

        verify(regionFacade).creerRegion(any());
    }

    @Test
    @DisplayName("GET /api/regions → 200, liste non paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(regionFacade.listerRegions()).thenReturn(List.of(detail(id)));

        mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("DK"))
                .andExpect(jsonPath("$.type").value("REGIONS_LISTED"));
    }

    @Test
    @DisplayName("GET /api/regions/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(regionFacade.obtenirRegion(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/regions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("REGION_FOUND"));
    }
}
