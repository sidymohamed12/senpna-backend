package ministere.sante.senpna.actualite.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.actualite.infrastructure.web.controller.implement.ActualitesController;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.CreateActualiteRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.UpdateActualiteRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Actualités.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * paramètres, exemples, codes de retour) séparément de
 * {@link ActualitesController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP) : documenter
 * l'API et l'exposer via HTTP ne sont pas la même responsabilité.
 * </p>
 *
 * <p>
 * Les annotations de routage et de binding ({@code @GetMapping},
 * {@code @RequestParam}, {@code @RequestBody}, {@code @Valid}...) sont
 * portées ici : Spring MVC les résout sur l'interface implémentée par le
 * bean contrôleur, il n'y a donc rien à répéter côté implémentation. Les
 * annotations de sécurité ({@code @PreAuthorize}) restent volontairement
 * sur {@link ActualitesController}, au plus près du code qu'elles
 * protègent.
 * </p>
 *
 * <pre>
 * POST   /api/actualites                    {categorie, titre, description?, medias?, tags?}
 * PUT    /api/actualites/{id}                {categorie, titre, description?, medias?, tags?}
 * PATCH  /api/actualites/{id}/publier
 * PATCH  /api/actualites/{id}/desactiver
 * PATCH  /api/actualites/{id}/brouillon
 * GET    /api/actualites/{id}
 * GET    /api/actualites?q=&categorie=&statut=&page=&size=&sortBy=&sortDirection=
 * GET    /api/actualites/public/{id}
 * GET    /api/actualites/public?q=&categorie=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Actualités", description = """
    Gestion éditoriale des actualités (vie associative, projets, partenariats...) : création, \
    modification, cycle de vie de publication (brouillon → publié → désactivé) et consultation, \
    y compris les endpoints publics `/public` et `/public/{id}` ne nécessitant pas d'authentification.""")
@RequestMapping("/api/actualites")
public interface IActualitesController {

  @Operation(summary = "Créer une actualité (à l'état brouillon)", description = """
      Crée une nouvelle actualité, toujours à l'état `BROUILLON` — la publication est une \
      décision éditoriale distincte de la saisie (cf. `PATCH /{id}/publier`).

      `categorie` doit être l'une des valeurs de `CategorieActualite` : `VIE_ASSOCIATIVE`, \
      `PROJET`, `PARTENARIAT`, `EVENEMENT`, `COMMUNIQUE`, `AUTRE`. Chaque média de \
      `medias` doit avoir un `type` valant `IMAGE` ou `VIDEO` (cf. `POST /api/medias/presigned-url` \
      pour obtenir l'URL d'un média uploadé). L'auteur est déduit du token JWT, pas du corps \
      de la requête.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "201", description = "Actualité créée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "ACTUALITE_CREATED",
        "message": "Actualité créée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "categorie": "PROJET",
          "titre": "Lancement du projet X",
          "description": "Une courte description du projet.",
          "medias": [
            { "id": "6b1f...", "type": "IMAGE", "url": "https://cdn.senpna.sn/actualite/6b1f.jpg", "ordre": 0 }
          ],
          "auteurId": "8a2c...",
          "auteurNom": "Fatou Ndiaye",
          "tags": ["santé", "senegal"],
          "statut": "BROUILLON",
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-01T12:00:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : titre/catégorie manquant, tailles dépassées, type de média incorrect")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer une actualité")
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateActualiteRequest request);

  @Operation(summary = "Modifier le contenu d'une actualité", description = """
      Remplace intégralement le contenu éditorial (`categorie`, `titre`, `description`, \
      `medias`, `tags`) d'une actualité existante, quel que soit son statut courant. \
      Le statut de publication n'est **pas** modifié par cet endpoint — utilisez \
      `PATCH /{id}/publier`, `/desactiver` ou `/brouillon` pour le cycle de vie éditorial.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Actualité modifiée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ACTUALITE_UPDATED",
        "message": "Actualité modifiée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "categorie": "PROJET",
          "titre": "Lancement du projet X — mise à jour",
          "description": "Description mise à jour.",
          "medias": [],
          "auteurId": "8a2c...",
          "auteurNom": "Fatou Ndiaye",
          "tags": ["santé"],
          "statut": "BROUILLON",
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-02T09:30:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier une actualité")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable (ACTUALITE_NOT_FOUND)")
  @PutMapping("/{id}")
  ResponseEntity<Map<String, Object>> modifier(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id,
      @Valid @RequestBody UpdateActualiteRequest request);

  @Operation(summary = "Publier une actualité", description = """
      Fait passer l'actualité à l'état `PUBLIE`, la rendant visible sur les endpoints \
      publics (`GET /public`, `GET /public/{id}`). Opération idempotente : appeler cet \
      endpoint sur une actualité déjà publiée ne renvoie pas d'erreur.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Actualité publiée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ACTUALITE_PUBLIEE",
        "message": "Actualité publiée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": { "id": "550e8400-e29b-41d4-a716-446655440000", "statut": "PUBLIE" }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à publier une actualité")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable (ACTUALITE_NOT_FOUND)")
  @PatchMapping("/{id}/publier")
  ResponseEntity<Map<String, Object>> publier(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id);

  @Operation(summary = "Désactiver une actualité", description = """
      Fait passer l'actualité à l'état `DESACTIVE` — le contenu est retiré de la diffusion \
      publique **sans être supprimé** (conservé pour un usage interne ou une republication \
      ultérieure via `PATCH /{id}/publier`). Opération idempotente.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Actualité désactivée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ACTUALITE_DESACTIVEE",
        "message": "Actualité désactivée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": { "id": "550e8400-e29b-41d4-a716-446655440000", "statut": "DESACTIVE" }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à désactiver une actualité")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable (ACTUALITE_NOT_FOUND)")
  @PatchMapping("/{id}/desactiver")
  ResponseEntity<Map<String, Object>> desactiver(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id);

  @Operation(summary = "Remettre une actualité en brouillon", description = """
      Fait repasser l'actualité à l'état `BROUILLON`, quel que soit son statut courant \
      (`PUBLIE` ou `DESACTIVE`) — utile pour retravailler un contenu déjà publié avant de \
      le republier. Opération idempotente.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Actualité remise en brouillon", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ACTUALITE_BROUILLON",
        "message": "Actualité remise en brouillon",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": { "id": "550e8400-e29b-41d4-a716-446655440000", "statut": "BROUILLON" }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable (ACTUALITE_NOT_FOUND)")
  @PatchMapping("/{id}/brouillon")
  ResponseEntity<Map<String, Object>> remettreEnBrouillon(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id);

  @Operation(summary = "Obtenir une actualité (tous statuts)", description = """
      Retourne le détail complet d'une actualité quel que soit son statut \
      (`BROUILLON`, `PUBLIE` ou `DESACTIVE`) — usage back-office. Pour la consultation \
      publique, restreinte aux actualités publiées, voir `GET /public/{id}`.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Actualité récupérée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable (ACTUALITE_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id);

  @Operation(summary = "Obtenir une actualité publiée (accès public)", description = """
      Endpoint public, sans authentification, destiné au site vitrine. Ne retourne \
      l'actualité que si elle est au statut `PUBLIE` — une actualité en `BROUILLON` ou \
      `DESACTIVE` renvoie une 404, même si son identifiant est correct, afin de ne jamais \
      exposer de contenu non publié.""")
  @ApiResponse(responseCode = "200", description = "Actualité publiée récupérée")
  @ApiResponse(responseCode = "404", description = "Actualité introuvable ou non publiée (ACTUALITE_NOT_FOUND)")
  @GetMapping("/public/{id}")
  ResponseEntity<Map<String, Object>> obtenirPublic(
      @Parameter(description = "Identifiant de l'actualité") @PathVariable UUID id);

  @Operation(summary = "Lister les actualités (tous statuts, back-office)", description = """
      Recherche paginée sur l'ensemble des actualités, quel que soit leur statut — usage \
      back-office. `q` recherche en texte libre (titre/description), `categorie` et \
      `statut` filtrent respectivement sur `CategorieActualite` et `StatutActualite`. \
      `sortBy`/`sortDirection` pilotent le tri (ex. `createdAt`/`DESC`).

      Une valeur de `categorie` ou de `statut` non reconnue déclenche une erreur de \
      validation plutôt que d'être silencieusement ignorée.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Liste des actualités récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "ACTUALITES_LISTED",
        "message": "Liste des actualités récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "categorie": "PROJET",
            "titre": "Lancement du projet X",
            "description": "Une courte description du projet.",
            "medias": [],
            "auteurId": "8a2c...",
            "auteurNom": "Fatou Ndiaye",
            "tags": ["santé"],
            "statut": "PUBLIE",
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 3,
          "totalItems": 57,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "categorie ou statut invalide (CATEGORIE_ACTUALITE_INVALIDE / STATUT_ACTUALITE_INVALIDE)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le titre ou la description") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur la catégorie (VIE_ASSOCIATIVE, PROJET, PARTENARIAT, EVENEMENT, COMMUNIQUE, AUTRE)") @RequestParam(required = false) String categorie,
      @Parameter(description = "Filtre sur le statut (BROUILLON, PUBLIE, DESACTIVE)") @RequestParam(required = false) String statut,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

  @Operation(summary = "Lister les actualités publiées (accès public)", description = """
      Endpoint public, sans authentification, destiné au site vitrine. Équivalent à \
      `GET /api/actualites` mais **forcé** sur `statut=PUBLIE` — le paramètre `statut` \
      n'existe volontairement pas ici pour qu'un contenu non publié ne puisse jamais \
      être listé publiquement.

      `q` recherche en texte libre, `categorie` filtre sur `CategorieActualite`, \
      `sortBy`/`sortDirection` pilotent le tri (ex. `createdAt`/`DESC`).""")
  @ApiResponse(responseCode = "200", description = "Liste des actualités publiées récupérée")
  @ApiResponse(responseCode = "400", description = "categorie invalide (CATEGORIE_ACTUALITE_INVALIDE)")
  @GetMapping("/public")
  ResponseEntity<Map<String, Object>> listerPublic(
      @Parameter(description = "Recherche texte libre sur le titre ou la description") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur la catégorie (VIE_ASSOCIATIVE, PROJET, PARTENARIAT, EVENEMENT, COMMUNIQUE, AUTRE)") @RequestParam(required = false) String categorie,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}