package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Assemble la représentation de sortie {@link UserDetail} à partir de
 * l'agrégat {@link User} — mutualisé entre tous les use cases de gestion
 * des utilisateurs afin d'éviter la duplication de logique de mapping.
 * Résout au passage l'affectation organisationnelle courante (entrepôt
 * ou structure sanitaire).
 */
@Component
public class UserDetailAssembler {

    private final UserRoleSummaryResolver roleSummaryResolver;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;

    public UserDetailAssembler(UserRoleSummaryResolver roleSummaryResolver,
            UserAffectationRepositoryPort userAffectationRepositoryPort) {
        this.roleSummaryResolver = roleSummaryResolver;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
    }

    public UserDetail assembler(User user) {
        UUID userId = user.getId().getValue();
        Optional<UserAffectationView> affectation = userAffectationRepositoryPort.findAffectation(userId);

        return new UserDetail(
                userId,
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                user.getTelephone() != null ? user.getTelephone().value() : null,
                user.isActif(),
                roleSummaryResolver.resoudre(user.getRoleIds()),
                affectation.map(UserAffectationView::entrepotId).orElse(null),
                affectation.map(UserAffectationView::structureSanitaireId).orElse(null),
                affectation.map(UserAffectationView::fournisseurId).orElse(null),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
