package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
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
import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.utilisateurs.application.service.EntrepotAffectationResolver;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.exception.CreationRoleReserveeException;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.FournisseurIdRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;
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
 *
 * <p>
 * Le rôle {@code FOURNISSEUR} (espace fournisseur) exige à l'inverse un
 * {@code fournisseurId} explicite — un compte fournisseur n'existe jamais
 * sans son rattachement, symétriquement à l'entrepôt pour les rôles
 * PNA/PRA.
 * </p>
 */
@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final String ROLE_GESTIONNAIRE_STRUCTURE = "GESTIONNAIRE_STRUCTURE";
    private static final String ROLE_FOURNISSEUR = "FOURNISSEUR";

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final UserHierarchyGuard userHierarchyGuard;
    private final EntrepotAffectationResolver entrepotAffectationResolver;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final FournisseurCachePort fournisseurCachePort;
    private final UserDetailAssembler userDetailAssembler;

    public CreateUserUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort, RoleQueryPort roleQueryPort,
            PasswordEncoderPort passwordEncoderPort, TemporaryPasswordGenerator temporaryPasswordGenerator,
            UserHierarchyGuard userHierarchyGuard, EntrepotAffectationResolver entrepotAffectationResolver,
            UserAffectationRepositoryPort userAffectationRepositoryPort, FournisseurCachePort fournisseurCachePort,
            UserDetailAssembler userDetailAssembler) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.userHierarchyGuard = userHierarchyGuard;
        this.entrepotAffectationResolver = entrepotAffectationResolver;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.fournisseurCachePort = fournisseurCachePort;
        this.userDetailAssembler = userDetailAssembler;
    }

    @Override
    @Transactional
    public CreatedUser creer(CreateUserCommand command) {
        Email email = Email.of(command.email());
        verifierEmailDisponible(email);

        Set<String> codesRoles = resoudreCodesRoles(command.roleIds());
        boolean estCompteFournisseur = codesRoles.contains(ROLE_FOURNISSEUR);

        verifierRoleNonReserve(codesRoles);
        verifierExclusiviteFournisseur(codesRoles, estCompteFournisseur);
        verifierHabilitation(command, estCompteFournisseur);

        Affectation affectation = resoudreAffectation(command, estCompteFournisseur, codesRoles);

        UserCree userCree = creerEtSauvegarderUser(command, email);
        affecter(userCree.saved().getId().getValue(), affectation);

        return new CreatedUser(userDetailAssembler.assembler(userCree.saved()), userCree.motDePasseTemporaire());
    }

    private void verifierEmailDisponible(Email email) {
        if (userManagementRepositoryPort.existsByEmail(email)) {
            throw new EmailDejaUtiliseException(email.value());
        }
    }

    private Set<String> resoudreCodesRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new SenPnaException("Un utilisateur doit posséder au moins un rôle", "ROLE_REQUIRED",
                    ErrorCategory.VALIDATION);
        }

        Set<String> codesRoles = new HashSet<>();
        for (UUID roleId : roleIds) {
            RoleProjection role = roleQueryPort.findById(roleId).orElseThrow(RoleIntrouvableException::new);
            codesRoles.add(role.code());
        }
        return codesRoles;
    }

    // GESTIONNAIRE_STRUCTURE ne se crée jamais manuellement — seul le
    // système le crée, à la validation d'une demande d'adhésion.
    private void verifierRoleNonReserve(Set<String> codesRoles) {
        if (codesRoles.contains(ROLE_GESTIONNAIRE_STRUCTURE)) {
            throw new CreationRoleReserveeException(ROLE_GESTIONNAIRE_STRUCTURE);
        }
    }

    // Un compte espace fournisseur ne cumule aucun autre rôle interne
    // PNA/PRA — évite qu'un même compte porte à la fois les habilitations
    // fournisseur et les habilitations internes.
    private void verifierExclusiviteFournisseur(Set<String> codesRoles, boolean estCompteFournisseur) {
        if (estCompteFournisseur && codesRoles.size() > 1) {
            throw new SenPnaException("Le rôle FOURNISSEUR ne peut pas être combiné à un autre rôle",
                    "FOURNISSEUR_ROLE_EXCLUSIVE", ErrorCategory.VALIDATION);
        }
    }

    private void verifierHabilitation(CreateUserCommand command, boolean estCompteFournisseur) {
        if (estCompteFournisseur) {
            // Les fournisseurs sont gérés au niveau national (cf. doc.
            // métier §5 : « les achats fournisseurs sont effectués
            // uniquement par la PNA ») — un ADMIN_PRA régional ne peut pas
            // provisionner de compte espace fournisseur.
            if (!userHierarchyGuard.estActeurNational(command.acteurId())) {
                throw new GestionUtilisateurInterditeException();
            }
        } else {
            // Un administrateur régional (PRA) ne peut pas créer un compte
            // national (PNA), un autre compte ADMIN_PRA, ni un compte
            // GESTIONNAIRE_STRUCTURE.
            userHierarchyGuard.verifierGestionAutorisee(command.acteurId(), command.roleIds());
        }
    }

    private Affectation resoudreAffectation(CreateUserCommand command, boolean estCompteFournisseur,
            Set<String> codesRoles) {
        if (estCompteFournisseur) {
            return resoudreAffectationFournisseur(command);
        }
        // Résout l'entrepôt à affecter (null si aucun n'est requis pour
        // ces rôles, ex: ADMIN_PNA seul) — lève une exception si l'entrepôt
        // fourni est incohérent avec les rôles demandés.
        UUID entrepotId = entrepotAffectationResolver.resoudre(command.acteurId(), codesRoles, command.entrepotId());
        return new Affectation(entrepotId, null);
    }

    private Affectation resoudreAffectationFournisseur(CreateUserCommand command) {
        if (command.entrepotId() != null) {
            throw new SenPnaException("Un compte FOURNISSEUR ne peut pas être affecté à un entrepôt",
                    "ENTREPOT_NOT_APPLICABLE", ErrorCategory.VALIDATION);
        }
        UUID fournisseurId = command.fournisseurId();
        if (fournisseurId == null) {
            throw new FournisseurIdRequisException();
        }
        fournisseurCachePort.findById(fournisseurId).orElseThrow(FournisseurIntrouvableException::new);
        return new Affectation(null, fournisseurId);
    }

    private UserCree creerEtSauvegarderUser(CreateUserCommand command, Email email) {
        Phone telephone = (command.telephone() != null && !command.telephone().isBlank())
                ? Phone.of(command.telephone())
                : null;

        String motDePasseTemporaire = temporaryPasswordGenerator.generer();
        HashedPassword hashedPassword = HashedPassword.of(passwordEncoderPort.encoder(motDePasseTemporaire));

        User user = User.creer(new User.CreationCommand(
                Nom.of(command.nom()),
                Prenom.of(command.prenom()),
                email,
                telephone,
                hashedPassword,
                command.roleIds()));

        User saved = userManagementRepositoryPort.save(user);
        return new UserCree(saved, motDePasseTemporaire);
    }

    // Affectation atomique : même transaction que la création — un compte
    // dont le rôle exige un entrepôt ou un fournisseur n'existe jamais
    // sans lui.
    private void affecter(UUID userId, Affectation affectation) {
        if (affectation.entrepotId() != null) {
            userAffectationRepositoryPort.affecterEntrepot(userId, affectation.entrepotId());
        } else if (affectation.fournisseurId() != null) {
            userAffectationRepositoryPort.affecterFournisseur(userId, affectation.fournisseurId());
        }
    }

    private record Affectation(UUID entrepotId, UUID fournisseurId) {
    }

    private record UserCree(User saved, String motDePasseTemporaire) {
    }
}
