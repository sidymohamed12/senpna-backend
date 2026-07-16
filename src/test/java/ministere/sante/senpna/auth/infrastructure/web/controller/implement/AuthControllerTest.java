package ministere.sante.senpna.auth.infrastructure.web.controller.implement;

import com.fasterxml.jackson.databind.ObjectMapper;

import ministere.sante.senpna.auth.application.facade.AuthFacade;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.domain.valueobject.OtpChannel;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ForgotPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LoginRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LogoutRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResendOtpRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResetPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.VerifyOtpRequest;
import ministere.sante.senpna.config.JwtAuthenticationFilter;
import ministere.sante.senpna.config.SecurityConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP (MockMvc) du contrôleur Auth — vérifient le câblage
 * route/statut/sérialisation et la délégation à {@link AuthFacade}, pas
 * les règles métier (déjà couvertes par les tests de use case).
 */
@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class, JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AuthController — HTTP")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthFacade authFacade;

    private UserSummary userSummary(UUID id) {
        return new UserSummary(id, "Diallo", "Awa", "awa.diallo@example.com", Set.of("ADMIN_PNA"),
                UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    @DisplayName("POST /api/auth/login → 200, délègue à la façade")
    void login_200() throws Exception {
        UUID id = UUID.randomUUID();
        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", 3600L);
        when(authFacade.login(any())).thenReturn(new LoginResult(tokens, userSummary(id)));

        LoginRequest request = new LoginRequest("awa.diallo@example.com", "MotDePasse123");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.results.accessToken").value("access-token"))
                .andExpect(jsonPath("$.results.userId").value(id.toString()));

        verify(authFacade).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password → 200, délègue à la façade")
    void forgotPassword_200() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("awa.diallo@example.com", OtpChannel.EMAIL);

        mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OTP_SENT"));

        verify(authFacade).forgotPassword(any());
    }

    @Test
    @DisplayName("POST /api/auth/resend-otp → 200, délègue à la façade")
    void resendOtp_200() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest("awa.diallo@example.com", OtpChannel.SMS);

        mockMvc.perform(post("/api/auth/resend-otp").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OTP_RESENT"));

        verify(authFacade).resendOtp(any());
    }

    @Test
    @DisplayName("POST /api/auth/verify → 200, délègue à la façade")
    void verifyOtp_200() throws Exception {
        when(authFacade.verifyOtp(any())).thenReturn(new VerifyOtpResult("reset-token"));

        VerifyOtpRequest request = new VerifyOtpRequest("awa.diallo@example.com", "123456");

        mockMvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OTP_VERIFIED"))
                .andExpect(jsonPath("$.results.resetToken").value("reset-token"));
    }

    @Test
    @DisplayName("POST /api/auth/reset-password → 200, délègue à la façade")
    void resetPassword_200() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token", "NouveauMotDePasse123");

        mockMvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PASSWORD_RESET"));

        verify(authFacade).resetPassword(any());
    }

    @Test
    @DisplayName("POST /api/auth/refresh → 200, délègue à la façade")
    void refresh_200() throws Exception {
        when(authFacade.refresh(any())).thenReturn(new AuthTokens("access-2", "refresh-2", 3600L));

        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TOKEN_REFRESHED"))
                .andExpect(jsonPath("$.results.accessToken").value("access-2"));
    }

    @Test
    @DisplayName("GET /api/auth/me → 200, résout l'utilisateur courant depuis le contexte de sécurité")
    @WithMockUser(username = "awa.diallo@example.com")
    void me_200() throws Exception {
        UUID id = UUID.randomUUID();
        when(authFacade.me(any())).thenReturn(userSummary(id));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ME_SUCCESS"))
                .andExpect(jsonPath("$.results.email").value("awa.diallo@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/logout → 200, extrait le token Bearer et transmet le refresh token")
    void logout_200() throws Exception {
        LogoutRequest request = new LogoutRequest("refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOGOUT_SUCCESS"));

        verify(authFacade).logout(any());
    }

    @Test
    @DisplayName("POST /api/auth/logout sans corps ni en-tête Authorization → 200")
    void logout_sansCorpsNiAuthorization_200() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("LOGOUT_SUCCESS"));

        verify(authFacade).logout(any());
    }
}
