package ministere.sante.senpna.actualite.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.actualite.application.facade.ActualiteFacade;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.CreateActualiteRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.MediaRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.UpdateActualiteRequest;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Actualités — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link ActualiteFacade},
 * pas les règles métier (déjà couvertes par les tests de use case) ni
 * l'autorisation ({@code @PreAuthorize}, désactivée ici via
 * {@code addFilters = false} — aucun contrôleur de ce projet ne teste la
 * sécurité au niveau MockMvc).
 *
 * <p>
 * L'authentification est néanmoins simulée via {@link SecurityContextHolder}
 * car {@code ActualitesController#currentUserId()} lit le principal
 * directement dessus (pas via {@code @AuthenticationPrincipal}), donc
 * {@code addFilters = false} ne suffit pas à la fournir.
 * </p>
 */
@WebMvcTest(controllers = ActualitesController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ActualitesController — HTTP")
class ActualitesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ActualiteFacade actualiteFacade;

    private static final UUID ACTUALITE_ID = UUID.randomUUID();
    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.getUserId()).thenReturn(AUTEUR_ID);
        Authentication authentication = new UsernamePasswordAuthenticationToken(currentUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ActualiteDetail detail() {
        Instant maintenant = Instant.now();
        return new ActualiteDetail(ACTUALITE_ID, "PROJET", "Titre", "Description",
                List.of(new MediaDetail(UUID.randomUUID(), "IMAGE", "https://cdn/1.png", 0)),
                AUTEUR_ID, "Auteur", List.of("sante"), "BROUILLON", maintenant, maintenant);
    }

    @Test
    @DisplayName("POST /api/actualites → 201, résout l'auteur depuis le contexte de sécurité et convertit les médias")
    void creer_201() throws Exception {
        when(actualiteFacade.creerActualite(any())).thenReturn(detail());

        CreateActualiteRequest request = new CreateActualiteRequest("PROJET", "Titre", "Description",
                List.of(new MediaRequest("IMAGE", "https://cdn/1.png")), List.of("sante"));

        mockMvc.perform(post("/api/actualites").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ACTUALITE_CREATED"))
                .andExpect(jsonPath("$.results.id").value(ACTUALITE_ID.toString()))
                .andExpect(jsonPath("$.results.medias[0].type").value("IMAGE"));

        verify(actualiteFacade).creerActualite(argThatCommandAuteurEquals(AUTEUR_ID));
    }

    @Test
    @DisplayName("POST /api/actualites sans médias construit une commande avec une liste de médias vide")
    void creer_sansMedias_listeVide() throws Exception {
        when(actualiteFacade.creerActualite(any())).thenReturn(detail());

        CreateActualiteRequest request = new CreateActualiteRequest("PROJET", "Titre", null, null, null);

        mockMvc.perform(post("/api/actualites").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<CreateActualiteCommand> captor = org.mockito.ArgumentCaptor
                .forClass(CreateActualiteCommand.class);
        verify(actualiteFacade).creerActualite(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().medias()).isEmpty();
    }

    @Test
    @DisplayName("PUT /api/actualites/{id} → 200, transmet l'identifiant et le contenu de la requête")
    void modifier_200() throws Exception {
        when(actualiteFacade.modifierActualite(any())).thenReturn(detail());

        UpdateActualiteRequest request = new UpdateActualiteRequest("PARTENARIAT", "Nouveau titre",
                "Nouvelle desc", List.of(), List.of());

        mockMvc.perform(put("/api/actualites/{id}", ACTUALITE_ID).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_UPDATED"));
    }

    @Test
    @DisplayName("PATCH /api/actualites/{id}/publier → 200")
    void publier_200() throws Exception {
        when(actualiteFacade.publierActualite(new PublierActualiteCommand(ACTUALITE_ID)))
                .thenReturn(detail());

        mockMvc.perform(patch("/api/actualites/{id}/publier", ACTUALITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_PUBLIEE"));

        verify(actualiteFacade).publierActualite(new PublierActualiteCommand(ACTUALITE_ID));
    }

    @Test
    @DisplayName("PATCH /api/actualites/{id}/desactiver → 200")
    void desactiver_200() throws Exception {
        when(actualiteFacade.desactiverActualite(new DesactiverActualiteCommand(ACTUALITE_ID)))
                .thenReturn(detail());

        mockMvc.perform(patch("/api/actualites/{id}/desactiver", ACTUALITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_DESACTIVEE"));
    }

    @Test
    @DisplayName("PATCH /api/actualites/{id}/brouillon → 200")
    void remettreEnBrouillon_200() throws Exception {
        when(actualiteFacade.remettreEnBrouillonActualite(new RemettreEnBrouillonActualiteCommand(ACTUALITE_ID)))
                .thenReturn(detail());

        mockMvc.perform(patch("/api/actualites/{id}/brouillon", ACTUALITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_BROUILLON"));
    }

    @Test
    @DisplayName("GET /api/actualites/{id} → 200")
    void obtenir_200() throws Exception {
        when(actualiteFacade.obtenirActualite(new GetActualiteQuery(ACTUALITE_ID))).thenReturn(detail());

        mockMvc.perform(get("/api/actualites/{id}", ACTUALITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_FOUND"));
    }

    @Test
    @DisplayName("GET /api/actualites/public/{id} → 200, délègue à la façade publique")
    void obtenirPublic_200() throws Exception {
        when(actualiteFacade.obtenirActualitePublique(new GetActualiteQuery(ACTUALITE_ID)))
                .thenReturn(detail());

        mockMvc.perform(get("/api/actualites/public/{id}", ACTUALITE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITE_FOUND"));

        verify(actualiteFacade).obtenirActualitePublique(new GetActualiteQuery(ACTUALITE_ID));
    }

    @Test
    @DisplayName("GET /api/actualites → 200, transmet les paramètres de recherche/pagination tels quels")
    void lister_200() throws Exception {
        ActualitePage page = new ActualitePage(List.of(detail()), 0, 20, 1, 1);
        when(actualiteFacade.listerActualites(any())).thenReturn(page);

        mockMvc.perform(get("/api/actualites")
                .param("q", "q")
                .param("categorie", "PROJET")
                .param("statut", "PUBLIE")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITES_LISTED"));

        org.mockito.ArgumentCaptor<ListActualitesQuery> captor = org.mockito.ArgumentCaptor
                .forClass(ListActualitesQuery.class);
        verify(actualiteFacade).listerActualites(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().recherche()).isEqualTo("q");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().categorie()).isEqualTo("PROJET");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().statut()).isEqualTo("PUBLIE");
    }

    @Test
    @DisplayName("GET /api/actualites/public → 200, force le statut à PUBLIE et ne l'expose pas comme paramètre")
    void listerPublic_forceStatutPublie() throws Exception {
        ActualitePage page = new ActualitePage(List.of(detail()), 0, 20, 1, 1);
        when(actualiteFacade.listerActualites(any())).thenReturn(page);

        mockMvc.perform(get("/api/actualites/public")
                .param("q", "q")
                .param("categorie", "PROJET")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "createdAt")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACTUALITES_LISTED"));

        org.mockito.ArgumentCaptor<ListActualitesQuery> captor = org.mockito.ArgumentCaptor
                .forClass(ListActualitesQuery.class);
        verify(actualiteFacade).listerActualites(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().statut()).isEqualTo("PUBLIE");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().recherche()).isEqualTo("q");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().categorie()).isEqualTo("PROJET");
    }

    @Test
    @DisplayName("GET /api/actualites calcule correctement first/last pour la dernière page")
    void lister_dernierePage_firstEtLastCorrects() throws Exception {
        ActualitePage page = new ActualitePage(List.of(detail()), 2, 20, 41, 3);
        when(actualiteFacade.listerActualites(any())).thenReturn(page);

        mockMvc.perform(get("/api/actualites").param("page", "2").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.currentPage").value(2))
                .andExpect(jsonPath("$.pagination.totalPages").value(3))
                .andExpect(jsonPath("$.pagination.totalItems").value(41))
                .andExpect(jsonPath("$.pagination.first").value(false))
                .andExpect(jsonPath("$.pagination.last").value(true));
    }

    private CreateActualiteCommand argThatCommandAuteurEquals(UUID auteurId) {
        return org.mockito.ArgumentMatchers.argThat(cmd -> cmd.auteurId().equals(auteurId));
    }
}