package ministere.sante.senpna.appeloffre.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.EspaceFournisseurOffresController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API — suivi des offres déposées par le fournisseur connecté. Cf.
 * {@link EspaceFournisseurOffresController} pour l'implémentation.
 *
 * <pre>
 * GET    /api/fournisseur/offres?statut=&page=&size=
 * GET    /api/fournisseur/offres/{id}
 * PATCH  /api/fournisseur/offres/{id}/retirer
 * </pre>
 */
@Tag(name = "Mes offres (espace fournisseur)", description = "Suivi du statut des offres soumises (cf. doc. métier « suivi du statut »).")
@RequestMapping("/api/fournisseur/offres")
public interface IEspaceFournisseurOffresController {

    @Operation(summary = "Lister mes offres", description = """
        Recherche paginée sur les offres soumises par le fournisseur connecté,
        éventuellement filtrées par statut.

        Rôle requis : `FOURNISSEUR`.""")
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutOffre statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Obtenir le détail d'une de mes offres", description = """
        Retourne le détail d'une offre — accès restreint au fournisseur propriétaire.

        Rôle requis : `FOURNISSEUR`.""")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(@Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);

    @Operation(summary = "Retirer une offre", description = """
        Retire une offre SOUMISE, avant la clôture de l'appel d'offres — au-delà, l'offre \
        ne peut plus être retirée.

        Rôle requis : `FOURNISSEUR`.""")
    @PatchMapping("/{id}/retirer")
    ResponseEntity<Map<String, Object>> retirer(@Parameter(description = "Identifiant de l'offre") @PathVariable UUID id);
}
