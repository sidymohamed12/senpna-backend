package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitairePage;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignPraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignRegionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateStructureSanitaireRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.RejectAdhesionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdateStructureSanitaireRequest;

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
 * Tests HTTP (MockMvc) du contrôleur Structures sanitaires — vérifient le
 * câblage route/statut/sérialisation et la délégation à
 * {@link OrganisationFacade}, pas les règles métier (déjà couvertes par les
 * tests de use case).
 */
@WebMvcTest(controllers = StructuresSanitairesController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("StructuresSanitairesController — HTTP")
class StructuresSanitairesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OrganisationFacade organisationFacade;

    private StructureSanitaireDetail detail(UUID id) {
        return new StructureSanitaireDetail(id, "HOP-DKR", "Hôpital de Dakar", TypeStructureSanitaire.HOPITAL,
                UUID.randomUUID(), "Dakar", UUID.randomUUID(), "PRA Dakar", "District", "Adresse", "+221771234567",
                "hopital@example.com", "Diallo", "Awa", StatutAdhesion.VALIDEE, null, true, Instant.now(),
                Instant.now());
    }

    @Test
    @DisplayName("POST /api/structures-sanitaires → 201, délègue à la façade")
    void creer_201() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.creerStructureSanitaire(any())).thenReturn(detail(id));

        CreateStructureSanitaireRequest request = new CreateStructureSanitaireRequest("HOP-DKR", "Hôpital de Dakar",
                TypeStructureSanitaire.HOPITAL, UUID.randomUUID(), "District", "Adresse", "+221771234567",
                "hopital@example.com", "Diallo", "Awa");

        mockMvc.perform(post("/api/structures-sanitaires").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_CREATED"));

        verify(organisationFacade).creerStructureSanitaire(any());
    }

    @Test
    @DisplayName("PUT /api/structures-sanitaires/{id} → 200")
    void modifier_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.modifierStructureSanitaire(any())).thenReturn(detail(id));

        UpdateStructureSanitaireRequest request = new UpdateStructureSanitaireRequest("Hôpital de Dakar", "District",
                "Adresse", "+221771234567", "hopital@example.com", "Diallo", "Awa");

        mockMvc.perform(put("/api/structures-sanitaires/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_UPDATED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/valider-adhesion → 200")
    void validerAdhesion_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.validerAdhesion(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/valider-adhesion", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADHESION_VALIDATED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/rejeter-adhesion → 200")
    void rejeterAdhesion_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.rejeterAdhesion(any())).thenReturn(detail(id));

        RejectAdhesionRequest request = new RejectAdhesionRequest("Documents manquants");

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/rejeter-adhesion", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADHESION_REJECTED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/activer → 200")
    void activer_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.activerStructureSanitaire(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/activer", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_ACTIVATED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/desactiver → 200")
    void desactiver_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.desactiverStructureSanitaire(any())).thenReturn(detail(id));

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/desactiver", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_DEACTIVATED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/region → 200")
    void affecterRegion_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.affecterStructureARegion(any())).thenReturn(detail(id));

        AssignRegionRequest request = new AssignRegionRequest(UUID.randomUUID());

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/region", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_REGION_ASSIGNED"));
    }

    @Test
    @DisplayName("PATCH /api/structures-sanitaires/{id}/pra → 200")
    void affecterPra_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.affecterStructureAPra(any())).thenReturn(detail(id));

        AssignPraRequest request = new AssignPraRequest(UUID.randomUUID());

        mockMvc.perform(patch("/api/structures-sanitaires/{id}/pra", id).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_PRA_ASSIGNED"));
    }

    @Test
    @DisplayName("GET /api/structures-sanitaires/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.obtenirStructureSanitaire(any())).thenReturn(detail(id));

        mockMvc.perform(get("/api/structures-sanitaires/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value("STRUCTURE_SANITAIRE_FOUND"));
    }

    @Test
    @DisplayName("GET /api/structures-sanitaires → 200, page paginée")
    void lister_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(organisationFacade.listerStructuresSanitaires(any()))
                .thenReturn(new StructureSanitairePage(List.of(detail(id)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/structures-sanitaires").param("q", "dkr").param("actif", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].code").value("HOP-DKR"))
                .andExpect(jsonPath("$.type").value("STRUCTURES_SANITAIRES_LISTED"))
                .andExpect(jsonPath("$.pagination.totalItems").value(1));
    }
}
