package ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.commandeachat.application.facade.EspaceFournisseurCommandeAchatFacade;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ConfirmerDelaiLivraisonRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.GenererAvisExpeditionRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.InfoExpeditionLigneRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.SoumettreFactureRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur espace fournisseur — cf.
 * {@code EspaceFournisseurAppelOffresControllerTest} pour le
 * raisonnement sur la simulation manuelle du {@link SecurityContextHolder}.
 */
@WebMvcTest(controllers = EspaceFournisseurCommandesAchatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("EspaceFournisseurCommandesAchatController — HTTP")
class EspaceFournisseurCommandesAchatControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;
        @MockitoBean
        EspaceFournisseurCommandeAchatFacade facade;

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

        private CommandeAchatDetail commandeDetail(UUID id) {
                return new CommandeAchatDetail(id, "BC-1", fournisseurId, UUID.randomUUID(),
                                StatutCommandeAchat.VALIDEE,
                                List.of(), null, null, null, null, null, null, null, null);
        }

        private FactureDetail factureDetail(UUID id) {
                return new FactureDetail(id, UUID.randomUUID(), fournisseurId, "FAC-1", BigDecimal.TEN, null, null,
                                null,
                                StatutFacture.SOUMISE, null, null, null);
        }

        @Test
        @DisplayName("GET /api/fournisseur/commandes-achat → 200")
        void lister_200() throws Exception {
                CommandeAchatSummary summary = new CommandeAchatSummary(UUID.randomUUID(), "BC-1", fournisseurId,
                                StatutCommandeAchat.VALIDEE, 1, null);
                when(facade.listerMesCommandes(any())).thenReturn(new CommandeAchatPage(List.of(summary), 0, 20, 1, 1));

                mockMvc.perform(get("/api/fournisseur/commandes-achat"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].reference").value("BC-1"));
        }

        @Test
        @DisplayName("GET /api/fournisseur/commandes-achat/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.obtenirCommande(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(get("/api/fournisseur/commandes-achat/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }

        @Test
        @DisplayName("PATCH /api/fournisseur/commandes-achat/{id}/accuser-reception → 200")
        void accuserReception_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.accuserReception(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(patch("/api/fournisseur/commandes-achat/{id}/accuser-reception", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_ACCUSE_RECEPTION"));
        }

        @Test
        @DisplayName("PATCH /api/fournisseur/commandes-achat/{id}/confirmer-delai → 200")
        void confirmerDelaiLivraison_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.confirmerDelaiLivraison(any())).thenReturn(commandeDetail(id));

                ConfirmerDelaiLivraisonRequest request = new ConfirmerDelaiLivraisonRequest(10,
                                LocalDate.now().plusDays(10));

                mockMvc.perform(patch("/api/fournisseur/commandes-achat/{id}/confirmer-delai", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_DELAI_CONFIRME"));
        }

        @Test
        @DisplayName("PATCH /api/fournisseur/commandes-achat/{id}/avis-expedition → 200")
        void genererAvisExpedition_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.genererAvisExpedition(any())).thenReturn(commandeDetail(id));

                GenererAvisExpeditionRequest request = new GenererAvisExpeditionRequest(LocalDate.now(), "DHL", "T-1",
                                LocalDate.now().plusDays(5),
                                List.of(new InfoExpeditionLigneRequest(UUID.randomUUID(), "LOT-1", LocalDate.now(),
                                                LocalDate.now().plusYears(1), null, BigDecimal.TEN)));

                mockMvc.perform(patch("/api/fournisseur/commandes-achat/{id}/avis-expedition", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_EXPEDIEE"));
        }

        @Test
        @DisplayName("POST /api/fournisseur/commandes-achat/{id}/factures → 201, fournisseurId résolu depuis le contexte de sécurité")
        void soumettreFacture_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.soumettreFacture(any())).thenReturn(factureDetail(UUID.randomUUID()));

                SoumettreFactureRequest request = new SoumettreFactureRequest("FAC-1", BigDecimal.valueOf(49_000),
                                LocalDate.now(), LocalDate.now().plusDays(30), null);

                mockMvc.perform(post("/api/fournisseur/commandes-achat/{id}/factures", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.type").value("FACTURE_SOUMISE"));

                ArgumentCaptor<SoumettreFactureCommand> captor = ArgumentCaptor.forClass(SoumettreFactureCommand.class);
                verify(facade).soumettreFacture(captor.capture());
                assertThat(captor.getValue().commandeAchatId()).isEqualTo(id);
                assertThat(captor.getValue().fournisseurId()).isEqualTo(fournisseurId);
        }

        @Test
        @DisplayName("GET /api/fournisseur/commandes-achat/factures → 200")
        void listerFactures_200() throws Exception {
                when(facade.listerMesFactures(any()))
                                .thenReturn(new FacturePage(List.of(factureDetail(UUID.randomUUID())), 0, 20, 1, 1));

                mockMvc.perform(get("/api/fournisseur/commandes-achat/factures"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results").isArray());
        }

        @Test
        @DisplayName("GET /api/fournisseur/commandes-achat/factures/{id} → 200")
        void obtenirFacture_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(facade.obtenirFacture(any())).thenReturn(factureDetail(id));

                mockMvc.perform(get("/api/fournisseur/commandes-achat/factures/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }
}