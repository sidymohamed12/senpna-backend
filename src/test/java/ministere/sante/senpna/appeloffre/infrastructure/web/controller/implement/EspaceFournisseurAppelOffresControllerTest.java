package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.appeloffre.application.facade.EspaceFournisseurAppelOffreFacade;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.LigneOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.SoumettreOffreRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur espace fournisseur. La sécurité
 * Spring ({@code @PreAuthorize}) est désactivée via {@code addFilters =
 * false}, mais le fournisseur connecté est tout de même simulé
 * manuellement dans le {@link SecurityContextHolder} : le contrôleur lit
 * {@code CurrentUser.getFournisseurId()} directement depuis le contexte
 * de sécurité (cf. {@code soumettreOffre}), indépendamment de la chaîne
 * de filtres.
 */
@WebMvcTest(controllers = EspaceFournisseurAppelOffresController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("EspaceFournisseurAppelOffresController — HTTP")
class EspaceFournisseurAppelOffresControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;
        @MockitoBean
        EspaceFournisseurAppelOffreFacade espaceFournisseurAppelOffreFacade;

        UUID fournisseurId;

        @BeforeEach
        void authentifieFournisseur() {
                fournisseurId = UUID.randomUUID();
                CurrentUser principal = mock(CurrentUser.class);
                when(principal.getFournisseurId()).thenReturn(fournisseurId);
                SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        }

        @AfterEach
        void nettoieContexteSecurite() {
                SecurityContextHolder.clearContext();
        }

        private AppelOffreDetail appelOffreDetail(UUID id) {
                return new AppelOffreDetail(id, "AO-1", "Objet", LocalDate.now().plusDays(10), StatutAppelOffre.PUBLIE,
                                List.of(), null, null);
        }

        @Test
        @DisplayName("GET /api/fournisseur/appels-offres → 200")
        void lister_200() throws Exception {
                AppelOffreSummary summary = new AppelOffreSummary(UUID.randomUUID(), "AO-1", "Objet",
                                LocalDate.now().plusDays(10), StatutAppelOffre.PUBLIE, 1, null);
                when(espaceFournisseurAppelOffreFacade.listerAppelsOffresPublies(any()))
                                .thenReturn(new AppelOffrePage(List.of(summary), 0, 20, 1, 1));

                mockMvc.perform(get("/api/fournisseur/appels-offres"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].reference").value("AO-1"));
        }

        @Test
        @DisplayName("GET /api/fournisseur/appels-offres/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(espaceFournisseurAppelOffreFacade.obtenirAppelOffre(any())).thenReturn(appelOffreDetail(id));

                mockMvc.perform(get("/api/fournisseur/appels-offres/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }

        @Test
        @DisplayName("POST /api/fournisseur/appels-offres/{id}/offres → 201, fournisseurId résolu depuis le contexte de sécurité")
        void soumettreOffre_201() throws Exception {
                UUID appelOffreId = UUID.randomUUID();
                UUID offreId = UUID.randomUUID();
                OffreDetail offreDetail = new OffreDetail(offreId, appelOffreId, fournisseurId, null,
                                StatutOffre.SOUMISE,
                                List.of(), null, null);
                when(espaceFournisseurAppelOffreFacade.soumettreOffre(any())).thenReturn(offreDetail);

                SoumettreOffreRequest request = new SoumettreOffreRequest("Commentaire",
                                List.of(new LigneOffreRequest(UUID.randomUUID(), BigDecimal.valueOf(2000), 15)));

                mockMvc.perform(post("/api/fournisseur/appels-offres/{id}/offres", appelOffreId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.fournisseurId").value(fournisseurId.toString()))
                                .andExpect(jsonPath("$.type").value("OFFRE_SOUMISE"));
        }
}