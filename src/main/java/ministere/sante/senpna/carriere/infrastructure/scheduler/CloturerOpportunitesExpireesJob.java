package ministere.sante.senpna.carriere.infrastructure.scheduler;

import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Clôture automatiquement les opportunités de carrière dont la date limite
 * de candidature est dépassée.
 *
 * <p>
 * Ce job matérialise en base la clôture automatique décrite par
 * {@link OpportuniteCarriere#estExpiree()} : sans lui, une offre expirée
 * resterait indéfiniment {@code OUVERT}/{@code EN_COURS} en base, bien que
 * l'API la présente déjà comme {@code CLOTURE} (cf.
 * {@code OpportuniteCarriereDetailAssembler}, qui utilise systématiquement
 * {@link OpportuniteCarriere#getStatutEffectif()}). Ce job garantit la
 * cohérence à moyen terme (filtres par statut, rapports, exports) même si
 * la lecture reste toujours correcte entre deux exécutions.
 * </p>
 */
@Component
public class CloturerOpportunitesExpireesJob {

    private static final Logger log = LoggerFactory.getLogger(CloturerOpportunitesExpireesJob.class);

    private final OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

    public CloturerOpportunitesExpireesJob(OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort) {
        this.opportuniteCarriereRepositoryPort = opportuniteCarriereRepositoryPort;
    }

    /**
     * Exécution quotidienne à 01h00 — les clôtures ne sont pas
     * urgentes à la minute près, une fréquence quotidienne suffit très
     * largement compte tenu de la granularité en jours de la date limite.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void cloturerLesOpportunitesExpirees() {
        List<OpportuniteCarriere> expirees = opportuniteCarriereRepositoryPort.findOuvertesExpirees(LocalDate.now());
        if (expirees.isEmpty()) {
            return;
        }

        for (OpportuniteCarriere opportunite : expirees) {
            opportunite.cloturer();
            opportuniteCarriereRepositoryPort.save(opportunite);
        }

        log.info("[CARRIERE] {} opportunité(s) clôturée(s) automatiquement (date limite dépassée)",
                expirees.size());
    }
}
