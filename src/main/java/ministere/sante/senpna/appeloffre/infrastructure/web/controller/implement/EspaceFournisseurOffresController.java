package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import ministere.sante.senpna.appeloffre.application.facade.EspaceFournisseurAppelOffreFacade;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.LigneOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListMesOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.IEspaceFournisseurOffresController;
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
public class EspaceFournisseurOffresController implements IEspaceFournisseurOffresController {

    private final EspaceFournisseurAppelOffreFacade espaceFournisseurAppelOffreFacade;

    public EspaceFournisseurOffresController(EspaceFournisseurAppelOffreFacade espaceFournisseurAppelOffreFacade) {
        this.espaceFournisseurAppelOffreFacade = espaceFournisseurAppelOffreFacade;
    }

    @Override
    public ResponseEntity<Map<String, Object>> lister(StatutOffre statut, Integer page, Integer size) {
        OffrePage result = espaceFournisseurAppelOffreFacade
                .listerMesOffres(new ListMesOffresQuery(currentFournisseurId(), statut, page, size));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "MES_OFFRES_LISTED",
                "Liste de mes offres récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        OffreDetail result = espaceFournisseurAppelOffreFacade
                .obtenirOffre(new GetOffreQuery(id, currentFournisseurId()));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "OFFRE_FOUND", "Offre récupérée"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> retirer(UUID id) {
        OffreDetail result = espaceFournisseurAppelOffreFacade
                .retirerOffre(new RetirerOffreCommand(id, currentFournisseurId()));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "OFFRE_RETIREE", "Offre retirée"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private UUID currentFournisseurId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CurrentUser principal = (CurrentUser) authentication.getPrincipal();
        return principal.getFournisseurId();
    }

    private OffreResponse toResponse(OffreDetail detail) {
        return new OffreResponse(detail.id(), detail.appelOffreId(), detail.fournisseurId(), detail.commentaire(),
                detail.statut(), detail.lignes().stream().map(this::toResponse).toList(), detail.createdAt(),
                detail.updatedAt());
    }

    private LigneOffreResponse toResponse(LigneOffreDetail ligne) {
        return new LigneOffreResponse(ligne.id(), ligne.ligneAppelOffreId(), ligne.prixUnitaire(),
                ligne.delaiLivraisonJours());
    }
}
