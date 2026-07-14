package ministere.sante.senpna.appeloffre.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement.EspaceFournisseurAppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.SoumettreOffreRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

/**
 * Contrat API du contrôleur Appels d'offres côté espace fournisseur — cf.
 * {@link EspaceFournisseurAppelOffresController} pour l'implémentation.
 * Le fournisseur est résolu depuis le JWT (claim {@code fournisseurId}),
 * jamais depuis un paramètre de requête — un fournisseur ne peut agir
 * qu'en son nom propre.
 *
 * <pre>
 * GET    /api/fournisseur/appels-offres?q=&page=&size=
 * GET    /api/fournisseur/appels-offres/{id}
 * POST   /api/fournisseur/appels-offres/{id}/offres      {commentaire?, lignes[]}
 * GET    /api/fournisseur/offres/{offreId}
 * PATCH  /api/fournisseur/offres/{offreId}/retirer
 * GET    /api/fournisseur/offres?statut=&page=&size=
 * </pre>
 */
@Tag(name = "Appels d'offres (espace fournisseur)", description = """
        Consultation des appels d'offres publiés par la PNA et gestion de ses propres offres \
        (cf. doc. métier §« Gestion des Appels d'Offres » : consultation, notification, \
        soumission électronique, suivi du statut).""")
@RequestMapping("/api/fournisseur/appels-offres")
public interface IEspaceFournisseurAppelOffresController {

    @Operation(summary = "Lister les appels d'offres ouverts", description = """
            Recherche paginée sur les appels d'offres actuellement publiés (statut PUBLIE \
            uniquement) — un fournisseur ne voit jamais un AO en brouillon, clôturé, attribué \
            ou annulé via ce endpoint.

            Rôle requis : `FOURNISSEUR`.""")
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Recherche texte libre sur la référence ou l'objet") @RequestParam(required = false) String q,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Obtenir le détail d'un appel d'offres", description = """
            Retourne le détail d'un appel d'offres — accessible dès sa publication et jusqu'à \
            son état terminal (pour le suivi), mais jamais à l'état BROUILLON.

            Rôle requis : `FOURNISSEUR`.""")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(
            @Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id);

    @Operation(summary = "Soumettre une offre", description = """
            Dépose une offre de prix en réponse à un appel d'offres publié, avant sa date de \
            clôture (cf. doc. métier « soumission électronique des offres »). Un fournisseur \
            ne peut avoir qu'une seule offre active (SOUMISE) par appel d'offres.

            Rôle requis : `FOURNISSEUR`.""")
    @PostMapping("/{id}/offres")
    ResponseEntity<Map<String, Object>> soumettreOffre(
            @Parameter(description = "Identifiant de l'appel d'offres") @PathVariable UUID id,
            @Valid @RequestBody SoumettreOffreRequest request);
}
