package ministere.sante.senpna.shared.infrastructure.ratelimit;

import ministere.sante.senpna.shared.domain.port.out.RateLimitPort.RateLimitResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import static org.awaitility.Awaitility.await;

@DisplayName("InMemoryRateLimitAdapter — rate limiting fenêtre fixe en mémoire (profil test)")
class InMemoryRateLimitAdapterTest {

    InMemoryRateLimitAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new InMemoryRateLimitAdapter();
    }

    @Test
    @DisplayName("première requête sous la limite → autorisée")
    void premiereRequete_autorisee() {
        RateLimitResult result = sut.tryConsume("k1", 5, 60_000L);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingTokens()).isEqualTo(4);
    }

    @Test
    @DisplayName("requêtes successives décrémentent le nombre restant")
    void requetesSuccessives_decrementent() {
        sut.tryConsume("k1", 5, 60_000L);
        RateLimitResult second = sut.tryConsume("k1", 5, 60_000L);

        assertThat(second.remainingTokens()).isEqualTo(3);
    }

    @Test
    @DisplayName("dépassement de la limite → refusée")
    void depassementLimite_refusee() {
        for (int i = 0; i < 3; i++) {
            sut.tryConsume("k1", 3, 60_000L);
        }

        RateLimitResult result = sut.tryConsume("k1", 3, 60_000L);

        assertThat(result.allowed()).isFalse();
    }

    @Test
    @DisplayName("clés différentes → compteurs indépendants")
    void clesDifferentes_compteursIndependants() {
        sut.tryConsume("k1", 1, 60_000L);

        RateLimitResult result = sut.tryConsume("k2", 1, 60_000L);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    @DisplayName("fenêtre expirée → compteur réinitialisé")
    void fenetreExpiree_compteurReinitialise() {
        sut.tryConsume("k1", 1, 50L);

        await()
                .atMost(Duration.ofMillis(500))
                .pollDelay(Duration.ofMillis(60))
                .pollInterval(Duration.ofMillis(10))
                .untilAsserted(() -> {
                    RateLimitResult result = sut.tryConsume("k1", 1, 50L);
                    assertThat(result.allowed()).isTrue();
                });
    }

    @Test
    @DisplayName("reset() vide tous les compteurs")
    void reset_videTousLesCompteurs() {
        sut.tryConsume("k1", 1, 60_000L);

        sut.reset();

        assertThat(sut.tryConsume("k1", 1, 60_000L).allowed()).isTrue();
    }
}
