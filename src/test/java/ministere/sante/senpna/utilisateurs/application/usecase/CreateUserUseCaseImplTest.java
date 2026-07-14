package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;
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
import ministere.sante.senpna.utilisateurs.domain.exception.FournisseurIdRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;
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
import static org.mockito.ArgumentMatchers.eq;
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
    FournisseurCachePort fournisseurCachePort;
    @Mock
    UserDetailAssembler userDetailAssembler;

    CreateUserUseCaseImpl sut;

    UUID roleId;
    UUID acteurId;

    @BeforeEach
    void setUp() {
        sut = new CreateUserUseCaseImpl(userManagementRepositoryPort, roleQueryPort, passwordEncoderPort,
                temporaryPasswordGenerator, userHierarchyGuard, entrepotAffectationResolver,
                userAffectationRepositoryPort, fournisseurCachePort, userDetailAssembler);
        roleId = UUID.randomUUID();
        acteurId = UUID.randomUUID();
    }

    private CreateUserCommand commande(Set<UUID> roleIds, UUID entrepotId) {
        return commande(roleIds, entrepotId, null);
    }

    private CreateUserCommand commande(Set<UUID> roleIds, UUID entrepotId, UUID fournisseurId) {
        return new CreateUserCommand(acteurId, "Diallo", "Mamadou", "nouveau@sante.gouv.sn", null, roleIds,
                entrepotId, fournisseurId);
    }

    private UserDetail mockUserDetail() {
        return new UserDetail(UUID.randomUUID(), "Diallo", "Mamadou", "nouveau@sante.gouv.sn", null, true, Set.of(),
                null, null, null, null, null);
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
                    .isInstanceOf(SenPnaException.class);
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
    }

    @Nested
    @DisplayName("rôle FOURNISSEUR — provisionnement d'un compte espace fournisseur")
    class RoleFournisseur {

        UUID fournisseurId;

        @BeforeEach
        void setUp() {
            fournisseurId = UUID.randomUUID();
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(roleId))
                    .thenReturn(Optional.of(new RoleProjection(roleId, "FOURNISSEUR", "Fournisseur")));
        }

        @Test
        @DisplayName("combiné à un autre rôle → ValidationException, aucun appel de garde hiérarchique")
        void combineAvecAutreRole_leveException() {
            UUID autreRoleId = UUID.randomUUID();
            when(roleQueryPort.findById(autreRoleId))
                    .thenReturn(Optional.of(new RoleProjection(autreRoleId, "GESTIONNAIRE_PNA", "Gestionnaire")));

            var command = commande(Set.of(roleId, autreRoleId), null, fournisseurId);
            assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(SenPnaException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("acteur non national → GestionUtilisateurInterditeException")
        void acteurNonNational_leveException() {
            when(userHierarchyGuard.estActeurNational(acteurId)).thenReturn(false);

            var command = commande(Set.of(roleId), null, fournisseurId);
            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(GestionUtilisateurInterditeException.class);

            verify(userManagementRepositoryPort, never()).save(any());
            // La hiérarchie régionale usuelle (verifierGestionAutorisee) ne s'applique pas au rôle
            // FOURNISSEUR — c'est estActeurNational() qui tranche, jamais les deux.
            verify(userHierarchyGuard, never()).verifierGestionAutorisee(any(), any());
        }

        @Test
        @DisplayName("entrepotId fourni avec FOURNISSEUR → ValidationException")
        void entrepotIdFourni_leveException() {
            when(userHierarchyGuard.estActeurNational(acteurId)).thenReturn(true);

            var command = commande(Set.of(roleId), UUID.randomUUID(), fournisseurId);
            assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(SenPnaException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("fournisseurId absent → FournisseurIdRequisException")
        void fournisseurIdAbsent_leveException() {
            when(userHierarchyGuard.estActeurNational(acteurId)).thenReturn(true);

            var command = commande(Set.of(roleId), null, null);
            assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(FournisseurIdRequisException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("fournisseur introuvable dans le cache → FournisseurIntrouvableException")
        void fournisseurIntrouvable_leveException() {
            when(userHierarchyGuard.estActeurNational(acteurId)).thenReturn(true);
            when(fournisseurCachePort.findById(fournisseurId)).thenReturn(Optional.empty());

            var command = commande(Set.of(roleId), null, fournisseurId);
            assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(FournisseurIntrouvableException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("cas nominal → affecterFournisseur() appelé, affecterEntrepot() jamais appelé")
        void casNominal_affecteFournisseur() {
            when(userHierarchyGuard.estActeurNational(acteurId)).thenReturn(true);
            when(fournisseurCachePort.findById(fournisseurId))
                    .thenReturn(Optional.of(new FournisseurProjection(fournisseurId, "Laboratoire A", true)));
            when(temporaryPasswordGenerator.generer()).thenReturn("Mdp@Temp1234!");
            when(passwordEncoderPort.encoder(anyString())).thenReturn("hash-encode");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(mockUserDetail());

            CreatedUser result = sut.creer(commande(Set.of(roleId), null, fournisseurId));

            verify(userAffectationRepositoryPort).affecterFournisseur(any(), eq(fournisseurId));
            verify(userAffectationRepositoryPort, never()).affecterEntrepot(any(), any());
            // Le rôle FOURNISSEUR n'exige pas d'entrepôt : le résolveur ne doit même pas être sollicité.
            verify(entrepotAffectationResolver, never()).resoudre(any(), any(), any());
            assertThat(result.motDePasseTemporaire()).isEqualTo("Mdp@Temp1234!");
        }
    }
}
