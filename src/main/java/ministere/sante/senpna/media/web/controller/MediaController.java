package ministere.sante.senpna.media.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;
import ministere.sante.senpna.media.domain.port.in.GenererPresignedUrlUseCase;
import ministere.sante.senpna.media.web.mapper.MediaWebMapper;
import ministere.sante.senpna.media.web.request.PresignedUrlRequest;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST — gestion des médias (images, avatars).
 *
 * <h3>Feature dédiée et réutilisable</h3>
 * <p>
 * Ce contrôleur est indépendant de toute feature métier.
 * Il peut être utilisé par {@code actualite/}, {@code user/} ou toute
 * autre feature nécessitant un upload de fichier.
 * </p>
 *
 * <h3>Flux Presigned URL — pourquoi 3 steps ?</h3>
 * 
 * <pre>
 * Step 1 — POST /api/medias/presigned-url  (~2ms, JSON léger)
 *   → Validation Content-Type + taille
 *   → Génération clé UUID côté serveur
 *   → Signature de l'URL via AWS SDK
 *   → Retourne uploadUrl + publicUrl
 *
 * Step 2 — PUT {uploadUrl}  (~100-500ms, fichier complet)
 *   → Le fichier va DIRECTEMENT de l'utilisateur vers R2/MinIO
 *   → Votre serveur Spring n'est PAS impliqué
 *   → Zéro RAM consommée côté serveur
 *   → Zéro bande passante serveur utilisée
 *
 * Step 3 — POST /api/actualites  (~5ms, JSON léger)
 *   → imageUrl = publicUrl reçue au Step 1
 *   → Transaction propre, uniquement de la persistance DB
 * </pre>
 *
 * <h3>Sécurité</h3>
 * <ul>
 * <li>Authentification requise pour générer une URL (évite le spam d'URLs)</li>
 * <li>La clé est générée côté serveur — le client ne contrôle pas le
 * chemin</li>
 * <li>Content-Type et taille validés avant génération</li>
 * <li>TTL court (10 min par défaut) — URL inutilisable après expiration</li>
 * <li>La Presigned URL n'autorise qu'un seul PUT sur la clé ciblée</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/medias")
@Tag(name = "Médias", description = "Upload d'images via Presigned URL — upload direct frontend → stockage R2/MinIO")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

        private final GenererPresignedUrlUseCase genererPresignedUrlUseCase;
        private final MediaWebMapper mapper;

        public MediaController(
                        GenererPresignedUrlUseCase genererPresignedUrlUseCase,
                        MediaWebMapper mapper) {
                this.genererPresignedUrlUseCase = genererPresignedUrlUseCase;
                this.mapper = mapper;
        }

        @Operation(summary = "Générer une URL pré-signée pour upload direct", description = """
                        Génère une URL sécurisée permettant au frontend d'uploader un fichier **directement**
                        vers le stockage (R2/MinIO) **sans transiter par le serveur applicatif**.

                        **Flux en 3 étapes :**
                        1. `POST /api/medias/presigned-url` — obtenir `uploadUrl` (valide 10 min) et `publicUrl`
                        2. `PUT {uploadUrl}` avec le fichier binaire, header `Content-Type` obligatoire — upload direct
                        3. `POST /api/actualites` avec `imageUrl = publicUrl` et `imageKey = key`

                        **Types de médias disponibles :**
                        | `mediaType`          | Formats acceptés        | Taille max |
                        |---|---|---|
                        | `ACTUALITE`      | image/jpeg, image/png, image/webp | 50 MB |
                        | `PROJET`         | image/jpeg, image/png, image/webp | 5 MB |
                        | `MEDIATHEQUE`    | image/jpeg, image/png, image/webp, image/gif, video/mp4 | 50 MB |
                        | `FICHE_DE_POSTE` | image/jpeg, image/png, image/webp | 5 MB |
                        | `CV`             | application/pdf | 5 MB |
                        | `LETTRE_DE_MOTIVATION` | application/pdf | 5 MB |

                        `results` est un `PresignedUrlResponse` :
                        - `uploadUrl` — URL signée pour le PUT (expire à `expiresAt`)
                        - `publicUrl` — URL publique permanente à stocker dans l'actualité
                        - `key` — clé bucket (à passer dans `imageKey` lors de la création de l'actualité)
                        - `expiresAt` — timestamp d'expiration de `uploadUrl`

                        Rôle requis : **tout utilisateur authentifié**.
                        """)
        @RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = PresignedUrlRequest.class), examples = {
                        @ExampleObject(name = "Image actualité JPEG", value = """
                                        {
                                          "mediaType": "ACTUALITE",
                                          "contentType": "image/jpeg",
                                          "tailleBytes": 2097152
                                        }
                                        """),
                        @ExampleObject(name = "Avatar utilisateur PNG", value = """
                                        {
                                          "mediaType": "AVATAR_UTILISATEUR",
                                          "contentType": "image/png",
                                          "tailleBytes": 524288
                                        }
                                        """)
        }))
        @ApiResponse(responseCode = "200", description = "URL pré-signée générée — valide 10 minutes", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "type": "PRESIGNED_URL_GENEREE",
                          "message": "URL d'upload générée. Valide 10 minutes. Faites un PUT sur uploadUrl avec le header Content-Type.",
                          "timestamp": "2024-01-01T12:00:00Z",
                          "results": {
                            "uploadUrl": "https://r2.senpna.sn/cv/550e8400.jpg?X-Amz-Signature=abc...",
                            "publicUrl": "https://cdn.senpna.sn/cv/550e8400-e29b-41d4-a716-446655440000.jpg",
                            "key": "cv/550e8400-e29b-41d4-a716-446655440000.jpg",
                            "expiresAt": "2024-01-01T12:10:00Z"
                          }
                        }
                        """)))
        @ApiResponse(responseCode = "400", description = "mediaType invalide | contentType non autorisé | taille > limite")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @PostMapping("/presigned-url")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> genererPresignedUrl(
                        @Valid @org.springframework.web.bind.annotation.RequestBody PresignedUrlRequest request) {

                PresignedUrlResult result = genererPresignedUrlUseCase.generer(
                                mapper.versCommand(request));

                return ResponseEntity.ok(RestResponse.response(
                                HttpStatus.OK,
                                mapper.versResponse(result),
                                "PRESIGNED_URL_GENEREE",
                                "URL d'upload générée. Valide " +
                                                java.time.Duration.between(java.time.Instant.now(), result.expiresAt())
                                                                .toMinutes()
                                                +
                                                " minutes. Faites un PUT sur uploadUrl avec le header Content-Type."));
        }

        @Operation(summary = "Générer une URL pré-signée pour upload direct (accès public)", description = """
                        Variante sans authentification de `POST /api/medias/presigned-url`, destinée aux pièces jointes
                        d'un formulaire public — à ce jour uniquement `CV` et `LETTRE_DE_MOTIVATION` (cf. formulaire de
                        postulation, `POST /api/candidatures`). Tout autre `mediaType` est refusé (403).

                        Mêmes garanties de sécurité que l'endpoint authentifié : clé générée côté serveur, Content-Type
                        et taille validés, TTL court, un seul PUT autorisé sur la clé signée.
                        """)
        @ApiResponse(responseCode = "200", description = "URL pré-signée générée — valide 10 minutes")
        @ApiResponse(responseCode = "400", description = "contentType non autorisé | taille > limite")
        @ApiResponse(responseCode = "403", description = "mediaType non éligible à un usage public (MEDIA_TYPE_NON_AUTORISE_PUBLIQUEMENT)")
        @PostMapping("/presigned-url/public")
        public ResponseEntity<Map<String, Object>> genererPresignedUrlPublic(
                        @Valid @org.springframework.web.bind.annotation.RequestBody PresignedUrlRequest request) {

                PresignedUrlResult result = genererPresignedUrlUseCase.genererPublique(
                                mapper.versCommand(request));

                return ResponseEntity.ok(RestResponse.response(
                                HttpStatus.OK,
                                mapper.versResponse(result),
                                "PRESIGNED_URL_GENEREE",
                                "URL d'upload générée. Valide " +
                                                java.time.Duration.between(java.time.Instant.now(), result.expiresAt())
                                                                .toMinutes()
                                                +
                                                " minutes. Faites un PUT sur uploadUrl avec le header Content-Type."));
        }
}
