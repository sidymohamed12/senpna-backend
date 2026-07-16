package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.medicament.application.facade.FormeFacade;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.FormeCommands.FormePage;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFormeRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFormeRequest;

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
 * Tests HTTP (MockMvc) du contrôleur Formes pharmaceutiques — vérifient le
 * câblage route/statut/sérialisation et la délégation à {@link FormeFacade},
 * pas les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = FormesController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("FormesController — HTTP")
class FormesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FormeFacade formeFacade;

    private FormeDetail detail(UUID id) {
        return new FormeDetail(id, "COMP", "Comprimé", "Forme solide orale", true, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/formes → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.creerForme(any())).thenReturn(detail(id));

        CreateFormeRequest request = new CreateFormeRequest("COMP", "Comprimé", "Forme solide orale");

        mockMvc.perform(post("/api/formes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("FORME_CREATED"));

        verify(formeFacade).creerForme(any());
    }

    @Test
    @DisplayName("PUT /api/formes/{id} → 200")
    void modifier_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.modifierForme(any())).thenReturn(detail(id));

        UpdateFormeRequest request = new UpdateFormeRequest("Comprimé", "Forme solide orale");

        mockMvc.perform(put("/api/formes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FORME_UPDATED"));
    }

    @Test
    @DisplayName("PATCH /api/formes/{id}/archiver → 200")
    void archiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.archiverForme(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/formes/{id}/archiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FORME_ARCHIVED"));
    }

    @Test
    @DisplayName("PATCH /api/formes/{id}/desarchiver → 200")
    void desarchiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.desarchiverForme(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/formes/{id}/desarchiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FORME_DESARCHIVED"));
    }

    @Test
    @DisplayName("GET /api/formes/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.obtenirForme(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/formes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("FORME_FOUND"));
    }

    @Test
    @DisplayName("GET /api/formes → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(formeFacade.listerFormes(any()))
                .thenReturn(new FormePage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/formes").param("q", "Comp").param("actif", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("COMP"))
                .andExpect(jsonPath("$.type").value("FORMES_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }
}
