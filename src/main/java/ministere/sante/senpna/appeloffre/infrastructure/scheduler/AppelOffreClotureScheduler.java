package ministere.sante.senpna.appeloffre.infrastructure.scheduler;

import ministere.sante.senpna.appeloffre.domain.port.in.ClorerAppelOffresExpiresUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job planifié quotidien qui clôture automatiquement les appels d'offres
 * {@code PUBLIE} dont la date de clôture est dépassée — évite de dépendre
 * d'une action manuelle de la PNA pour figer la liste des offres
 * recevables avant analyse (cf. {@code LotExpirationScheduler} pour le
 * même principe appliqué aux lots).
 */
@Component
public class AppelOffreClotureScheduler {

    private static final Logger log = LoggerFactory.getLogger(AppelOffreClotureScheduler.class);

    private final ClorerAppelOffresExpiresUseCase clorerAppelOffresExpiresUseCase;

    public AppelOffreClotureScheduler(ClorerAppelOffresExpiresUseCase clorerAppelOffresExpiresUseCase) {
        this.clorerAppelOffresExpiresUseCase = clorerAppelOffresExpiresUseCase;
    }

    /** Tous les jours à 00h30 — juste après le passage à la date de clôture. */
    @Async("alerteExecutor")
    @Scheduled(cron = "0 30 0 * * *")
    public void clorerAppelOffresExpires() {
        try {
            int nombreClotures = clorerAppelOffresExpiresUseCase.clorerExpires();
            log.info("[AppelOffreClotureScheduler] Exécution terminée : {} appel(s) d'offres clôturé(s)",
                    nombreClotures);
        } catch (Exception e) {
            log.error("[AppelOffreClotureScheduler] Échec de l'exécution planifiée : {}", e.getMessage(), e);
        }
    }
}
