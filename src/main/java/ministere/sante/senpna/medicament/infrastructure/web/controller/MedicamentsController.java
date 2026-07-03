package ministere.sante.senpna.medicament.infrastructure.web.controller;

import jakarta.validation.Valid;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.MedicamentResponse;
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
 * Gestion du catalogue national des médicaments.
 * Chaque médicament référence une {@code Famille} thérapeutique et une
 * {@code Forme} pharmaceutique, toutes deux devant être actives.
 *
 * <pre>
 * POST   /api/medicaments                  {code, nomCommercial, dci, dosage, formeId, familleId, ...}
 * PUT    /api/medicaments/{id}              {nomCommercial, dci, dosage, formeId, familleId, ...}
 * PATCH  /api/medicaments/{id}/archiver
 * PATCH  /api/medicaments/{id}/desarchiver
 * GET    /api/medicaments/{id}
 * GET    /api/medicaments?q=&familleId=&formeId=&actif=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/medicaments")
public class MedicamentsController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public MedicamentsController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @PostMapping
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateMedicamentRequest request) {
                MedicamentDetail result = medicamentFacade.creerMedicament(new CreateMedicamentCommand(
                                request.code(), request.nomCommercial(), request.dci(), request.dosage(),
                                request.formeId(),
                                request.familleId(), request.voieAdministration(), request.temperatureConservation(),
                                request.programmeSante(), request.delaiApprovisionnementJours(),
                                request.necessiteOrdonnance(),
                                request.fabricant(), request.stockMinimum(), request.stockMaximum()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "MEDICAMENT_CREATED",
                                                "Médicament créé avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateMedicamentRequest request) {
                MedicamentDetail result = medicamentFacade.modifierMedicament(new UpdateMedicamentCommand(
                                id, request.nomCommercial(), request.dci(), request.dosage(), request.formeId(),
                                request.familleId(), request.voieAdministration(), request.temperatureConservation(),
                                request.programmeSante(), request.delaiApprovisionnementJours(),
                                request.necessiteOrdonnance(),
                                request.fabricant(), request.stockMinimum(), request.stockMaximum()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_UPDATED",
                                "Médicament modifié avec succès"));
        }

        @PatchMapping("/{id}/archiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(@PathVariable UUID id) {
                MedicamentDetail result = medicamentFacade.archiverMedicament(new ArchiveMedicamentCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_ARCHIVED",
                                "Médicament archivé avec succès"));
        }

        @PatchMapping("/{id}/desarchiver")
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(@PathVariable UUID id) {
                MedicamentDetail result = medicamentFacade.desarchiverMedicament(new DesarchiveMedicamentCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_DESARCHIVED",
                                                "Médicament désarchivé avec succès"));
        }

        @GetMapping("/{id}")
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                MedicamentDetail result = medicamentFacade.obtenirMedicament(new GetMedicamentQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_FOUND",
                                "Médicament récupéré"));
        }

        @GetMapping
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) UUID familleId,
                        @RequestParam(required = false) UUID formeId,
                        @RequestParam(required = false) Boolean actif,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

                MedicamentPage result = medicamentFacade.listerMedicaments(
                                new ListMedicamentsQuery(q, familleId, formeId, actif, page, size, sortBy,
                                                sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "MEDICAMENTS_LISTED",
                                "Liste des médicaments récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private MedicamentResponse toResponse(MedicamentDetail detail) {
                return new MedicamentResponse(detail.id(), detail.code(), detail.nomCommercial(), detail.dci(),
                                detail.dosage(), detail.formeId(), detail.formeLibelle(), detail.familleId(),
                                detail.familleLibelle(), detail.voieAdministration(), detail.temperatureConservation(),
                                detail.programmeSante(), detail.delaiApprovisionnementJours(),
                                detail.necessiteOrdonnance(),
                                detail.fabricant(), detail.stockMinimum(), detail.stockMaximum(), detail.actif(),
                                detail.createdAt(), detail.updatedAt());
        }
}
