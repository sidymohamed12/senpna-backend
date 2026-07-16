package ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.commandeachat.application.facade.CommandeAchatFacade;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.CreateCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.InfoReceptionLigneRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.LigneCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.MotifRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ReceptionnerCommandeAchatRequest;

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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur PNA — cf.
 * {@code AppelOffresControllerTest} pour le périmètre exact (câblage
 * route/statut/sérialisation, pas les règles métier ni l'autorisation).
 */
@WebMvcTest(controllers = CommandesAchatController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("CommandesAchatController — HTTP")
class CommandesAchatControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;
        @MockitoBean
        CommandeAchatFacade commandeAchatFacade;

        private CommandeAchatDetail commandeDetail(UUID id) {
                return new CommandeAchatDetail(id, "BC-1", UUID.randomUUID(), UUID.randomUUID(),
                                StatutCommandeAchat.EN_ATTENTE_VALIDATION, List.of(), null, null, null, null, null,
                                null, null,
                                null);
        }

        private FactureDetail factureDetail(UUID id) {
                return new FactureDetail(id, UUID.randomUUID(), UUID.randomUUID(), "FAC-1", BigDecimal.TEN, null, null,
                                null, StatutFacture.SOUMISE, null, null, null);
        }

        @Test
        @DisplayName("POST /api/commandes-achat → 201")
        void creer_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.creer(any())).thenReturn(commandeDetail(id));

                CreateCommandeAchatRequest request = new CreateCommandeAchatRequest("BC-1", UUID.randomUUID(),
                                UUID.randomUUID(), "Commentaire",
                                List.of(new LigneCommandeAchatRequest(UUID.randomUUID(), UUID.randomUUID(),
                                                BigDecimal.TEN,
                                                BigDecimal.valueOf(200_000))));

                mockMvc.perform(post("/api/commandes-achat").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_CREATED"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/{id}/valider → 200")
        void valider_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.valider(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/{id}/valider", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_VALIDEE"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/{id}/rejeter → 200, transmet le motif")
        void rejeter_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.rejeter(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/{id}/rejeter", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new MotifRequest("Prix hors marché"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_REJETEE"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/{id}/annuler → 200")
        void annuler_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.annuler(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/{id}/annuler", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_ANNULEE"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/{id}/receptionner → 200")
        void receptionner_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.receptionner(any())).thenReturn(commandeDetail(id));

                ReceptionnerCommandeAchatRequest request = new ReceptionnerCommandeAchatRequest(
                                List.of(new InfoReceptionLigneRequest(UUID.randomUUID(), BigDecimal.TEN,
                                                BigDecimal.ZERO, null)));

                mockMvc.perform(patch("/api/commandes-achat/{id}/receptionner", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("COMMANDE_ACHAT_RECEPTIONNEE"));
        }

        @Test
        @DisplayName("GET /api/commandes-achat/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.obtenir(any())).thenReturn(commandeDetail(id));

                mockMvc.perform(get("/api/commandes-achat/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }

        @Test
        @DisplayName("GET /api/commandes-achat → 200, page paginée")
        void lister_200() throws Exception {
                CommandeAchatSummary summary = new CommandeAchatSummary(UUID.randomUUID(), "BC-1", UUID.randomUUID(),
                                StatutCommandeAchat.VALIDEE, 1, null);
                when(commandeAchatFacade.lister(any()))
                                .thenReturn(new CommandeAchatPage(List.of(summary), 0, 20, 1, 1));

                mockMvc.perform(get("/api/commandes-achat").param("statut", "VALIDEE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].reference").value("BC-1"));
        }

        @Test
        @DisplayName("GET /api/commandes-achat/factures → 200")
        void listerFactures_200() throws Exception {
                when(commandeAchatFacade.listerFactures(any()))
                                .thenReturn(new FacturePage(List.of(factureDetail(UUID.randomUUID())), 0, 20, 1, 1));

                mockMvc.perform(get("/api/commandes-achat/factures"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results").isArray());
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/factures/{id}/valider → 200")
        void validerFacture_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.validerFacture(any())).thenReturn(factureDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/factures/{id}/valider", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("FACTURE_VALIDEE"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/factures/{id}/rejeter → 200")
        void rejeterFacture_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.rejeterFacture(any())).thenReturn(factureDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/factures/{id}/rejeter", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new MotifRequest("Montant incohérent"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("FACTURE_REJETEE"));
        }

        @Test
        @DisplayName("PATCH /api/commandes-achat/factures/{id}/payer → 200")
        void marquerFacturePayee_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(commandeAchatFacade.marquerFacturePayee(any())).thenReturn(factureDetail(id));

                mockMvc.perform(patch("/api/commandes-achat/factures/{id}/payer", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("FACTURE_PAYEE"));
        }
}