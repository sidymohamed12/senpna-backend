package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToEntrepotRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToStructureRequest;

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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Affectations — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link OrganisationFacade},
 * pas les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = AffectationsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AffectationsController — HTTP")
class AffectationsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OrganisationFacade organisationFacade;

    @Test
    @DisplayName("POST /api/affectations/utilisateurs/{userId}/entrepot → 200, délègue à la façade")
    void affecterAEntrepot_200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID entrepotId = UUID.randomUUID();
        when(organisationFacade.affecterUtilisateurAEntrepot(any()))
                .thenReturn(new UserAffectationDetail(userId, entrepotId, null));

        AssignUserToEntrepotRequest request = new AssignUserToEntrepotRequest(entrepotId);

        mockMvc.perform(post("/api/affectations/utilisateurs/{userId}/entrepot", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.entrepotId").value(entrepotId.toString()))
                .andExpect(jsonPath("$.type").value("USER_ASSIGNED_ENTREPOT"));

        verify(organisationFacade).affecterUtilisateurAEntrepot(any());
    }

    @Test
    @DisplayName("POST /api/affectations/utilisateurs/{userId}/structure-sanitaire → 200, délègue à la façade")
    void affecterAStructure_200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID structureId = UUID.randomUUID();
        when(organisationFacade.affecterUtilisateurAStructure(any()))
                .thenReturn(new UserAffectationDetail(userId, null, structureId));

        AssignUserToStructureRequest request = new AssignUserToStructureRequest(structureId);

        mockMvc.perform(post("/api/affectations/utilisateurs/{userId}/structure-sanitaire", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.structureSanitaireId").value(structureId.toString()))
                .andExpect(jsonPath("$.type").value("USER_ASSIGNED_STRUCTURE"));

        verify(organisationFacade).affecterUtilisateurAStructure(any());
    }

    @Test
    @DisplayName("DELETE /api/affectations/utilisateurs/{userId} → 200, délègue à la façade")
    void retirerAffectation_200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(organisationFacade.retirerAffectationUtilisateur(any()))
                .thenReturn(new UserAffectationDetail(userId, null, null));

        mockMvc.perform(delete("/api/affectations/utilisateurs/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("USER_UNASSIGNED"));

        verify(organisationFacade).retirerAffectationUtilisateur(any());
    }
}
