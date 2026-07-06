package ministere.sante.senpna.projet.infrastructure.web.controller;

import jakarta.validation.Valid;

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
import ministere.sante.senpna.projet.infrastructure.web.dto.request.CreateProjetRequest;
import ministere.sante.senpna.projet.infrastructure.web.dto.request.UpdateProjetRequest;
import ministere.sante.senpna.projet.infrastructure.web.dto.response.ProjetResponse;
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
 * Gestion des projets portés ou soutenus par la PNA.
 *
 * <pre>
 * POST   /api/projets                    {categorie, nom, description?, objectifs?, impacts?, imageUrl?}
 * PUT    /api/projets/{id}                {categorie, nom, description?, objectifs?, impacts?, imageUrl?}
 * PATCH  /api/projets/{id}/publier
 * PATCH  /api/projets/{id}/archiver
 * PATCH  /api/projets/{id}/desactiver
 * PATCH  /api/projets/{id}/brouillon
 * GET    /api/projets/{id}
 * GET    /api/projets?q=&categorie=&statut=&page=&size=&sortBy=&sortDirection=
 * </pre>
 */
@RestController
@RequestMapping("/api/projets")
public class ProjetsController {

        private final ProjetFacade projetFacade;

        public ProjetsController(ProjetFacade projetFacade) {
                this.projetFacade = projetFacade;
        }

        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> creer(@Valid @RequestBody CreateProjetRequest request) {
                ProjetDetail result = projetFacade.creerProjet(new CreateProjetCommand(
                                request.categorie(), request.nom(), request.description(), request.objectifs(),
                                request.impacts(),
                                request.imageUrl()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "PROJET_CREATED",
                                                "Projet créé avec succès"));
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> modifier(@PathVariable UUID id,
                        @Valid @RequestBody UpdateProjetRequest request) {
                ProjetDetail result = projetFacade.modifierProjet(new UpdateProjetCommand(
                                id, request.categorie(), request.nom(), request.description(), request.objectifs(),
                                request.impacts(), request.imageUrl()));

                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_UPDATED",
                                "Projet modifié avec succès"));
        }

        @PatchMapping("/{id}/publier")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> publier(@PathVariable UUID id) {
                ProjetDetail result = projetFacade.publierProjet(new PublierProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_PUBLIE",
                                "Projet publié avec succès"));
        }

        @PatchMapping("/{id}/archiver")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> archiver(@PathVariable UUID id) {
                ProjetDetail result = projetFacade.archiverProjet(new ArchiverProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_ARCHIVE",
                                "Projet archivé avec succès"));
        }

        @PatchMapping("/{id}/desactiver")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> desactiver(@PathVariable UUID id) {
                ProjetDetail result = projetFacade.desactiverProjet(new DesactiverProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_DESACTIVE",
                                "Projet désactivé avec succès"));
        }

        @PatchMapping("/{id}/brouillon")
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> remettreEnBrouillon(@PathVariable UUID id) {
                ProjetDetail result = projetFacade.remettreEnBrouillonProjet(new RemettreEnBrouillonProjetCommand(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_BROUILLON",
                                "Projet remis en brouillon"));
        }

        @GetMapping("/{id}")
        public ResponseEntity<Map<String, Object>> obtenir(@PathVariable UUID id) {
                ProjetDetail result = projetFacade.obtenirProjet(new GetProjetQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "PROJET_FOUND",
                                "Projet récupéré"));
        }

        @GetMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<Map<String, Object>> lister(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String categorie,
                        @RequestParam(required = false) String statut,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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

        @GetMapping("/public")
        public ResponseEntity<Map<String, Object>> listerPublic(
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String categorie,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {

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
