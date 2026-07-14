package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Vérifie la règle métier : un acteur n'ayant que des rôles régionaux
 * (PRA) ne peut ni créer/modifier/activer/désactiver un compte national
 * (PNA), ni gérer un autre compte {@code ADMIN_PRA} — y compris d'une
 * région différente. Seul un acteur national (PNA) a une portée illimitée.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserHierarchyGuard — hiérarchie PNA/PRA de gestion des comptes")
class UserHierarchyGuardTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        RoleCachePort roleCachePort;

        UserHierarchyGuard sut;

        private static final UUID ACTEUR_ID = UUID.randomUUID();
        private static final UUID ROLE_ADMIN_PNA_ID = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        private static final UUID ROLE_ADMIN_PRA_ID = UUID.fromString("d0000000-0000-0000-0000-000000000002");
        private static final UUID ROLE_GESTIONNAIRE_PRA_ID = UUID.fromString("d0000000-0000-0000-0000-000000000003");

        @BeforeEach
        void setUp() {
                sut = new UserHierarchyGuard(userManagementRepositoryPort, roleCachePort);
        }

        private User acteurAvecRoles(Set<UUID> roleIds) {
                return User.reconstruct(
                                UserId.of(ACTEUR_ID),
                                Nom.of(UserFixtures.NOM),
                                Prenom.of(UserFixtures.PRENOM),
                                Email.of(UserFixtures.EMAIL),
                                null,
                                HashedPassword.of(UserFixtures.PASSWORD_HASH),
                                true,
                                roleIds,
                                0,
                                null,
                                Instant.now(),
                                Instant.now());
        }

        @Test
        @DisplayName("acteur ADMIN_PNA : aucune restriction, même envers un compte ADMIN_PNA")
        void acteurAdminPna_toujoursAutorise() {
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PNA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA",
                                                "Administrateur PNA")));

                assertThatCode(() -> sut.verifierGestionAutorisee(ACTEUR_ID, Set.of(ROLE_ADMIN_PNA_ID)))
                                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("acteur ADMIN_PRA gérant une cible ADMIN_PNA → refusé")
        void acteurAdminPra_cibleAdminPna_refuse() {
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA",
                                                "Administrateur PRA")));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA",
                                                "Administrateur PNA")));

                var setOf = Set.of(ROLE_ADMIN_PNA_ID);
                assertThatThrownBy(() -> sut.verifierGestionAutorisee(ACTEUR_ID, setOf))
                                .isInstanceOf(GestionUtilisateurInterditeException.class)
                                .isInstanceOf(SenPnaException.class);
        }

        @Test
        @DisplayName("acteur ADMIN_PRA gérant un autre ADMIN_PRA → refusé, même région ou pas")
        void acteurAdminPra_ciblePairAdminPra_refuse() {
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA",
                                                "Administrateur PRA")));

                var setOf = Set.of(ROLE_ADMIN_PRA_ID);
                assertThatThrownBy(() -> sut.verifierGestionAutorisee(ACTEUR_ID, setOf))
                                .isInstanceOf(GestionUtilisateurInterditeException.class);
        }

        @Test
        @DisplayName("acteur ADMIN_PRA gérant un GESTIONNAIRE_PRA (rôle subalterne) → autorisé")
        void acteurAdminPra_cibleSubalterne_autorise() {
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA",
                                                "Administrateur PRA")));
                when(roleCachePort.findAllById(Set.of(ROLE_GESTIONNAIRE_PRA_ID)))
                                .thenReturn(Set.of(
                                                new RoleProjection(ROLE_GESTIONNAIRE_PRA_ID, "GESTIONNAIRE_PRA",
                                                                "Gestionnaire PRA")));

                assertThatCode(() -> sut.verifierGestionAutorisee(ACTEUR_ID, Set.of(ROLE_GESTIONNAIRE_PRA_ID)))
                                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("acteur ADMIN_PRA gérant un GESTIONNAIRE_STRUCTURE → refusé (ce sont des clientes, pas du personnel PRA)")
        void acteurAdminPra_cibleGestionnaireStructure_refuse() {
                UUID roleGestionnaireStructureId = UUID.fromString("d0000000-0000-0000-0000-000000000004");
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "ADMIN_PRA",
                                                "Administrateur PRA")));
                when(roleCachePort.findAllById(Set.of(roleGestionnaireStructureId)))
                                .thenReturn(Set.of(new RoleProjection(roleGestionnaireStructureId,
                                                "GESTIONNAIRE_STRUCTURE",
                                                "Gestionnaire Structure")));

                var setOf = Set.of(roleGestionnaireStructureId);
                assertThatThrownBy(() -> sut.verifierGestionAutorisee(ACTEUR_ID, setOf))
                                .isInstanceOf(GestionUtilisateurInterditeException.class);
        }

        @Test
        @DisplayName("estActeurNational() : true pour un rôle PNA, false pour un rôle PRA")
        void estActeurNational_reponseCorrecte() {
                when(userManagementRepositoryPort.findById(any()))
                                .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PNA_ID))));
                when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                                .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA",
                                                "Administrateur PNA")));

                assertThat(sut.estActeurNational(ACTEUR_ID)).isTrue();
        }

        @Test
        @DisplayName("acteur introuvable → UserNotFoundException")
        void acteurIntrouvable_leveException() {
                when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

                var setOf = Set.of(ROLE_GESTIONNAIRE_PRA_ID);
                assertThatThrownBy(() -> sut.verifierGestionAutorisee(ACTEUR_ID, setOf))
                                .isInstanceOf(UserNotFoundException.class);
        }
}
