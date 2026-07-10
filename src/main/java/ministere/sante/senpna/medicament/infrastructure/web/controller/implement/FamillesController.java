package ministere.sante.senpna.medicament.infrastructure.web.controller.implement;

import ministere.sante.senpna.medicament.application.facade.FamilleFacade;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ArchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.CreateFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.DesarchiveFamilleCommand;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamilleDetail;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.FamillePage;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.GetFamilleQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.ListFamillesQuery;
import ministere.sante.senpna.medicament.domain.command.FamilleCommands.UpdateFamilleCommand;
import ministere.sante.senpna.medicament.infrastructure.web.controller.IFamillesController;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.CreateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.request.UpdateFamilleRequest;
import ministere.sante.senpna.medicament.infrastructure.web.dto.response.FamilleResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class FamillesController implements IFamillesController {

        private static final String ROLES_LECTURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";
        private static final String ROLES_ECRITURE = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA')";

        private final FamilleFacade familleFacade;

        public FamillesController(FamilleFacade familleFacade) {
                this.familleFacade = familleFacade;
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> creer(CreateFamilleRequest request) {
                FamilleDetail result = familleFacade.creerFamille(
                                new CreateFamilleCommand(request.code(), request.libelle(), request.description()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FAMILLE_CREATED",
                                                "Famille créée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateFamilleRequest request) {
                FamilleDetail result = familleFacade.modifierFamille(
                                new UpdateFamilleCommand(id, request.libelle(), request.description()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_UPDATED",
                                "Famille modifiée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> archiver(UUID id) {
                FamilleDetail result = familleFacade.archiverFamille(new ArchiveFamilleCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_ARCHIVED",
                                "Famille archivée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_ECRITURE)
        public ResponseEntity<Map<String, Object>> desarchiver(UUID id) {
                FamilleDetail result = familleFacade.desarchiverFamille(new DesarchiveFamilleCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_DESARCHIVED",
                                "Famille désarchivée avec succès"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                FamilleDetail result = familleFacade.obtenirFamille(new GetFamilleQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FAMILLE_FOUND",
                                "Famille récupérée"));
        }

        @Override
        @PreAuthorize(ROLES_LECTURE)
        public ResponseEntity<Map<String, Object>> lister(
                        String q, Boolean actif, Integer page, Integer size, String sortBy, String sortDirection) {

                FamillePage result = familleFacade.listerFamilles(
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