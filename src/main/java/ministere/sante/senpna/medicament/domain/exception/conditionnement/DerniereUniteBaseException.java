package ministere.sante.senpna.medicament.domain.exception.conditionnement;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsqu'on tente d'archiver, ou de retirer le statut d'unité de
 * base, du seul conditionnement de base d'un médicament — un médicament
 * actif doit toujours conserver une unité de base pour exprimer son stock.
 */
public class DerniereUniteBaseException extends SenPnaException {
    public DerniereUniteBaseException() {
        super("Impossible de retirer l'unique unité de base d'un médicament — définissez d'abord une autre unité de base",
                "CONDITIONNEMENT_LAST_UNITE_BASE", ErrorCategory.BUSINESS_RULE);
    }
}
