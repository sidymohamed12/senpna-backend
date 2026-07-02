package ministere.sante.senpna.utilisateurs.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.criteria.UserSearchCriteria;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.PasswordEncoderPort;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.utilisateurs.application.service.EntrepotAffectationResolver;
import ministere.sante.senpna.utilisateurs.application.service.TemporaryPasswordGenerator;
import ministere.sante.senpna.utilisateurs.application.service.UserDetailAssembler;
import ministere.sante.senpna.utilisateurs.application.service.UserHierarchyGuard;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.*;
import ministere.sante.senpna.utilisateurs.domain.exception.*;
import ministere.sante.senpna.utilisateurs.fixtures.RoleFixtures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires des use cases du module {@code utilisateurs}
 * (création, consultation, mise à jour, activation/désactivation et
 * gestion des rôles d'un utilisateur).
 *
 * <p>
 * Suit le même format que {@code AuthUseCasesTest} : chaque use case est
 * regroupé dans une classe imbriquée dédiée, avec ses propres mocks.
 * </p>
 */
@DisplayName("User Management Use Cases — create / get / list / update / activate / deactivate / assign-role / revoke-role")
class UserManagementUseCasesTest {

    private static final UUID USER_ID = UserFixtures.USER_ID;
    private static final UUID AUTRE_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ACTEUR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ROLE_ID = RoleFixtures.ROLE_GESTIONNAIRE_PNA_ID;
    private static final UUID AUTRE_ROLE_ID = RoleFixtures.ROLE_PHARMACIEN_PRA_ID;

    private static final UserDetail DETAIL_FICTIF = new UserDetail(
            USER_ID, UserFixtures.NOM, UserFixtures.PRENOM, UserFixtures.EMAIL, UserFixtures.TELEPHONE,
            true, Set.of(), null, null, Instant.now(), Instant.now());

    // ══════════════════════════════════════════════════════════════════════
    // CreateUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("CreateUserUseCaseImpl")
    class CreateUserTest {

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
        @InjectMocks
        CreateUserUseCaseImpl sut;

        private CreateUserCommand commandeValide() {
            return new CreateUserCommand(ACTEUR_ID, UserFixtures.NOM, UserFixtures.PRENOM, UserFixtures.EMAIL,
                    UserFixtures.TELEPHONE, Set.of(ROLE_ID), null);
        }

        @Test
        @DisplayName("crée l'utilisateur, hash un mot de passe temporaire et retourne le détail assemblé")
        void creer_succes_retourne_created_user() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(ROLE_ID)).thenReturn(Optional.of(RoleFixtures.gestionnairePna()));
            when(entrepotAffectationResolver.resoudre(eq(ACTEUR_ID), any(), any())).thenReturn(null);
            when(temporaryPasswordGenerator.generer()).thenReturn("TempPass1!23");
            when(passwordEncoderPort.encoder("TempPass1!23")).thenReturn("hash-du-mdp-temp");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            CreatedUser result = sut.creer(commandeValide());

            assertThat(result.motDePasseTemporaire()).isEqualTo("TempPass1!23");
            assertThat(result.user()).isEqualTo(DETAIL_FICTIF);
            verifyNoInteractions(userAffectationRepositoryPort); // aucun entrepôt requis ici
        }

        @Test
        @DisplayName("sauvegarde un utilisateur actif avec le mot de passe haché et les rôles fournis")
        void creer_succes_sauvegarde_champs_corrects() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(ROLE_ID)).thenReturn(Optional.of(RoleFixtures.gestionnairePna()));
            when(entrepotAffectationResolver.resoudre(eq(ACTEUR_ID), any(), any())).thenReturn(null);
            when(temporaryPasswordGenerator.generer()).thenReturn("TempPass1!23");
            when(passwordEncoderPort.encoder("TempPass1!23")).thenReturn("hash-du-mdp-temp");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            sut.creer(commandeValide());

            verify(userManagementRepositoryPort).save(captor.capture());
            User cree = captor.getValue();
            assertThat(cree.getNom().getValue()).isEqualTo(UserFixtures.NOM);
            assertThat(cree.getPrenom().getValue()).isEqualTo(UserFixtures.PRENOM);
            assertThat(cree.getEmail().value()).isEqualTo(UserFixtures.EMAIL);
            assertThat(cree.getTelephone().value()).isEqualTo(UserFixtures.TELEPHONE);
            assertThat(cree.getHashedPassword().value()).isEqualTo("hash-du-mdp-temp");
            assertThat(cree.isActif()).isTrue();
            assertThat(cree.getRoleIds()).containsExactly(ROLE_ID);
        }

        @Test
        @DisplayName("rôle exigeant un entrepôt → affectation atomique après sauvegarde")
        void creer_succes_affecteEntrepotAtomiquement() {
            UUID entrepotId = UUID.randomUUID();
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(ROLE_ID)).thenReturn(Optional.of(RoleFixtures.gestionnairePna()));
            when(entrepotAffectationResolver.resoudre(eq(ACTEUR_ID), any(), any())).thenReturn(entrepotId);
            when(temporaryPasswordGenerator.generer()).thenReturn("TempPass1!23");
            when(passwordEncoderPort.encoder(any())).thenReturn("hash");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            sut.creer(commandeValide());

            verify(userAffectationRepositoryPort).affecterEntrepot(any(), eq(entrepotId));
        }

        @Test
        @DisplayName("sans téléphone (vide) → l'utilisateur est créé sans téléphone")
        void creer_sansTelephone_telephoneNull() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(ROLE_ID)).thenReturn(Optional.of(RoleFixtures.gestionnairePna()));
            when(entrepotAffectationResolver.resoudre(eq(ACTEUR_ID), any(), any())).thenReturn(null);
            when(temporaryPasswordGenerator.generer()).thenReturn("TempPass1!23");
            when(passwordEncoderPort.encoder(any())).thenReturn("hash");
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            CreateUserCommand command = new CreateUserCommand(ACTEUR_ID, UserFixtures.NOM, UserFixtures.PRENOM,
                    UserFixtures.EMAIL, "  ", Set.of(ROLE_ID), null);

            sut.creer(command);

            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getTelephone()).isNull();
        }

        @Test
        @DisplayName("email déjà utilisé → EmailDejaUtiliseException, aucune autre interaction")
        void creer_emailDejaUtilise_leve_exception() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(true);

            assertThatThrownBy(() -> sut.creer(commandeValide()))
                    .isInstanceOf(EmailDejaUtiliseException.class);

            verifyNoInteractions(roleQueryPort, passwordEncoderPort, temporaryPasswordGenerator, userDetailAssembler,
                    entrepotAffectationResolver, userAffectationRepositoryPort);
            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("aucun rôle fourni (null) → ValidationException avec le code ROLE_REQUIRED")
        void creer_roleIdsNull_leve_validation_exception() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            CreateUserCommand command = new CreateUserCommand(ACTEUR_ID, UserFixtures.NOM, UserFixtures.PRENOM,
                    UserFixtures.EMAIL, null, null, null);

            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(ValidationException.class)
                    .hasFieldOrPropertyWithValue("type", "ROLE_REQUIRED");

            verifyNoInteractions(roleQueryPort, passwordEncoderPort);
            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("ensemble de rôles vide → ValidationException")
        void creer_roleIdsVide_leve_validation_exception() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            CreateUserCommand command = new CreateUserCommand(ACTEUR_ID, UserFixtures.NOM, UserFixtures.PRENOM,
                    UserFixtures.EMAIL, null, Set.of(), null);

            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(ValidationException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("rôle introuvable → RoleIntrouvableException, aucune sauvegarde")
        void creer_roleIntrouvable_leve_exception() {
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(ROLE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.creer(commandeValide()))
                    .isInstanceOf(RoleIntrouvableException.class);

            verifyNoInteractions(passwordEncoderPort, temporaryPasswordGenerator, userDetailAssembler);
            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("rôle GESTIONNAIRE_STRUCTURE demandé → CreationRoleReserveeException, création manuelle interdite")
        void creer_gestionnaireStructure_leve_exception() {
            UUID roleGestionnaireStructureId = UUID.randomUUID();
            when(userManagementRepositoryPort.existsByEmail(any())).thenReturn(false);
            when(roleQueryPort.findById(roleGestionnaireStructureId)).thenReturn(Optional.of(
                    new ministere.sante.senpna.shared.domain.projection.RoleProjection(
                            roleGestionnaireStructureId, "GESTIONNAIRE_STRUCTURE", "Gestionnaire Structure")));
            CreateUserCommand command = new CreateUserCommand(ACTEUR_ID, UserFixtures.NOM, UserFixtures.PRENOM,
                    UserFixtures.EMAIL, null, Set.of(roleGestionnaireStructureId), null);

            assertThatThrownBy(() -> sut.creer(command))
                    .isInstanceOf(CreationRoleReserveeException.class);

            verifyNoInteractions(userHierarchyGuard, entrepotAffectationResolver, passwordEncoderPort,
                    userDetailAssembler);
            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GetUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("GetUserUseCaseImpl")
    class GetUserTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        GetUserUseCaseImpl sut;

        @Test
        @DisplayName("utilisateur trouvé → retourne le détail assemblé")
        void obtenir_succes_retourne_detail() {
            User user = UserFixtures.actif();
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(user));
            when(userDetailAssembler.assembler(user)).thenReturn(DETAIL_FICTIF);

            UserDetail result = sut.obtenir(new GetUserQuery(USER_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void obtenir_introuvable_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.obtenir(new GetUserQuery(USER_ID)))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(userDetailAssembler);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ListUsersUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ListUsersUseCaseImpl")
    class ListUsersTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        ListUsersUseCaseImpl sut;

        @Test
        @DisplayName("construit les critères et la pagination à partir de la query, puis mappe le résultat")
        void lister_succes_mappe_page() {
            User user1 = UserFixtures.actif();
            User user2 = UserFixtures.multiRoles();
            PageResult<User> pageResult = PageResult.of(List.of(user1, user2), 0, 20, 2);

            when(userManagementRepositoryPort.search(any(), any())).thenReturn(pageResult);
            when(userDetailAssembler.assembler(user1)).thenReturn(DETAIL_FICTIF);
            when(userDetailAssembler.assembler(user2)).thenReturn(DETAIL_FICTIF);

            ListUsersQuery query = new ListUsersQuery("dupont", true, ROLE_ID, 0, 20, "nom", "ASC");

            UserPage result = sut.lister(query);

            assertThat(result.content()).containsExactly(DETAIL_FICTIF, DETAIL_FICTIF);
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);

            ArgumentCaptor<UserSearchCriteria> criteriaCaptor = ArgumentCaptor.forClass(UserSearchCriteria.class);
            ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
            verify(userManagementRepositoryPort).search(criteriaCaptor.capture(), pageRequestCaptor.capture());

            assertThat(criteriaCaptor.getValue().recherche()).isEqualTo("dupont");
            assertThat(criteriaCaptor.getValue().actif()).isTrue();
            assertThat(criteriaCaptor.getValue().roleId()).isEqualTo(ROLE_ID);
            assertThat(pageRequestCaptor.getValue().page()).isEqualTo(0);
            assertThat(pageRequestCaptor.getValue().size()).isEqualTo(20);
            assertThat(pageRequestCaptor.getValue().sortBy()).isEqualTo("nom");
        }

        @Test
        @DisplayName("aucune correspondance → page vide")
        void lister_aucunResultat_pageVide() {
            when(userManagementRepositoryPort.search(any(), any()))
                    .thenReturn(PageResult.of(List.of(), 0, 20, 0));

            UserPage result = sut.lister(new ListUsersQuery(null, null, null, null, null, null, null));

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            verifyNoInteractions(userDetailAssembler);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UpdateUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UpdateUserUseCaseImpl")
    class UpdateUserTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserHierarchyGuard userHierarchyGuard;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        UpdateUserUseCaseImpl sut;

        @Test
        @DisplayName("met à jour le nom, le prénom et le téléphone puis sauvegarde")
        void modifier_succes_met_a_jour_champs() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            UpdateUserCommand command = new UpdateUserCommand(USER_ID, ACTEUR_ID, "Ndiaye", "Fatou",
                    "+221709876543");

            UserDetail result = sut.modifier(command);

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getNom().getValue()).isEqualTo("Ndiaye");
            assertThat(captor.getValue().getPrenom().getValue()).isEqualTo("Fatou");
            assertThat(captor.getValue().getTelephone().value()).isEqualTo("+221709876543");
        }

        @Test
        @DisplayName("téléphone vide → le téléphone de l'utilisateur est effacé")
        void modifier_telephoneVide_efface_telephone() {
            when(userManagementRepositoryPort.findById(any()))
                    .thenReturn(Optional.of(UserFixtures.actifAvecTelephone()));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            UpdateUserCommand command = new UpdateUserCommand(USER_ID, ACTEUR_ID, UserFixtures.NOM,
                    UserFixtures.PRENOM, " ");

            sut.modifier(command);

            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getTelephone()).isNull();
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void modifier_introuvable_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            UpdateUserCommand command = new UpdateUserCommand(USER_ID, ACTEUR_ID, "Ndiaye", "Fatou", null);

            assertThatThrownBy(() -> sut.modifier(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ActivateUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ActivateUserUseCaseImpl")
    class ActivateUserTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserHierarchyGuard userHierarchyGuard;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        ActivateUserUseCaseImpl sut;

        @Test
        @DisplayName("active un utilisateur inactif et réinitialise ses échecs de connexion")
        void activer_succes_active_et_reinitialise_echecs() {
            // Utilisateur inactif ayant accumulé des échecs de connexion avant sa
            // désactivation : l'activation doit repartir sur des bases saines.
            User utilisateurInactifAvecEchecs = UserFixtures.inactif();
            utilisateurInactifAvecEchecs.enregistrerEchecConnexion(5, Instant.now().plusSeconds(900));
            utilisateurInactifAvecEchecs.enregistrerEchecConnexion(5, Instant.now().plusSeconds(900));

            when(userManagementRepositoryPort.findById(any()))
                    .thenReturn(Optional.of(utilisateurInactifAvecEchecs));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserDetail result = sut.activer(new ActivateUserCommand(USER_ID, ACTEUR_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().isActif()).isTrue();
            assertThat(captor.getValue().getTentativesEchecConnexion()).isZero();
            assertThat(captor.getValue().getVerrouilleJusqua()).isNull();
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void activer_introuvable_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.activer(new ActivateUserCommand(USER_ID, ACTEUR_ID)))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // DeactivateUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("DeactivateUserUseCaseImpl")
    class DeactivateUserTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserHierarchyGuard userHierarchyGuard;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        DeactivateUserUseCaseImpl sut;

        @Test
        @DisplayName("désactive un utilisateur actif et sauvegarde")
        void desactiver_succes_desactive() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserDetail result = sut.desactiver(new DeactivateUserCommand(USER_ID, AUTRE_USER_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().isActif()).isFalse();
        }

        @Test
        @DisplayName("auto-désactivation → AutoDesactivationInterditeException, sans accès au repository")
        void desactiver_soiMeme_leve_exception() {
            assertThatThrownBy(() -> sut.desactiver(new DeactivateUserCommand(USER_ID, USER_ID)))
                    .isInstanceOf(AutoDesactivationInterditeException.class);

            verifyNoInteractions(userManagementRepositoryPort, userDetailAssembler);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void desactiver_introuvable_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.desactiver(new DeactivateUserCommand(USER_ID, AUTRE_USER_ID)))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // AssignRoleUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AssignRoleUseCaseImpl")
    class AssignRoleTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        RoleQueryPort roleQueryPort;
        @Mock
        UserHierarchyGuard userHierarchyGuard;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        AssignRoleUseCaseImpl sut;

        @Test
        @DisplayName("attribue un nouveau rôle à l'utilisateur et sauvegarde")
        void assigner_succes_ajoute_role() {
            when(roleQueryPort.existsById(AUTRE_ROLE_ID)).thenReturn(true);
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserDetail result = sut.assigner(new AssignRoleCommand(USER_ID, ACTEUR_ID, AUTRE_ROLE_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getRoleIds()).contains(ROLE_ID, AUTRE_ROLE_ID);
        }

        @Test
        @DisplayName("rôle introuvable → RoleIntrouvableException, sans accès au repository utilisateur")
        void assigner_roleIntrouvable_leve_exception() {
            when(roleQueryPort.existsById(AUTRE_ROLE_ID)).thenReturn(false);

            assertThatThrownBy(() -> sut.assigner(new AssignRoleCommand(USER_ID, ACTEUR_ID, AUTRE_ROLE_ID)))
                    .isInstanceOf(RoleIntrouvableException.class);

            verifyNoInteractions(userManagementRepositoryPort, userDetailAssembler);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void assigner_userIntrouvable_leve_exception() {
            when(roleQueryPort.existsById(AUTRE_ROLE_ID)).thenReturn(true);
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.assigner(new AssignRoleCommand(USER_ID, ACTEUR_ID, AUTRE_ROLE_ID)))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("rôle déjà assigné → RoleDejaAssigneException")
        void assigner_dejaAssigne_leve_exception() {
            when(roleQueryPort.existsById(ROLE_ID)).thenReturn(true);
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));

            assertThatThrownBy(() -> sut.assigner(new AssignRoleCommand(USER_ID, ACTEUR_ID, ROLE_ID)))
                    .isInstanceOf(RoleDejaAssigneException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RevokeRoleUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("RevokeRoleUseCaseImpl")
    class RevokeRoleTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        UserHierarchyGuard userHierarchyGuard;
        @Mock
        UserDetailAssembler userDetailAssembler;
        @InjectMocks
        RevokeRoleUseCaseImpl sut;

        @Test
        @DisplayName("retire un rôle parmi plusieurs et sauvegarde")
        void retirer_succes_retire_role() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.multiRoles()));
            when(userManagementRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(userDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserDetail result = sut.retirer(new RevokeRoleCommand(USER_ID, ACTEUR_ID, AUTRE_ROLE_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
            verify(userManagementRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getRoleIds()).doesNotContain(AUTRE_ROLE_ID);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void retirer_userIntrouvable_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.retirer(new RevokeRoleCommand(USER_ID, ACTEUR_ID, ROLE_ID)))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("rôle non assigné → RoleNonAssigneException")
        void retirer_roleNonAssigne_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));

            assertThatThrownBy(() -> sut.retirer(new RevokeRoleCommand(USER_ID, ACTEUR_ID, AUTRE_ROLE_ID)))
                    .isInstanceOf(RoleNonAssigneException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("dernier rôle de l'utilisateur → DernierRoleException")
        void retirer_dernierRole_leve_exception() {
            when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.of(UserFixtures.actif()));

            assertThatThrownBy(() -> sut.retirer(new RevokeRoleCommand(USER_ID, ACTEUR_ID, ROLE_ID)))
                    .isInstanceOf(DernierRoleException.class);

            verify(userManagementRepositoryPort, never()).save(any());
        }
    }
}
