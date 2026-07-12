package ministere.sante.senpna.utilisateurs.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.utilisateurs.infrastructure.web.controller.implement.UsersController;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.AssignRoleRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.CreateUserRequest;
import ministere.sante.senpna.utilisateurs.infrastructure.web.dto.request.UpdateUserRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * Contrat API du contrôleur Utilisateurs.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, corps de requête,
 * paramètres, exemples, codes de retour) séparément de
 * {@link UsersController}, qui n'en est que l'implémentation métier —
 * cf. règle « une classe, une raison de changer » (SOLID/SRP). Les
 * annotations de routage/binding sont portées ici. L'annotation de
 * sécurité ({@code @PreAuthorize}), ici définie au niveau classe (même
 * périmètre de rôles pour tous les endpoints), reste volontairement sur
 * {@link UsersController}.
 * </p>
 *
 * <pre>
 * POST   /api/users                     {nom,prenom,email,telephone?,roleIds,entrepotId?}
 * GET    /api/users?q=&actif=&roleId=&page=&size=&sortBy=&sortDirection=
 * GET    /api/users/{id}
 * PUT    /api/users/{id}                 {nom,prenom,telephone?}
 * PATCH  /api/users/{id}/activer
 * PATCH  /api/users/{id}/desactiver
 * POST   /api/users/{id}/roles           {roleId}
 * DELETE /api/users/{id}/roles/{roleId}
 * </pre>
 */
@Tag(name = "Utilisateurs", description = """
    Administration des comptes utilisateurs : création, consultation, modification, \
    activation/désactivation et gestion des rôles (RBAC). Réservé aux rôles `ADMIN_PNA` et \
    `ADMIN_PRA`.

    **Hiérarchie de gestion** : un `ADMIN_PRA` ne peut ni créer, modifier, activer, désactiver \
    ou changer les rôles d'un compte possédant un rôle national (PNA), ni d'un autre compte \
    `ADMIN_PRA` — y compris d'une région différente. Seul un rôle national (PNA) a une portée \
    illimitée sur l'ensemble des comptes. Cette règle s'applique à tous les endpoints de \
    modification ci-dessous, systématiquement rappelée dans leur description.""")
@RequestMapping("/api/users")
public interface IUsersController {

  @Operation(summary = "Créer un compte utilisateur", description = """
      Crée un nouveau compte, actif par défaut, avec un mot de passe temporaire \
      généré automatiquement (retourné une seule fois dans la réponse — non recalculable \
      ensuite, à communiquer à l'utilisateur par un canal sécurisé).

      `roleIds` doit contenir au moins un rôle existant. Certains rôles ne peuvent pas \
      être attribués manuellement ici — `GESTIONNAIRE_STRUCTURE`, notamment, n'est créé \
      qu'automatiquement à la validation d'une demande d'adhésion (module organisation).

      `entrepotId` n'est à fournir que si l'un des rôles demandés l'exige (rôles `*_PRA` \
      ou rôles PNA hors `ADMIN_PNA`) : \
      - fourni par un acteur **national** (PNA), il est utilisé tel quel — l'entrepôt \
      choisi doit être actif et du type attendu par les rôles demandés ; \
      - pour un acteur **régional** (`ADMIN_PRA`), il est **ignoré** : le nouveau compte \
      est automatiquement affecté au même entrepôt que le créateur, ce qui empêche tout \
      rattachement à une autre région.

      Cette règle de hiérarchie de gestion documentée au niveau du contrôleur s'applique \
      également ici : un `ADMIN_PRA` ne peut pas créer de compte avec un rôle national.""")
  @ApiResponse(responseCode = "201", description = "Utilisateur créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 201,
        "type": "USER_CREATED",
        "message": "Utilisateur créé avec succès",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": {
          "user": {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "nom": "Ndiaye",
            "prenom": "Fatou",
            "email": "fatou.ndiaye@senpna.sn",
            "telephone": "+221771234567",
            "actif": true,
            "roles": [
              { "id": "8a2c...", "code": "PHARMACIEN_PRA", "nom": "Pharmacien PRA" }
            ],
            "entrepotId": "3e7a...",
            "structureSanitaireId": null,
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          },
          "motDePasseTemporaire": "Xk7$mQ2pLw9r"
        }
      }
      """)))
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : nom/prénom/email manquant, email ou téléphone mal formé, aucun rôle fourni (ROLE_REQUIRED)")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur PRA tentant d'attribuer un rôle hors de sa portée hiérarchique (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Entrepôt introuvable (ENTREPOT_NOT_FOUND)")
  @ApiResponse(responseCode = "409", description = "Cet email est déjà utilisé par un autre utilisateur (EMAIL_ALREADY_USED)")
  @ApiResponse(responseCode = "422", description = "Rôle réservé à une création automatisée (ROLE_CREATION_RESERVED), entrepôt désactivé (ENTREPOT_INACTIVE), type d'entrepôt incompatible avec les rôles demandés (ENTREPOT_TYPE_INCOMPATIBLE), ou combinaison de rôles exigeant des types d'entrepôt différents (ENTREPOT_TYPE_CONFLICT)")
  @PostMapping
  ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateUserRequest request);

  @Operation(summary = "Lister les utilisateurs", description = """
      Recherche paginée sur l'ensemble des comptes visibles par l'acteur courant. `q` \
      recherche en texte libre (nom, prénom, email). `actif` filtre sur le statut. \
      `roleId` filtre sur les utilisateurs possédant un rôle donné. `sortBy`/`sortDirection` \
      pilotent le tri (défaut `createdAt`/`DESC`).""")
  @ApiResponse(responseCode = "200", description = "Liste des utilisateurs récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "USERS_LISTED",
        "message": "Liste des utilisateurs récupérée",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "id": "550e8400-e29b-41d4-a716-446655440000",
            "nom": "Ndiaye",
            "prenom": "Fatou",
            "email": "fatou.ndiaye@senpna.sn",
            "telephone": "+221771234567",
            "actif": true,
            "roles": [
              { "id": "8a2c...", "code": "PHARMACIEN_PRA", "nom": "Pharmacien PRA" }
            ],
            "entrepotId": "3e7a...",
            "structureSanitaireId": null,
            "createdAt": "2024-01-01T12:00:00Z",
            "updatedAt": "2024-01-01T12:00:00Z"
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 4,
          "totalItems": 73,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @GetMapping
  ResponseEntity<Map<String, Object>> lister(
      @Parameter(description = "Recherche texte libre sur le nom, le prénom ou l'email") @RequestParam(required = false) String q,
      @Parameter(description = "Filtre sur le statut actif/inactif") @RequestParam(required = false) Boolean actif,
      @Parameter(description = "Filtre sur les utilisateurs possédant ce rôle") @RequestParam(required = false) UUID roleId,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
      @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
      @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

  @Operation(summary = "Obtenir un utilisateur", description = """
      Retourne le détail complet d'un compte utilisateur, y compris ses rôles et son \
      rattachement organisationnel (entrepôt ou structure sanitaire).""")
  @ApiResponse(responseCode = "200", description = "Utilisateur récupéré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @GetMapping("/{id}")
  ResponseEntity<Map<String, Object>> obtenir(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id);

  @Operation(summary = "Modifier un utilisateur", description = """
      Modifie `nom`, `prenom` et `telephone` d'un compte existant. Ni l'email, ni les \
      rôles, ni le rattachement organisationnel, ni le statut actif/inactif ne sont \
      modifiés par cet endpoint — utilisez les endpoints dédiés (`/activer`, \
      `/desactiver`, `/roles`).

      Soumis à la même hiérarchie de gestion que la création (un `ADMIN_PRA` ne peut pas \
      modifier un compte à portée nationale ou un autre `ADMIN_PRA`).""")
  @ApiResponse(responseCode = "200", description = "Utilisateur modifié")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : nom/prénom manquant, téléphone mal formé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou compte hors de la portée hiérarchique de l'acteur (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @PutMapping("/{id}")
  ResponseEntity<Map<String, Object>> modifier(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request);

  @Operation(summary = "Activer un utilisateur", description = """
      Réactive un compte désactivé, lui permettant de nouveau de s'authentifier.

      Soumis à la même hiérarchie de gestion que la création.""")
  @ApiResponse(responseCode = "200", description = "Utilisateur activé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou compte hors de la portée hiérarchique de l'acteur (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @PatchMapping("/{id}/activer")
  ResponseEntity<Map<String, Object>> activer(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id);

  @Operation(summary = "Désactiver un utilisateur", description = """
      Désactive un compte, l'empêchant de s'authentifier (sans le supprimer). Un \
      utilisateur ne peut pas désactiver son propre compte, y compris s'il en aurait la \
      portée hiérarchique — cette restriction évite qu'un administrateur ne se retrouve \
      accidentellement lui-même verrouillé hors du système.

      Soumis par ailleurs à la même hiérarchie de gestion que la création.""")
  @ApiResponse(responseCode = "200", description = "Utilisateur désactivé")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou compte hors de la portée hiérarchique de l'acteur (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable (USER_NOT_FOUND)")
  @ApiResponse(responseCode = "422", description = "Un utilisateur ne peut pas désactiver son propre compte (SELF_DEACTIVATION_FORBIDDEN)")
  @PatchMapping("/{id}/desactiver")
  ResponseEntity<Map<String, Object>> desactiver(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id);

  @Operation(summary = "Attribuer un rôle à un utilisateur", description = """
      Ajoute un rôle supplémentaire au compte — un utilisateur peut posséder plusieurs \
      rôles simultanément. Un rôle déjà attribué ne peut pas être ajouté une seconde fois.

      Soumis à la même hiérarchie de gestion que la création : la vérification porte sur \
      l'ensemble des rôles du compte **après** attribution — un `ADMIN_PRA` ne peut donc \
      pas, par ce biais, faire passer un compte sous sa gestion à portée nationale.""")
  @ApiResponse(responseCode = "200", description = "Rôle attribué")
  @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : roleId manquant")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou compte/rôle hors de la portée hiérarchique de l'acteur (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Utilisateur ou rôle introuvable (USER_NOT_FOUND / ROLE_NOT_FOUND)")
  @ApiResponse(responseCode = "409", description = "Ce rôle est déjà attribué à cet utilisateur (ROLE_ALREADY_ASSIGNED)")
  @PostMapping("/{id}/roles")
  ResponseEntity<Map<String, Object>> assignerRole(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
      @Valid @RequestBody AssignRoleRequest request);

  @Operation(summary = "Retirer un rôle à un utilisateur", description = """
      Retire un rôle du compte. Un utilisateur doit toujours posséder au moins un rôle \
      actif — retirer son unique rôle restant est refusé plutôt que de laisser un compte \
      sans aucun droit.

      Soumis à la même hiérarchie de gestion que la création.""")
  @ApiResponse(responseCode = "200", description = "Rôle retiré")
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou compte hors de la portée hiérarchique de l'acteur (USER_MANAGEMENT_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Utilisateur introuvable, ou ce rôle n'est pas attribué à cet utilisateur (USER_NOT_FOUND / USER_ROLE_NOT_ASSIGNED)")
  @ApiResponse(responseCode = "422", description = "Ce rôle est l'unique rôle actif de l'utilisateur (LAST_ROLE_REQUIRED)")
  @DeleteMapping("/{id}/roles/{roleId}")
  ResponseEntity<Map<String, Object>> retirerRole(
      @Parameter(description = "Identifiant de l'utilisateur") @PathVariable UUID id,
      @Parameter(description = "Identifiant du rôle à retirer") @PathVariable UUID roleId);
}
