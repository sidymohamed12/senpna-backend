package ministere.sante.senpna.catalogue.application.service;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.catalogue.domain.exception.CatalogueAccesRefuseException;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.StructureSanitaireQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.projection.StructureSanitaireProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogueAccessGuard")
class CatalogueAccessGuardTest {

        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        RoleCachePort roleCachePort;
        @Mock
        UserAffectationRepositoryPort userAffectationRepositoryPort;
        @Mock
        EntrepotQueryPort entrepotQueryPort;
        @Mock
        StructureSanitaireQueryPort structureSanitaireQueryPort;

        CatalogueAccessGuard sut;

        private static final UUID ACTEUR_ID = UUID.randomUUID();
        private static final UUID ROLE_ADMIN_PNA_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
        private static final UUID ROLE_ADMIN_PRA_ID = UUID.fromString("e0000000-0000-0000-0000-000000000002");

        @BeforeEach
        void setUp() {
                sut = new CatalogueAccessGuard(userManagementRepositoryPort, roleCachePort,
                                userAffectationRepositoryPort,
                                entrepotQueryPort, structureSanitaireQueryPort);
                SecurityContextHolder.clearContext();
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        private User acteurAvecRoles(Set<UUID> roleIds) {
                return User.reconstruct(UserId.of(ACTEUR_ID), Nom.of(UserFixtures.NOM), Prenom.of(UserFixtures.PRENOM),
                                Email.of(UserFixtures.EMAIL), null, HashedPassword.of(UserFixtures.PASSWORD_HASH), true,
                                roleIds, 0,
                                null, Instant.now(), Instant.now());
        }

        private void authentifier(String... roleCodes) {
                CurrentUser currentUser = mock(CurrentUser.class);
                Mockito.lenient().when(currentUser.getUserId()).thenReturn(ACTEUR_ID);

                List<GrantedAuthority> authorities = List.of(roleCodes).stream()
                                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + code))
                                .toList();

                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(currentUser, null, authorities));
        }

        @Nested
        @DisplayName("verifierActeurPnaOuPra()")
        class VerifierActeurPnaOuPra {

                @Test
                @DisplayName("acteur PNA → autorisé")
                void pna_autorise() {
                        authentifier("GESTIONNAIRE_PNA");
                        assertThatCode(sut::verifierActeurPnaOuPra).doesNotThrowAnyException();
                }

                @Test
                @DisplayName("acteur PRA → autorisé")
                void pra_autorise() {
                        authentifier("MAGASINIER_PRA");
                        assertThatCode(sut::verifierActeurPnaOuPra).doesNotThrowAnyException();
                }

                @Test
                @DisplayName("acteur structure sanitaire → CatalogueAccesRefuseException")
                void structure_refuse() {
                        authentifier("GESTIONNAIRE_STRUCTURE");
                        assertThatThrownBy(sut::verifierActeurPnaOuPra)
                                        .isInstanceOf(CatalogueAccesRefuseException.class);
                }
        }

        @Nested
        @DisplayName("resoudreRegionPourCatalogueRegional()")
        class ResoudreRegionPourCatalogueRegional {

                @Test
                @DisplayName("acteur national (PNA) sans région fournie → ValidationException")
                void national_sansRegion_leveValidationException() {
                        authentifier("ADMIN_PNA");
                        when(userManagementRepositoryPort.findById(any()))
                                        .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PNA_ID))));
                        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                                        .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA",
                                                        "Administrateur PNA")));

                        assertThatThrownBy(() -> sut.resoudreRegionPourCatalogueRegional(null))
                                        .isInstanceOf(ValidationException.class);
                }

                @Test
                @DisplayName("acteur national (PNA) avec région fournie → région conservée telle quelle")
                void national_avecRegion_conserveeTelleQuelle() {
                        authentifier("ADMIN_PNA");
                        when(userManagementRepositoryPort.findById(any()))
                                        .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PNA_ID))));
                        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PNA_ID)))
                                        .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PNA_ID, "ADMIN_PNA",
                                                        "Administrateur PNA")));

                        UUID regionDemandee = UUID.randomUUID();

                        assertThat(sut.resoudreRegionPourCatalogueRegional(regionDemandee)).isEqualTo(regionDemandee);
                }

                @Test
                @DisplayName("structure sanitaire → région demandée ignorée, remplacée par sa propre région")
                void structure_rameneeASaPropreRegion() {
                        authentifier("GESTIONNAIRE_STRUCTURE");
                        when(userManagementRepositoryPort.findById(any()))
                                        .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                        .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID,
                                                        "GESTIONNAIRE_STRUCTURE", "Gest.")));

                        UUID regionStructure = UUID.randomUUID();
                        UUID structureId = UUID.randomUUID();
                        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                                        .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, null, structureId)));
                        when(structureSanitaireQueryPort.findById(structureId)).thenReturn(Optional.of(
                                        new StructureSanitaireProjection(structureId, "HOP-X", "Hôpital X",
                                                        regionStructure, null,
                                                        true)));

                        UUID autreRegionDemandee = UUID.randomUUID();

                        assertThat(sut.resoudreRegionPourCatalogueRegional(autreRegionDemandee))
                                        .isEqualTo(regionStructure);
                }

                @Test
                @DisplayName("acteur PRA → toujours ramené à la région de sa propre PRA")
                void pra_rameneASaPropreRegion() {
                        authentifier("MAGASINIER_PRA");
                        when(userManagementRepositoryPort.findById(any()))
                                        .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                        .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID, "MAGASINIER_PRA",
                                                        "Magasinier PRA")));

                        UUID regionPra = UUID.randomUUID();
                        UUID entrepotId = UUID.randomUUID();
                        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                                        .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, entrepotId, null)));
                        when(entrepotQueryPort.findById(entrepotId)).thenReturn(Optional.of(
                                        new EntrepotProjection(entrepotId, "PRA-X", "PRA X", "PRA", regionPra, true)));

                        assertThat(sut.resoudreRegionPourCatalogueRegional(UUID.randomUUID())).isEqualTo(regionPra);
                }

                @Test
                @DisplayName("acteur non affecté → CatalogueAccesRefuseException")
                void nonAffecte_refuse() {
                        authentifier("GESTIONNAIRE_STRUCTURE");
                        when(userManagementRepositoryPort.findById(any()))
                                        .thenReturn(Optional.of(acteurAvecRoles(Set.of(ROLE_ADMIN_PRA_ID))));
                        when(roleCachePort.findAllById(Set.of(ROLE_ADMIN_PRA_ID)))
                                        .thenReturn(Set.of(new RoleProjection(ROLE_ADMIN_PRA_ID,
                                                        "GESTIONNAIRE_STRUCTURE", "Gest.")));
                        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID)).thenReturn(Optional.empty());

                        var randomUUID = UUID.randomUUID();
                        assertThatThrownBy(() -> sut.resoudreRegionPourCatalogueRegional(randomUUID))
                                        .isInstanceOf(CatalogueAccesRefuseException.class);
                }
        }
}
