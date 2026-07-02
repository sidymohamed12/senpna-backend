package ministere.sante.senpna.utilisateurs.infrastructure.event;

import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.utilisateurs.domain.port.in.CreateGestionnaireStructureAccountUseCase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Consomme {@link AdhesionValideeEvent}, publié par le module
 * {@code organisation} à la validation d'une demande d'adhésion.
 *
 * <p>
 * Écoute <strong>après le commit</strong> de la transaction qui a validé
 * l'adhésion ({@link TransactionPhase#AFTER_COMMIT}) : si cette
 * transaction échoue ou est annulée, aucun compte n'est créé pour une
 * adhésion qui, finalement, n'a pas été validée. La création du compte
 * s'exécute dans sa propre transaction (aucune n'est active à ce stade).
 * </p>
 */
@Component
public class AdhesionValideeListener {

    private final CreateGestionnaireStructureAccountUseCase createGestionnaireStructureAccountUseCase;

    public AdhesionValideeListener(
            CreateGestionnaireStructureAccountUseCase createGestionnaireStructureAccountUseCase) {
        this.createGestionnaireStructureAccountUseCase = createGestionnaireStructureAccountUseCase;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surAdhesionValidee(AdhesionValideeEvent event) {
        createGestionnaireStructureAccountUseCase.creer(event);
    }
}
