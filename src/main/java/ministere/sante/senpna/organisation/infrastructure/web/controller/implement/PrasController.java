package ministere.sante.senpna.organisation.infrastructure.web.controller.implement;

import ministere.sante.senpna.organisation.application.facade.EntrepotFacade;
import ministere.sante.senpna.organisation.application.facade.PraFacade;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.Entrepot.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.Entrepot.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.Entrepot.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.command.Entrepot.UpdatePraCommand;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.infrastructure.web.controller.IPrasController;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.CreatePraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.UpdatePraRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.EntrepotResponse;
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
public class PrasController implements IPrasController {

        private final PraFacade praFacade;
        private final EntrepotFacade entrepotFacade;

        public PrasController(PraFacade praFacade, EntrepotFacade entrepotFacade) {
                this.praFacade = praFacade;
                this.entrepotFacade = entrepotFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(CreatePraRequest request) {
                EntrepotDetail result = praFacade.creerPra(new CreatePraCommand(
                                request.code(), request.nom(), request.regionId(), request.adresse(),
                                request.telephone()));
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "PRA_CREATED",
                                                "PRA créée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdatePraRequest request) {
                EntrepotDetail result = praFacade.modifierPra(new UpdatePraCommand(
                                id, currentUserId(), request.nom(), request.adresse(), request.telephone(),
                                request.regionId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_UPDATED",
                                "PRA modifiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                EntrepotDetail result = praFacade.desactiverPra(new DeactivatePraCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_DEACTIVATED",
                                "PRA désactivée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> activer(UUID id) {
                EntrepotDetail result = praFacade.activerPra(new ActivatePraCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_ACTIVATED",
                                "PRA activée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                EntrepotDetail result = entrepotFacade.obtenirEntrepot(new GetEntrepotQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_FOUND",
                                "PRA récupérée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> lister(
                        String q, UUID regionId, Boolean actif, Integer page, Integer size, String sortBy,
                        String sortDirection) {

                EntrepotPage result = entrepotFacade.listerEntrepots(
                                new ListEntrepotsQuery(q, TypeEntrepot.PRA, regionId, actif, page, size, sortBy,
                                                sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "PRAS_LISTED",
                                "Liste des PRA récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private EntrepotResponse toResponse(EntrepotDetail detail) {
                return new EntrepotResponse(detail.id(), detail.code(), detail.nom(), detail.type(), detail.regionId(),
                                detail.regionNom(), detail.adresse(), detail.telephone(), detail.responsableUserId(),
                                detail.actif(), detail.createdAt(), detail.updatedAt());
        }

        private UUID currentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CurrentUser principal = (CurrentUser) authentication.getPrincipal();
                return principal.getUserId();
        }
}
