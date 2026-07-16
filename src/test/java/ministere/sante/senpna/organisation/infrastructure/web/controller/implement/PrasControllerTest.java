package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.organisation.application.facade.EntrepotFacade;
import ministere.sante.senpna.organisation.application.facade.PraFacade;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreatePraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdatePraRequest;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur PRA — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link PraFacade} /
 * {@link EntrepotFacade}. Le contrôleur lit {@code CurrentUser.getUserId()}
 * pour tracer l'acteur de chaque opération — cf. {@code UsersControllerTest}
 * pour le même raisonnement sur la simulation du
 * {@code SecurityContextHolder}.
 */
@WebMvcTest(controllers = PrasController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("PrasController — HTTP")
class PrasControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PraFacade praFacade;

    @MockitoBean
    EntrepotFacade entrepotFacade;

    @BeforeEach
    void authentifieActeur() {
        CurrentUser principal = mock(CurrentUser.class);
        when(principal.getUserId()).thenReturn(UUID.randomUUID());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void nettoieContexteSecurite() {
        SecurityContextHolder.clearContext();
    }

    private EntrepotDetail detail(UUID id) {
        return new EntrepotDetail(id, "PRA-DAKAR", "PRA Dakar", TypeEntrepot.PRA, UUID.randomUUID(), "Dakar",
                "Adresse", "+221771234567", UUID.randomUUID(), true, Instant.now(), Instant.now());
    }

    @Test
    @DisplayName("POST /api/pras → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(praFacade.creerPra(any())).thenReturn(detail(id));

        CreatePraRequest request = new CreatePraRequest("PRA-DAKAR", "PRA Dakar", UUID.randomUUID(), "Adresse",
                "+221771234567");

        mockMvc.perform(post("/api/pras").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("PRA_CREATED"));
    }

    @Test
    @DisplayName("PUT /api/pras/{id} → 200, transmet l'acteur courant")
    void modifier_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(praFacade.modifierPra(any())).thenReturn(detail(id));

        UpdatePraRequest request = new UpdatePraRequest("PRA Dakar", "Adresse", "+221771234567", UUID.randomUUID());

        mockMvc.perform(put("/api/pras/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRA_UPDATED"));
    }

    @Test
    @DisplayName("PATCH /api/pras/{id}/desactiver → 200")
    void desactiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(praFacade.desactiverPra(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/pras/{id}/desactiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRA_DEACTIVATED"));
    }

    @Test
    @DisplayName("PATCH /api/pras/{id}/activer → 200")
    void activer_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(praFacade.activerPra(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/pras/{id}/activer", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRA_ACTIVATED"));
    }

    @Test
    @DisplayName("GET /api/pras/{id} → 200, délègue à EntrepotFacade")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(entrepotFacade.obtenirEntrepot(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/pras/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRA_FOUND"));
    }

    @Test
    @DisplayName("GET /api/pras → 200, force le type PRA dans la requête")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(entrepotFacade.listerEntrepots(any())).thenReturn(new EntrepotPage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/pras").param("q", "dakar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("PRA-DAKAR"))
                .andExpect(jsonPath("$.type").value("PRAS_LISTED"));
    }
}
