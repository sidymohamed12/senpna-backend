package ministere.sante.senpna.auth.application.service;

import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRoleResolver / UserAffectationResolver")
class UserRoleResolverTest {

    @Mock
    RoleCachePort roleCachePort;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;

    @Nested
    @DisplayName("UserRoleResolver.resoudreCodes()")
    class ResoudreCodes {

        UserRoleResolver sut;

        @BeforeEach
        void setUp() {
            sut = new UserRoleResolver(roleCachePort);
        }

        @Test
        @DisplayName("résout les identifiants en codes techniques")
        void resoutIdentifiantsEnCodes() {
            UUID roleId = UUID.randomUUID();
            when(roleCachePort.findAllById(Set.of(roleId))).thenReturn(
                    Set.of(new RoleProjection(roleId, "ADMIN_PNA", "Administrateur")));

            assertThat(sut.resoudreCodes(Set.of(roleId))).containsExactly("ADMIN_PNA");
        }

        @Test
        @DisplayName("ensemble vide → résultat vide")
        void ensembleVide_resultatVide() {
            when(roleCachePort.findAllById(Set.of())).thenReturn(Set.of());

            assertThat(sut.resoudreCodes(Set.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("UserAffectationResolver.resoudre()")
    class Resoudre {

        UserAffectationResolver sut;

        @BeforeEach
        void setUp() {
            sut = new UserAffectationResolver(userAffectationRepositoryPort);
        }

        @Test
        @DisplayName("affectation existante → vue renvoyée telle quelle")
        void affectationExistante_vueRenvoyee() {
            UUID userId = UUID.randomUUID();
            UUID entrepotId = UUID.randomUUID();
            UserAffectationView vue = new UserAffectationView(userId, entrepotId, null, null);
            when(userAffectationRepositoryPort.findAffectation(userId)).thenReturn(Optional.of(vue));

            assertThat(sut.resoudre(userId)).isSameAs(vue);
        }

        @Test
        @DisplayName("aucune affectation → vue vide (entrepotId et structureSanitaireId null)")
        void aucuneAffectation_vueVide() {
            UUID userId = UUID.randomUUID();
            when(userAffectationRepositoryPort.findAffectation(userId)).thenReturn(Optional.empty());

            UserAffectationView result = sut.resoudre(userId);

            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.entrepotId()).isNull();
            assertThat(result.structureSanitaireId()).isNull();
        }
    }
}
