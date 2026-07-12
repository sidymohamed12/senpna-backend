package ministere.sante.senpna.organisation.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.infrastructure.web.controller.implement.AffectationsController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToEntrepotRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToStructureRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Affectations.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link AffectationsController}, qui n'en est que l'implémentation
 * métier — cf. règle « une classe, une raison de changer » (SOLID/SRP).
 * Les annotations de routage/binding sont portées ici. L'annotation de
 * sécurité ({@code @PreAuthorize}), ici définie au niveau classe (même
 * périmètre de rôles pour les trois endpoints), reste volontairement sur
 * {@link AffectationsController}.
 * </p>
 *
 * <pre>
 * POST   /api/affectations/utilisateurs/{userId}/entrepot           {entrepotId}
 * POST   /api/affectations/utilisateurs/{userId}/structure-sanitaire {structureSanitaireId}
 * DELETE /api/affectations/utilisateurs/{userId}
 * </pre>
 */
@Tag(name = "Affectations", description = """
    Affectation d'un utilisateur à une unité organisationnelle : une PNA, une PRA, ou une \
    structure sanitaire. Un utilisateur n'est rattaché qu'à une seule unité à la fois. Réservé \
    aux administrateurs — même périmètre que la gestion des comptes.""")
@RequestMapping("/api/affectations/utilisateurs")
public interface IAffectationsController {

  @Operation(summary = "Affecter un utilisateur à un entrepôt", description = """
      Rattache l'utilisateur à un entrepôt (PRA ou PNA centrale). Remplace toute \
      affectation précédente de cet utilisateur (à un entrepôt ou à une structure \
      sanitaire) — un utilisateur n'est jamais rattaché qu'à une seule unité \
      organisationnelle à la fois. L'entrepôt cible doit être actif.

      Rôle requis : `ADMIN_PNA` ou `ADMIN_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Utilisateur affecté", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "USER_ASSIGNED_ENTREPOT",
        "message": "Utilisateur affecté à l'entrepôt avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "userId": "550e8400-e29b-41d4-a716-446655440000",
          "entrepotId": "8a2c...",
          "structureSanitaireId": null
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : entrepotId manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "L'entrepôt ciblé est désactivé (ENTREPOT_INACTIVE)")
  @PostMapping("/{userId}/entrepot")
  ResponseEntity<Map<String, Object>> affecterAEntrepot(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID userId,
      @Valid @RequestBody AssignUserToEntrepotRequest request);

  @Operation(summary = "Affecter un utilisateur à une structure sanitaire", description = """
      Rattache l'utilisateur à une structure sanitaire. Remplace toute affectation \
      précédente de cet utilisateur. La structure sanitaire cible doit avoir son adhésion \
      validée (`StatutAdhesion.VALIDEE`) — une structure en attente ou rejetée ne peut \
      recevoir aucune affectation.

      Rôle requis : `ADMIN_PNA` ou `ADMIN_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Utilisateur affecté", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "USER_ASSIGNED_STRUCTURE",
        "message": "Utilisateur affecté à la structure sanitaire avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "userId": "550e8400-e29b-41d4-a716-446655440000",
          "entrepotId": null,
          "structureSanitaireId": "3e7a..."
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : structureSanitaireId manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "L'adhésion de la structure sanitaire ciblée n'est pas validée (STRUCTURE_SANITAIRE_ADHESION_NOT_VALIDATED)")
  @PostMapping("/{userId}/structure-sanitaire")
  ResponseEntity<Map<String, Object>> affecterAStructure(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID userId,
      @Valid @RequestBody AssignUserToStructureRequest request);

  @Operation(summary = "Retirer l'affectation d'un utilisateur", description = """
      Retire le rattachement organisationnel de l'utilisateur (entrepôt ou structure \
      sanitaire, quel qu'il soit) — l'utilisateur n'est alors plus affecté à aucune unité.

      Rôle requis : `ADMIN_PNA` ou `ADMIN_PRA`.""")
  @ApiResponse(responseCode = "200", description = "Affectation retirée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "USER_UNASSIGNED",
        "message": "Affectation de l'utilisateur retirée avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "userId": "550e8400-e29b-41d4-a716-446655440000",
          "entrepotId": null,
          "structureSanitaireId": null
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @DeleteMapping("/{userId}")
  ResponseEntity<Map<String, Object>> retirerAffectation(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID userId);
}
