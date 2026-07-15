package ministere.sante.senpna.commandeachat.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement.EspaceFournisseurCommandesAchatController;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ConfirmerDelaiLivraisonRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.GenererAvisExpeditionRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.SoumettreFactureRequest;

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
 * Contrat API — espace fournisseur : réception et traitement des bons de
 * commande émis par la PNA, mise à jour des informations de livraison, et
 * soumission des factures (cf. table de fonctionnalités « Gestion des
 * Commandes d'Achat » et « Suivi des Livraisons »). Cf.
 * {@link EspaceFournisseurCommandesAchatController} pour l'implémentation.
 * Le fournisseur est résolu depuis le JWT, jamais depuis un paramètre de
 * requête.
 *
 * <pre>
 * GET    /api/fournisseur/commandes-achat?statut=&page=&size=
 * GET    /api/fournisseur/commandes-achat/{id}
 * PATCH  /api/fournisseur/commandes-achat/{id}/accuser-reception
 * PATCH  /api/fournisseur/commandes-achat/{id}/confirmer-delai      {delaiJours, dateLivraisonConfirmee}
 * PATCH  /api/fournisseur/commandes-achat/{id}/avis-expedition      {dateExpedition, transporteur?, numeroSuivi?, lignes[]}
 * POST   /api/fournisseur/commandes-achat/{id}/factures             {numeroFacture, montant, dateEmission, ...}
 * GET    /api/fournisseur/commandes-achat/factures?statut=&page=&size=
 * GET    /api/fournisseur/commandes-achat/factures/{id}
 * </pre>
 */
@Tag(name = "Commandes d'achat (espace fournisseur)", description = """
    Réception et traitement des bons de commande émis par la SEN-PNA (accusé de \
    réception, confirmation des délais, génération de l'avis d'expédition) et suivi des \
    livraisons vers les entrepôts SEN-PNA (lots, dates de péremption, certificats \
    d'analyse), ainsi que dépôt des factures.""")
@RequestMapping("/api/fournisseur/commandes-achat")
public interface IEspaceFournisseurCommandesAchatController {

    @Operation(summary = "Lister mes commandes d'achat", description = """
        Recherche paginée sur les commandes d'achat émises par la PNA pour le fournisseur \
        connecté.

        Rôle requis : `FOURNISSEUR`.""")
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutCommandeAchat statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Obtenir une de mes commandes d'achat", description = """
        Retourne le détail d'une commande d'achat — accès restreint au fournisseur \
        destinataire.

        Rôle requis : `FOURNISSEUR`.""")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(@Parameter(description = "Identifiant de la commande") @PathVariable UUID id);

    @Operation(summary = "Accuser réception du bon de commande", description = """
        Confirme la prise de connaissance du bon de commande — n'affecte pas le statut \
        de la commande, se contente d'horodater l'accusé de réception (cf. doc. métier \
        « Accusé de réception »).

        Rôle requis : `FOURNISSEUR`.""")
    @PatchMapping("/{id}/accuser-reception")
    ResponseEntity<Map<String, Object>> accuserReception(@Parameter(description = "Identifiant de la commande") @PathVariable UUID id);

    @Operation(summary = "Confirmer le délai de livraison", description = """
        Confirme le délai de livraison (en jours) et la date de livraison prévue — fait \
        passer la commande de VALIDEE à EN_TRANSIT.

        Rôle requis : `FOURNISSEUR`.""")
    @PatchMapping("/{id}/confirmer-delai")
    ResponseEntity<Map<String, Object>> confirmerDelaiLivraison(
            @Parameter(description = "Identifiant de la commande") @PathVariable UUID id,
            @Valid @RequestBody ConfirmerDelaiLivraisonRequest request);

    @Operation(summary = "Générer l'avis d'expédition", description = """
        Renseigne, pour chaque ligne expédiée, le numéro de lot, les dates de \
        fabrication/péremption, le certificat d'analyse et la quantité expédiée (cf. \
        doc. métier « Suivi des Livraisons »). Fait passer la commande de EN_TRANSIT à \
        EXPEDIEE — au-delà, la commande ne peut plus être modifiée.

        Rôle requis : `FOURNISSEUR`.""")
    @PatchMapping("/{id}/avis-expedition")
    ResponseEntity<Map<String, Object>> genererAvisExpedition(
            @Parameter(description = "Identifiant de la commande") @PathVariable UUID id,
            @Valid @RequestBody GenererAvisExpeditionRequest request);

    @Operation(summary = "Soumettre une facture", description = """
        Dépose une facture pour une commande d'achat livrée (cf. doc. métier « Soumission \
        de Factures »).

        Rôle requis : `FOURNISSEUR`.""")
    @PostMapping("/{id}/factures")
    ResponseEntity<Map<String, Object>> soumettreFacture(
            @Parameter(description = "Identifiant de la commande") @PathVariable UUID id,
            @Valid @RequestBody SoumettreFactureRequest request);

    @Operation(summary = "Lister mes factures", description = """
        Recherche paginée sur les factures soumises par le fournisseur connecté.

        Rôle requis : `FOURNISSEUR`.""")
    @GetMapping("/factures")
    ResponseEntity<Map<String, Object>> listerFactures(
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutFacture statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Obtenir une de mes factures", description = "Rôle requis : `FOURNISSEUR`.")
    @GetMapping("/factures/{id}")
    ResponseEntity<Map<String, Object>> obtenirFacture(@Parameter(description = "Identifiant de la facture") @PathVariable UUID id);
}
