package ministere.sante.senpna.medicament.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFormeCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormeDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FormePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFormeQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFormesQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFormeCommand;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFormeRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFormeRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.FormeResponse;
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
 * Gestion du référentiel des formes pharmaceutiques.
 *
 * <pre>
 * POST   /api/formes                   {code, libelle, description?}
 * PUT    /api/formes/{id}               {libelle, description?}
 * PATCH  /api/formes/{id}/archiver
 * PATCH  /api/formes/{id}/desarchiver
 * GET    /api/formes/{id}
 * GET    /api/formes?q=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/formes")
public class FormesController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public FormesController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @PostMapping
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateFormeRequest request) {
                FormeDetail result = medicamentFacade.creerForme(
                                new CreateFormeCommand(request.code(), request.libelle(), request.description()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FORME_CREATED",
                                                "Forme créée avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateFormeRequest request) {
                FormeDetail result = medicamentFacade.modifierForme(
                                new UpdateFormeCommand(id, request.libelle(), request.description()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FORME_UPDATED",
                                "Forme modifiée avec succès"));
        }

        @PatchMapping("/{id}/archiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(@PathVariable UUID id) {
                FormeDetail result = medicamentFacade.archiverForme(new ArchiveFormeCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FORME_ARCHIVED",
                                "Forme archivée avec succès"));
        }

        @PatchMapping("/{id}/desarchiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(@PathVariable UUID id) {
                FormeDetail result = medicamentFacade.desarchiverForme(new DesarchiveFormeCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FORME_DESARCHIVED",
                                "Forme désarchivée avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                FormeDetail result = medicamentFacade.obtenirForme(new GetFormeQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FORME_FOUND",
                                "Forme récupérée"));
        }

        @GetMapping
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "libelle") String sortBy,
                        @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

                FormePage result = medicamentFacade.listerFormes(
                                new ListFormesQuery(q, actif, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "FORMES_LISTED",
                                "Liste des formes récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private FormeResponse toResponse(FormeDetail detail) {
                return new FormeResponse(detail.id(), detail.code(), detail.libelle(), detail.description(),
                                detail.actif(),
                                detail.createdAt(), detail.updatedAt());
        }
}
