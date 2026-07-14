package ministere.sante.senpna.shared.domain.exception;

import java.util.Objects;

/**
 * Racine de la hiérarchie des exceptions métier — toute exception levée
 * par le domaine ou l'application étend directement cette classe.
 *
 * <p>
 * La catégorisation fonctionnelle (« à quel statut HTTP correspond cette
 * erreur ? ») est portée par le champ {@link #category}, pas par
 * l'héritage — cf. {@link ErrorCategory} pour le raisonnement complet
 * (correction de la règle SonarQube {@code java:S110}, profondeur
 * d'héritage). Chaque exception métier concrète doit donc étendre
 * {@code SenPnaException} directement, jamais une sous-classe
 * intermédiaire.
 * </p>
 *
 * <p>
 * Concrète (non {@code abstract}) : peut être levée directement pour un
 * cas d'erreur ponctuel qui ne justifie pas une classe dédiée
 * (ex : {@code throw new SenPnaException(message, type,
 * ErrorCategory.VALIDATION)}), en plus du cas usuel où une exception
 * métier nommée en hérite pour documenter l'intention au niveau du
 * type.
 * </p>
 */
public class SenPnaException extends RuntimeException {

    private final String type;
    private final ErrorCategory category;

    public SenPnaException(String message, String type, ErrorCategory category) {
        super(message);
        this.type = type;
        this.category = Objects.requireNonNull(category, "La catégorie d'erreur est obligatoire");
    }

    public String getType() {
        return type;
    }

    public ErrorCategory getCategory() {
        return category;
    }
}
