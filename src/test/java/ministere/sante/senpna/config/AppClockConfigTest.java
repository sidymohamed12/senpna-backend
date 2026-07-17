package ministere.sante.senpna.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRulesException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests unitaires directs sur {@link AppClockConfig}, sans démarrage de
 * contexte Spring : les méthodes {@code @Bean} sont de simples méthodes
 * Java, on peut donc les appeler sur une instance nue de la classe pour
 * vérifier leur comportement de façon rapide et déterministe, sans
 * dépendre du profil réellement actif au moment de l'exécution des tests.
 */
@DisplayName("AppClockConfig — horloge applicative")
class AppClockConfigTest {

    private final AppClockConfig sut = new AppClockConfig();

    @Nested
    @DisplayName("applicationClock() — profils dev/prod")
    class ApplicationClock {

        @Test
        @DisplayName("fuseau par défaut (Africa/Dakar) → horloge vivante sur ce fuseau")
        void fuseauParDefaut_horlogeSurDakar() {
            Clock clock = sut.applicationClock("Africa/Dakar");

            assertThat(clock.getZone())
                    .isEqualTo(ZoneId.of("Africa/Dakar"));
        }

        @Test
        @DisplayName("fuseau surchargé par app.clock.timezone → horloge sur fuseau demandé")
        void fuseauSurcharge_horlogeSurFuseauDemande() {
            Clock clock = sut.applicationClock("Europe/Paris");

            assertThat(clock.getZone())
                    .isEqualTo(ZoneId.of("Europe/Paris"));
        }

        @Test
        @DisplayName("avance en temps réel — proche de Instant.now()")
        void avanceEnTempsReel() {
            Clock clock = sut.applicationClock("Africa/Dakar");

            assertThat(clock.instant())
                    .isCloseTo(Instant.now(), within(500, ChronoUnit.MILLIS));
        }

        @Test
        @DisplayName("fuseau invalide → ZoneRulesException (fail-fast)")
        void fuseauInvalide_leveException() {
            assertThatThrownBy(() -> sut.applicationClock("Etc/Nowhere"))
                    .isInstanceOf(ZoneRulesException.class);
        }
    }

    @Nested
    @DisplayName("testClock() — profil test")
    class TestClockBean {

        @Test
        @DisplayName("fixé au 2026-01-15T12:00:00 sur le fuseau applicatif (Africa/Dakar)")
        void fixeAuQuinzeJanvierMidiDakar() {
            Clock clock = sut.testClock();

            assertThat(clock.getZone())
                    .isEqualTo(AppClock.APP_ZONE);

            assertThat(LocalDateTime.now(clock))
                    .isEqualTo(LocalDateTime.of(
                            2026,
                            Month.JANUARY,
                            15,
                            12,
                            0,
                            0));
        }

        @Test
        @DisplayName("tombe un jeudi — cf. doc. tests menu/consulterMenuDuJour()")
        void tombeUnJeudi() {
            Clock clock = sut.testClock();

            assertThat(LocalDateTime.now(clock).getDayOfWeek())
                    .isEqualTo(DayOfWeek.THURSDAY);
        }

        @Test
        @DisplayName("reste identique à chaque appel — pas d'écoulement du temps")
        void resteFigeEntreDeuxAppels() {
            Clock clock = sut.testClock();

            Instant premierInstant = clock.instant();
            Instant secondInstant = clock.instant();

            assertThat(premierInstant)
                    .isEqualTo(secondInstant);
        }
    }
}