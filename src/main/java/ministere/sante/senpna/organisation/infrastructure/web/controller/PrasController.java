package ministere.sante.senpna.organisation.infrastructure.web.controller;

import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivatePraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.EntrepotPage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetEntrepotQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListEntrepotsQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdatePraCommand;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Gestion des Pharmacies Régionales d'Approvisionnement (PRA) —
 * représentations régionales de la PNA (cf. doc. métier §4 : "Gestion des
 * PRA").
 *
 * <p>
 * La création reste réservée aux rôles nationaux PNA. La modification,
 * l'activation et la désactivation sont également ouvertes aux rôles PRA
 * ({@code ADMIN_PRA}, {@code GESTIONNAIRE_PRA}), mais restreintes à la
 * région dont ils relèvent (cf. {@code RegionScopeResolver}) — un
 * utilisateur d'une région ne peut pas gérer l'entrepôt d'une autre
 * région.
 * </p>
 *
 * <pre>
 * POST   /api/pras                  {code, nom, regionId, adresse?, telephone?}
 * PUT    /api/pras/{id}             {nom, adresse?, telephone?, regionId?}
 * PATCH  /api/pras/{id}/desactiver
 * PATCH  /api/pras/{id}/activer
 * GET    /api/pras/{id}
 * GET    /api/pras?q=&regionId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/pras")
public class PrasController {

        private final OrganisationFacade organisationFacade;

        public PrasController(OrganisationFacade organisationFacade) {
                this.organisationFacade = organisationFacade;
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreatePraRequest request) {
                EntrepotDetail result = organisationFacade.creerPra(new CreatePraCommand(
                                request.code(), request.nom(), request.regionId(), request.adresse(),
                                request.telephone()));
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "PRA_CREATED",
                                                "PRA créée avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdatePraRequest request) {
                EntrepotDetail result = organisationFacade.modifierPra(new UpdatePraCommand(
                                id, currentUserId(), request.nom(), request.adresse(), request.telephone(),
                                request.regionId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_UPDATED",
                                "PRA modifiée avec succès"));
        }

        @PatchMapping("/{id}/desactiver")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
                EntrepotDetail result = organisationFacade.desactiverPra(new DeactivatePraCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_DEACTIVATED",
                                "PRA désactivée avec succès"));
        }

        @PatchMapping("/{id}/activer")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> activer(@PathVariable UUID id) {
                EntrepotDetail result = organisationFacade.activerPra(new ActivatePraCommand(id, currentUserId()));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_ACTIVATED",
                                "PRA activée avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                EntrepotDetail result = organisationFacade.obtenirEntrepot(new GetEntrepotQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PRA_FOUND",
                                "PRA récupérée"));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','ADMIN_PRA','GESTIONNAIRE_PRA')")
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) UUID regionId,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

                EntrepotPage result = organisationFacade.listerEntrepots(
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
