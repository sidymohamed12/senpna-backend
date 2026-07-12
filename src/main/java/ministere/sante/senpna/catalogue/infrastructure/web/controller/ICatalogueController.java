package ministere.sante.senpna.catalogue.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ministere.sante.senpna.catalogue.infrastructure.web.controller.impl.CatalogueController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Catalogue.
 *
 * <p>
 * Porte la documentation OpenAPI/Swagger (routes, paramètres, exemples,
 * codes de retour) séparément de {@link CatalogueController}, qui n'en est
 * que l'implémentation métier — cf. règle « une classe, une raison de
 * changer » (SOLID/SRP) : documenter l'API et l'exposer via HTTP ne sont
 * pas la même responsabilité, et ne doivent donc pas changer pour les mêmes
 * raisons.
 * </p>
 *
 * <p>
 * Les annotations de routage ({@code @GetMapping}, {@code @RequestParam})
 * sont portées ici : Spring MVC les résout sur l'interface implémentée par
 * le bean contrôleur, il n'y a donc rien à répéter côté implémentation.
 * Les annotations de sécurité ({@code @PreAuthorize}) restent volontairement
 * sur {@link CatalogueController}, au plus près du code qu'elles protègent.
 * </p>
 *
 * <pre>
 * GET /api/catalogue/national?recherche=&ruptureUniquement=&page=&size=
 *     → stock de la PNA centrale, agrégé par médicament — visible par les PRA et la PNA.
 *
 * GET /api/catalogue/inter-pra?recherche=&medicamentId=&ruptureUniquement=&page=&size=
 *     → disponibilités de toutes les PRA, ventilées PRA par PRA — visible par toutes les PRA et la PNA.
 *
 * GET /api/catalogue/regional?regionId=&recherche=&ruptureUniquement=&page=&size=
 *     → stock de la PRA d'une région, agrégé par médicament — visible uniquement par les
 *       structures sanitaires de cette région (regionId n'est utilisable que par un acteur PNA ;
 *       pour toute autre personne, sa propre région est utilisée quel que soit ce paramètre).
 * </pre>
 */
@Tag(name = "Catalogue", description = """
    Consultation des catalogues — vues calculées en lecture seule, jamais persistées, \
    agrégées à la volée sur les lignes de stock existantes (modules stock, medicament, organisation). \
    Aucun endpoint d'écriture : ce contrôleur ne fait que projeter des données déjà possédées \
    par d'autres features.""")
@RequestMapping("/api/catalogue")
public interface ICatalogueController {

  @Operation(summary = "Consulter le catalogue national (stock PNA centrale)", description = """
      Retourne le stock de la PNA centrale, agrégé par médicament (toutes les lignes de \
      stock actives sont sommées, tous conditionnements ayant un prix confondus).

      **Visibilité** : acteurs PNA et PRA uniquement (`ADMIN_PNA`, `GESTIONNAIRE_PNA`, \
      `PHARMACIEN_PNA`, `MAGASINIER_PNA`, `ADMIN_PRA`, `GESTIONNAIRE_PRA`, `PHARMACIEN_PRA`, \
      `MAGASINIER_PRA`) — une structure sanitaire n'a pas accès au stock de la PNA centrale.

      `recherche` filtre sur le code, le nom commercial ou la DCI du médicament (insensible \
      à la casse). `ruptureUniquement=true` ne retourne que les médicaments dont \
      `enRupture=true`.""")
  @ApiResponse(responseCode = "200", description = "Catalogue national récupéré", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "CATALOGUE_NATIONAL_LISTED",
        "message": "Catalogue de la PNA récupéré",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "medicamentId": "550e8400-e29b-41d4-a716-446655440000",
            "code": "PARA500",
            "nomCommercial": "Doliprane",
            "dci": "Paracétamol",
            "familleNom": "Antalgiques",
            "fabricant": "Sanofi",
            "fournisseurNom": "Sanofi Sénégal",
            "quantiteDisponible": 12000,
            "nombreLotsActifs": 3,
            "prochaineDateExpiration": "2026-03-15",
            "enRupture": false,
            "conditionnements": [
              {
                "id": "6b1f...",
                "nom": "Boîte de 20",
                "niveau": 1,
                "quantiteUniteBase": 20,
                "prixAchat": 500,
                "prixVente": 750
              }
            ]
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 4,
          "totalItems": 78,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à consulter le catalogue national")
  @ApiResponse(responseCode = "404", description = "Aucun entrepôt PNA centrale actif n'est configuré (PNA_CENTRALE_NOT_CONFIGURED)")
  @GetMapping("/national")
  ResponseEntity<Map<String, Object>> catalogueNational(
      @Parameter(description = "Filtre texte sur le code, le nom commercial ou la DCI du médicament") @RequestParam(required = false) String recherche,
      @Parameter(description = "Si true, ne retourne que les médicaments en rupture de stock") @RequestParam(required = false) Boolean ruptureUniquement,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

  // ===================================================================================================

  @Operation(summary = "Consulter le catalogue inter-PRA (disponibilités ventilées par PRA)", description = """
      Retourne, pour chaque médicament, la quantité disponible dans **chaque** PRA du réseau \
      (bloc `disponibilites`), en plus du total réseau (`quantiteTotaleReseau`). Cas d'usage \
      principal : une PRA en rupture identifie une autre PRA disposant du stock pour lui \
      demander un transfert inter-PRA.

      **Visibilité** : acteurs PNA et PRA uniquement (mêmes rôles que le catalogue national) \
      — une structure sanitaire n'a pas accès à cette vue réseau.

      `medicamentId`, si fourni, restreint la recherche de disponibilités à un seul \
      médicament. `recherche` filtre sur le code, le nom commercial ou la DCI. \
      `ruptureUniquement=true` ne retourne que les médicaments en rupture au niveau réseau.""")
  @ApiResponse(responseCode = "200", description = "Catalogue inter-PRA récupéré", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "CATALOGUE_INTER_PRA_LISTED",
        "message": "Catalogue inter-PRA récupéré",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "medicamentId": "550e8400-e29b-41d4-a716-446655440000",
            "code": "PARA500",
            "nomCommercial": "Doliprane",
            "dci": "Paracétamol",
            "familleNom": "Antalgiques",
            "fabricant": "Sanofi",
            "quantiteTotaleReseau": 8400,
            "conditionnements": [
              {
                "id": "6b1f...",
                "nom": "Boîte de 20",
                "niveau": 1,
                "quantiteUniteBase": 20,
                "prixAchat": 500,
                "prixVente": 750
              }
            ],
            "disponibilites": [
              {
                "entrepotId": "8a2c...",
                "codeEntrepot": "PRA-DAKAR",
                "nomEntrepot": "PRA de Dakar",
                "regionId": "3e7a...",
                "fournisseurNom": "Sanofi Sénégal",
                "quantiteDisponible": 5400,
                "prochaineDateExpiration": "2026-05-01"
              }
            ]
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 2,
          "totalItems": 34,
          "first": true,
          "last": false
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé à consulter le catalogue inter-PRA")
  @GetMapping("/inter-pra")
  ResponseEntity<Map<String, Object>> catalogueInterPra(
      @Parameter(description = "Filtre texte sur le code, le nom commercial ou la DCI du médicament") @RequestParam(required = false) String recherche,
      @Parameter(description = "Restreint la recherche de disponibilités à un seul médicament") @RequestParam(required = false) UUID medicamentId,
      @Parameter(description = "Si true, ne retourne que les médicaments en rupture au niveau réseau") @RequestParam(required = false) Boolean ruptureUniquement,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

  // ===================================================================================================

  @Operation(summary = "Consulter le catalogue régional (stock de la PRA d'une région)", description = """
      Retourne le stock de la PRA rattachée à une région, agrégé par médicament — même \
      forme que le catalogue national, mais borné à une seule région.

      **Visibilité** : acteurs PNA, PRA et `GESTIONNAIRE_STRUCTURE` — une structure \
      sanitaire ne voit que le catalogue de sa propre région.

      **Résolution de `regionId`** : ce paramètre n'est utilisable que par un acteur PNA \
      (supervision inter-régions). Pour tout autre acteur, sa région est résolue \
      automatiquement à partir de son affectation et `regionId` est **ignoré**, quelle \
      que soit la valeur envoyée (cf. `CatalogueAccessGuard`).

      `recherche` filtre sur le code, le nom commercial ou la DCI. `ruptureUniquement=true` \
      ne retourne que les médicaments en rupture dans cette région.""")
  @ApiResponse(responseCode = "200", description = "Catalogue régional récupéré", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
      {
        "status": 200,
        "type": "CATALOGUE_REGIONAL_LISTED",
        "message": "Catalogue régional récupéré",
        "timestamp": "2024-01-01T12:00:00Z",
        "results": [
          {
            "medicamentId": "550e8400-e29b-41d4-a716-446655440000",
            "code": "PARA500",
            "nomCommercial": "Doliprane",
            "dci": "Paracétamol",
            "familleNom": "Antalgiques",
            "fabricant": "Sanofi",
            "fournisseurNom": "Sanofi Sénégal",
            "quantiteDisponible": 5400,
            "nombreLotsActifs": 2,
            "prochaineDateExpiration": "2026-05-01",
            "enRupture": false,
            "conditionnements": [
              {
                "id": "6b1f...",
                "nom": "Boîte de 20",
                "niveau": 1,
                "quantiteUniteBase": 20,
                "prixAchat": 500,
                "prixVente": 750
              }
            ]
          }
        ],
        "pagination": {
          "currentPage": 0,
          "totalPages": 1,
          "totalItems": 19,
          "first": true,
          "last": true
        }
      }
      """)))
  @ApiResponse(responseCode = "401", description = "JWT absent ou invalide")
  @ApiResponse(responseCode = "403", description = "Rôle non autorisé, ou acteur non affecté à une région (CATALOGUE_ACCESS_FORBIDDEN)")
  @ApiResponse(responseCode = "404", description = "Aucune PRA active rattachée à cette région (PRA_NOT_FOUND_FOR_REGION)")
  @GetMapping("/regional")
  ResponseEntity<Map<String, Object>> catalogueRegional(
      @Parameter(description = "Région ciblée — pris en compte uniquement pour un acteur PNA ; ignoré pour tout autre acteur, dont la région est résolue automatiquement") @RequestParam(required = false) UUID regionId,
      @Parameter(description = "Filtre texte sur le code, le nom commercial ou la DCI du médicament") @RequestParam(required = false) String recherche,
      @Parameter(description = "Si true, ne retourne que les médicaments en rupture dans cette région") @RequestParam(required = false) Boolean ruptureUniquement,
      @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
      @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

}
