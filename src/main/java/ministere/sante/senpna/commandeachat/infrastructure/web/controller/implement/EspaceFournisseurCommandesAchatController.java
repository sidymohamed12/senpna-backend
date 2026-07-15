package ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement;

import ministere.sante.senpna.commandeachat.application.facade.EspaceFournisseurCommandeAchatFacade;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AccuserReceptionCommandeCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AvisExpeditionDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ConfirmerDelaiLivraisonCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GenererAvisExpeditionCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoExpeditionLigneInput;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListMesCommandesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.SoumettreFactureCommand;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.IEspaceFournisseurCommandesAchatController;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ConfirmerDelaiLivraisonRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.GenererAvisExpeditionRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.InfoExpeditionLigneRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.SoumettreFactureRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.AvisExpeditionResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.CommandeAchatResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.CommandeAchatSummaryResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.FactureResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.LigneCommandeAchatResponse;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('FOURNISSEUR')")
public class EspaceFournisseurCommandesAchatController implements IEspaceFournisseurCommandesAchatController {

    private final EspaceFournisseurCommandeAchatFacade facade;

    public EspaceFournisseurCommandesAchatController(EspaceFournisseurCommandeAchatFacade facade) {
        this.facade = facade;
    }

    @Override
    public ResponseEntity<Map<String, Object>> lister(StatutCommandeAchat statut, Integer page, Integer size) {
        CommandeAchatPage result = facade
                .listerMesCommandes(new ListMesCommandesQuery(currentFournisseurId(), statut, page, size));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "MES_COMMANDES_ACHAT_LISTED",
                "Liste de mes commandes d'achat récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        CommandeAchatDetail result = facade.obtenirCommande(new GetCommandeAchatQuery(id, currentFournisseurId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "COMMANDE_ACHAT_FOUND",
                "Commande d'achat récupérée"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> accuserReception(UUID id) {
        CommandeAchatDetail result = facade
                .accuserReception(new AccuserReceptionCommandeCommand(id, currentFournisseurId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "COMMANDE_ACHAT_ACCUSE_RECEPTION", "Accusé de réception enregistré"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmerDelaiLivraison(UUID id,
            ConfirmerDelaiLivraisonRequest request) {
        CommandeAchatDetail result = facade.confirmerDelaiLivraison(new ConfirmerDelaiLivraisonCommand(id,
                currentFournisseurId(), request.delaiJours(), request.dateLivraisonConfirmee()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "COMMANDE_ACHAT_DELAI_CONFIRME", "Délai de livraison confirmé"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> genererAvisExpedition(UUID id, GenererAvisExpeditionRequest request) {
        CommandeAchatDetail result = facade.genererAvisExpedition(new GenererAvisExpeditionCommand(
                id, currentFournisseurId(), request.dateExpedition(), request.transporteur(),
                request.numeroSuivi(), request.dateLivraisonEstimee(),
                request.lignes().stream().map(this::toInput).toList()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "COMMANDE_ACHAT_EXPEDIEE", "Avis d'expédition généré avec succès"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> soumettreFacture(UUID id, SoumettreFactureRequest request) {
        FactureDetail result = facade.soumettreFacture(new SoumettreFactureCommand(id, currentFournisseurId(),
                request.numeroFacture(), request.montant(), request.dateEmission(), request.dateEcheance(),
                request.pieceJointeMediaId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FACTURE_SOUMISE",
                        "Facture soumise avec succès"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> listerFactures(StatutFacture statut, Integer page, Integer size) {
        FacturePage result = facade
                .listerMesFactures(new ListMesFacturesQuery(currentFournisseurId(), statut, page, size));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "MES_FACTURES_LISTED",
                "Liste de mes factures récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    public ResponseEntity<Map<String, Object>> obtenirFacture(UUID id) {
        FactureDetail result = facade.obtenirFacture(new GetFactureQuery(id, currentFournisseurId()));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FACTURE_FOUND", "Facture récupérée"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private UUID currentFournisseurId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser principal = (CurrentUser) authentication.getPrincipal();
        return principal.getFournisseurId();
    }

    private InfoExpeditionLigneInput toInput(InfoExpeditionLigneRequest request) {
        return new InfoExpeditionLigneInput(request.ligneId(), request.numeroLot(), request.dateFabrication(),
                request.dateExpiration(), request.certificatAnalyseUrl(), request.quantiteExpediee());
    }

    private CommandeAchatResponse toResponse(CommandeAchatDetail detail) {
        return new CommandeAchatResponse(detail.id(), detail.reference(), detail.fournisseurId(),
                detail.entrepotDestinationId(), detail.statut(),
                detail.lignes().stream().map(this::toResponse).toList(), detail.dateAccuseReceptionFournisseur(),
                detail.delaiLivraisonConfirmeJours(), detail.dateLivraisonConfirmee(),
                toResponse(detail.avisExpedition()), detail.motifRejet(), detail.commentaire(), detail.createdAt(),
                detail.updatedAt());
    }

    private CommandeAchatSummaryResponse toResponse(CommandeAchatSummary summary) {
        return new CommandeAchatSummaryResponse(summary.id(), summary.reference(), summary.fournisseurId(),
                summary.statut(), summary.nombreLignes(), summary.createdAt());
    }

    private LigneCommandeAchatResponse toResponse(LigneCommandeAchatDetail ligne) {
        return new LigneCommandeAchatResponse(ligne.id(), ligne.medicamentId(), ligne.conditionnementId(),
                ligne.quantiteCommandee(), ligne.prixUnitaire(), ligne.numeroLot(), ligne.dateFabrication(),
                ligne.dateExpiration(), ligne.certificatAnalyseUrl(), ligne.quantiteExpediee(),
                ligne.quantiteRecue(), ligne.quantiteRefusee(), ligne.motifRefus());
    }

    private AvisExpeditionResponse toResponse(AvisExpeditionDetail avis) {
        if (avis == null) {
            return null;
        }
        return new AvisExpeditionResponse(avis.dateExpedition(), avis.transporteur(), avis.numeroSuivi(),
                avis.dateLivraisonEstimee());
    }

    private FactureResponse toResponse(FactureDetail detail) {
        return new FactureResponse(detail.id(), detail.commandeAchatId(), detail.fournisseurId(),
                detail.numeroFacture(), detail.montant(), detail.dateEmission(), detail.dateEcheance(),
                detail.pieceJointeMediaId(), detail.statut(), detail.motifRejet(), detail.createdAt(),
                detail.updatedAt());
    }
}
