package ministere.sante.senpna.stock.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ministere.sante.senpna.stock.domain.port.in.lot.MarquerLotsExpiresUseCase;

/**
 * Job planifié quotidien qui détecte les lots {@code ACTIF} dont la date
 * d'expiration est dépassée et les marque {@code EXPIRE}. S'exécute sur
 * l'executor dédié {@code alerteExecutor} (cf. {@code AsyncConfig}) afin de ne
 * pas saturer le pool des requêtes HTTP.
 *
 * <p>
 * Complète les alertes de péremption anticipées (12/6/3/1 mois, cf. doc.
 * métier §17), qui restent disponibles en continu via
 * {@code GET /api/lots/alertes/peremption} sans attendre ce job — celui-ci
 * ne fait que positionner le statut terminal {@code EXPIRE} une fois la
 * date effectivement dépassée, empêchant toute réservation ou expédition
 * ultérieure du lot.
 * </p>
 */
@Component
public class LotExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(LotExpirationScheduler.class);

    private final MarquerLotsExpiresUseCase marquerLotsExpiresUseCase;

    public LotExpirationScheduler(MarquerLotsExpiresUseCase marquerLotsExpiresUseCase) {
        this.marquerLotsExpiresUseCase = marquerLotsExpiresUseCase;
    }

    /**
     * Tous les jours à 01h00 — heure creuse, avant le début d'activité des
     * entrepôts.
     */
    @Async("alerteExecutor")
    @Scheduled(cron = "0 0 1 * * *")
    public void marquerLotsExpires() {
        try {
            int nombreLotsExpires = marquerLotsExpiresUseCase.marquerExpires();
            log.info("[LotExpirationScheduler] Exécution terminée : {} lot(s) marqué(s) EXPIRE",
                    nombreLotsExpires);
        } catch (Exception e) {
            log.error("[LotExpirationScheduler] Échec de l'exécution planifiée : {}", e.getMessage(), e);
        }
    }
}
