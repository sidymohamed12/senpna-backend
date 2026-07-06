package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.infrastructure.web.controller.IMedicamentsController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateMedicamentRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.MedicamentResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class MedicamentsController implements IMedicamentsController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public MedicamentsController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(CreateMedicamentRequest request) {
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

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateMedicamentRequest request) {
                MedicamentDetail result = medicamentFacade.modifierMedicament(new UpdateMedicamentCommand(
                                id, request.nomCommercial(), request.dci(), request.dosage(), request.formeId(),
                                request.familleId(), request.voieAdministration(), request.temperatureConservation(),
                                request.programmeSante(), request.delaiApprovisionnementJours(),
                                request.necessiteOrdonnance(),
                                request.fabricant(), request.stockMinimum(), request.stockMaximum()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_UPDATED",
                                "Médicament modifié avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(UUID id) {
                MedicamentDetail result = medicamentFacade.archiverMedicament(new ArchiveMedicamentCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_ARCHIVED",
                                "Médicament archivé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(UUID id) {
                MedicamentDetail result = medicamentFacade.desarchiverMedicament(new DesarchiveMedicamentCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_DESARCHIVED",
                                                "Médicament désarchivé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                MedicamentDetail result = medicamentFacade.obtenirMedicament(new GetMedicamentQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "MEDICAMENT_FOUND",
                                "Médicament récupéré"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        String q, UUID familleId, UUID formeId, Boolean actif, Integer page, Integer size,
                        String sortBy, String sortDirection) {

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