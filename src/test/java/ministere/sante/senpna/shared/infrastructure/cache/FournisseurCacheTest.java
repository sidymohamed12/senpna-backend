package ministere.sante.senpna.shared.infrastructure.cache;

import ministere.sante.senpna.shared.domain.port.out.FournisseurQueryPort;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;

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
@DisplayName("FournisseurCache — cache en mémoire des fournisseurs")
class FournisseurCacheTest {

    @Mock
    FournisseurQueryPort fournisseurQueryPort;

    FournisseurCache sut;

    UUID id1;
    UUID id2;
    FournisseurProjection fournisseur1;
    FournisseurProjection fournisseur2;

    @BeforeEach
    void setUp() {
        sut = new FournisseurCache(fournisseurQueryPort);
        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        fournisseur1 = new FournisseurProjection(id1, "Pharma Plus", true);
        fournisseur2 = new FournisseurProjection(id2, "MediSupply", false);
    }

    @Nested
    @DisplayName("run() / reload()")
    class Reload {

        @Test
        @DisplayName("charge tous les fournisseurs renvoyés par le port")
        void run_chargeLeCache() {
            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur1, fournisseur2));

            sut.run(null);

            assertThat(sut.size()).isEqualTo(2);
            assertThat(sut.findById(id2)).contains(fournisseur2);
        }

        @Test
        @DisplayName("reload() remplace intégralement l'ancien snapshot")
        void reload_remplaceAncienSnapshot() {
            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur1));
            sut.reload();

            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur2));
            sut.reload();

            assertThat(sut.size()).isEqualTo(1);
            assertThat(sut.findById(id1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("identifiant inconnu → Optional vide")
        void inconnu() {
            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur1));
            sut.reload();

            assertThat(sut.findById(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllById()")
    class FindAllById {

        @Test
        @DisplayName("résout les identifiants connus et ignore les inconnus")
        void findAllById_ignoreInconnus() {
            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur1, fournisseur2));
            sut.reload();

            Set<FournisseurProjection> result = sut.findAllById(Set.of(id1, id2, UUID.randomUUID()));

            assertThat(result).containsExactlyInAnyOrder(fournisseur1, fournisseur2);
        }

        @Test
        @DisplayName("ensemble d'identifiants vide → résultat vide")
        void findAllById_ensembleVide() {
            when(fournisseurQueryPort.findAll()).thenReturn(List.of(fournisseur1));
            sut.reload();

            assertThat(sut.findAllById(Set.of())).isEmpty();
        }
    }
}
