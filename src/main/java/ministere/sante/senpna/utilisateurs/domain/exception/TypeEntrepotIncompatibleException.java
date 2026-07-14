package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée lorsque le type de l'entrepôt fourni ({@code PRA} ou
 * {@code PNA_CENTRAL}) ne correspond pas au type exigé par les rôles
 * demandés pour le compte (cf. {@code RolesEntrepot}).
 */
public class TypeEntrepotIncompatibleException extends SenPnaException {
    public TypeEntrepotIncompatibleException(String typeAttendu) {
        super("L'entrepôt fourni n'est pas de type " + typeAttendu + ", incompatible avec le(s) rôle(s) demandé(s)",
                "ENTREPOT_TYPE_INCOMPATIBLE", ErrorCategory.BUSINESS_RULE);
    }
}
