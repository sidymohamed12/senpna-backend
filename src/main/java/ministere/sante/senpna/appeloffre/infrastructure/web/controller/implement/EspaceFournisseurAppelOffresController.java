package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import ministere.sante.senpna.appeloffre.application.facade.EspaceFournisseurAppelOffreFacade;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.LigneOffreInput;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.IEspaceFournisseurAppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.LigneOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.SoumettreOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.AppelOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.AppelOffreSummaryResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.LigneAppelOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.LigneOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.OffreResponse;
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
public class EspaceFournisseurAppelOffresController implements IEspaceFournisseurAppelOffresController {

    private final EspaceFournisseurAppelOffreFacade espaceFournisseurAppelOffreFacade;

    public EspaceFournisseurAppelOffresController(
            EspaceFournisseurAppelOffreFacade espaceFournisseurAppelOffreFacade) {
        this.espaceFournisseurAppelOffreFacade = espaceFournisseurAppelOffreFacade;
    }

    @Override
    public ResponseEntity<Map<String, Object>> lister(String q, Integer page, Integer size) {
        AppelOffrePage result = espaceFournisseurAppelOffreFacade.listerAppelsOffresPublies(
                new ListAppelOffresQuery(q, null, page, size, "dateCloture", "ASC"));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "APPELS_OFFRES_LISTED",
                "Liste des appels d'offres ouverts récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        AppelOffreDetail result = espaceFournisseurAppelOffreFacade.obtenirAppelOffre(new GetAppelOffreQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_FOUND",
                "Appel d'offres récupéré"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> soumettreOffre(UUID id, SoumettreOffreRequest request) {
        UUID fournisseurId = currentFournisseurId();

        OffreDetail result = espaceFournisseurAppelOffreFacade.soumettreOffre(new SoumettreOffreCommand(
                id, fournisseurId, request.commentaire(), request.lignes().stream().map(this::toInput).toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "OFFRE_SOUMISE",
                        "Offre soumise avec succès"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private UUID currentFournisseurId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser principal = (CurrentUser) authentication.getPrincipal();
        return principal.getFournisseurId();
    }

    private LigneOffreInput toInput(LigneOffreRequest request) {
        return new LigneOffreInput(request.ligneAppelOffreId(), request.prixUnitaire(),
                request.delaiLivraisonJours());
    }

    private AppelOffreResponse toResponse(AppelOffreDetail detail) {
        return new AppelOffreResponse(detail.id(), detail.reference(), detail.objet(), detail.dateCloture(),
                detail.statut(), detail.lignes().stream().map(this::toResponse).toList(), detail.createdAt(),
                detail.updatedAt());
    }

    private AppelOffreSummaryResponse toResponse(AppelOffreSummary summary) {
        return new AppelOffreSummaryResponse(summary.id(), summary.reference(), summary.objet(),
                summary.dateCloture(), summary.statut(), summary.nombreLignes(), summary.createdAt());
    }

    private LigneAppelOffreResponse toResponse(LigneAppelOffreDetail ligne) {
        return new LigneAppelOffreResponse(ligne.id(), ligne.medicamentId(), ligne.designation(),
                ligne.quantiteEstimee(), ligne.uniteBase());
    }

    private OffreResponse toResponse(OffreDetail detail) {
        return new OffreResponse(detail.id(), detail.appelOffreId(), detail.fournisseurId(), detail.commentaire(),
                detail.statut(), detail.lignes().stream()
                        .map(l -> new LigneOffreResponse(l.id(), l.ligneAppelOffreId(), l.prixUnitaire(),
                                l.delaiLivraisonJours()))
                        .toList(),
                detail.createdAt(), detail.updatedAt());
    }
}
