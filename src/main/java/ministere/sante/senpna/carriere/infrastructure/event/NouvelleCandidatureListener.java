package ministere.sante.senpna.carriere.infrastructure.event;

import ministere.sante.senpna.carriere.domain.events.NouvelleCandidatureEvent;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureMailPort;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Consomme {@link NouvelleCandidatureEvent}, publié par
 * {@code Candidature.soumettre()} (module {@code carriere}).
 *
 * <h3>Après commit, en tâche de fond</h3>
 * <p>
 * Écoute après le commit de la transaction qui a enregistré la candidature
 * ({@link TransactionPhase#AFTER_COMMIT}) : si cette transaction échoue,
 * aucun e-mail n'est envoyé pour une candidature qui, finalement, n'a pas
 * été persistée. {@code @Async} délègue l'envoi à l'executor général (cf.
 * {@code AsyncConfig}) pour ne jamais retarder la réponse HTTP envoyée au
 * candidat — l'envoi des deux e-mails (accusé de réception, notification
 * RH) est de toute façon best-effort côté adaptateur SMTP.
 * </p>
 */
@Component
public class NouvelleCandidatureListener {

    private final CandidatureMailPort candidatureMailPort;

    public NouvelleCandidatureListener(CandidatureMailPort candidatureMailPort) {
        this.candidatureMailPort = candidatureMailPort;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surNouvelleCandidature(NouvelleCandidatureEvent event) {
        candidatureMailPort.envoyerAccuseReceptionCandidature(
                event.emailCandidat(), event.nomCandidat(), event.titreOffre(), event.nomEntreprise());

        candidatureMailPort.envoyerNotificationNouvelleCandidature(
                event.emailContactRH(), event.titreOffre(), event.nomEntreprise(), event.nomCandidat(),
                event.emailCandidat(), event.telephoneCandidat());
    }
}
