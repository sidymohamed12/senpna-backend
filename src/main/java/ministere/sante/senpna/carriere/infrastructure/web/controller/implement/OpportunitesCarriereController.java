package ministere.sante.senpna.carriere.infrastructure.web.controller.implement;

import ministere.sante.senpna.carriere.application.facade.OpportuniteCarriereFacade;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.infrastructure.web.controller.IOpportunitesCarriereController;
import ministere.sante.senpna.carriere.infrastructure.web.dto.request.CreateOpportuniteCarriereRequest;
import ministere.sante.senpna.carriere.infrastructure.web.dto.request.UpdateOpportuniteCarriereRequest;
import ministere.sante.senpna.carriere.infrastructure.web.dto.response.OpportuniteCarriereResponse;
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
public class OpportunitesCarriereController implements IOpportunitesCarriereController {

        private final OpportuniteCarriereFacade opportuniteCarriereFacade;

        public OpportunitesCarriereController(OpportuniteCarriereFacade opportuniteCarriereFacade) {
                this.opportuniteCarriereFacade = opportuniteCarriereFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(CreateOpportuniteCarriereRequest request) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade.creerOpportunite(
                                new CreateOpportuniteCarriereCommand(currentUserId(), request.titre(),
                                                request.nomEntreprise(), request.description(),
                                                request.ficheDePosteUrl(), request.lieu(), request.typeContrat(),
                                                request.dateDebut(), request.dateLimiteCandidature(),
                                                request.emailContact()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result),
                                                "OPPORTUNITE_CARRIERE_CREATED", "Offre créée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateOpportuniteCarriereRequest request) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade.modifierOpportunite(
                                new UpdateOpportuniteCarriereCommand(id, request.titre(), request.nomEntreprise(),
                                                request.description(), request.ficheDePosteUrl(), request.lieu(),
                                                request.typeContrat(), request.dateDebut(),
                                                request.dateLimiteCandidature(), request.emailContact()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_UPDATED", "Offre modifiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> publier(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .publierOpportunite(new PublierOpportuniteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_PUBLIEE", "Offre publiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> mettreEnCours(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .mettreEnCoursOpportunite(new MettreEnCoursOpportuniteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_EN_COURS", "Offre passée en traitement"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> cloturer(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .cloturerOpportunite(new CloturerOpportuniteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_CLOTUREE", "Offre clôturée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> remettreEnBrouillon(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .remettreEnBrouillonOpportunite(new RemettreEnBrouillonOpportuniteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_BROUILLON", "Offre remise en brouillon"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .obtenirOpportunite(new GetOpportuniteCarriereQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_FOUND", "Offre récupérée"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> obtenirPublic(UUID id) {
                OpportuniteCarriereDetail result = opportuniteCarriereFacade
                                .obtenirOpportunitePublique(new GetOpportuniteCarriereQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "OPPORTUNITE_CARRIERE_FOUND", "Offre récupérée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> lister(String q, String typeContrat, String statut, Integer page,
                        Integer size, String sortBy, String sortDirection) {

                OpportuniteCarrierePage result = opportuniteCarriereFacade.listerOpportunites(
                                new ListOpportunitesCarriereQuery(q, typeContrat, statut, false, page, size, sortBy,
                                                sortDirection));

                return paginatedResponse(result);
        }

        @Override
        public ResponseEntity<Map<String, Object>> listerPublic(String q, String typeContrat, Integer page,
                        Integer size, String sortBy, String sortDirection) {

                OpportuniteCarrierePage result = opportuniteCarriereFacade.listerOpportunites(
                                new ListOpportunitesCarriereQuery(q, typeContrat, null, true, page, size, sortBy,
                                                sortDirection));

                return paginatedResponse(result);
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private ResponseEntity<Map<String, Object>> paginatedResponse(OpportuniteCarrierePage result) {
                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "OPPORTUNITES_CARRIERE_LISTED",
                                "Liste des opportunités de carrière récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private UUID currentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CurrentUser principal = (CurrentUser) authentication.getPrincipal();
                return principal.getUserId();
        }

        private OpportuniteCarriereResponse toResponse(OpportuniteCarriereDetail detail) {
                return new OpportuniteCarriereResponse(detail.id(), detail.titre(), detail.nomEntreprise(),
                                detail.description(), detail.ficheDePosteUrl(), detail.lieu(), detail.typeContrat(),
                                detail.dateDebut(), detail.dateLimiteCandidature(), detail.auteurId(),
                                detail.auteurNom(), detail.emailContact(), detail.statut(), detail.createdAt(),
                                detail.updatedAt());
        }
}
