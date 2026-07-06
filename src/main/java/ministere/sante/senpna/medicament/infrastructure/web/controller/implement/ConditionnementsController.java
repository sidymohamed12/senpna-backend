package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import ministere.sante.senpna.medicament.application.facade.MedicamentFacade;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.infrastructure.web.controller.IConditionnementsController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateConditionnementRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.ConditionnementResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class ConditionnementsController implements IConditionnementsController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final MedicamentFacade medicamentFacade;

        public ConditionnementsController(MedicamentFacade medicamentFacade) {
                this.medicamentFacade = medicamentFacade;
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(CreateConditionnementRequest request) {
                ConditionnementDetail result = medicamentFacade.creerConditionnement(new CreateConditionnementCommand(
                                request.medicamentId(), request.nom(), request.niveau(), request.quantiteUniteBase(),
                                request.estUniteBase(), request.prixAchat(), request.prixVente()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "CONDITIONNEMENT_CREATED",
                                                "Conditionnement créé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateConditionnementRequest request) {
                ConditionnementDetail result = medicamentFacade
                                .modifierConditionnement(new UpdateConditionnementCommand(
                                                id, request.nom(), request.niveau(), request.quantiteUniteBase(),
                                                request.estUniteBase(), request.prixAchat(), request.prixVente()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_UPDATED", "Conditionnement modifié avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(UUID id) {
                ConditionnementDetail result = medicamentFacade.archiverConditionnement(
                                new ArchiveConditionnementCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_ARCHIVED", "Conditionnement archivé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(UUID id) {
                ConditionnementDetail result = medicamentFacade.desarchiverConditionnement(
                                new DesarchiveConditionnementCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CONDITIONNEMENT_DESARCHIVED", "Conditionnement désarchivé avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                ConditionnementDetail result = medicamentFacade.obtenirConditionnement(new GetConditionnementQuery(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "CONDITIONNEMENT_FOUND",
                                                "Conditionnement récupéré"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        UUID medicamentId, Boolean actif, Integer page, Integer size, String sortBy,
                        String sortDirection) {

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
                                detail.quantiteUniteBase(), detail.estUniteBase(), detail.prixAchat(),
                                detail.prixVente(), detail.actif(), detail.createdAt(), detail.updatedAt());
        }
}
