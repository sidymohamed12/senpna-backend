package ministere.sante.senpna.shared.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("InMemoryCacheAdapter — cache clé/valeur en mémoire (profil test)")
class InMemoryCacheAdapterTest {

    InMemoryCacheAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new InMemoryCacheAdapter();
    }

    @Test
    @DisplayName("put() puis get() renvoie la valeur stockée")
    void putPuisGet_renvoieValeur() {
        sut.put("k1", "valeur", Duration.ofMinutes(5));

        assertThat(sut.get("k1")).contains("valeur");
    }

    @Test
    @DisplayName("clé absente → Optional vide")
    void cleAbsente_optionalVide() {
        assertThat(sut.get("inexistante")).isEmpty();
    }

    @Test
    @DisplayName("entrée expirée → Optional vide, entrée retirée du cache")
    void entreeExpiree_optionalVideEtRetiree() {
        sut.put("k1", "valeur", Duration.ofMillis(20));

        await()
                .atMost(Duration.ofMillis(500))
                .pollDelay(Duration.ofMillis(30))
                .pollInterval(Duration.ofMillis(10))
                .untilAsserted(() -> {
                    assertThat(sut.get("k1")).isEmpty();
                    assertThat(sut.activeSize()).isZero();
                });
    }

    @Test
    @DisplayName("evict() retire la clé")
    void evict_retireLaCle() {
        sut.put("k1", "valeur", Duration.ofMinutes(5));

        sut.evict("k1");

        assertThat(sut.get("k1")).isEmpty();
    }

    @Test
    @DisplayName("evictByPrefix() retire toutes les clés partageant le préfixe")
    void evictByPrefix_retireClesAvecPrefixe() {
        sut.put("user:1", "a", Duration.ofMinutes(5));
        sut.put("user:2", "b", Duration.ofMinutes(5));
        sut.put("role:1", "c", Duration.ofMinutes(5));

        sut.evictByPrefix("user:");

        assertThat(sut.get("user:1")).isEmpty();
        assertThat(sut.get("user:2")).isEmpty();
        assertThat(sut.get("role:1")).isPresent();
    }

    @Test
    @DisplayName("clear() vide entièrement le cache")
    void clear_videEntierementLeCache() {
        sut.put("k1", "a", Duration.ofMinutes(5));
        sut.put("k2", "b", Duration.ofMinutes(5));

        sut.clear();

        assertThat(sut.activeSize()).isZero();
    }

    @Test
    @DisplayName("put() sur une clé existante écrase la valeur précédente")
    void put_surCleExistante_ecrase() {
        sut.put("k1", "ancienne", Duration.ofMinutes(5));

        sut.put("k1", "nouvelle", Duration.ofMinutes(5));

        assertThat(sut.get("k1")).contains("nouvelle");
    }
}
