package ministere.sante.senpna.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

/**
 * Configuration du {@link Clock} applicatif.
 *
 * <h3>Pourquoi un bean Clock et pas AppClock.APP_ZONE direct ?</h3>
 * <p>
 * {@code LocalDate.now(AppClock.APP_ZONE)} est un appel statique non
 * substituable dans les tests. Injecter un {@link Clock} permet de fixer
 * l'instant courant dans chaque test sans {@code PowerMock} ni hack.
 * C'est le pattern recommandé par Martin Fowler et l'équipe Spring.
 * </p>
 *
 * <h3>Fuseau dynamique</h3>
 * <p>
 * La propriété {@code app.clock.timezone} (défaut : {@code Africa/Dakar})
 * permet de changer le fuseau sans recompilation, ce qui est utile lors
 * d'un déploiement dans une nouvelle région ou pour des tests end-to-end
 * simulant un autre fuseau.
 * </p>
 *
 * <h3>Profil test</h3>
 * <p>
 * En profil {@code test}, un {@link Clock} fixé à un instant déterministe
 * est fourni. Les tests de la feature {@code menu/} ne dépendent plus de
 * l'horloge système : pas de flakiness à 23h55 ou lors d'un changement
 * de date pendant l'exécution de la suite CI.
 * </p>
 */
@Configuration
public class AppClockConfig {

    /**
     * Clock production / développement — avance en temps réel dans le
     * fuseau configuré.
     *
     * <p>
     * La propriété {@code app.clock.timezone} est lue au démarrage.
     * Une valeur invalide (ex: {@code "Etc/Nowhere"}) lève une
     * {@link java.time.zone.ZoneRulesException} au démarrage du contexte
     * Spring, ce qui est le comportement fail-fast souhaité.
     * </p>
     */
    @Bean
    @Profile({ "dev", "prod" })
    public Clock applicationClock(
            @Value("${app.clock.timezone:Africa/Dakar}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }

    /**
     * Clock test — fixé au 2026-01-15T12:00:00 Dakar.
     *
     * <p>
     * Midi un mercredi : les tests de {@code consulterMenuDuJour()}
     * reçoivent toujours MERCREDI à l'heure du déjeuner, quel que soit
     * le moment où la suite CI tourne.
     * </p>
     *
     * <p>
     * Un test nécessitant un instant différent peut déclarer son propre
     * bean {@code Clock} {@code @Primary} ou utiliser
     * {@code Clock.fixed(Instant, ZoneId)} dans son contexte local.
     * </p>
     */
    @Bean
    @Profile("test")
    Clock testClock() {
        return Clock.fixed(
                LocalDateTime.of(2026, Month.JANUARY, 15, 12, 0)
                        .atZone(AppClock.APP_ZONE)
                        .toInstant(),
                AppClock.APP_ZONE);
    }
}
