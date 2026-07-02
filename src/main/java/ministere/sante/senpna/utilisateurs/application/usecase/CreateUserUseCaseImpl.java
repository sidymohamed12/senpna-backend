package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.utilisateurs.application.service.EntrepotAffectationResolver;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.exception.CreationRoleReserveeException;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateUserUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Crée un compte utilisateur et l'affecte atomiquement à son unité
 * organisationnelle (cf. {@link EntrepotAffectationResolver}) — la
 * création et l'affectation se produisent dans la même transaction,
 * jamais l'une sans l'autre pour un rôle qui l'exige.
 *
 * <p>
 * Le rôle {@code GESTIONNAIRE_STRUCTURE} ne peut jamais être demandé ici
 * — son compte est créé automatiquement à la validation d'une demande
 * d'adhésion (cf. {@code AdhesionValideeEvent}), jamais manuellement.
 * </p>
 */
@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final String ROLE_GESTIONNAIRE_STRUCTURE = "GESTIONNAIRE_STRUCTURE";

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final UserHierarchyGuard userHierarchyGuard;
    private final EntrepotAffectationResolver entrepotAffectationResolver;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final UserDetailAssembler userDetailAssembler;

    public CreateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort, RoleQueryPort roleQueryPort,
            PasswordEncoderPort passwordEncoderPort, TemporaryPasswordGenerator temporaryPasswordGenerator,
            UserHierarchyGuard userHierarchyGuard, EntrepotAffectationResolver entrepotAffectationResolver,
            UserAffectationRepositoryPort userAffectationRepositoryPort, UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.userHierarchyGuard = userHierarchyGuard;
        this.entrepotAffectationResolver = entrepotAffectationResolver;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public CreatedUser creer(CreateUserCommand command) {
        Email email = Email.of(command.email());

        if (userManagementRepositoryPort.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email.value());
        }

        Set<UUID> roleIds = command.roleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ValidationException("Un utilisateur doit posséder au moins un rôle", "ROLE_REQUIRED");
        }

        Set<String> codesRoles = new HashSet<>();
        for (UUID roleId : roleIds) {
            RoleProjection role = roleQueryPort.findById(roleId).orElseThrow(RoleIntrouvableException::new);
            codesRoles.add(role.code());
        }

        // GESTIONNAIRE_STRUCTURE ne se crée jamais manuellement — seul le
        // système le crée, à la validation d'une demande d'adhésion.
        if (codesRoles.contains(ROLE_GESTIONNAIRE_STRUCTURE)) {
            throw new CreationRoleReserveeException(ROLE_GESTIONNAIRE_STRUCTURE);
        }

        // Un administrateur régional (PRA) ne peut pas créer un compte
        // national (PNA), un autre compte ADMIN_PRA, ni un compte
        // GESTIONNAIRE_STRUCTURE.
        userHierarchyGuard.verifierGestionAutorisee(command.acteurId(), roleIds);

        // Résout l'entrepôt à affecter (null si aucun n'est requis pour ces
        // rôles, ex: ADMIN_PNA seul) — lève une exception si l'entrepôt
        // fourni est incohérent avec les rôles demandés.
        UUID entrepotId = entrepotAffectationResolver.resoudre(command.acteurId(), codesRoles, command.entrepotId());

        Phone telephone = (command.telephone() != null && !command.telephone().isBlank())
                ? Phone.of(command.telephone())
                : null;

        String motDePasseTemporaire = temporaryPasswordGenerator.generer();
        HashedPassword hashedPassword = HashedPassword.of(passwordEncoderPort.encoder(motDePasseTemporaire));

        User user = User.creer(
                Nom.of(command.nom()),
                Prenom.of(command.prenom()),
                email,
                telephone,
                hashedPassword,
                roleIds);

        User saved = userManagementRepositoryPort.save(user);

        // Affectation atomique : même transaction que la création — un
        // compte dont le rôle exige un entrepôt n'existe jamais sans lui.
        if (entrepotId != null) {
            userAffectationRepositoryPort.affecterEntrepot(saved.getId().getValue(), entrepotId);
        }

        return new CreatedUser(userDetailAssembler.assembler(saved), motDePasseTemporaire);
    }
}
