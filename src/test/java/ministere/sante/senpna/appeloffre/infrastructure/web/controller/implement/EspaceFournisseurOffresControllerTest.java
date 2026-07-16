package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import ministere.sante.senpna.appeloffre.application.facade.EspaceFournisseurAppelOffreFacade;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EspaceFournisseurOffresController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("EspaceFournisseurOffresController — HTTP")
class EspaceFournisseurOffresControllerTest {

    @Autowired
    MockMvc mockMvc;
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

    private OffreDetail offreDetail(UUID id) {
        return new OffreDetail(id, UUID.randomUUID(), fournisseurId, null, StatutOffre.SOUMISE, List.of(), null,
                null);
    }

    @Test
    @DisplayName("GET /api/fournisseur/offres → 200")
    void lister_200() throws Exception {
        when(espaceFournisseurAppelOffreFacade.listerMesOffres(any()))
                .thenReturn(new OffrePage(List.of(offreDetail(UUID.randomUUID())), 0, 20, 1, 1));

        mockMvc.perform(get("/api/fournisseur/offres").param("statut", "SOUMISE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("GET /api/fournisseur/offres/{id} → 200")
    void obtenir_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(espaceFournisseurAppelOffreFacade.obtenirOffre(any())).thenReturn(offreDetail(id));

        mockMvc.perform(get("/api/fournisseur/offres/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.id").value(id.toString()));
    }

    @Test
    @DisplayName("PATCH /api/fournisseur/offres/{id}/retirer → 200, retire l'offre au nom du fournisseur connecté")
    void retirer_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(espaceFournisseurAppelOffreFacade.retirerOffre(any())).thenReturn(offreDetail(id));

        mockMvc.perform(patch("/api/fournisseur/offres/{id}/retirer", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OFFRE_RETIREE"));

        ArgumentCaptor<RetirerOffreCommand> captor = ArgumentCaptor.forClass(RetirerOffreCommand.class);
        verify(espaceFournisseurAppelOffreFacade).retirerOffre(captor.capture());
        assertThat(captor.getValue().offreId()).isEqualTo(id);
        assertThat(captor.getValue().fournisseurId()).isEqualTo(fournisseurId);
    }
}