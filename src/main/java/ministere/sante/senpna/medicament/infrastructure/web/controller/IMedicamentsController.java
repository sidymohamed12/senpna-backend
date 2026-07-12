package ministere.sante.senpna.medicament.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ministere.sante.senpna.medicament.infrastructure.web.controller.implement.MedicamentsController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateMedicamentRequest;

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
 * Contrat API du contrôleur Médicaments.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger séparément de
 * {@link MedicamentsController}, qui n'en est que l'implémentation
 * métier — cf. règle « une classe, une raison de changer » (SOLID/SRP).
 * Les annotations de routage/binding sont portées ici. Les annotations de
 * sécurité ({@code @PreAuthorize}) restent volontairement sur
 * {@link MedicamentsController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * POST   /api/medicaments                  {code, nomCommercial, dci, dosage, formeId, familleId, ...}
 * PUT    /api/medicaments/{id}              {nomCommercial, dci, dosage, formeId, familleId, ...}
 * PATCH  /api/medicaments/{id}/archiver
 * PATCH  /api/medicaments/{id}/desarchiver
 * GET    /api/medicaments/{id}
 * GET    /api/medicaments?q=&familleId=&formeId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@Tag(name = "Médicaments", description = """
                Gestion du catalogue national des médicaments (référentiel, pas la disponibilité en stock — \
                voir le contrôleur Catalogue pour cela). Chaque médicament référence une famille \
                thérapeutique et une forme pharmaceutique, toutes deux devant être actives.""")
@RequestMapping("/api/medicaments")
public interface IMedicamentsController {

        @Operation(summary = "Créer un médicament", description = """
                        Crée un nouveau médicament du référentiel national, actif par défaut. Le `code` \
                        doit être unique (2 à 30 caractères alphanumériques). `formeId` et `familleId` \
                        doivent référencer une forme et une famille **actives** — une référence archivée \
                        est rejetée, même si elle existe.

                        `voieAdministration` et `temperatureConservation` sont optionnels. Rôle requis : \
                        `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "201", description = "Médicament créé", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        {
                          "status": 201,
                          "type": "MEDICAMENT_CREATED",
                          "message": "Médicament créé avec succès",
                          "timestamp": "2024-01-01T12:00:00Z",
                          "results": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "code": "PARA500",
                            "nomCommercial": "Doliprane",
                            "dci": "Paracétamol",
                            "dosage": "500mg",
                            "formeId": "8a2c...",
                            "formeLibelle": "Comprimé",
                            "familleId": "3e7a...",
                            "familleLibelle": "Antalgiques",
                            "voieAdministration": "ORALE",
                            "temperatureConservation": "AMBIANTE",
                            "programmeSante": null,
                            "delaiApprovisionnementJours": 30,
                            "necessiteOrdonnance": false,
                            "fabricant": "Sanofi",
                            "stockMinimum": 500,
                            "stockMaximum": 5000,
                            "actif": true,
                            "createdAt": "2024-01-01T12:00:00Z",
                            "updatedAt": "2024-01-01T12:00:00Z"
                          }
                        }
                        """)))
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR) : code/nom/dci/dosage/forme/famille manquant, code mal formé, seuils de stock négatifs")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à créer un médicament")
        @ApiResponse(responseCode = "404", description = "Forme ou famille référencée introuvable (FORME_NOT_FOUND / FAMILLE_NOT_FOUND)")
        @ApiResponse(responseCode = "409", description = "Un médicament porte déjà ce code (MEDICAMENT_CODE_ALREADY_USED)")
        @ApiResponse(responseCode = "422", description = "Forme ou famille référencée archivée (FORME_INACTIVE / FAMILLE_INACTIVE)")
        @PostMapping
        ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateMedicamentRequest request);

        @Operation(summary = "Modifier un médicament", description = """
                        Remplace intégralement les informations d'un médicament existant. Le `code`, \
                        identifiant stable du médicament, n'est **pas** modifiable par cet endpoint. Le \
                        statut actif/archivé n'est pas non plus modifié ici — utilisez `PATCH /{id}/archiver` \
                        ou `/desarchiver`. `formeId` et `familleId`, s'ils changent, doivent référencer des \
                        entités actives.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Médicament modifié")
        @ApiResponse(responseCode = "400", description = "Corps invalide (VALIDATION_ERROR)")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé à modifier un médicament")
        @ApiResponse(responseCode = "404", description = "Médicament, forme ou famille référencée introuvable")
        @ApiResponse(responseCode = "422", description = "Forme ou famille référencée archivée (FORME_INACTIVE / FAMILLE_INACTIVE)")
        @PutMapping("/{id}")
        ResponseEntity<Map<String, Object>> modifier(
                        @Parameter(description = "Identifiant du médicament") @PathVariable UUID id,
                        @Valid @RequestBody UpdateMedicamentRequest request);

        @Operation(summary = "Archiver un médicament", description = """
                        Retire le médicament du catalogue actif — il n'est alors plus proposé pour de \
                        nouveaux achats, réceptions ou ventes, mais reste consultable pour l'historique des \
                        mouvements de stock déjà enregistrés. Opération idempotente.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Médicament archivé")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Médicament introuvable (MEDICAMENT_NOT_FOUND)")
        @PatchMapping("/{id}/archiver")
        ResponseEntity<Map<String, Object>> archiver(
                        @Parameter(description = "Identifiant du médicament") @PathVariable UUID id);

        @Operation(summary = "Désarchiver un médicament", description = """
                        Réintègre le médicament dans le catalogue actif. Opération idempotente.

                        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
        @ApiResponse(responseCode = "200", description = "Médicament désarchivé")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Médicament introuvable (MEDICAMENT_NOT_FOUND)")
        @PatchMapping("/{id}/desarchiver")
        ResponseEntity<Map<String, Object>> desarchiver(
                        @Parameter(description = "Identifiant du médicament") @PathVariable UUID id);

        @Operation(summary = "Obtenir un médicament", description = """
                        Retourne le détail complet d'un médicament, actif ou archivé, y compris le libellé \
                        de sa forme et de sa famille (résolus, pas seulement leurs identifiants).

                        Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
        @ApiResponse(responseCode = "200", description = "Médicament récupéré")
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @ApiResponse(responseCode = "404", description = "Médicament introuvable (MEDICAMENT_NOT_FOUND)")
        @GetMapping("/{id}")
        ResponseEntity<Map<String, Object>> obtenir(
                        @Parameter(description = "Identifiant du médicament") @PathVariable UUID id);

        @Operation(summary = "Lister les médicaments", description = """
                        Recherche paginée sur l'ensemble du catalogue. `q` recherche en texte libre (code, \
                        nom commercial, DCI). `familleId`/`formeId` filtrent sur une famille ou une forme \
                        données. `actif` filtre sur le statut. `sortBy`/`sortDirection` pilotent le tri \
                        (défaut `createdAt`/`DESC`).

                        Rôle requis : acteurs PNA, PRA ou `GESTIONNAIRE_STRUCTURE`.""")
        @ApiResponse(responseCode = "200", description = "Liste des médicaments récupérée", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                        {
                          "status": 200,
                          "type": "MEDICAMENTS_LISTED",
                          "message": "Liste des médicaments récupérée",
                          "timestamp": "2024-01-01T12:00:00Z",
                          "results": [
                            {
                              "id": "550e8400-e29b-41d4-a716-446655440000",
                              "code": "PARA500",
                              "nomCommercial": "Doliprane",
                              "dci": "Paracétamol",
                              "dosage": "500mg",
                              "formeId": "8a2c...",
                              "formeLibelle": "Comprimé",
                              "familleId": "3e7a...",
                              "familleLibelle": "Antalgiques",
                              "voieAdministration": "ORALE",
                              "temperatureConservation": "AMBIANTE",
                              "programmeSante": null,
                              "delaiApprovisionnementJours": 30,
                              "necessiteOrdonnance": false,
                              "fabricant": "Sanofi",
                              "stockMinimum": 500,
                              "stockMaximum": 5000,
                              "actif": true,
                              "createdAt": "2024-01-01T12:00:00Z",
                              "updatedAt": "2024-01-01T12:00:00Z"
                            }
                          ],
                          "pagination": {
                            "currentPage": 0,
                            "totalPages": 5,
                            "totalItems": 96,
                            "first": true,
                            "last": false
                          }
                        }
                        """)))
        @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
        @ApiResponse(responseCode = "403", description = "Rôle non autorisé")
        @GetMapping
        ResponseEntity<Map<String, Object>> lister(
                        @Parameter(description = "Recherche texte libre sur le code, le nom commercial ou la DCI") @RequestParam(required = false) String q,
                        @Parameter(description = "Filtre sur une famille thérapeutique") @RequestParam(required = false) UUID familleId,
                        @Parameter(description = "Filtre sur une forme pharmaceutique") @RequestParam(required = false) UUID formeId,
                        @Parameter(description = "Filtre sur le statut actif/archivé") @RequestParam(required = false) Boolean actif,
                        @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
                        @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
                        @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);
}