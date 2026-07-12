package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FamilleQueryPort;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("FamilleCache — cache en mémoire des familles thérapeutiques")
class FamilleCacheTest {

    @Mock
    FamilleQueryPort familleQueryPort;

    FamilleCache sut;

    UUID id1;
    UUID id2;
    FamilleProjection famille1;
    FamilleProjection famille2;

    @BeforeEach
    void setUp() {
        sut = new FamilleCache(familleQueryPort);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        famille1 = new FamilleProjection(id1, "ANTIBIO", "Antibiotiques", true);
        famille2 = new FamilleProjection(id2, "ANTALG", "Antalgiques", true);
    }

    @Nested
    @DisplayName("avant tout chargement")
    class AvantChargement {

        @Test
        @DisplayName("le cache est vide")
        void cacheVideParDefaut() {
            assertThat(sut.size()).isZero();
            assertThat(sut.findById(id1)).isEmpty();
            assertThat(sut.existsById(id1)).isFalse();
        }
    }

    @Nested
    @DisplayName("run() / reload()")
    class Reload {

        @Test
        @DisplayName("charge toutes les familles renvoyées par le port au démarrage")
        void run_chargeLeCache() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1, famille2));

            sut.run(null);

            assertThat(sut.size()).isEqualTo(2);
            assertThat(sut.findById(id1)).contains(famille1);
            assertThat(sut.findById(id2)).contains(famille2);
        }

        @Test
        @DisplayName("reload() remplace intégralement l'ancien snapshot")
        void reload_remplaceAncienSnapshot() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();
            assertThat(sut.size()).isEqualTo(1);

            when(familleQueryPort.findAll()).thenReturn(List.of(famille2));
            sut.reload();

            assertThat(sut.size()).isEqualTo(1);
            assertThat(sut.findById(id1)).isEmpty();
            assertThat(sut.findById(id2)).contains(famille2);
            verify(familleQueryPort, times(2)).findAll();
        }

        @Test
        @DisplayName("liste vide renvoyée par le port → cache vide, pas d'erreur")
        void reload_listeVide() {
            when(familleQueryPort.findAll()).thenReturn(List.of());

            sut.reload();

            assertThat(sut.size()).isZero();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("identifiant connu → Optional non vide")
        void findById_connu() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();

            assertThat(sut.findById(id1)).contains(famille1);
        }

        @Test
        @DisplayName("identifiant inconnu → Optional vide")
        void findById_inconnu() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();

            assertThat(sut.findById(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsById {

        @Test
        @DisplayName("identifiant connu → true")
        void existsById_connu() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();

            assertThat(sut.existsById(id1)).isTrue();
        }

        @Test
        @DisplayName("identifiant inconnu → false")
        void existsById_inconnu() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();

            assertThat(sut.existsById(UUID.randomUUID())).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllById()")
    class FindAllById {

        @Test
        @DisplayName("résout les identifiants connus et ignore les inconnus")
        void findAllById_ignoreInconnus() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1, famille2));
            sut.reload();

            Set<FamilleProjection> result = sut.findAllById(Set.of(id1, UUID.randomUUID()));

            assertThat(result).containsExactly(famille1);
        }

        @Test
        @DisplayName("ensemble d'identifiants vide → résultat vide")
        void findAllById_ensembleVide() {
            when(familleQueryPort.findAll()).thenReturn(List.of(famille1));
            sut.reload();

            assertThat(sut.findAllById(Set.of())).isEmpty();
        }
    }
}
