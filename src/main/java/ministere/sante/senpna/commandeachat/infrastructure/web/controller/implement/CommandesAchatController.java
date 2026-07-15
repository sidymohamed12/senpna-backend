package ministere.sante.senpna.commandeachat.infrastructure.web.controller.implement;

import ministere.sante.senpna.commandeachat.application.facade.CommandeAchatFacade;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AnnulerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.AvisExpeditionDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatPage;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatSummary;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.GetCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.InfoReceptionLigneInput;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatInput;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ListCommandeAchatQuery;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ReceptionnerCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.RejeterCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.ValiderCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.MarquerFacturePayeeCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.RejeterFactureCommand;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ValiderFactureCommand;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.web.controller.ICommandesAchatController;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.CreateCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.InfoReceptionLigneRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.LigneCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.MotifRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.request.ReceptionnerCommandeAchatRequest;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.AvisExpeditionResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.CommandeAchatResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.CommandeAchatSummaryResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.FactureResponse;
import ministere.sante.senpna.commandeachat.infrastructure.web.dto.response.LigneCommandeAchatResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class CommandesAchatController implements ICommandesAchatController {

    private final CommandeAchatFacade commandeAchatFacade;

    public CommandesAchatController(CommandeAchatFacade commandeAchatFacade) {
        this.commandeAchatFacade = commandeAchatFacade;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> creer(CreateCommandeAchatRequest request) {
        CommandeAchatDetail result = commandeAchatFacade.creer(new CreateCommandeAchatCommand(
                request.reference(), request.fournisseurId(), request.entrepotDestinationId(),
                request.commentaire(), request.lignes().stream().map(this::toInput).toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "COMMANDE_ACHAT_CREATED",
                        "Commande d'achat créée avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> valider(UUID id) {
        CommandeAchatDetail result = commandeAchatFacade.valider(new ValiderCommandeAchatCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "COMMANDE_ACHAT_VALIDEE",
                "Commande d'achat validée avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> rejeter(UUID id, MotifRequest request) {
        CommandeAchatDetail result = commandeAchatFacade
                .rejeter(new RejeterCommandeAchatCommand(id, request.motif()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "COMMANDE_ACHAT_REJETEE",
                "Commande d'achat rejetée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> annuler(UUID id) {
        CommandeAchatDetail result = commandeAchatFacade.annuler(new AnnulerCommandeAchatCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "COMMANDE_ACHAT_ANNULEE",
                "Commande d'achat annulée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','MAGASINIER_PNA')")
    public ResponseEntity<Map<String, Object>> receptionner(UUID id, ReceptionnerCommandeAchatRequest request) {
        CommandeAchatDetail result = commandeAchatFacade.receptionner(new ReceptionnerCommandeAchatCommand(
                id, request.lignes().stream().map(this::toInfo).toList()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                "COMMANDE_ACHAT_RECEPTIONNEE", "Réception enregistrée avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        CommandeAchatDetail result = commandeAchatFacade.obtenir(new GetCommandeAchatQuery(id, null));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "COMMANDE_ACHAT_FOUND",
                "Commande d'achat récupérée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
    public ResponseEntity<Map<String, Object>> lister(String q, StatutCommandeAchat statut, Integer page,
            Integer size, String sortBy, String sortDirection) {
        CommandeAchatPage result = commandeAchatFacade
                .lister(new ListCommandeAchatQuery(q, statut, page, size, sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "COMMANDES_ACHAT_LISTED",
                "Liste des commandes d'achat récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> listerFactures(StatutFacture statut, Integer page, Integer size) {
        FacturePage result = commandeAchatFacade.listerFactures(new ListFacturesQuery(statut, page, size));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "FACTURES_LISTED",
                "Liste des factures récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> validerFacture(UUID id) {
        FactureDetail result = commandeAchatFacade.validerFacture(new ValiderFactureCommand(id));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FACTURE_VALIDEE", "Facture validée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> rejeterFacture(UUID id, MotifRequest request) {
        FactureDetail result = commandeAchatFacade.rejeterFacture(new RejeterFactureCommand(id, request.motif()));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FACTURE_REJETEE", "Facture rejetée"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> marquerFacturePayee(UUID id) {
        FactureDetail result = commandeAchatFacade.marquerFacturePayee(new MarquerFacturePayeeCommand(id));
        return ResponseEntity.ok(
                RestResponse.response(HttpStatus.OK, toResponse(result), "FACTURE_PAYEE", "Facture marquée payée"));
    }

    // ── Mapping ──────────────────────────────────────────────────────────

    private LigneCommandeAchatInput toInput(LigneCommandeAchatRequest request) {
        return new LigneCommandeAchatInput(request.medicamentId(), request.conditionnementId(),
                request.quantiteCommandee(), request.prixUnitaire());
    }

    private InfoReceptionLigneInput toInfo(InfoReceptionLigneRequest request) {
        return new InfoReceptionLigneInput(request.ligneId(), request.quantiteRecue(), request.quantiteRefusee(),
                request.motifRefus());
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
