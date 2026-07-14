package ministere.sante.senpna.shared.domain.exception;

/**
 * Catégorie fonctionnelle d'une {@link SenPnaException}, utilisée par les
 * gestionnaires d'exceptions HTTP pour déterminer le statut de réponse
 * approprié.
 *
 * <p>
 * Remplace l'ancienne hiérarchie de sous-classes intermédiaires
 * ({@code NotFoundException}, {@code ValidationException},
 * {@code ForbiddenException}, {@code ConflictException},
 * {@code BusinessRuleException}, {@code UnauthorizedException}), qui
 * portait la même information (« à quel statut HTTP correspond cette
 * erreur ? ») via l'arbre d'héritage plutôt que via une donnée.
 * </p>
 *
 * <p>
 * Ce changement corrige la règle SonarQube {@code java:S110}
 * (« Inheritance tree of classes should not be too deep », seuil par
 * défaut : 5) : avec les sous-classes intermédiaires, toute exception
 * métier concrète (ex : {@code CommandeAchatIntrouvableException extends
 * NotFoundException extends SenPnaException extends RuntimeException
 * extends Exception extends Throwable}) comptait 6 ancêtres. En
 * remplaçant l'héritage par cette donnée, chaque exception concrète
 * n'hérite plus que directement de {@code SenPnaException} (5 ancêtres),
 * et cette profondeur reste constante quel que soit le nombre
 * d'exceptions métier ajoutées par la suite — contrairement à
 * l'ancienne hiérarchie, qui ne pouvait qu'empirer.
 * </p>
 *
 * <p>
 * Volontairement dépourvu de toute dépendance à {@code HttpStatus} (ou à
 * tout autre type Spring) : le mapping catégorie → statut HTTP est une
 * responsabilité d'infrastructure, portée exclusivement par les
 * {@code @RestControllerAdvice} (cf. {@code GlobalExceptionHandler}), pas
 * par le domaine.
 * </p>
 */
public enum ErrorCategory {

    /** 401 — jeton invalide, identifiants incorrects. */
    UNAUTHORIZED,

    /** 403 — accès refusé (portée métier, hiérarchique ou de propriété). */
    FORBIDDEN,

    /** 404 — ressource introuvable. */
    NOT_FOUND,

    /** 409 — conflit (unicité, état déjà atteint). */
    CONFLICT,

    /** 422 — violation d'une règle métier ou transition d'état invalide. */
    BUSINESS_RULE,

    /** 400 — validation d'entrée invalide. */
    VALIDATION
}
