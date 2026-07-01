package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.auth.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.auth.domain.model.User;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link UserDetail} à partir de
 * l'agrégat {@link User} — mutualisé entre tous les use cases de gestion
 * des utilisateurs afin d'éviter la duplication de logique de mapping.
 */
@Component
public class UserDetailAssembler {

    private final UserRoleSummaryResolver roleSummaryResolver;

    public UserDetailAssembler(UserRoleSummaryResolver roleSummaryResolver) {
        this.roleSummaryResolver = roleSummaryResolver;
    }

    public UserDetail assembler(User user) {
        return new UserDetail(
                user.getId().getValue(),
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                user.getTelephone() != null ? user.getTelephone().value() : null,
                user.isActif(),
                roleSummaryResolver.resoudre(user.getRoleIds()),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
