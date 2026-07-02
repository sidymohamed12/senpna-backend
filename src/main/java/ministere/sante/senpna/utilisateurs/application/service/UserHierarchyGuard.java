package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.RolesNationaux;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fait respecter la hiérarchie de gestion des comptes utilisateurs :
 *
 * <ul>
 * <li>Un acteur possédant un rôle national (cf. {@link RolesNationaux})
 * peut gérer n'importe quel compte, sans restriction.</li>
 * <li>Un acteur n'ayant que des rôles régionaux (ex: {@code ADMIN_PRA})
 * ne peut gérer <strong>ni</strong> un compte possédant un rôle national,
 * <strong>ni</strong> un compte {@code ADMIN_PRA} — y compris d'une autre
 * région. Il ne peut gérer que les comptes de rôles subalternes
 * ({@code GESTIONNAIRE_PRA}, {@code PHARMACIEN_PRA},
 * {@code MAGASINIER_PRA}, {@code GESTIONNAIRE_STRUCTURE}...).</li>
 * </ul>
 *
 * <p>
 * Contrôlé pour toutes les opérations de gestion de compte : création
 * (rôles demandés), modification, activation, désactivation, attribution
 * et retrait de rôle (rôles résultants).
 * </p>
 */
@Component
public class UserHierarchyGuard {

    private static final String ROLE_ADMIN_PRA = "ADMIN_PRA";

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleCachePort roleCachePort;

    public UserHierarchyGuard(UserManagementRepositoryPort userManagementRepositoryPort,
            RoleCachePort roleCachePort) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleCachePort = roleCachePort;
    }

    /**
     * Vérifie que {@code acteurId} est autorisé à gérer un compte dont
     * l'ensemble de rôles (existant, ou résultant de l'opération en cours :
     * création, attribution de rôle...) est {@code roleIdsCible}.
     *
     * @throws GestionUtilisateurInterditeException si l'acteur n'a que des
     *                                                rôles régionaux et que
     *                                                la cible possède un
     *                                                rôle national ou
     *                                                {@code ADMIN_PRA}
     */
    public void verifierGestionAutorisee(UUID acteurId, Set<UUID> roleIdsCible) {
        User acteur = userManagementRepositoryPort.findById(UserId.of(acteurId))
                .orElseThrow(UserNotFoundException::new);

        if (RolesNationaux.contientRoleNational(codes(acteur.getRoleIds()))) {
            return; // rôle national : aucune restriction hiérarchique
        }

        Set<String> codesCible = codes(roleIdsCible);
        if (RolesNationaux.contientRoleNational(codesCible) || codesCible.contains(ROLE_ADMIN_PRA)) {
            throw new GestionUtilisateurInterditeException();
        }
    }

    private Set<String> codes(Set<UUID> roleIds) {
        return roleCachePort.findAllById(roleIds).stream()
                .map(RoleProjection::code)
                .collect(Collectors.toUnmodifiableSet());
    }
}
