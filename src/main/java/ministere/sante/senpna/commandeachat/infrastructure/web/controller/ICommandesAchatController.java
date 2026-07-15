package ministere.sante.senpna.commandeachat.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement.CommandesAchatController;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.CreateCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.MotifRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ReceptionnerCommandeAchatRequest;

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
 * Contrat API — gestion des commandes d'achat fournisseur côté PNA. Cf.
 * {@link CommandesAchatController} pour l'implémentation.
 *
 * <pre>
 * POST   /api/commandes-achat                       {reference, fournisseurId, entrepotDestinationId, lignes[]}
 * PATCH  /api/commandes-achat/{id}/valider
 * PATCH  /api/commandes-achat/{id}/rejeter           {motif?}
 * PATCH  /api/commandes-achat/{id}/annuler
 * PATCH  /api/commandes-achat/{id}/receptionner      {lignes[]}
 * GET    /api/commandes-achat/{id}
 * GET    /api/commandes-achat?q=&statut=&page=&size=
 * GET    /api/commandes-achat/factures?statut=&page=&size=
 * PATCH  /api/commandes-achat/factures/{id}/valider
 * PATCH  /api/commandes-achat/factures/{id}/rejeter  {motif?}
 * PATCH  /api/commandes-achat/factures/{id}/payer
 * </pre>
 */
@Tag(name = "Commandes d'achat (PNA)", description = """
    Gestion des bons de commande d'achat fournisseur (cf. doc. métier flows CAS 1) : \
    création, validation interne, réception, ainsi que le suivi et la validation des \
    factures fournisseurs.""")
@RequestMapping("/api/commandes-achat")
public interface ICommandesAchatController {

    @Operation(summary = "Créer une commande d'achat", description = """
        Crée un bon de commande d'achat fournisseur à l'état EN_ATTENTE_VALIDATION (cf. \
        doc. métier §d « Création du Bon de Commande »).

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PostMapping
    ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateCommandeAchatRequest request);

    @Operation(summary = "Valider une commande d'achat", description = """
        Validation interne (cf. doc. métier §e : Pharmacien Responsable, Direction \
        Générale, Direction Financière) — fait passer la commande en VALIDEE, la rendant \
        visible et actionnable dans l'espace fournisseur.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @PatchMapping("/{id}/valider")
    ResponseEntity<Map<String, Object>> valider(@Parameter(description = "Identifiant de la commande") @PathVariable UUID id);

    @Operation(summary = "Rejeter une commande d'achat", description = """
        Rejette la commande en validation interne — statut terminal REJETEE.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @PatchMapping("/{id}/rejeter")
    ResponseEntity<Map<String, Object>> rejeter(
            @Parameter(description = "Identifiant de la commande") @PathVariable UUID id,
            @RequestBody MotifRequest request);

    @Operation(summary = "Annuler une commande d'achat", description = """
        Annule la commande — possible uniquement avant expédition (EN_ATTENTE_VALIDATION, \
        VALIDEE ou EN_TRANSIT).

        Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.""")
    @PatchMapping("/{id}/annuler")
    ResponseEntity<Map<String, Object>> annuler(@Parameter(description = "Identifiant de la commande") @PathVariable UUID id);

    @Operation(summary = "Réceptionner une commande d'achat", description = """
        Enregistre une réception totale ou partielle (cf. doc. métier §11 « Gestion des \
        réceptions ») — le statut de la commande évolue automatiquement vers \
        PARTIELLEMENT_RECEPTIONNEE ou RECEPTIONNEE selon que toutes les lignes sont ou \
        non complètement réceptionnées.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `MAGASINIER_PNA`.""")
    @PatchMapping("/{id}/receptionner")
    ResponseEntity<Map<String, Object>> receptionner(
            @Parameter(description = "Identifiant de la commande") @PathVariable UUID id,
            @Valid @RequestBody ReceptionnerCommandeAchatRequest request);

    @Operation(summary = "Obtenir une commande d'achat", description = """
        Retourne le détail complet d'une commande d'achat.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou `MAGASINIER_PNA`.""")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenir(@Parameter(description = "Identifiant de la commande") @PathVariable UUID id);

    @Operation(summary = "Lister les commandes d'achat", description = """
        Recherche paginée sur l'ensemble des commandes d'achat.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA`, `PHARMACIEN_PNA` ou `MAGASINIER_PNA`.""")
    @GetMapping
    ResponseEntity<Map<String, Object>> lister(
            @Parameter(description = "Recherche texte libre sur la référence") @RequestParam(required = false) String q,
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutCommandeAchat statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sens de tri (ASC ou DESC)") @RequestParam(required = false, defaultValue = "DESC") String sortDirection);

    @Operation(summary = "Lister les factures fournisseurs", description = """
        Recherche paginée sur l'ensemble des factures soumises par les fournisseurs.

        Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.""")
    @GetMapping("/factures")
    ResponseEntity<Map<String, Object>> listerFactures(
            @Parameter(description = "Filtre sur le statut") @RequestParam(required = false) StatutFacture statut,
            @Parameter(description = "Numéro de page, base 0") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Taille de page") @RequestParam(required = false, defaultValue = "20") Integer size);

    @Operation(summary = "Valider une facture", description = "Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.")
    @PatchMapping("/factures/{id}/valider")
    ResponseEntity<Map<String, Object>> validerFacture(@Parameter(description = "Identifiant de la facture") @PathVariable UUID id);

    @Operation(summary = "Rejeter une facture", description = "Rôle requis : `ADMIN_PNA`, `GESTIONNAIRE_PNA` ou `PHARMACIEN_PNA`.")
    @PatchMapping("/factures/{id}/rejeter")
    ResponseEntity<Map<String, Object>> rejeterFacture(
            @Parameter(description = "Identifiant de la facture") @PathVariable UUID id,
            @RequestBody MotifRequest request);

    @Operation(summary = "Marquer une facture comme payée", description = "Rôle requis : `ADMIN_PNA` ou `GESTIONNAIRE_PNA`.")
    @PatchMapping("/factures/{id}/payer")
    ResponseEntity<Map<String, Object>> marquerFacturePayee(@Parameter(description = "Identifiant de la facture") @PathVariable UUID id);
}
