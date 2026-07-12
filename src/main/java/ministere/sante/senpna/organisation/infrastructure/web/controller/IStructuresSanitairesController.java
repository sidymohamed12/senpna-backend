package ministere.sante.senpna.organisation.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.StructuresSanitairesController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignPraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignRegionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreateStructureSanitaireRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.RejectAdhesionRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdateStructureSanitaireRequest;

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
 * Contrat API du contrôleur Structures sanitaires.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link StructuresSanitairesController}, qui n'en est que
 * l'implémentation métier — cf. règle « une classe, une raison de
 * changer » (SOLID/SRP). Les annotations de routage/binding sont portées
 * ici. Les annotations de sécurité ({@code @PreAuthorize}) restent
 * volontairement sur {@link StructuresSanitairesController}, au plus
 * près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/structures-sanitaires                      {code, nom, type, regionId, district?, adresse?, telephone?, email?, responsableNom, responsablePrenom}
 * PUT    /api/structures-sanitaires/{id}                  {nom, district?, adresse?, telephone?, email?, responsableNom?, responsablePrenom?}
 * PATCH  /api/structures-sanitaires/{id}/valider-adhesion
 * PATCH  /api/structures-sanitaires/{id}/rejeter-adhesion {motif}
 * PATCH  /api/structures-sanitaires/{id}/activer
 * PATCH  /api/structures-sanitaires/{id}/desactiver
 * PATCH  /api/structures-sanitaires/{id}/region           {regionId}
 * PATCH  /api/structures-sanitaires/{id}/pra              {praId}
 * GET    /api/structures-sanitaires/{id}
 * GET    /api/structures-sanitaires?q=&type=&regionId=&praId=&statutAdhesion=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Structures sanitaires", description = """
    Gestion des structures sanitaires bénéficiaires (hôpitaux, districts, centres et postes de \
    santé, ONG), depuis leur demande d'adhésion jusqu'à leur rattachement organisationnel \
    (région, PRA).""")
@RequestMapping("/api/structures-sanitaires")
public interface IStructuresSanitairesController {

  @Operation(summary = "Déposer une demande d'adhésion", description = """
      Enregistre une nouvelle structure sanitaire à l'état `EN_ATTENTE_VALIDATION` — ce \
      n'est **pas** une création directement active : la structure doit ensuite être \
      validée via `PATCH /{id}/valider-adhesion` avant de pouvoir être activée et \
      rattachée à une PRA. Le `code` doit être unique (2 à 30 caractères alphanumériques). \
      La région référencée doit être active.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "201", description = "Demande d'adhésion enregistrée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "STRUCTURE_SANITAIRE_CREATED",
        "message": "Demande d'adhésion de la structure sanitaire enregistrée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "code": "CS-PIKINE",
          "nom": "Centre de santé de Pikine",
          "type": "CENTRE_SANTE",
          "regionId": "3e7a...",
          "regionNom": "Dakar",
          "praId": null,
          "praNom": null,
          "district": "Pikine",
          "adresse": "Avenue Bourguiba",
          "telephone": "+221771234567",
          "email": "contact@cs-pikine.sn",
          "responsableNom": "Fall",
          "responsablePrenom": "Awa",
          "statutAdhesion": "EN_ATTENTE_VALIDATION",
          "motifRejet": null,
          "actif": false,
          "createdAt": "2024-01-01T12:00:00Z",
          "updatedAt": "2024-01-01T12:00:00Z"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : code/nom/type/région/responsable manquant, code, téléphone ou email mal formé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "409", description = "Ce code de structure sanitaire est déjà utilisé (STRUCTURE_SANITAIRE_CODE_ALREADY_USED)")
  @ApiResponse(responseCode = "422", description = "La région référencée est désactivée (REGION_INACTIVE)")
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateStructureSanitaireRequest request);

  @Operation(summary = "Modifier une structure sanitaire", description = """
      Modifie les informations descriptives d'une structure sanitaire (`nom`, `district`, \
      `adresse`, `telephone`, `email`, responsable). Ni le statut d'adhésion, ni le \
      rattachement région/PRA, ni le statut actif/inactif ne sont modifiés par cet \
      endpoint — utilisez les endpoints dédiés (`/valider-adhesion`, `/region`, `/pra`, \
      `/activer`, `/desactiver`).

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire modifiée")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @PutMapping("/{id}")
  ResponseEntity<Map<String, Object>> modifier(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id,
      @Valid @RequestBody UpdateStructureSanitaireRequest request);

  @Operation(summary = "Valider une demande d'adhésion", description = """
      Valide la demande d'adhésion : la structure passe à `statutAdhesion = VALIDEE` et \
      devient active. Ne peut être appliqué qu'à une demande encore \
      `EN_ATTENTE_VALIDATION` — une demande déjà validée ou rejetée ne peut pas être \
      revalidée.

      Déclenche, après validation, la création automatique du compte \
      `GESTIONNAIRE_STRUCTURE` du responsable désigné, avec envoi de ses identifiants par \
      e-mail.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Demande d'adhésion validée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Cette demande a déjà été traitée, validée ou rejetée (ADHESION_ALREADY_PROCESSED)")
  @PatchMapping("/{id}/valider-adhesion")
  ResponseEntity<Map<String, Object>> validerAdhesion(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id);

  @Operation(summary = "Rejeter une demande d'adhésion", description = """
      Rejette la demande d'adhésion avec un `motif` obligatoire : la structure passe à \
      `statutAdhesion = REJETEE` et reste inactive. Ne peut être appliqué qu'à une \
      demande encore `EN_ATTENTE_VALIDATION`.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Demande d'adhésion rejetée")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : motif manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Cette demande a déjà été traitée, validée ou rejetée (ADHESION_ALREADY_PROCESSED)")
  @PatchMapping("/{id}/rejeter-adhesion")
  ResponseEntity<Map<String, Object>> rejeterAdhesion(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id,
      @Valid @RequestBody RejectAdhesionRequest request);

  @Operation(summary = "Activer une structure sanitaire", description = """
      Fait passer la structure à l'état actif. Ne peut être appliqué qu'à une structure \
      dont l'adhésion a déjà été validée (`statutAdhesion = VALIDEE`) — une structure \
      encore en attente ou rejetée doit d'abord passer par \
      `PATCH /{id}/valider-adhesion`. En dehors de ce cas, opération idempotente.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire activée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "L'adhésion de la structure n'est pas encore validée (STRUCTURE_SANITAIRE_ADHESION_NOT_VALIDATED)")
  @PatchMapping("/{id}/activer")
  ResponseEntity<Map<String, Object>> activer(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id);

  @Operation(summary = "Désactiver une structure sanitaire", description = """
      Fait passer la structure à l'état inactif — elle ne peut alors plus recevoir de \
      nouvelle affectation utilisateur ni passer de commandes, mais reste consultable. \
      Opération idempotente.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire désactivée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @PatchMapping("/{id}/desactiver")
  ResponseEntity<Map<String, Object>> desactiver(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id);

  @Operation(summary = "Rattacher une structure sanitaire à une région", description = """
      Change la région de rattachement d'une structure sanitaire. La région cible doit \
      être active.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA` uniquement — contrairement aux \
      autres opérations de gestion des structures, ce rattachement, qui affecte la \
      supervision régionale, n'est pas ouvert aux rôles PRA.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire rattachée à la région")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : regionId manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire ou région introuvable")
  @ApiResponse(responseCode = "422", description = "La région ciblée est désactivée (REGION_INACTIVE)")
  @PatchMapping("/{id}/region")
  ResponseEntity<Map<String, Object>> affecterRegion(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id,
      @Valid @RequestBody AssignRegionRequest request);

  @Operation(summary = "Rattacher une structure sanitaire à une PRA", description = """
      Désigne la PRA qui approvisionne cette structure sanitaire. L'entrepôt ciblé doit \
      être de type `PRA` (pas la PNA centrale) et actif.

      Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA` uniquement — même restriction que \
      pour le rattachement à une région.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire rattachée à la PRA")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : praId manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire ou entrepôt introuvable")
  @ApiResponse(responseCode = "422", description = "L'entrepôt ciblé n'est pas de type PRA (INVALID_ENTREPOT_TYPE), ou est désactivé (ENTREPOT_INACTIVE)")
  @PatchMapping("/{id}/pra")
  ResponseEntity<Map<String, Object>> affecterPra(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id,
      @Valid @RequestBody AssignPraRequest request);

  @Operation(summary = "Obtenir une structure sanitaire", description = """
      Retourne le détail complet d'une structure sanitaire, quel que soit son statut \
      d'adhésion ou d'activité.

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Structure sanitaire récupérée")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Structure sanitaire introuvable (STRUCTURE_SANITAIRE_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de la structure sanitaire") @PathVariable UUID id);

  @Operation(summary = "Lister les structures sanitaires", description = """
      Recherche paginée sur l'ensemble des structures sanitaires. `q` recherche en texte \
      libre (code, nom). `type` filtre sur le type d'établissement. `regionId`/`praId` \
      filtrent sur un rattachement donné. `statutAdhesion` filtre sur \
      `EN_ATTENTE_VALIDATION`, `VALIDEE` ou `REJETEE` — utile notamment pour lister les \
      demandes en attente de traitement. `actif` filtre sur le statut d'activité. \
      `sortBy`/`sortDirection` pilotent le tri (défaut `createdAt`/`DESC`).

      Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `ADMIN_PRA` ou `GESTIONNAIRE_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Liste des structures sanitaires récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "STRUCTURES_SANITAIRES_LISTED",
        "message": "Liste des structures sanitaires récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "code": "CS-PIKINE",
            "nom": "Centre de santé de Pikine",
            "type": "CENTRE_SANTE",
            "regionId": "3e7a...",
            "regionNom": "Dakar",
            "praId": "8a2c...",
            "praNom": "PRA de Dakar",
            "district": "Pikine",
            "adresse": "Avenue Bourguiba",
            "telephone": "+221771234567",
            "email": "contact@cs-pikine.sn",
            "responsableNom": "Fall",
            "responsablePrenom": "Awa",
            "statutAdhesion": "VALIDEE",
            "motifRejet": null,
            "actif": true,
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 6,
          "totalItems": 112,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le code ou le nom") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur le type d'établissement") @RequestParam(required = false) TypeStructureSanitaire type,
      @Parameter(description = "Filtre sur une région") @RequestParam(required = false) UUID regionId,
      @Parameter(description = "Filtre sur une PRA") @RequestParam(required = false) UUID praId,
      @Parameter(description = "Filtre sur le statut d'adhésion (EN_ATTENTE_VALIDATION, VALIDEE, REJETEE)") @RequestParam(required = false) StatutAdhesion statutAdhesion,
      @Parameter(description = "Filtre sur le statut actif/inactif") @RequestParam(required = false) Boolean actif,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}
