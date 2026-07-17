package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.AccountMailPort;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateGestionnaireStructureAccountUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Crée le compte {@code GESTIONNAIRE_STRUCTURE} et l'affecte
 * atomiquement à sa structure sanitaire — même principe que la création
 * atomique manuelle (cf. {@code CreateUserUseCaseImpl}), mais déclenchée
 * par {@link AdhesionValideeEvent} plutôt que par un acteur humain.
 *
 * <p>
 * Idempotent par prudence : si un compte existe déjà pour l'e-mail de la
 * structure (rejeu d'événement), la création est silencieusement
 * ignorée plutôt que de lever une erreur.
 * </p>
 */
@Service
public class CreateGestionnaireStructureAccountUseCaseImpl implements CreateGestionnaireStructureAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateGestionnaireStructureAccountUseCaseImpl.class);
    private static final String ROLE_GESTIONNAIRE_STRUCTURE = "GESTIONNAIRE_STRUCTURE";

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleQueryPort roleQueryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final AccountMailPort accountMailPort;

    public CreateGestionnaireStructureAccountUseCaseImpl(UserManagementRepositoryPort userManagementRepositoryPort,
            RoleQueryPort roleQueryPort, PasswordEncoderPort passwordEncoderPort,
            TemporaryPasswordGenerator temporaryPasswordGenerator,
            UserAffectationRepositoryPort userAffectationRepositoryPort, AccountMailPort accountMailPort) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleQueryPort = roleQueryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.accountMailPort = accountMailPort;
    }

    @Override
    @Transactional
    public void creer(AdhesionValideeEvent event) {
        Email email = Email.of(event.email());

        if (userManagementRepositoryPort.existsByEmail(email)) {
            log.warn("[GestionnaireStructure] Compte déjà existant pour {} — création ignorée (structure={})",
                    email.value(), event.structureSanitaireId());
            return;
        }

        RoleProjection role = roleQueryPort.findByCode(ROLE_GESTIONNAIRE_STRUCTURE)
                .orElseThrow(RoleIntrouvableException::new);

        String motDePasseTemporaire = temporaryPasswordGenerator.generer();
        HashedPassword hashedPassword = HashedPassword.of(passwordEncoderPort.encoder(motDePasseTemporaire));

        User user = User.creer(new User.CreationCommand(
                Nom.of(event.responsableNom()),
                Prenom.of(event.responsablePrenom()),
                email,
                null,
                hashedPassword,
                Set.of(role.id())));

        User saved = userManagementRepositoryPort.save(user);

        userAffectationRepositoryPort.affecterStructureSanitaire(saved.getId().getValue(),
                event.structureSanitaireId());

        accountMailPort.envoyerIdentifiantsCompte(email.value(), event.responsableNom(), event.responsablePrenom(),
                motDePasseTemporaire);

        log.info("[GestionnaireStructure] Compte créé et affecté pour la structure {}",
                event.structureSanitaireId());
    }
}
