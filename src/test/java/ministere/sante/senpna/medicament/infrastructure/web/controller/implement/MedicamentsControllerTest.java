package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateMedicamentRequest;

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
 * Tests HTTP (MockMvc) du contrôleur Médicaments — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link MedicamentFacade},
 * pas les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = MedicamentsController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("MedicamentsController — HTTP")
class MedicamentsControllerTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        MedicamentFacade medicamentFacade;

        private MedicamentDetail detail(UUID id) {
                return new MedicamentDetail(id, "MED-1", "Doliprane", "Paracétamol", "500mg", UUID.randomUUID(),
                                "Comprimé",
                                UUID.randomUUID(), "Antalgiques", VoieAdministration.ORALE, null, null, 30, false,
                                "Sanofi", 10, 100,
                                true, Instant.now(), Instant.now());
        }

        private CreateMedicamentRequest createRequest() {
                return new CreateMedicamentRequest("MED-1", "Doliprane", "Paracétamol", "500mg", UUID.randomUUID(),
                                UUID.randomUUID(), VoieAdministration.ORALE, null, null, 30, false, "Sanofi", 10, 100);
        }

        private UpdateMedicamentRequest updateRequest() {
                return new UpdateMedicamentRequest("Doliprane", "Paracétamol", "500mg", UUID.randomUUID(),
                                UUID.randomUUID(),
                                VoieAdministration.ORALE, null, null, 30, false, "Sanofi", 10, 100);
        }

        @Test
        @DisplayName("POST /api/medicaments → 201, délègue à la façade")
        void creer_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.creerMedicament(any())).thenReturn(detail(id));

                mockMvc.perform(post("/api/medicaments").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("MEDICAMENT_CREATED"));

                verify(medicamentFacade).creerMedicament(any());
        }

        @Test
        @DisplayName("PUT /api/medicaments/{id} → 200")
        void modifier_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.modifierMedicament(any())).thenReturn(detail(id));

                mockMvc.perform(put("/api/medicaments/{id}", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest())))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("MEDICAMENT_UPDATED"));
        }

        @Test
        @DisplayName("PATCH /api/medicaments/{id}/archiver → 200")
        void archiver_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.archiverMedicament(any())).thenReturn(detail(id));

                mockMvc.perform(patch("/api/medicaments/{id}/archiver", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("MEDICAMENT_ARCHIVED"));
        }

        @Test
        @DisplayName("PATCH /api/medicaments/{id}/desarchiver → 200")
        void desarchiver_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.desarchiverMedicament(any())).thenReturn(detail(id));

                mockMvc.perform(patch("/api/medicaments/{id}/desarchiver", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("MEDICAMENT_DESARCHIVED"));
        }

        @Test
        @DisplayName("GET /api/medicaments/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.obtenirMedicament(any())).thenReturn(detail(id));

                mockMvc.perform(get("/api/medicaments/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("MEDICAMENT_FOUND"));
        }

        @Test
        @DisplayName("GET /api/medicaments → 200, page paginée")
        void lister_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(medicamentFacade.listerMedicaments(any()))
                                .thenReturn(new MedicamentPage(List.of(detail(id)), 0, 20, 1, 1));

                mockMvc.perform(get("/api/medicaments").param("q", "Doli").param("actif", "true"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].code").value("MED-1"))
                                .andExpect(jsonPath("$.type").value("MEDICAMENTS_LISTED"))
                                .andExpect(jsonPath("$.pagination.totalItems").value(1));
        }
}
