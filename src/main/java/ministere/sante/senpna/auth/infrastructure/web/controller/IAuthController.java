package ministere.sante.senpna.auth.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import ministere.sante.senpna.auth.infrastructure.web.controller.implement.AuthController;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ForgotPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LoginRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.LogoutRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.RefreshTokenRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResendOtpRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.ResetPasswordRequest;
import ministere.sante.senpna.auth.infrastructure.web.dto.request.VerifyOtpRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Contrat API du contrôleur Auth.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * exemples, codes de retour) séparément de {@link AuthController}, qui
 * n'en est que l'implémentation métier — cf. règle « une classe, une
 * raison de changer » (SOLID/SRP) : documenter l'API et l'exposer via
 * HTTP ne sont pas la même responsabilité.
 * </p>
 *
 * <p>
 * Les annotations de routage et de binding ({@code @PostMapping},
 * {@code @GetMapping}, {@code @RequestBody}, {@code @Valid}...) sont
 * portées ici : Spring MVC les résout sur l'interface implémentée par le
 * bean contrôleur, il n'y a donc rien à répéter côté implémentation.
 * L'autorisation fine des routes reste définie dans {@code SecurityConfig}
 * (routes publiques listées explicitement) plutôt que par
 * {@code @PreAuthorize} — ce contrôleur n'en porte donc pas.
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
 * <h3>Routes authentifiées</h3>
 *
 * <pre>
 * GET  /api/auth/me     → profil de l'utilisateur courant (déduit du JWT)
 * POST /api/auth/logout → révoque immédiatement l'access token courant
 *                         (et le refresh token si transmis)
 * </pre>
 */
@Tag(name = "Authentification", description = """
        Connexion, cycle de mot de passe oublié par OTP (email ou SMS), rotation de tokens JWT \
        et déconnexion. Toutes les routes sont publiques sauf `GET /me` et `POST /logout`, qui \
        exigent un access token JWT valide (en-tête `Authorization: Bearer <token>`).""")
@RequestMapping("/api/auth")
public interface IAuthController {

    @Operation(summary = "Se connecter", description = """
            Authentifie un utilisateur par email/mot de passe et retourne une paire de tokens \
            JWT (`accessToken` + `refreshToken`) ainsi que le résumé de son profil.

            Après plusieurs échecs de connexion consécutifs, le compte est temporairement \
            verrouillé (`ACCOUNT_LOCKED`) — indépendamment de la validité du mot de passe \
            soumis ensuite.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connexion réussie", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "LOGIN_SUCCESS",
                      "message": "Connexion réussie",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": {
                        "accessToken": "eyJhbGciOi...",
                        "refreshToken": "eyJhbGciOi...",
                        "expiresInSeconds": 3600,
                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                        "nom": "Ndiaye",
                        "prenom": "Fatou",
                        "email": "fatou.ndiaye@senpna.sn",
                        "roles": ["PHARMACIEN_PRA"],
                        "entrepotId": "8a2c...",
                        "structureSanitaireId": null
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : email ou mot de passe manquant/mal formé"),
            @ApiResponse(responseCode = "401", description = "Identifiant ou mot de passe incorrect (INVALID_CREDENTIALS)"),
            @ApiResponse(responseCode = "403", description = "Compte verrouillé (ACCOUNT_LOCKED) ou désactivé (ACCOUNT_INACTIVE)")
    })
    @PostMapping("/login")
    ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Demander un code de réinitialisation de mot de passe", description = """
            Première étape du parcours « mot de passe oublié » : génère un code OTP à durée de \
            vie limitée et l'envoie sur le canal demandé (`EMAIL` ou `SMS`), à la destination \
            résolue à partir du profil de l'utilisateur (email ou numéro de téléphone).

            Répondre 200 ne garantit pas la remise effective du message (délai réseau opérateur \
            SMS/SMTP) — seul un échec de transport détecté immédiatement remonte une erreur.

            Un nouvel appel avant expiration du délai anti-spam renvoie 429 (`OTP_COOLDOWN`) — \
            voir aussi `POST /resend-otp` pour ce cas d'usage explicite.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code envoyé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "OTP_SENT",
                      "message": "Un code de vérification a été envoyé",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : email ou channel manquant/invalide"),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet email (USER_NOT_FOUND)"),
            @ApiResponse(responseCode = "422", description = "Envoi du code impossible : canal indisponible pour ce profil, ou échec de transport (OTP_SEND_FAILED)"),
            @ApiResponse(responseCode = "429", description = "Délai anti-spam non écoulé depuis le dernier envoi (OTP_COOLDOWN)")
    })
    @PostMapping("/forgot-password")
    ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(summary = "Renvoyer un code de réinitialisation", description = """
            Identique à `POST /forgot-password` (même règle de délai anti-spam) — endpoint \
            distinct pour permettre au frontend de distinguer, côté analytics/UX, une demande \
            initiale d'une demande de renvoi (ex. lien « je n'ai rien reçu »).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code renvoyé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "OTP_RESENT",
                      "message": "Un nouveau code de vérification a été envoyé",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)"),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet email (USER_NOT_FOUND)"),
            @ApiResponse(responseCode = "422", description = "Envoi du code impossible : canal indisponible pour ce profil, ou échec de transport (OTP_SEND_FAILED)"),
            @ApiResponse(responseCode = "429", description = "Délai anti-spam non écoulé depuis le dernier envoi (OTP_COOLDOWN)")
    })
    @PostMapping("/resend-otp")
    ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequest request);

    @Operation(summary = "Vérifier le code OTP", description = """
            Deuxième étape du parcours « mot de passe oublié » : valide le code reçu par \
            l'utilisateur et, s'il est correct, délivre un jeton de réinitialisation \
            (`resetToken`) à usage unique, valide 5 minutes, à fournir tel quel à \
            `POST /reset-password`.

            Après un nombre maximal de tentatives incorrectes, le code est bloqué \
            (`OTP_MAX_ATTEMPTS`) — l'utilisateur doit en redemander un nouveau via \
            `POST /resend-otp`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code vérifié", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "OTP_VERIFIED",
                      "message": "Code vérifié avec succès",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": { "resetToken": "9f1c2e3a-...-resettoken" }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : email manquant, code non conforme au format attendu"),
            @ApiResponse(responseCode = "401", description = "Code incorrect (OTP_INVALID) ou expiré (OTP_EXPIRED)"),
            @ApiResponse(responseCode = "403", description = "Nombre maximal de tentatives atteint (OTP_MAX_ATTEMPTS)"),
            @ApiResponse(responseCode = "404", description = "Aucun utilisateur pour cet email (USER_NOT_FOUND)")
    })
    @PostMapping("/verify")
    ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request);

    @Operation(summary = "Réinitialiser le mot de passe", description = """
            Dernière étape du parcours « mot de passe oublié » : consomme le `resetToken` \
            obtenu via `POST /verify` (usage unique — un second appel avec le même jeton \
            échoue) et applique le nouveau mot de passe.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mot de passe modifié", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "PASSWORD_RESET",
                      "message": "Mot de passe modifié avec succès",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": null
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : resetToken manquant, mot de passe trop court (< 8 caractères)"),
            @ApiResponse(responseCode = "401", description = "Jeton de réinitialisation invalide, expiré ou déjà utilisé (RESET_TOKEN_INVALID)"),
            @ApiResponse(responseCode = "404", description = "Utilisateur associé au jeton introuvable (USER_NOT_FOUND)")
    })
    @PostMapping("/reset-password")
    ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(summary = "Rafraîchir la paire de tokens", description = """
            Échange un refresh token valide contre une nouvelle paire access/refresh \
            (pattern *refresh token rotation*) : l'ancien refresh token est immédiatement \
            révoqué, qu'il soit réutilisé par un client légitime ou un attaquant qui l'aurait \
            intercepté — dans les deux cas le second usage échoue.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens rafraîchis", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "TOKEN_REFRESHED",
                      "message": "Token rafraîchi avec succès",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": {
                        "accessToken": "eyJhbGciOi...",
                        "refreshToken": "eyJhbGciOi...",
                        "expiresInSeconds": 3600
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : refreshToken manquant"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide, expiré, déjà révoqué, ou d'un type différent (INVALID_REFRESH_TOKEN)"),
            @ApiResponse(responseCode = "403", description = "Compte désactivé entre-temps (ACCOUNT_INACTIVE)")
    })
    @PostMapping("/refresh")
    ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "Obtenir le profil de l'utilisateur courant", description = """
            Retourne le résumé du profil de l'utilisateur authentifié — identité déduite du \
            JWT fourni en en-tête `Authorization`, pas d'un quelconque paramètre.""")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil récupéré", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "ME_SUCCESS",
                      "message": "Profil récupéré",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "nom": "Ndiaye",
                        "prenom": "Fatou",
                        "email": "fatou.ndiaye@senpna.sn",
                        "roles": ["PHARMACIEN_PRA"],
                        "entrepotId": "8a2c...",
                        "structureSanitaireId": null
                      }
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré"),
            @ApiResponse(responseCode = "404", description = "Utilisateur du JWT introuvable en base (compte supprimé entre-temps) (USER_NOT_FOUND)")
    })
    @GetMapping("/me")
    ResponseEntity<Map<String, Object>> me();

    @Operation(summary = "Se déconnecter", description = """
            Révoque immédiatement l'access token courant (extrait de l'en-tête \
            `Authorization`) en le plaçant en liste noire — il devient inutilisable même \
            avant son expiration naturelle. Si `refreshToken` est fourni dans le corps, il est \
            également révoqué pour empêcher toute régénération ultérieure d'un access token.

            Idempotent et silencieux sur un token déjà invalide ou malformé : une déconnexion \
            ne doit jamais échouer côté client.""")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Déconnexion réussie", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "type": "LOGOUT_SUCCESS",
                      "message": "Déconnexion réussie",
                      "timestamp": "2024-01-01T12:00:00Z",
                      "results": null
                    }
                    """))),
            @ApiResponse(responseCode = "401", description = "JWT absent, invalide ou expiré")
    })
    @PostMapping("/logout")
    ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest httpRequest,
            @Parameter(description = "Refresh token à révoquer en plus de l'access token — optionnel") @RequestBody(required = false) LogoutRequest request);
}