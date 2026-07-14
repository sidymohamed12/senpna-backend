package ministere.sante.senpna.appeloffre.infrastructure.web.controller.implement;

import ministere.sante.senpna.appeloffre.application.facade.AppelOffreFacade;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AnnulerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ClorerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreInput;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.PublierAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RejeterOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.web.controller.IAppelOffresController;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.AttribuerAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.CreateAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.request.LigneAppelOffreRequest;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.AppelOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.AppelOffreSummaryResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.LigneAppelOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.LigneOffreResponse;
import ministere.sante.senpna.appeloffre.infrastructure.web.dto.response.OffreResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class AppelOffresController implements IAppelOffresController {

    private final AppelOffreFacade appelOffreFacade;

    public AppelOffresController(AppelOffreFacade appelOffreFacade) {
        this.appelOffreFacade = appelOffreFacade;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> creer(CreateAppelOffreRequest request) {
        AppelOffreDetail result = appelOffreFacade.creer(new CreateAppelOffreCommand(
                request.reference(), request.objet(), request.dateCloture(),
                request.lignes().stream().map(this::toInput).toList()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.response(HttpStatus.CREATED, toResponse(result), "APPEL_OFFRE_CREATED",
                        "Appel d'offres créé avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> publier(UUID id) {
        AppelOffreDetail result = appelOffreFacade.publier(new PublierAppelOffreCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_PUBLIE",
                "Appel d'offres publié avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> clorer(UUID id) {
        AppelOffreDetail result = appelOffreFacade.clorer(new ClorerAppelOffreCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_CLOTURE",
                "Appel d'offres clôturé avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> annuler(UUID id) {
        AppelOffreDetail result = appelOffreFacade.annuler(new AnnulerAppelOffreCommand(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_ANNULE",
                "Appel d'offres annulé avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
    public ResponseEntity<Map<String, Object>> attribuer(UUID id, AttribuerAppelOffreRequest request) {
        AppelOffreDetail result = appelOffreFacade.attribuer(new AttribuerAppelOffreCommand(id,
                request.offresRetenuesIds(), request.offresRejeteesIds()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_ATTRIBUE",
                "Appel d'offres attribué avec succès"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
    public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
        AppelOffreDetail result = appelOffreFacade.obtenir(new GetAppelOffreQuery(id));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "APPEL_OFFRE_FOUND",
                "Appel d'offres récupéré"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
    public ResponseEntity<Map<String, Object>> lister(String q, StatutAppelOffre statut, Integer page, Integer size,
            String sortBy, String sortDirection) {
        AppelOffrePage result = appelOffreFacade
                .lister(new ListAppelOffresQuery(q, statut, page, size, sortBy, sortDirection));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "APPELS_OFFRES_LISTED",
                "Liste des appels d'offres récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> listerOffres(UUID id, Integer page, Integer size) {
        OffrePage result = appelOffreFacade.listerOffres(new ListOffresAppelOffreQuery(id, page, size));

        return ResponseEntity.ok(RestResponse.responsePaginate(
                HttpStatus.OK,
                result.content().stream().map(this::toResponse).toList(),
                "OFFRES_LISTED",
                "Liste des offres récupérée",
                result.page(),
                result.totalPages(),
                result.totalElements(),
                result.page() == 0,
                result.page() >= result.totalPages() - 1));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> retenirOffre(UUID offreId) {
        OffreDetail result = appelOffreFacade.retenirOffre(new RetenirOffreCommand(offreId));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "OFFRE_RETENUE", "Offre retenue"));
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')")
    public ResponseEntity<Map<String, Object>> rejeterOffre(UUID offreId) {
        OffreDetail result = appelOffreFacade.rejeterOffre(new RejeterOffreCommand(offreId));
        return ResponseEntity
                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "OFFRE_REJETEE", "Offre rejetée"));
    }

    // ── Mapping ──────────────────────────────────────────────────────────

    private LigneAppelOffreInput toInput(LigneAppelOffreRequest request) {
        return new LigneAppelOffreInput(request.medicamentId(), request.designation(), request.quantiteEstimee(),
                request.uniteBase());
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
