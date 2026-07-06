package ministere.sante.senpna.actualite.infrastructure.web.controller.implement;

import ministere.sante.senpna.actualite.application.facade.ActualiteFacade;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaInput;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;
import ministere.sante.senpna.actualite.infrastructure.web.controller.IActualitesController;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.CreateActualiteRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.MediaRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.request.UpdateActualiteRequest;
import ministere.sante.senpna.actualite.infrastructure.web.dto.response.ActualiteResponse;
import ministere.sante.senpna.actualite.infrastructure.web.dto.response.MediaResponse;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ActualitesController implements IActualitesController {

        private final ActualiteFacade actualiteFacade;

        public ActualitesController(ActualiteFacade actualiteFacade) {
                this.actualiteFacade = actualiteFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(CreateActualiteRequest request) {
                ActualiteDetail result = actualiteFacade.creerActualite(new CreateActualiteCommand(
                                currentUserId(), request.categorie(), request.titre(), request.description(),
                                toMediaInputs(request.medias()), request.tags()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "ACTUALITE_CREATED",
                                                "Actualité créée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateActualiteRequest request) {
                ActualiteDetail result = actualiteFacade.modifierActualite(new UpdateActualiteCommand(
                                id, request.categorie(), request.titre(), request.description(),
                                toMediaInputs(request.medias()), request.tags()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_UPDATED",
                                "Actualité modifiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> publier(UUID id) {
                ActualiteDetail result = actualiteFacade.publierActualite(new PublierActualiteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_PUBLIEE",
                                "Actualité publiée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                ActualiteDetail result = actualiteFacade.desactiverActualite(new DesactiverActualiteCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_DESACTIVEE",
                                                "Actualité désactivée avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> remettreEnBrouillon(UUID id) {
                ActualiteDetail result = actualiteFacade
                                .remettreEnBrouillonActualite(new RemettreEnBrouillonActualiteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_BROUILLON",
                                "Actualité remise en brouillon"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                ActualiteDetail result = actualiteFacade.obtenirActualite(new GetActualiteQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_FOUND",
                                "Actualité récupérée"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> obtenirPublic(UUID id) {
                ActualiteDetail result = actualiteFacade.obtenirActualitePublique(new GetActualiteQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_FOUND",
                                "Actualité récupérée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> lister(
                        String q, String categorie, String statut, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                ActualitePage result = actualiteFacade.listerActualites(
                                new ListActualitesQuery(q, categorie, statut, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "ACTUALITES_LISTED",
                                "Liste des actualités récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @Override
        public ResponseEntity<Map<String, Object>> listerPublic(
                        String q, String categorie, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                ActualitePage result = actualiteFacade.listerActualites(
                                new ListActualitesQuery(q, categorie, "PUBLIE", page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "ACTUALITES_LISTED",
                                "Liste des actualités récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private UUID currentUserId() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CurrentUser principal = (CurrentUser) authentication.getPrincipal();
                return principal.getUserId();
        }

        private List<MediaInput> toMediaInputs(List<MediaRequest> medias) {
                if (medias == null) {
                        return List.of();
                }
                return medias.stream().map(m -> new MediaInput(m.type(), m.url())).toList();
        }

        private ActualiteResponse toResponse(ActualiteDetail detail) {
                List<MediaResponse> medias = detail.medias().stream()
                                .map(this::toMediaResponse)
                                .toList();
                return new ActualiteResponse(detail.id(), detail.categorie(), detail.titre(), detail.description(),
                                medias,
                                detail.auteurId(), detail.auteurNom(), detail.tags(), detail.statut(),
                                detail.createdAt(),
                                detail.updatedAt());
        }

        private MediaResponse toMediaResponse(MediaDetail detail) {
                return new MediaResponse(detail.id(), detail.type(), detail.url(), detail.ordre());
        }
}