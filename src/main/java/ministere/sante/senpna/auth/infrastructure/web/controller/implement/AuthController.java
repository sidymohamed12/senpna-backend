package ministere.sante.senpna.auth.infrastructure.web.controller.implement;

import jakarta.servlet.http.HttpServletRequest;
import ministere.sante.senpna.auth.application.facade.AuthFacade;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LogoutCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.infrastructure.web.controller.IAuthController;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ForgotPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LoginRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LogoutRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResendOtpRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResetPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.VerifyOtpRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.response.AuthTokensResponse;
import ministere.sante.senpna.auth.infrastructure.web.dto.response.MeResponse;
import ministere.sante.senpna.auth.infrastructure.web.dto.response.VerifyOtpResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController implements IAuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @Override
    public ResponseEntity<Map<String, Object>> login(LoginRequest request) {
        LoginResult result = authFacade.login(new LoginCommand(request.email(), request.password()));

        AuthTokensResponse body = new AuthTokensResponse(
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                result.tokens().expiresInSeconds(),
                result.user().id(),
                result.user().nom(),
                result.user().prenom(),
                result.user().email(),
                result.user().roles(), result.user().entrepotId(),
                result.user().structureSanitaireId());

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, body, "LOGIN_SUCCESS", "Connexion réussie"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> forgotPassword(ForgotPasswordRequest request) {
        authFacade.forgotPassword(new ForgotPasswordCommand(request.email(), request.channel()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "OTP_SENT",
                "Un code de vérification a été envoyé"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> resendOtp(ResendOtpRequest request) {
        authFacade.resendOtp(new ResendOtpCommand(request.email(), request.channel()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "OTP_RESENT",
                "Un nouveau code de vérification a été envoyé"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> verifyOtp(VerifyOtpRequest request) {
        VerifyOtpResult result = authFacade.verifyOtp(new VerifyOtpCommand(request.email(), request.code()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK,
                new VerifyOtpResponse(result.resetToken()), "OTP_VERIFIED", "Code vérifié avec succès"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> resetPassword(ResetPasswordRequest request) {
        authFacade.resetPassword(new ResetPasswordCommand(request.resetToken(), request.nouveauMotDePasse()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "PASSWORD_RESET",
                "Mot de passe modifié avec succès"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> refresh(RefreshTokenRequest request) {
        AuthTokens tokens = authFacade.refresh(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, tokens, "TOKEN_REFRESHED",
                "Token rafraîchi avec succès"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserSummary summary = authFacade.me(new MeQuery(email));

        MeResponse body = new MeResponse(
                summary.id(), summary.nom(), summary.prenom(), summary.email(), summary.roles(), summary.entrepotId(),
                summary.structureSanitaireId());

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, body, "ME_SUCCESS", "Profil récupéré"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest httpRequest, LogoutRequest request) {
        String accessToken = extractBearerToken(httpRequest);
        String refreshToken = request != null ? request.refreshToken() : null;

        authFacade.logout(new LogoutCommand(accessToken, refreshToken));

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "LOGOUT_SUCCESS",
                "Déconnexion réussie"));
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}