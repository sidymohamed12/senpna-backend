package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.appeloffre.application.facade.AppelOffreFacade;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.AttribuerAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.CreateAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.LigneAppelOffreRequest;

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
 * Tests HTTP (MockMvc) du contrôleur PNA — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link AppelOffreFacade},
 * pas les règles métier (déjà couvertes par les tests de use case) ni
 * l'autorisation ({@code @PreAuthorize}, désactivée ici via
 * {@code addFilters = false} — aucun contrôleur de ce projet ne teste la
 * sécurité au niveau MockMvc).
 */
@WebMvcTest(controllers = AppelOffresController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AppelOffresController — HTTP")
class AppelOffresControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;
        @MockitoBean
        AppelOffreFacade appelOffreFacade;

        private AppelOffreDetail appelOffreDetail(UUID id) {
                return new AppelOffreDetail(id, "AO-1", "Objet", LocalDate.now().plusDays(10),
                                StatutAppelOffre.BROUILLON,
                                List.of(), null, null);
        }

        private OffreDetail offreDetail(UUID id) {
                return new OffreDetail(id, UUID.randomUUID(), UUID.randomUUID(), null, StatutOffre.SOUMISE, List.of(),
                                null,
                                null);
        }

        @Test
        @DisplayName("POST /api/appels-offres → 201, délègue à la façade")
        void creer_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(appelOffreFacade.creer(any())).thenReturn(appelOffreDetail(id));

                CreateAppelOffreRequest request = new CreateAppelOffreRequest("AO-1", "Objet",
                                LocalDate.now().plusDays(10),
                                List.of(new LigneAppelOffreRequest(UUID.randomUUID(), "Amoxicilline", BigDecimal.TEN,
                                                "Comprimé")));

                mockMvc.perform(post("/api/appels-offres").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("APPEL_OFFRE_CREATED"));

                verify(appelOffreFacade).creer(any());
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/{id}/publier → 200")
        void publier_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(appelOffreFacade.publier(any())).thenReturn(appelOffreDetail(id));

                mockMvc.perform(patch("/api/appels-offres/{id}/publier", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("APPEL_OFFRE_PUBLIE"));
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/{id}/cloturer → 200")
        void clorer_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(appelOffreFacade.clorer(any())).thenReturn(appelOffreDetail(id));

                mockMvc.perform(patch("/api/appels-offres/{id}/cloturer", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("APPEL_OFFRE_CLOTURE"));
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/{id}/annuler → 200")
        void annuler_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(appelOffreFacade.annuler(any())).thenReturn(appelOffreDetail(id));

                mockMvc.perform(patch("/api/appels-offres/{id}/annuler", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("APPEL_OFFRE_ANNULE"));
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/{id}/attribuer → 200, transmet les listes d'offres")
        void attribuer_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID offreId = UUID.randomUUID();
                when(appelOffreFacade.attribuer(any())).thenReturn(appelOffreDetail(id));

                AttribuerAppelOffreRequest request = new AttribuerAppelOffreRequest(List.of(offreId), List.of());

                mockMvc.perform(patch("/api/appels-offres/{id}/attribuer", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("APPEL_OFFRE_ATTRIBUE"));
        }

        @Test
        @DisplayName("GET /api/appels-offres/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(appelOffreFacade.obtenir(any())).thenReturn(appelOffreDetail(id));

                mockMvc.perform(get("/api/appels-offres/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }

        @Test
        @DisplayName("GET /api/appels-offres → 200, page paginée")
        void lister_200() throws Exception {
                AppelOffreSummary summary = new AppelOffreSummary(UUID.randomUUID(), "AO-1", "Objet",
                                LocalDate.now().plusDays(10), StatutAppelOffre.PUBLIE, 1, null);
                when(appelOffreFacade.lister(any())).thenReturn(new AppelOffrePage(List.of(summary), 0, 20, 1, 1));

                mockMvc.perform(get("/api/appels-offres").param("q", "Amox").param("statut", "PUBLIE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].reference").value("AO-1"))
                                .andExpect(jsonPath("$.pagination.totalItems").value(1));
        }

        @Test
        @DisplayName("GET /api/appels-offres/{id}/offres → 200")
        void listerOffres_200() throws Exception {
                UUID appelOffreId = UUID.randomUUID();
                when(appelOffreFacade.listerOffres(any()))
                                .thenReturn(new OffrePage(List.of(offreDetail(UUID.randomUUID())),
                                                0, 20, 1, 1));

                mockMvc.perform(get("/api/appels-offres/{id}/offres", appelOffreId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results").isArray());
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/offres/{offreId}/retenir → 200")
        void retenirOffre_200() throws Exception {
                UUID offreId = UUID.randomUUID();
                when(appelOffreFacade.retenirOffre(any())).thenReturn(offreDetail(offreId));

                mockMvc.perform(patch("/api/appels-offres/offres/{offreId}/retenir", offreId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("OFFRE_RETENUE"));
        }

        @Test
        @DisplayName("PATCH /api/appels-offres/offres/{offreId}/rejeter → 200")
        void rejeterOffre_200() throws Exception {
                UUID offreId = UUID.randomUUID();
                when(appelOffreFacade.rejeterOffre(any())).thenReturn(offreDetail(offreId));

                mockMvc.perform(patch("/api/appels-offres/offres/{offreId}/rejeter", offreId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("OFFRE_REJETEE"));
        }
}