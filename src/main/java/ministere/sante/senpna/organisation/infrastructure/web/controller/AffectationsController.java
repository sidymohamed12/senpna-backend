package ministere.sante.senpna.organisation.infrastructure.web.controller;

import jakarta.validation.Valid;
import ministere.sante.senpna.organisation.application.facade.OrganisationFacade;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToEntrepotCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToStructureCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UnassignUserCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToEntrepotRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.request.AssignUserToStructureRequest;
import ministere.sante.senpna.organisation.infrastructure.web.dto.response.UserAffectationResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Affectation d'un utilisateur à une unité organisationnelle — cf. doc.
 * métier §2 : "Affectation d'un utilisateur à : une PNA ; une PRA ; une
 * structure sanitaire". Un utilisateur n'est rattaché qu'à une seule unité
 * à la fois.
 *
 * <p>
 * Réservé aux administrateurs ({@code ADMIN_PNA}, {@code ADMIN_PRA}) —
 * même périmètre que la gestion des comptes (cf. {@code UsersController}).
 * </p>
 *
 * <pre>
 * POST   /api/affectations/utilisateurs/{userId}/entrepot           {entrepotId}
 * POST   /api/affectations/utilisateurs/{userId}/structure-sanitaire {structureSanitaireId}
 * DELETE /api/affectations/utilisateurs/{userId}
 * </pre>
 */
@RestController
@RequestMapping("/api/affectations/utilisateurs")
@PreAuthorize("hasAnyRole('ADMIN_PNA','ADMIN_PRA')")
public class AffectationsController {

    private final OrganisationFacade organisationFacade;

    public AffectationsController(OrganisationFacade organisationFacade) {
        this.organisationFacade = organisationFacade;
    }

    @PostMapping("/{userId}/entrepot")
    public ResponseEntity<Map<String, Object>> affecterAEntrepot(@PathVariable UUID userId,
            @Valid @RequestBody AssignUserToEntrepotRequest request) {
        UserAffectationDetail result = organisationFacade
                .affecterUtilisateurAEntrepot(new AssignUserToEntrepotCommand(userId, request.entrepotId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ASSIGNED_ENTREPOT",
                "Utilisateur affecté à l'entrepôt avec succès"));
    }

    @PostMapping("/{userId}/structure-sanitaire")
    public ResponseEntity<Map<String, Object>> affecterAStructure(@PathVariable UUID userId,
            @Valid @RequestBody AssignUserToStructureRequest request) {
        UserAffectationDetail result = organisationFacade.affecterUtilisateurAStructure(
                new AssignUserToStructureCommand(userId, request.structureSanitaireId()));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_ASSIGNED_STRUCTURE",
                "Utilisateur affecté à la structure sanitaire avec succès"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> retirerAffectation(@PathVariable UUID userId) {
        UserAffectationDetail result = organisationFacade
                .retirerAffectationUtilisateur(new UnassignUserCommand(userId));
        return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result), "USER_UNASSIGNED",
                "Affectation de l'utilisateur retirée avec succès"));
    }

    private UserAffectationResponse toResponse(UserAffectationDetail detail) {
        return new UserAffectationResponse(detail.userId(), detail.entrepotId(), detail.structureSanitaireId());
    }
}
