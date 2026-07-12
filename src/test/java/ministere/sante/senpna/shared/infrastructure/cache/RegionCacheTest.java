package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.RegionQueryPort;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegionCache — cache en mémoire des régions")
class RegionCacheTest {

    @Mock
    RegionQueryPort regionQueryPort;

    RegionCache sut;

    UUID id1;
    UUID id2;
    RegionProjection region1;
    RegionProjection region2;

    @BeforeEach
    void setUp() {
        sut = new RegionCache(regionQueryPort);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        region1 = new RegionProjection(id1, "THIES", "Thiès", true);
        region2 = new RegionProjection(id2, "DAKAR", "Dakar", true);
    }

    @Nested
    @DisplayName("run() / reload()")
    class Reload {

        @Test
        @DisplayName("charge toutes les régions et alimente les deux index (id et code)")
        void run_chargeLeCache() {
            when(regionQueryPort.findAll()).thenReturn(List.of(region1, region2));

            sut.run(null);

            assertThat(sut.size()).isEqualTo(2);
            assertThat(sut.findById(id1)).contains(region1);
            assertThat(sut.findByCode("DAKAR")).contains(region2);
        }

        @Test
        @DisplayName("reload() remplace intégralement l'ancien snapshot (les deux index)")
        void reload_remplaceAncienSnapshot() {
            when(regionQueryPort.findAll()).thenReturn(List.of(region1));
            sut.reload();

            when(regionQueryPort.findAll()).thenReturn(List.of(region2));
            sut.reload();

            assertThat(sut.size()).isEqualTo(1);
            assertThat(sut.findById(id1)).isEmpty();
            assertThat(sut.findByCode("THIES")).isEmpty();
            assertThat(sut.findByCode("DAKAR")).contains(region2);
        }
    }

    @Nested
    @DisplayName("findByCode()")
    class FindByCode {

        @Test
        @DisplayName("code connu → Optional non vide")
        void connu() {
            when(regionQueryPort.findAll()).thenReturn(List.of(region1));
            sut.reload();

            assertThat(sut.findByCode("THIES")).contains(region1);
        }

        @Test
        @DisplayName("code inconnu → Optional vide")
        void inconnu() {
            when(regionQueryPort.findAll()).thenReturn(List.of(region1));
            sut.reload();

            assertThat(sut.findByCode("INCONNU")).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsById {

        @Test
        @DisplayName("identifiant connu → true, inconnu → false")
        void existsById() {
            when(regionQueryPort.findAll()).thenReturn(List.of(region1));
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
            when(regionQueryPort.findAll()).thenReturn(List.of(region1, region2));
            sut.reload();

            Set<RegionProjection> result = sut.findAllById(Set.of(id1, UUID.randomUUID()));

            assertThat(result).containsExactly(region1);
        }
    }
}
