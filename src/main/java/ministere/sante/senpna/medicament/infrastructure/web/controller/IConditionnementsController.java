package ministere.sante.senpna.medicament.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.ConditionnementsController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateConditionnementRequest;

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
 * Contrat API du contrôleur Conditionnements.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link ConditionnementsController}, qui n'en est que l'implémentation
 * métier — cf. règle « une classe, une raison de changer » (SOLID/SRP).
 * Les annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link ConditionnementsController}, au plus près du code qu'elles
 * protègent.
 * </p>
 *
 * <pre>
 * POST   /api/conditionnements                  {medicamentId, nom, niveau, quantiteUniteBase, estUniteBase}
 * PUT    /api/conditionnements/{id}              {nom, niveau, quantiteUniteBase, estUniteBase}
 * PATCH  /api/conditionnements/{id}/archiver
 * PATCH  /api/conditionnements/{id}/desarchiver
 * GET    /api/conditionnements/{id}
 * GET    /api/conditionnements?medicamentId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Conditionnements", description = """
                Gestion des conditionnements (niveaux d'emballage) d'un médicament — ex. comprimé → boîte \
                → carton. Chaque médicament doit posséder exactement une unité de base (`estUniteBase = true`), \
                dans laquelle son stock réel est exprimé.""")
@RequestMapping("/api/conditionnements")
public interface IConditionnementsController {

        @Operation(summary = "Créer un conditionnement", description = """
                        Crée un nouveau conditionnement pour un médicament donné, actif par défaut.

                        Contraintes vérifiées, propres à chaque médicament (pas globales) :
                        - `niveau` doit être unique parmi les conditionnements du médicament ;
                        - `nom` doit être unique (insensible à la casse) parmi les conditionnements du \
                        médicament ;
                        - si `estUniteBase = true`, le médicament ne doit pas déjà posséder de \
                        conditionnement marqué unité de base — un seul est autorisé à la fois.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "201", description = "Conditionnement créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        {
                          "status": 201,
                          "type": "CONDITIONNEMENT_CREATED",
                          "message": "Conditionnement créé avec succès",
                          "timestamp": "2024-01-01T12:00:00Z",
                          "results": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "medicamentId": "8a2c...",
                            "nom": "Boîte de 20",
                            "niveau": 1,
                            "quantiteUniteBase": 20,
                            "estUniteBase": false,
                            "prixAchat": 500,
                            "prixVente": 750,
                            "actif": true,
                            "createdAt": "2024-01-01T12:00:00Z",
                            "updatedAt": "2024-01-01T12:00:00Z"
                          }
                        }
                        """)))
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : medicamentId manquant, nom manquant, niveau < 1, quantiteUniteBase non strictement positive, prix négatif")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer un conditionnement")
        @ApiResponse(responseCode = "404", description = "Médicament introuvable (MEDICAMENT_NOT_FOUND)")
        @ApiResponse(responseCode = "409", description = "Niveau déjà utilisé (CONDITIONNEMENT_NIVEAU_ALREADY_USED), nom déjà utilisé (CONDITIONNEMENT_NOM_ALREADY_USED), ou unité de base déjà définie pour ce médicament (CONDITIONNEMENT_UNITE_BASE_ALREADY_DEFINED)")
        @PostMapping
        ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateConditionnementRequest request);

        @Operation(summary = "Modifier un conditionnement", description = """
                        Remplace intégralement les caractéristiques d'un conditionnement existant (`nom`, \
                        `niveau`, `quantiteUniteBase`, `estUniteBase`, prix). Le médicament rattaché n'est \
                        **pas** modifiable par cet endpoint. Les mêmes contraintes d'unicité de `niveau` et \
                        `nom` par médicament, et d'unicité de l'unité de base, s'appliquent.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Conditionnement modifié")
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier un conditionnement")
        @ApiResponse(responseCode = "404", description = "Conditionnement introuvable (CONDITIONNEMENT_NOT_FOUND)")
        @ApiResponse(responseCode = "409", description = "Niveau ou nom déjà utilisé par un autre conditionnement du même médicament, ou unité de base déjà définie")
        @PutMapping("/{id}")
        ResponseEntity<Map<String, Object>> modifier(
                        @Parameter(description = "Identifiant du conditionnement") @PathVariable UUID id,
                        @Valid @RequestBody UpdateConditionnementRequest request);

        @Operation(summary = "Archiver un conditionnement", description = """
                        Retire le conditionnement des niveaux d'emballage sélectionnables. Un conditionnement \
                        marqué unité de base ne peut pas être archivé s'il est le **seul** de son médicament \
                        — un médicament actif doit toujours conserver une unité de base pour exprimer son \
                        stock ; il faut d'abord définir une autre unité de base. En dehors de ce cas, \
                        opération idempotente.""")
        @ApiResponse(responseCode = "200", description = "Conditionnement archivé")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Conditionnement introuvable (CONDITIONNEMENT_NOT_FOUND)")
        @ApiResponse(responseCode = "422", description = "Ce conditionnement est l'unique unité de base du médicament (CONDITIONNEMENT_LAST_UNITE_BASE)")
        @PatchMapping("/{id}/archiver")
        ResponseEntity<Map<String, Object>> archiver(
                        @Parameter(description = "Identifiant du conditionnement") @PathVariable UUID id);

        @Operation(summary = "Désarchiver un conditionnement", description = """
                        Réintègre le conditionnement parmi les niveaux d'emballage sélectionnables. \
                        Opération idempotente.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Conditionnement désarchivé")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Conditionnement introuvable (CONDITIONNEMENT_NOT_FOUND)")
        @PatchMapping("/{id}/desarchiver")
        ResponseEntity<Map<String, Object>> desarchiver(
                        @Parameter(description = "Identifiant du conditionnement") @PathVariable UUID id);

        @Operation(summary = "Obtenir un conditionnement", description = """
                        Retourne le détail complet d'un conditionnement, actif ou archivé.

                        Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
        @ApiResponse(responseCode = "200", description = "Conditionnement récupéré")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Conditionnement introuvable (CONDITIONNEMENT_NOT_FOUND)")
        @GetMapping("/{id}")
        ResponseEntity<Map<String, Object>> obtenir(
                        @Parameter(description = "Identifiant du conditionnement") @PathVariable UUID id);

        @Operation(summary = "Lister les conditionnements", description = """
                        Recherche paginée sur l'ensemble des conditionnements. `medicamentId`, si fourni, \
                        restreint la liste aux conditionnements de ce seul médicament (cas d'usage \
                        principal : afficher la liste des emballages disponibles d'un médicament donné). \
                        `actif` filtre sur le statut. `sortBy`/`sortDirection` pilotent le tri (défaut \
                        `niveau`/`ASC`, pour un affichage naturel du plus petit au plus grand emballage).

                        Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
        @ApiResponse(responseCode = "200", description = "Liste des conditionnements récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "type": "CONDITIONNEMENTS_LISTED",
                          "message": "Liste des conditionnements récupérée",
                          "timestamp": "2024-01-01T12:00:00Z",
                          "results": [
                            {
                              "id": "550e8400-e29b-41d4-a716-446655440000",
                              "medicamentId": "8a2c...",
                              "nom": "Comprimé",
                              "niveau": 0,
                              "quantiteUniteBase": 1,
                              "estUniteBase": true,
                              "prixAchat": 25,
                              "prixVente": 40,
                              "actif": true,
                              "createdAt": "2024-01-01T12:00:00Z",
                              "updatedAt": "2024-01-01T12:00:00Z"
                            }
                          ],
                          "pagination": {
                            "currentPage": 0,
                            "totalPages": 1,
                            "totalItems": 3,
                            "first": true,
                            "last": true
                          }
                        }
                        """)))
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @GetMapping
        ResponseEntity<Map<String, Object>> lister(
                        @Parameter(description = "Restreint la liste aux conditionnements de ce médicament") @RequestParam(required = false) UUID medicamentId,
                        @Parameter(description = "Filtre sur le statut actif/archivé") @RequestParam(required = false) Boolean actif,
                        @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
                        @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
                        @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "niveau") String sortBy,
                        @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "ASC") String sortDirection);
}