package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.exception.SenPnaException;
import ministere.sante.senpna.shared.domain.port.out.RoleQueryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleCache — cache en mémoire des rôles")
class RoleCacheTest {

    @Mock
    RoleQueryPort roleQueryPort;

    RoleCache sut;

    UUID id1;
    UUID id2;
    RoleProjection role1;
    RoleProjection role2;

    @BeforeEach
    void setUp() {
        sut = new RoleCache(roleQueryPort);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        role1 = new RoleProjection(id1, "ADMIN", "Administrateur");
        role2 = new RoleProjection(id2, "PHARMACIEN", "Pharmacien");
    }

    @Nested
    @DisplayName("run() / reload()")
    class Reload {

        @Test
        @DisplayName("charge tous les rôles et alimente les deux index (id et code)")
        void run_chargeLeCache() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1, role2));

            sut.run(null);

            assertThat(sut.size()).isEqualTo(2);
            assertThat(sut.findById(id1)).contains(role1);
            assertThat(sut.findByCode("PHARMACIEN")).contains(role2);
        }

        @Test
        @DisplayName("reload() remplace intégralement l'ancien snapshot")
        void reload_remplaceAncienSnapshot() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1));
            sut.reload();

            when(roleQueryPort.findAll()).thenReturn(List.of(role2));
            sut.reload();

            assertThat(sut.size()).isEqualTo(1);
            assertThat(sut.findByCode("ADMIN")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCode()")
    class GetCode {

        @Test
        @DisplayName("identifiant connu → renvoie le code technique")
        void connu() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1));
            sut.reload();

            assertThat(sut.getCode(id1)).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("identifiant inconnu → NotFoundException")
        void inconnu_leveNotFoundException() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1));
            sut.reload();

            UUID inconnu = UUID.randomUUID();
            assertThatThrownBy(() -> sut.getCode(inconnu))
                    .isInstanceOf(SenPnaException.class)
                    .hasMessageContaining(inconnu.toString());
        }
    }

    @Nested
    @DisplayName("findByCode() / existsById()")
    class FindByCodeEtExists {

        @Test
        @DisplayName("code inconnu → Optional vide")
        void findByCode_inconnu() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1));
            sut.reload();

            assertThat(sut.findByCode("INCONNU")).isEmpty();
        }

        @Test
        @DisplayName("existsById() reflète le contenu du cache")
        void existsById() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1));
            sut.reload();

            assertThat(sut.existsById(id1)).isTrue();
            assertThat(sut.existsById(UUID.randomUUID())).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllById()")
    class FindAllById {

        @Test
        @DisplayName("résout les identifiants connus et ignore les inconnus")
        void findAllById_ignoreInconnus() {
            when(roleQueryPort.findAll()).thenReturn(List.of(role1, role2));
            sut.reload();

            Set<RoleProjection> result = sut.findAllById(Set.of(id2, UUID.randomUUID()));

            assertThat(result).containsExactly(role2);
        }
    }
}
