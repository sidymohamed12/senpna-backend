package ministere.sante.senpna.fournisseur.infrastructure.web.controller.implemment;

import ministere.sante.senpna.fournisseur.application.facade.FournisseurFacade;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.infrastructure.web.controller.IFournisseursController;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.CreateFournisseurRequest;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.request.UpdateFournisseurRequest;
import ministere.sante.senpna.fournisseur.infrastructure.web.dto.response.FournisseurResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class FournisseursController implements IFournisseursController {

        private final FournisseurFacade fournisseurFacade;

        public FournisseursController(FournisseurFacade fournisseurFacade) {
                this.fournisseurFacade = fournisseurFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(CreateFournisseurRequest request) {
                FournisseurDetail result = fournisseurFacade.creerFournisseur(new CreateFournisseurCommand(
                                request.nom(), request.adresse(), request.telephone(), request.email(),
                                request.contactPrincipal()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "FOURNISSEUR_CREATED",
                                                "Fournisseur créé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateFournisseurRequest request) {
                FournisseurDetail result = fournisseurFacade.modifierFournisseur(new UpdateFournisseurCommand(
                                id, request.nom(), request.adresse(), request.telephone(), request.email(),
                                request.contactPrincipal()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_UPDATED",
                                "Fournisseur modifié avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> activer(UUID id) {
                FournisseurDetail result = fournisseurFacade.activerFournisseur(new ActivateFournisseurCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_ACTIVATED",
                                                "Fournisseur activé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                FournisseurDetail result = fournisseurFacade
                                .desactiverFournisseur(new DeactivateFournisseurCommand(id));
                return ResponseEntity
                                .ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_DEACTIVATED",
                                                "Fournisseur désactivé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                FournisseurDetail result = fournisseurFacade.obtenirFournisseur(new GetFournisseurQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "FOURNISSEUR_FOUND",
                                "Fournisseur récupéré"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA','MAGASINIER_PNA')")
        public ResponseEntity<Map<String, Object>> lister(
                        String q, Boolean actif, Integer page, Integer size, String sortBy, String sortDirection) {

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