package ministere.sante.senpna.medicament.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.FamilleResponse;
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
 * Gestion du référentiel des familles thérapeutiques.
 * Utilisé par le catalogue des médicaments pour le classement et les
 * statistiques de consommation par famille.
 *
 * <pre>
 * POST   /api/familles                   {code, libelle, description?}
 * PUT    /api/familles/{id}               {libelle, description?}
 * PATCH  /api/familles/{id}/archiver
 * PATCH  /api/familles/{id}/desarchiver
 * GET    /api/familles/{id}
 * GET    /api/familles?q=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/familles")
public class FamillesController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public FamillesController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @PostMapping
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateFamilleRequest request) {
                FamilleDetail result = medicamentFacade.creerFamille(
                                new CreateFamilleCommand(request.code(), request.libelle(), request.description()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FAMILLE_CREATED",
                                                "Famille créée avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateFamilleRequest request) {
                FamilleDetail result = medicamentFacade.modifierFamille(
                                new UpdateFamilleCommand(id, request.libelle(), request.description()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_UPDATED",
                                "Famille modifiée avec succès"));
        }

        @PatchMapping("/{id}/archiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(@PathVariable UUID id) {
                FamilleDetail result = medicamentFacade.archiverFamille(new ArchiveFamilleCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_ARCHIVED",
                                "Famille archivée avec succès"));
        }

        @PatchMapping("/{id}/desarchiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(@PathVariable UUID id) {
                FamilleDetail result = medicamentFacade.desarchiverFamille(new DesarchiveFamilleCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_DESARCHIVED",
                                "Famille désarchivée avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                FamilleDetail result = medicamentFacade.obtenirFamille(new GetFamilleQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_FOUND",
                                "Famille récupérée"));
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

                FamillePage result = medicamentFacade.listerFamilles(
                                new ListFamillesQuery(q, actif, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "FAMILLES_LISTED",
                                "Liste des familles récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private FamilleResponse toResponse(FamilleDetail detail) {
                return new FamilleResponse(detail.id(), detail.code(), detail.libelle(), detail.description(),
                                detail.actif(), detail.createdAt(), detail.updatedAt());
        }
}
