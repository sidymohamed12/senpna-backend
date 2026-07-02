package ministere.sante.senpna.fournisseur.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.fournisseur.application.facade.FournisseurFacade;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.CreateFournisseurRequest;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.UpdateFournisseurRequest;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.response.FournisseurResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Gestion des fournisseurs de médicaments — CRUD (cf. doc. métier §5). La
 * PNA est seule habilitée à gérer les fournisseurs (les achats
 * fournisseurs ne sont réalisés que par la PNA).
 *
 * <pre>
 * POST   /api/fournisseurs                  {nom, adresse?, telephone?, email?, contactPrincipal?}
 * PUT    /api/fournisseurs/{id}              {nom, adresse?, telephone?, email?, contactPrincipal?}
 * PATCH  /api/fournisseurs/{id}/activer
 * PATCH  /api/fournisseurs/{id}/desactiver
 * GET    /api/fournisseurs/{id}
 * GET    /api/fournisseurs?q=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/fournisseurs")
public class FournisseursController {

        private final FournisseurFacade fournisseurFacade;

        public FournisseursController(FournisseurFacade fournisseurFacade) {
                this.fournisseurFacade = fournisseurFacade;
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateFournisseurRequest request) {
                FournisseurDetail result = fournisseurFacade.creerFournisseur(new CreateFournisseurCommand(
                                request.nom(), request.adresse(), request.telephone(), request.email(),
                                request.contactPrincipal()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FOURNISSEUR_CREATED",
                                                "Fournisseur créé avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateFournisseurRequest request) {
                FournisseurDetail result = fournisseurFacade.modifierFournisseur(new UpdateFournisseurCommand(
                                id, request.nom(), request.adresse(), request.telephone(), request.email(),
                                request.contactPrincipal()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_UPDATED",
                                "Fournisseur modifié avec succès"));
        }

        @PatchMapping("/{id}/activer")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> activer(@PathVariable UUID id) {
                FournisseurDetail result = fournisseurFacade.activerFournisseur(new ActivateFournisseurCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_ACTIVATED",
                                                "Fournisseur activé avec succès"));
        }

        @PatchMapping("/{id}/desactiver")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
                FournisseurDetail result = fournisseurFacade
                                .desactiverFournisseur(new DeactivateFournisseurCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_DEACTIVATED",
                                                "Fournisseur désactivé avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                FournisseurDetail result = fournisseurFacade.obtenirFournisseur(new GetFournisseurQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_FOUND",
                                "Fournisseur récupéré"));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

                FournisseurPage result = fournisseurFacade.listerFournisseurs(
                                new ListFournisseursQuery(q, actif, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "FOURNISSEURS_LISTED",
                                "Liste des fournisseurs récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private FournisseurResponse toResponse(FournisseurDetail detail) {
                return new FournisseurResponse(detail.id(), detail.nom(), detail.adresse(), detail.telephone(),
                                detail.email(), detail.contactPrincipal(), detail.actif(), detail.createdAt(),
                                detail.updatedAt());
        }
}
