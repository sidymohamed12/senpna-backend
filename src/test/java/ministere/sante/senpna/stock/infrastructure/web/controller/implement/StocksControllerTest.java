package ministere.sante.senpna.stock.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.stock.application.facade.StockFacade;
import ministere.sante.senpna.stock.domain.command.StockCommands.ReservationFefoResult;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.DefinirSeuilAlerteRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.EntreeStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.LibererReservationRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockFefoRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.ReserverStockRequest;
import ministere.sante.senpna.stock.infrastructure.web.dto.request.SortieStockRequest;

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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Stocks — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link StockFacade}. Le
 * contrôleur lit {@code CurrentUser.getUserId()} sur les mouvements — cf.
 * {@code UsersControllerTest} pour le même raisonnement sur la simulation
 * du {@code SecurityContextHolder}.
 */
@WebMvcTest(controllers = StocksController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("StocksController — HTTP")
class StocksControllerTest {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @MockitoBean
        StockFacade stockFacade;

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

        private StockDetail detail(UUID id) {
                return new StockDetail(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                                new BigDecimal("100.0000"), BigDecimal.ZERO, new BigDecimal("100.0000"),
                                BigDecimal.ZERO,
                                new BigDecimal("10.0000"), false, false, Instant.now(), Instant.now());
        }

        @Test
        @DisplayName("POST /api/stocks/entrees → 201, transmet l'acteur courant")
        void entrer_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.entrerStock(any())).thenReturn(detail(id));

                EntreeStockRequest request = new EntreeStockRequest(UUID.randomUUID(), UUID.randomUUID(),
                                BigDecimal.TEN,
                                "ENTREE_ACHAT", UUID.randomUUID(), "REF-1", null);

                mockMvc.perform(post("/api/stocks/entrees").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("STOCK_ENTREE_ENREGISTREE"));
        }

        @Test
        @DisplayName("POST /api/stocks/sorties → 201, transmet l'acteur courant")
        void sortir_201() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.sortirStock(any())).thenReturn(detail(id));

                SortieStockRequest request = new SortieStockRequest(UUID.randomUUID(), UUID.randomUUID(),
                                BigDecimal.ONE,
                                "SORTIE_STRUCTURE", false, null, UUID.randomUUID(), "REF-2", "Livraison");

                mockMvc.perform(post("/api/stocks/sorties").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.type").value("STOCK_SORTIE_ENREGISTREE"));
        }

        @Test
        @DisplayName("POST /api/stocks/reservations → 200")
        void reserver_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.reserverStock(any())).thenReturn(detail(id));

                ReserverStockRequest request = new ReserverStockRequest(UUID.randomUUID(), UUID.randomUUID(),
                                BigDecimal.ONE,
                                UUID.randomUUID());

                mockMvc.perform(post("/api/stocks/reservations").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("STOCK_RESERVE"));
        }

        @Test
        @DisplayName("POST /api/stocks/reservations/fefo → 200")
        void reserverFefo_200() throws Exception {
                UUID entrepotId = UUID.randomUUID();
                UUID medicamentId = UUID.randomUUID();
                when(stockFacade.reserverStockFefo(any()))
                                .thenReturn(new ReservationFefoResult(entrepotId, medicamentId,
                                                BigDecimal.TEN, BigDecimal.TEN, List.of()));

                ReserverStockFefoRequest request = new ReserverStockFefoRequest(entrepotId, medicamentId,
                                BigDecimal.TEN,
                                UUID.randomUUID());

                mockMvc.perform(post("/api/stocks/reservations/fefo").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("STOCK_RESERVE_FEFO"))
                                .andExpect(jsonPath("$.results.entierementSatisfaite").value(true));
        }

        @Test
        @DisplayName("POST /api/stocks/reservations/liberer → 200")
        void libererReservation_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.libererReservation(any())).thenReturn(detail(id));

                LibererReservationRequest request = new LibererReservationRequest(UUID.randomUUID(), UUID.randomUUID(),
                                BigDecimal.ONE, UUID.randomUUID(), "Erreur de saisie");

                mockMvc.perform(post("/api/stocks/reservations/liberer").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("RESERVATION_LIBEREE"));
        }

        @Test
        @DisplayName("GET /api/stocks/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.obtenirStock(any())).thenReturn(detail(id));

                mockMvc.perform(get("/api/stocks/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("STOCK_FOUND"));
        }

        @Test
        @DisplayName("PATCH /api/stocks/{id}/seuil-alerte → 200")
        void definirSeuilAlerte_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.definirSeuilAlerte(any())).thenReturn(detail(id));

                DefinirSeuilAlerteRequest request = new DefinirSeuilAlerteRequest(new BigDecimal("20.0000"));

                mockMvc.perform(patch("/api/stocks/{id}/seuil-alerte", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("STOCK_SEUIL_DEFINI"));
        }

        @Test
        @DisplayName("GET /api/stocks → 200, page paginée")
        void lister_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.listerStocks(any())).thenReturn(new StockPage(List.of(detail(id)), 0, 20, 1, 1));

                mockMvc.perform(get("/api/stocks").param("ruptureUniquement", "false"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results[0].id").value(id.toString()))
                                .andExpect(jsonPath("$.type").value("STOCKS_LISTED"))
                                .andExpect(jsonPath("$.pagination.totalItems").value(1));
        }

        @Test
        @DisplayName("GET /api/stocks/alertes/rupture → 200, page paginée")
        void alertesRupture_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(stockFacade.listerAlertesRupture(any()))
                                .thenReturn(new StockPage(List.of(detail(id)), 0, 20, 1, 1));

                mockMvc.perform(get("/api/stocks/alertes/rupture"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("STOCK_ALERTES_RUPTURE_LISTED"));
        }
}
