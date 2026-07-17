package ministere.sante.senpna.config;

import java.time.ZoneId;

/**
 * Constante de fuseau horaire de l'application.
 *
 * <p>
 * {@code APP_ZONE} reste disponible comme constante de référence
 * (migrations Flyway, seeds, documentation), mais <strong>ne doit plus
 * être appelé directement dans le code applicatif</strong>.
 * </p>
 *
 * <p>
 * Toute classe ayant besoin de l'heure courante doit injecter un
 * {@link java.time.Clock} fourni par {@link AppClockConfig}. Cela rend
 * le code testable sans mock statique et respecte le principe d'inversion
 * de dépendance (DIP) : les classes métier dépendent de l'abstraction
 * {@code Clock}, pas de l'implémentation concrète {@code ZoneId.of(...)}.
 * </p>
 *
 * <h3>Migration</h3>
 * 
 * <pre>
 * AVANT  : LocalDate.now(AppClock.APP_ZONE)
 * APRÈS  : LocalDate.now(clock)   // clock injecté via constructeur
 * </pre>
 */
public final class AppClock {

    /**
     * Fuseau horaire officiel de Dakar, Sénégal — UTC+0 toute l'année.
     *
     * <p>
     * On utilise le nom IANA complet plutôt que {@code ZoneOffset.UTC}
     * pour que le code exprime l'intention métier et s'adapte
     * automatiquement si les règles de fuseau évoluent dans la base IANA.
     * </p>
     *
     * <p>
     * <strong>Usage réservé :</strong> configuration Spring
     * ({@link AppClockConfig}),
     * migrations Flyway, et documentation. Pas dans les services métier.
     * </p>
     */
    public static final ZoneId APP_ZONE = ZoneId.of("Africa/Dakar");

    private AppClock() {
    }
}
