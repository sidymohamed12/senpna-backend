package ministere.sante.senpna.carriere.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'une candidature est soumise sur une opportunité qui
 * n'accepte plus de nouvelles candidatures — statut effectif différent de
 * {@code OUVERT} (brouillon, en cours de traitement, clôturée manuellement
 * ou automatiquement du fait du dépassement de la date limite).
 */
public class OpportuniteFermeeException extends SenPnaException {
    public OpportuniteFermeeException() {
        super("Cette offre n'accepte plus de nouvelles candidatures", "OPPORTUNITE_FERMEE", ErrorCategory.BUSINESS_RULE);
    }
}
