package ministere.sante.senpna.projet.infrastructure.web.controller.implement;

import ministere.sante.senpna.projet.application.facade.ProjetFacade;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ArchiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.CreateProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.DesactiverProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.GetProjetQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ListProjetsQuery;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetDetail;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.ProjetPage;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.PublierProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.RemettreEnBrouillonProjetCommand;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.infrastructure.web.controller.IProjetsController;
import ministere.sante.senpna.projet.infrastructure.web.dto.request.CreateProjetRequest;
import ministere.sante.senpna.projet.infrastructure.web.dto.request.UpdateProjetRequest;
import ministere.sante.senpna.projet.infrastructure.web.dto.response.ProjetResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class ProjetsController implements IProjetsController {

        private final ProjetFacade projetFacade;

        public ProjetsController(ProjetFacade projetFacade) {
                this.projetFacade = projetFacade;
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(CreateProjetRequest request) {
                ProjetDetail result = projetFacade.creerProjet(new CreateProjetCommand(
                                request.categorie(), request.nom(), request.description(), request.objectifs(),
                                request.impacts(),
                                request.imageUrl()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "PROJET_CREATED",
                                                "Projet créé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(UUID id, UpdateProjetRequest request) {
                ProjetDetail result = projetFacade.modifierProjet(new UpdateProjetCommand(
                                id, request.categorie(), request.nom(), request.description(), request.objectifs(),
                                request.impacts(), request.imageUrl()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_UPDATED",
                                "Projet modifié avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> publier(UUID id) {
                ProjetDetail result = projetFacade.publierProjet(new PublierProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_PUBLIE",
                                "Projet publié avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> archiver(UUID id) {
                ProjetDetail result = projetFacade.archiverProjet(new ArchiverProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_ARCHIVE",
                                "Projet archivé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(UUID id) {
                ProjetDetail result = projetFacade.desactiverProjet(new DesactiverProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_DESACTIVE",
                                "Projet désactivé avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> remettreEnBrouillon(UUID id) {
                ProjetDetail result = projetFacade.remettreEnBrouillonProjet(new RemettreEnBrouillonProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_BROUILLON",
                                "Projet remis en brouillon"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                ProjetDetail result = projetFacade.obtenirProjet(new GetProjetQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_FOUND",
                                "Projet récupéré"));
        }

        @Override
        public ResponseEntity<Map<String, Object>> obtenirPublic(UUID id) {
                ProjetDetail result = projetFacade.obtenirProjetPublique(new GetProjetQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_FOUND",
                                "Projet récupéré"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> lister(
                        String q, String categorie, String statut, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                ProjetPage result = projetFacade.listerProjets(
                                new ListProjetsQuery(q, categorie, statut, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "PROJETS_LISTED",
                                "Liste des projets récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @Override
        public ResponseEntity<Map<String, Object>> listerPublic(
                        String q, String categorie, Integer page, Integer size, String sortBy, String sortDirection) {

                ProjetPage result = projetFacade.listerProjets(
                                new ListProjetsQuery(q, categorie, "PUBLIE", page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "PROJETS_LISTED_PUBLIC",
                                "Liste des projets récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private ProjetResponse toResponse(ProjetDetail detail) {
                return new ProjetResponse(detail.id(), detail.categorie(), detail.nom(), detail.description(),
                                detail.objectifs(), detail.impacts(), detail.imageUrl(), detail.statut(),
                                detail.createdAt(),
                                detail.updatedAt());
        }
}
