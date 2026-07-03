package ministere.sante.senpna.medicament.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.ConditionnementResponse;
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
 * Gestion des conditionnements (niveaux d'emballage) d'un médicament.
 * Chaque médicament doit posséder exactement une unité de base
 * ({@code estUniteBase = true}),
 * dans laquelle son stock réel est exprimé.
 *
 * <pre>
 * POST   /api/conditionnements                  {medicamentId, nom, niveau, quantiteUniteBase, estUniteBase}
 * PUT    /api/conditionnements/{id}              {nom, niveau, quantiteUniteBase, estUniteBase}
 * PATCH  /api/conditionnements/{id}/archiver
 * PATCH  /api/conditionnements/{id}/desarchiver
 * GET    /api/conditionnements/{id}
 * GET    /api/conditionnements?medicamentId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/conditionnements")
public class ConditionnementsController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public ConditionnementsController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @PostMapping
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateConditionnementRequest request) {
                ConditionnementDetail result = medicamentFacade.creerConditionnement(new CreateConditionnementCommand(
                                request.medicamentId(), request.nom(), request.niveau(), request.quantiteUniteBase(),
                                request.estUniteBase()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "CONDITIONNEMENT_CREATED",
                                                "Conditionnement créé avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateConditionnementRequest request) {
                ConditionnementDetail result = medicamentFacade
                                .modifierConditionnement(new UpdateConditionnementCommand(
                                                id, request.nom(), request.niveau(), request.quantiteUniteBase(),
                                                request.estUniteBase()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_UPDATED", "Conditionnement modifié avec succès"));
        }

        @PatchMapping("/{id}/archiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(@PathVariable UUID id) {
                ConditionnementDetail result = medicamentFacade.archiverConditionnement(
                                new ArchiveConditionnementCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_ARCHIVED", "Conditionnement archivé avec succès"));
        }

        @PatchMapping("/{id}/desarchiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(@PathVariable UUID id) {
                ConditionnementDetail result = medicamentFacade.desarchiverConditionnement(
                                new DesarchiveConditionnementCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_DESARCHIVED", "Conditionnement désarchivé avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                ConditionnementDetail result = medicamentFacade.obtenirConditionnement(new GetConditionnementQuery(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "CONDITIONNEMENT_FOUND",
                                                "Conditionnement récupéré"));
        }

        @GetMapping
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) UUID medicamentId,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "niveau") String sortBy,
                        @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

                ConditionnementPage result = medicamentFacade.listerConditionnements(
                                new ListConditionnementsQuery(medicamentId, actif, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "CONDITIONNEMENTS_LISTED",
                                "Liste des conditionnements récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private ConditionnementResponse toResponse(ConditionnementDetail detail) {
                return new ConditionnementResponse(detail.id(), detail.medicamentId(), detail.nom(), detail.niveau(),
                                detail.quantiteUniteBase(), detail.estUniteBase(), detail.actif(), detail.createdAt(),
                                detail.updatedAt());
        }
}
