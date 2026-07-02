package ministere.sante.senpna.utilisateurs.domain.exception;

import ministere.sante.senpna.shared.domain.exception.BusinessRuleException;

/**
 * Levée lorsqu'une création manuelle de compte demande un rôle dont le
 * compte ne peut être créé que par un processus automatisé — ex:
 * {@code GESTIONNAIRE_STRUCTURE}, créé uniquement à la validation d'une
 * demande d'adhésion (cf. {@code AdhesionValideeEvent}).
 */
public class CreationRoleReserveeException extends BusinessRuleException {
    public CreationRoleReserveeException(String codeRole) {
        super("Le rôle " + codeRole + " ne peut pas être attribué manuellement — le compte est créé "
                + "automatiquement par le système", "ROLE_CREATION_RESERVED");
    }
}
