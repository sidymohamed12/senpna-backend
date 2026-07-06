package ministere.sante.senpna.actualite.infrastructure.web.controller;

import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestion des actualités (vie associative, projets, partenariats...).
 *
 * <pre>
 * POST   /api/actualites                    {categorie, titre, description?, medias?, tags?}
 * PUT    /api/actualites/{id}                {categorie, titre, description?, medias?, tags?}
 * PATCH  /api/actualites/{id}/publier
 * PATCH  /api/actualites/{id}/desactiver
 * PATCH  /api/actualites/{id}/brouillon
 * GET    /api/actualites/{id}
 * GET    /api/actualites?q=&categorie=&statut=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/actualites")
public class ActualitesController {

        private final ActualiteFacade actualiteFacade;

        public ActualitesController(ActualiteFacade actualiteFacade) {
                this.actualiteFacade = actualiteFacade;
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateActualiteRequest request) {
                ActualiteDetail result = actualiteFacade.creerActualite(new CreateActualiteCommand(
                                currentUserId(), request.categorie(), request.titre(), request.description(),
                                toMediaInputs(request.medias()), request.tags()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "ACTUALITE_CREATED",
                                                "Actualité créée avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateActualiteRequest request) {
                ActualiteDetail result = actualiteFacade.modifierActualite(new UpdateActualiteCommand(
                                id, request.categorie(), request.titre(), request.description(),
                                toMediaInputs(request.medias()), request.tags()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_UPDATED",
                                "Actualité modifiée avec succès"));
        }

        @PatchMapping("/{id}/publier")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> publier(@PathVariable UUID id) {
                ActualiteDetail result = actualiteFacade.publierActualite(new PublierActualiteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_PUBLIEE",
                                "Actualité publiée avec succès"));
        }

        @PatchMapping("/{id}/desactiver")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
                ActualiteDetail result = actualiteFacade.desactiverActualite(new DesactiverActualiteCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_DESACTIVEE",
                                                "Actualité désactivée avec succès"));
        }

        @PatchMapping("/{id}/brouillon")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> remettreEnBrouillon(@PathVariable UUID id) {
                ActualiteDetail result = actualiteFacade
                                .remettreEnBrouillonActualite(new RemettreEnBrouillonActualiteCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_BROUILLON",
                                "Actualité remise en brouillon"));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                ActualiteDetail result = actualiteFacade.obtenirActualite(new GetActualiteQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_FOUND",
                                "Actualité récupérée"));
        }

        @GetMapping("/public/{id}")
        public ResponseEntity<Map<String, Object>> obtenirPublic(@PathVariable UUID id) {
                ActualiteDetail result = actualiteFacade.obtenirActualite(new GetActualiteQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "ACTUALITE_FOUND",
                                "Actualité récupérée"));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String categorie,
                        @RequestParam(required = false) String statut,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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

        @GetMapping("/public")
        public ResponseEntity<Map<String, Object>> listerPublic(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String categorie,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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
