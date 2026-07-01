package ministere.sante.senpna.auth.infrastructure.web.controller;

import jakarta.validation.Valid;
import ministere.sante.senpna.auth.application.facade.AuthFacade;
import ministere.sante.senpna.auth.domain.command.AuthCommands.AuthTokens;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ForgotPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.LoginResult;
import ministere.sante.senpna.auth.domain.command.AuthCommands.MeQuery;
import ministere.sante.senpna.auth.domain.command.AuthCommands.RefreshTokenCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResendOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.ResetPasswordCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.UserSummary;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpCommand;
import ministere.sante.senpna.auth.domain.command.AuthCommands.VerifyOtpResult;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ForgotPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LoginRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints d'authentification.
 *
 * <p>
 * Ne dépend que de {@link AuthFacade} — aucune connaissance des use cases
 * individuels ni de leurs implémentations.
 * </p>
 *
 * <h3>Routes publiques</h3>
 * 
 * <pre>
 * POST /api/auth/login
 * POST /api/auth/forgot-password  {email, channel}     → envoie un OTP
 * POST /api/auth/verify           {email, code}        → renvoie un resetToken (5 min)
 * POST /api/auth/reset-password   {resetToken, newPwd} → applique le nouveau mot de passe
 * POST /api/auth/resend-otp       {email, channel}      → renvoie un nouveau code (cooldown)
 * POST /api/auth/refresh
 * </pre>
 *
 * <h3>Route authentifiée</h3>
 * 
 * <pre>
 * GET /api/auth/me → profil de l'utilisateur courant (déduit du JWT)
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authFacade.login(new LoginCommand(request.email(), request.password()));

        AuthTokensResponse body = new AuthTokensResponse(
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                result.tokens().expiresInSeconds(),
                result.user().id(),
                result.user().nom(),
                result.user().prenom(),
                result.user().email(),
                result.user().roles());

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, body, "LOGIN_SUCCESS", "Connexion réussie"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authFacade.forgotPassword(new ForgotPasswordCommand(request.email(), request.channel()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "OTP_SENT",
                "Un code de vérification a été envoyé"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authFacade.resendOtp(new ResendOtpCommand(request.email(), request.channel()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "OTP_RESENT",
                "Un nouveau code de vérification a été envoyé"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResult result = authFacade.verifyOtp(new VerifyOtpCommand(request.email(), request.code()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK,
                new VerifyOtpResponse(result.resetToken()), "OTP_VERIFIED", "Code vérifié avec succès"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authFacade.resetPassword(new ResetPasswordCommand(request.resetToken(), request.nouveauMotDePasse()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, null, "PASSWORD_RESET",
                "Mot de passe modifié avec succès"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokens tokens = authFacade.refresh(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, tokens, "TOKEN_REFRESHED",
                "Token rafraîchi avec succès"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserSummary summary = authFacade.me(new MeQuery(email));

        MeResponse body = new MeResponse(
                summary.id(), summary.nom(), summary.prenom(), summary.email(), summary.roles());

        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, body, "ME_SUCCESS", "Profil récupéré"));
    }
}
