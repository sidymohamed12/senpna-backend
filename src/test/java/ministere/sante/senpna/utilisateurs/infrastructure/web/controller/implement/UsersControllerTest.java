package ministere.sante.senpna.utilisateurs.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.utilisateurs.application.facade.UserManagementFacade;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserPage;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.AssignRoleRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.CreateUserRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.UpdateUserRequest;

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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) — cf. {@code AppelOffresControllerTest} pour le
 * périmètre exact et {@code EspaceFournisseurAppelOffresControllerTest}
 * pour le raisonnement sur la simulation du {@link SecurityContextHolder}
 * (ce contrôleur lit {@code CurrentUser.getUserId()} pour tracer
 * l'acteur de chaque opération).
 */
@WebMvcTest(controllers = UsersController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("UsersController — HTTP")
class UsersControllerTest {

        @Autowired
        MockMvc mockMvc;
        @Autowired
        ObjectMapper objectMapper;
        @MockitoBean
        UserManagementFacade userManagementFacade;

        UUID acteurId;

        @BeforeEach
        void authentifieActeur() {
                acteurId = UUID.randomUUID();
                CurrentUser principal = mock(CurrentUser.class);
                when(principal.getUserId()).thenReturn(acteurId);
                SecurityContextHolder.getContext()
                                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
        }

        @AfterEach
        void nettoieContexteSecurite() {
                SecurityContextHolder.clearContext();
        }

        private UserDetail userDetail(UUID id) {
                return new UserDetail(id, "Diallo", "Mamadou", "mamadou.diallo@sante.gouv.sn", null, true, Set.of(),
                                null,
                                null, null, null, null);
        }

        @Test
        @DisplayName("POST /api/users → 201, fournisseurId transmis dans la commande")
        void creer_201_transmetFournisseurId() throws Exception {
                UUID roleId = UUID.randomUUID();
                UUID fournisseurId = UUID.randomUUID();
                UUID nouvelId = UUID.randomUUID();
                when(userManagementFacade.creer(any()))
                                .thenReturn(new CreatedUser(userDetail(nouvelId), "Mdp@Temp1234!"));

                CreateUserRequest request = new CreateUserRequest("Espace Fournisseur", "Pharma Sénégal",
                                "fournisseur@pharma-senegal.sn", null, Set.of(roleId), null, fournisseurId);

                mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.results.user.id").value(nouvelId.toString()))
                                .andExpect(jsonPath("$.results.motDePasseTemporaire").value("Mdp@Temp1234!"))
                                .andExpect(jsonPath("$.type").value("USER_CREATED"));

                ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
                verify(userManagementFacade).creer(captor.capture());
                assertThat(captor.getValue().fournisseurId()).isEqualTo(fournisseurId);
                assertThat(captor.getValue().acteurId()).isEqualTo(acteurId);
        }

        @Test
        @DisplayName("GET /api/users → 200")
        void lister_200() throws Exception {
                when(userManagementFacade.lister(any()))
                                .thenReturn(new UserPage(List.of(userDetail(UUID.randomUUID())), 0, 20, 1, 1));

                mockMvc.perform(get("/api/users"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results").isArray());
        }

        @Test
        @DisplayName("GET /api/users/{id} → 200")
        void obtenir_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(userManagementFacade.obtenir(any())).thenReturn(userDetail(id));

                mockMvc.perform(get("/api/users/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.results.id").value(id.toString()));
        }

        @Test
        @DisplayName("PUT /api/users/{id} → 200")
        void modifier_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(userManagementFacade.modifier(any())).thenReturn(userDetail(id));

                UpdateUserRequest request = new UpdateUserRequest("Diallo", "Mamadou", null);

                mockMvc.perform(put("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("USER_UPDATED"));
        }

        @Test
        @DisplayName("PATCH /api/users/{id}/activer → 200")
        void activer_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(userManagementFacade.activer(any())).thenReturn(userDetail(id));

                mockMvc.perform(patch("/api/users/{id}/activer", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("USER_ACTIVATED"));
        }

        @Test
        @DisplayName("PATCH /api/users/{id}/desactiver → 200")
        void desactiver_200() throws Exception {
                UUID id = UUID.randomUUID();
                when(userManagementFacade.desactiver(any())).thenReturn(userDetail(id));

                mockMvc.perform(patch("/api/users/{id}/desactiver", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("USER_DEACTIVATED"));
        }

        @Test
        @DisplayName("POST /api/users/{id}/roles → 200")
        void assignerRole_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID roleId = UUID.randomUUID();
                when(userManagementFacade.assignerRole(any())).thenReturn(userDetail(id));

                mockMvc.perform(post("/api/users/{id}/roles", id).contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new AssignRoleRequest(roleId))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("ROLE_ASSIGNED"));
        }

        @Test
        @DisplayName("DELETE /api/users/{id}/roles/{roleId} → 200")
        void retirerRole_200() throws Exception {
                UUID id = UUID.randomUUID();
                UUID roleId = UUID.randomUUID();
                when(userManagementFacade.retirerRole(any())).thenReturn(userDetail(id));

                mockMvc.perform(delete("/api/users/{id}/roles/{roleId}", id, roleId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.type").value("ROLE_REVOKED"));
        }
}