package ministere.sante.senpna.appeloffre.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.AppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.AttribuerAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.CreateAppelOffreRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Appels d'offres (côté PNA). Cf.
 * {@link AppelOffresController} pour l'implémentation, et
 * {@link IEspaceFournisseurAppelOffresController} pour le pendant côté
 * espace fournisseur.
 *
 * <pre>
 * POST   /api/appels-offres                        {reference, objet, dateCloture, lignes[]}
 * PATCH  /api/appels-offres/{id}/publier
 * PATCH  /api/appels-offres/{id}/cloturer
 * PATCH  /api/appels-offres/{id}/annuler
 * PATCH  /api/appels-offres/{id}/attribuer          {offresRetenuesIds[], offresRejeteesIds[]}
 * GET    /api/appels-offres/{id}
 * GET    /api/appels-offres?q=&statut=&page=&size=
 * GET    /api/appels-offres/{id}/offres
 * </pre>
 */
@Tag(name = "Appels d'offres (PNA)", description = """
    Mise en concurrence des fournisseurs par la PNA (cf. doc. métier §b, flows CAS 1) : \
    création, publication, clôture, annulation et attribution des appels d'offres, ainsi \
    que l'analyse des offres reçues.""")
@RequestMapping("/api/appels-offres")
public interface IAppelOffresController {

    @Operation(summary = "Créer un appel d'offres", description = """
        Crée un appel d'offres à l'état BROUILLON, invisible des fournisseurs tant qu'il \
        n'est pas publié. La date de clôture doit être future. Au moins une ligne est \
        requise.

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PostMapping
    ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateAppelOffreRequest request);

    @Operation(summary = "Publier un appel d'offres", description = """
        Fait passer l'appel d'offres de BROUILLON à PUBLIE — il devient dès lors visible \
        et ouvert à la soumission d'offres par tous les fournisseurs actifs.

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PatchMapping("/{id}/publier")
    ResponseEntity<Map<String, Object>> publier(@Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id);

    @Operation(summary = "Clôturer un appel d'offres", description = """
        Fait passer l'appel d'offres de PUBLIE à CLOTURE — plus aucune offre ne peut être \
        soumise ou retirée. Peut être appelé manuellement par la PNA avant la date de \
        clôture, ou intervient automatiquement (scheduler) une fois celle-ci dépassée.

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PatchMapping("/{id}/cloturer")
    ResponseEntity<Map<String, Object>> clorer(@Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id);

    @Operation(summary = "Annuler un appel d'offres", description = """
        Annule l'appel d'offres — possible depuis n'importe quel statut non terminal \
        (BROUILLON, PUBLIE, CLOTURE), jamais depuis ATTRIBUE.

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PatchMapping("/{id}/annuler")
    ResponseEntity<Map<String, Object>> annuler(@Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id);

    @Operation(summary = "Attribuer un appel d'offres", description = """
        Fait passer l'appel d'offres de CLOTURE à ATTRIBUE et statue simultanément sur \
        les offres reçues : celles listées dans `offresRetenuesIds` passent à RETENUE, \
        celles de `offresRejeteesIds` à REJETEE (cf. doc. métier §c « Signature du \
        contrat »).

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PatchMapping("/{id}/attribuer")
    ResponseEntity<Map<String, Object>> attribuer(
            @Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id,
            @RequestBody AttribuerAppelOffreRequest request);

    @Operation(summary = "Obtenir un appel d'offres", description = """
        Retourne le détail complet d'un appel d'offres, quel que soit son statut.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou `MAGASINIER_PNA`.""")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(@Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id);

    @Operation(summary = "Lister les appels d'offres", description = """
        Recherche paginée sur l'ensemble des appels d'offres, tous statuts confondus.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou `MAGASINIER_PNA`.""")
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Recherche texte libre sur la référence ou l'objet") @RequestParam(required = false) String q,
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutAppelOffre statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

    @Operation(summary = "Lister les offres reçues pour un appel d'offres", description = """
        Analyse comparative des offres soumises par les fournisseurs pour cet appel \
        d'offres — utilisé par la PNA avant attribution.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @GetMapping("/{id}/offres")
    ResponseEntity<Map<String, Object>> listerOffres(
            @Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Retenir une offre", description = """
        Marque une offre comme retenue, hors attribution globale de l'AO — utile pour \
        affiner la décision avant l'attribution finale.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @PatchMapping("/offres/{offreId}/retenir")
    ResponseEntity<Map<String, Object>> retenirOffre(@Parameter(description = "Identifiant de l'offre") @PathVariable UUID offreId);

    @Operation(summary = "Rejeter une offre", description = """
        Marque une offre comme rejetée.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @PatchMapping("/offres/{offreId}/rejeter")
    ResponseEntity<Map<String, Object>> rejeterOffre(@Parameter(description = "Identifiant de l'offre") @PathVariable UUID offreId);
}
