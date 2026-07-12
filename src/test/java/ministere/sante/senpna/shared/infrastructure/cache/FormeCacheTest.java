package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FormeQueryPort;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;

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
@DisplayName("FormeCache — cache en mémoire des formes pharmaceutiques")
class FormeCacheTest {

    @Mock
    FormeQueryPort formeQueryPort;

    FormeCache sut;

    UUID id1;
    UUID id2;
    FormeProjection forme1;
    FormeProjection forme2;

    @BeforeEach
    void setUp() {
        sut = new FormeCache(formeQueryPort);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        forme1 = new FormeProjection(id1, "COMP", "Comprimé", true);
        forme2 = new FormeProjection(id2, "SIROP", "Sirop", true);
    }

    @Nested
    @DisplayName("run() / reload()")
    class Reload {

        @Test
        @DisplayName("charge toutes les formes renvoyées par le port")
        void run_chargeLeCache() {
            when(formeQueryPort.findAll()).thenReturn(List.of(forme1, forme2));

            sut.run(null);

            assertThat(sut.size()).isEqualTo(2);
            assertThat(sut.findById(id1)).contains(forme1);
        }

        @Test
        @DisplayName("reload() remplace intégralement l'ancien snapshot")
        void reload_remplaceAncienSnapshot() {
            when(formeQueryPort.findAll()).thenReturn(List.of(forme1));
            sut.reload();

            when(formeQueryPort.findAll()).thenReturn(List.of(forme2));
            sut.reload();

            assertThat(sut.size()).isEqualTo(1);
            assertThat(sut.findById(id1)).isEmpty();
            assertThat(sut.findById(id2)).contains(forme2);
        }
    }

    @Nested
    @DisplayName("findById() / existsById()")
    class FindByIdEtExists {

        @Test
        @DisplayName("identifiant connu")
        void connu() {
            when(formeQueryPort.findAll()).thenReturn(List.of(forme1));
            sut.reload();

            assertThat(sut.findById(id1)).contains(forme1);
            assertThat(sut.existsById(id1)).isTrue();
        }

        @Test
        @DisplayName("identifiant inconnu")
        void inconnu() {
            when(formeQueryPort.findAll()).thenReturn(List.of(forme1));
            sut.reload();

            UUID inconnu = UUID.randomUUID();
            assertThat(sut.findById(inconnu)).isEmpty();
            assertThat(sut.existsById(inconnu)).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllById()")
    class FindAllById {

        @Test
        @DisplayName("résout les identifiants connus et ignore les inconnus")
        void findAllById_ignoreInconnus() {
            when(formeQueryPort.findAll()).thenReturn(List.of(forme1, forme2));
            sut.reload();

            Set<FormeProjection> result = sut.findAllById(Set.of(id2, UUID.randomUUID()));

            assertThat(result).containsExactly(forme2);
        }
    }
}
