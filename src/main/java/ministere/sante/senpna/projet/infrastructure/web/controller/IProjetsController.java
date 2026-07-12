package ministere.sante.senpna.projet.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.projet.infrastructure.web.controller.implement.ProjetsController;
import ministere.sante.senpna.projet.infrastructure.web.dto.request.CreateProjetRequest;
import ministere.sante.senpna.projet.infrastructure.web.dto.request.UpdateProjetRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Projets.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * paramètres, exemples, codes de retour) séparément de
 * {@link ProjetsController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link ProjetsController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/projets                    {categorie, nom, description?, objectifs?, impacts?, imageUrl?}
 * PUT    /api/projets/{id}                {categorie, nom, description?, objectifs?, impacts?, imageUrl?}
 * PATCH  /api/projets/{id}/publier
 * PATCH  /api/projets/{id}/archiver
 * PATCH  /api/projets/{id}/desactiver
 * PATCH  /api/projets/{id}/brouillon
 * GET    /api/projets/{id}
 * GET    /api/projets?q=&categorie=&statut=&page=&size=&sortBy=&sortDirection=
 * GET    /api/projets/public/{id}
 * GET    /api/projets/public?q=&categorie=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Projets", description = """
    Gestion des projets portés ou soutenus par la PNA : création, modification, cycle de vie \
    éditorial (brouillon → publié → archivé/désactivé) et consultation, y compris les \
    endpoints publics `/public` et `/public/{id}` ne nécessitant pas d'authentification.""")
@RequestMapping("/api/projets")
public interface IProjetsController {

  @Operation(summary = "Créer un projet (à l'état brouillon)", description = """
      Crée un nouveau projet, toujours à l'état `BROUILLON` — la publication est une \
      décision éditoriale distincte de la saisie (cf. `PATCH /{id}/publier`).

      `categorie` doit être l'une des valeurs de `CategorieProjet` : `ENVIRONNEMENT`, \
      `SOCIAL`, `INNOVATION`, `EDUCATION`, `SANTE`, `AUTRE`. `objectifs` et `impacts` sont \
      chacun limités à 10 éléments de 200 caractères maximum.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "201", description = "Projet créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "PROJET_CREATED",
        "message": "Projet créé avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "categorie": "SANTE",
          "nom": "Vaccination rurale 2026",
          "description": "Campagne de vaccination dans les zones reculées.",
          "objectifs": ["Vacciner 50 000 enfants", "Former 200 agents de santé"],
          "impacts": ["Réduction de la mortalité infantile"],
          "imageUrl": "https://cdn.senpna.sn/projet/6b1f.jpg",
          "statut": "BROUILLON",
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-01T12:00:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : catégorie/nom manquant, tailles dépassées")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer un projet")
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateProjetRequest request);

  @Operation(summary = "Modifier le contenu d'un projet", description = """
      Remplace intégralement le contenu éditorial (`categorie`, `nom`, `description`, \
      `objectifs`, `impacts`, `imageUrl`) d'un projet existant, quel que soit son statut \
      courant. Le statut éditorial n'est **pas** modifié par cet endpoint — utilisez \
      `PATCH /{id}/publier`, `/archiver`, `/desactiver` ou `/brouillon` pour le cycle de \
      vie.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet modifié")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier un projet")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @PutMapping("/{id}")
  ResponseEntity<Map<String, Object>> modifier(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id,
      @Valid @RequestBody UpdateProjetRequest request);

  @Operation(summary = "Publier un projet", description = """
      Fait passer le projet à l'état `PUBLIE`, le rendant visible sur les endpoints publics \
      (`GET /public`, `GET /public/{id}`). Peut être appelé aussi bien depuis `BROUILLON` \
      que depuis `DESACTIVE` (republication). Opération idempotente : appeler cet endpoint \
      sur un projet déjà publié ne renvoie pas d'erreur.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet publié")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à publier un projet")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @PatchMapping("/{id}/publier")
  ResponseEntity<Map<String, Object>> publier(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Archiver un projet", description = """
      Fait passer le projet à l'état `ARCHIVE` — statut terminal du cycle de vie éditorial \
      (cf. `StatutProjet`), destiné aux projets clos définitivement. Contrairement à \
      `DESACTIVE`, un projet archivé n'est pas destiné à être republié, mais reste \
      consultable pour l'historique. Opération idempotente.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet archivé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à archiver un projet")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @PatchMapping("/{id}/archiver")
  ResponseEntity<Map<String, Object>> archiver(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Désactiver un projet", description = """
      Fait passer le projet à l'état `DESACTIVE` — le contenu est retiré de la diffusion \
      publique **sans être supprimé**, et peut être republié ultérieurement via \
      `PATCH /{id}/publier`. Opération idempotente.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet désactivé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à désactiver un projet")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @PatchMapping("/{id}/desactiver")
  ResponseEntity<Map<String, Object>> desactiver(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Remettre un projet en brouillon", description = """
      Fait repasser le projet à l'état `BROUILLON`, quel que soit son statut courant \
      (`PUBLIE`, `ARCHIVE` ou `DESACTIVE`) — utile pour retravailler un contenu déjà \
      publié ou archivé avant de le republier. Opération idempotente.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet remis en brouillon")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @PatchMapping("/{id}/brouillon")
  ResponseEntity<Map<String, Object>> remettreEnBrouillon(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Obtenir un projet (tous statuts)", description = """
      Retourne le détail complet d'un projet quel que soit son statut (`BROUILLON`, \
      `PUBLIE`, `ARCHIVE` ou `DESACTIVE`) — usage back-office. Pour la consultation \
      publique, restreinte aux projets publiés, voir `GET /public/{id}`.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Projet récupéré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Projet introuvable (PROJET_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Obtenir un projet publié (accès public)", description = """
      Endpoint public, sans authentification, destiné au site vitrine. Ne retourne le \
      projet que si son statut est `PUBLIE` — un projet en `BROUILLON`, `ARCHIVE` ou \
      `DESACTIVE` renvoie une 404, même si son identifiant est correct, afin de ne jamais \
      exposer de contenu non publié.""")
  @ApiResponse(responseCode = "200", description = "Projet publié récupéré")
  @ApiResponse(responseCode = "404", description = "Projet introuvable ou non publié (PROJET_NOT_FOUND)")
  @GetMapping("/public/{id}")
  ResponseEntity<Map<String, Object>> obtenirPublic(
      @Parameter(description = "Identifiant du projet") @PathVariable UUID id);

  @Operation(summary = "Lister les projets (tous statuts, back-office)", description = """
      Recherche paginée sur l'ensemble des projets, quel que soit leur statut — usage \
      back-office. `q` recherche en texte libre (nom/description), `categorie` et \
      `statut` filtrent respectivement sur `CategorieProjet` et `StatutProjet`. \
      `sortBy`/`sortDirection` pilotent le tri (ex. `createdAt`/`DESC`).

      Une valeur de `categorie` ou de `statut` non reconnue déclenche une erreur de \
      validation plutôt que d'être silencieusement ignorée.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
  @ApiResponse(responseCode = "200", description = "Liste des projets récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "PROJETS_LISTED",
        "message": "Liste des projets récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "categorie": "SANTE",
            "nom": "Vaccination rurale 2026",
            "description": "Campagne de vaccination dans les zones reculées.",
            "objectifs": ["Vacciner 50 000 enfants"],
            "impacts": ["Réduction de la mortalité infantile"],
            "imageUrl": "https://cdn.senpna.sn/projet/6b1f.jpg",
            "statut": "PUBLIE",
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 2,
          "totalItems": 23,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "categorie ou statut invalide (CATEGORIE_PROJET_INVALIDE / STATUT_PROJET_INVALIDE)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le nom ou la description") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur la catégorie (ENVIRONNEMENT, SOCIAL, INNOVATION, EDUCATION, SANTE, AUTRE)") @RequestParam(required = false) String categorie,
      @Parameter(description = "Filtre sur le statut (BROUILLON, PUBLIE, ARCHIVE, DESACTIVE)") @RequestParam(required = false) String statut,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

  @Operation(summary = "Lister les projets publiés (accès public)", description = """
      Endpoint public, sans authentification, destiné au site vitrine. Équivalent à \
      `GET /api/projets` mais **forcé** sur `statut=PUBLIE` — le paramètre `statut` \
      n'existe volontairement pas ici pour qu'un contenu non publié ne puisse jamais \
      être listé publiquement.

      `q` recherche en texte libre, `categorie` filtre sur `CategorieProjet`, \
      `sortBy`/`sortDirection` pilotent le tri (ex. `createdAt`/`DESC`).""")
  @ApiResponse(responseCode = "200", description = "Liste des projets publiés récupérée")
  @ApiResponse(responseCode = "400", description = "categorie invalide (CATEGORIE_PROJET_INVALIDE)")
  @GetMapping("/public")
  ResponseEntity<Map<String, Object>> listerPublic(
      @Parameter(description = "Recherche texte libre sur le nom ou la description") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur la catégorie (ENVIRONNEMENT, SOCIAL, INNOVATION, EDUCATION, SANTE, AUTRE)") @RequestParam(required = false) String categorie,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
