package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.shared.domain.port.out.AccountMailPort;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGestionnaireStructureAccountUseCaseImpl — création automatique de compte à l'adhésion")
class CreateGestionnaireStructureAccountUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    RoleQueryPort roleQueryPort;
    @Mock
    PasswordEncoderPort passwordEncoderPort;
    @Mock
    TemporaryPasswordGenerator temporaryPasswordGenerator;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;
    @Mock
    AccountMailPort accountMailPort;

    CreateGestionnaireStructureAccountUseCaseImpl sut;

    UUID structureId;

    @BeforeEach
    void setUp() {
        sut = new CreateGestionnaireStructureAccountUseCaseImpl(userManagementRepositoryPort, roleQueryPort,
                passwordEncoderPort, temporaryPasswordGenerator, userAffectationRepositoryPort, accountMailPort);
        structureId = UUID.randomUUID();
    }

    private AdhesionValideeEvent event() {
        return new AdhesionValideeEvent(structureId, "Poste de santé de Fann", "Ndiaye", "Fatou",
                "fatou.ndiaye@sante.gouv.sn", Instant.now());
    }

    @Test
    @DisplayName("compte déjà existant pour l'email → création ignorée silencieusement, idempotence")
    void compteDejaExistant_ignoreSilencieusement() {
        when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(true);

        sut.creer(event());

        verify(userManagementRepositoryPort, never()).save(any());
        verify(accountMailPort, never()).envoyerIdentifiantsCompte(any(), any(), any(), any());
    }

    @Test
    @DisplayName("rôle GESTIONNAIRE_STRUCTURE introuvable → RoleIntrouvableException")
    void roleIntrouvable_leveException() {
        when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
        when(roleQueryPort.findByCode("GESTIONNAIRE_STRUCTURE")).thenReturn(Optional.empty());

        var event = event();
        assertThatThrownBy(() -> sut.creer(event)).isInstanceOf(RoleIntrouvableException.class);
    }

    @Test
    @DisplayName("création réussie → compte créé, affecté à la structure, e-mail envoyé")
    void creationReussie_creeAffecteEtEnvoieMail() {
        UUID roleId = UUID.randomUUID();
        when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
        when(roleQueryPort.findByCode("GESTIONNAIRE_STRUCTURE"))
                .thenReturn(Optional.of(new RoleProjection(roleId, "GESTIONNAIRE_STRUCTURE", "Gestionnaire")));
        when(temporaryPasswordGenerator.generer()).thenReturn("Mdp@Temp1234!");
        when(passwordEncoderPort.encoder(anyString())).thenReturn("hash");
        when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.creer(event());

        verify(userAffectationRepositoryPort).affecterStructureSanitaire(any(), eq(structureId));
        verify(accountMailPort).envoyerIdentifiantsCompte("fatou.ndiaye@sante.gouv.sn", "Ndiaye", "Fatou",
                "Mdp@Temp1234!");
    }
}
