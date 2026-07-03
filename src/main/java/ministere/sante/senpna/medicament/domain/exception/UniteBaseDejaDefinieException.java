package ministere.sante.senpna.medicament.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

/**
 * Levée lorsqu'on tente de définir un second conditionnement comme unité
 * de base pour un médicament qui en possède déjà un — un médicament ne
 * peut avoir qu'une seule unité de base (règle métier §5 du modèle
 * complémentaire : « Chaque médicament possède une unité de base »).
 */
public class UniteBaseDejaDefinieException extends BusinessRuleException {
    public UniteBaseDejaDefinieException() {
        super("Ce médicament possède déjà un conditionnement défini comme unité de base",
                "CONDITIONNEMENT_UNITE_BASE_ALREADY_DEFINED");
    }
}
