package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.utilisateurs.application.service.EntrepotAffectationResolver;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreateUserCommand;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.CreatedUser;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.domain.exception.CreationRoleReserveeException;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserUseCaseImpl — création atomique d'un compte utilisateur")
class CreateUserUseCaseImplTest {

    @Mock
    UserManagementRepositoryPort userManagementRepositoryPort;
    @Mock
    RoleQueryPort roleQueryPort;
    @Mock
    PasswordEncoderPort passwordEncoderPort;
    @Mock
    TemporaryPasswordGenerator temporaryPasswordGenerator;
    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    EntrepotAffectationResolver entrepotAffectationResolver;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;
    @Mock
    UserDetailAssembler userDetailAssembler;

    CreateUserUseCaseImpl sut;

    UUID roleId;
    UUID acteurId;

    @BeforeEach
    void setUp() {
        sut = new CreateUserUseCaseImpl(userManagementRepositoryPort, roleQueryPort, passwordEncoderPort,
                temporaryPasswordGenerator, userHierarchyGuard, entrepotAffectationResolver,
                userAffectationRepositoryPort, userDetailAssembler);
        roleId = UUID.randomUUID();
        acteurId = UUID.randomUUID();
    }

    private CreateUserCommand commande(Set<UUID> roleIds, UUID entrepotId) {
        return new CreateUserCommand(acteurId, "Diallo", "Mamadou", "nouveau@sante.gouv.sn", null, roleIds,
                entrepotId);
    }

    @Nested
    @DisplayName("validations préalables")
    class ValidationsPrealables {

        @Test
        @DisplayName("email déjà utilisé → EmailDejaUtiliseException")
        void emailDejaUtilise_leveException() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(true);

            var command = commande(Set.of(roleId), null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(EmailDejaUtiliseException.class);
        }

        @Test
        @DisplayName("aucun rôle fourni → ValidationException")
        void aucunRole_leveException() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);

            var command = commande(Set.of(), null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(ValidationException.class);
        }

        @Test
        @DisplayName("rôle demandé introuvable → RoleIntrouvableException")
        void roleIntrouvable_leveException() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(roleId)).thenReturn(Optional.empty());

            var command = commande(Set.of(roleId), null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(RoleIntrouvableException.class);
        }

        @Test
        @DisplayName("rôle GESTIONNAIRE_STRUCTURE demandé manuellement → CreationRoleReserveeException")
        void roleGestionnaireStructure_reserve() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(roleId)).thenReturn(
                    Optional.of(new RoleProjection(roleId, "GESTIONNAIRE_STRUCTURE", "Gestionnaire structure")));

            var command = commande(Set.of(roleId), null);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(CreationRoleReserveeException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("création réussie")
    class CreationReussie {

        @BeforeEach
        void stubsCommuns() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(roleId)).thenReturn(
                    Optional.of(new RoleProjection(roleId, "PHARMACIEN_PRA", "Pharmacien PRA")));
            when(temporaryPasswordGenerator.generer()).thenReturn("Mdp@Temp1234!");
            when(passwordEncoderPort.encoder(anyString())).thenReturn("hash-encode");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(mockUserDetail());
        }

        @Test
        @DisplayName("entrepôt résolu → affectation créée dans la même opération")
        void entrepotResolu_affectationCreee() {
            UUID entrepotResolu = UUID.randomUUID();
            when(entrepotAffectationResolver.resoudre(any(), any(), any())).thenReturn(entrepotResolu);

            CreatedUser result = sut.creer(commande(Set.of(roleId), entrepotResolu));

            verify(userAffectationRepositoryPort).affecterEntrepot(any(),
                    org.mockito.ArgumentMatchers.eq(entrepotResolu));
            assertThat(result.motDePasseTemporaire()).isEqualTo("Mdp@Temp1234!");
        }

        @Test
        @DisplayName("aucun entrepôt requis pour le(s) rôle(s) → pas d'affectation créée")
        void aucunEntrepotRequis_pasAffectation() {
            when(entrepotAffectationResolver.resoudre(any(), any(), any())).thenReturn(null);

            sut.creer(commande(Set.of(roleId), null));

            verify(userAffectationRepositoryPort, never()).affecterEntrepot(any(), any());
        }

        @Test
        @DisplayName("vérifie la hiérarchie de gestion avec l'ensemble des rôles demandés")
        void verifieHierarchieAvecRolesDemandes() {
            when(entrepotAffectationResolver.resoudre(any(), any(), any())).thenReturn(null);

            sut.creer(commande(Set.of(roleId), null));

            verify(userHierarchyGuard).verifierGestionAutorisee(acteurId, Set.of(roleId));
        }

        private UserDetail mockUserDetail() {
            return new UserDetail(UUID.randomUUID(), "Diallo", "Mamadou", "nouveau@sante.gouv.sn", null, true,
                    Set.of(), null, null, null, null);
        }
    }
}
