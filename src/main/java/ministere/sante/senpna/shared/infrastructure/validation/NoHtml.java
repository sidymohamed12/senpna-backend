package ministere.sante.senpna.shared.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interdit tout balisage HTML/script dans un champ texte libre.
 *
 * <h3>Pourquoi (OWASP A03:2021 — Injection / XSS)</h3>
 * <p>
 * Cette API est un backend JSON pur : Jackson échappe déjà correctement
 * les chaînes lors de la sérialisation, et le front (Angular) échappe par
 * défaut le contenu inséré dans le DOM. {@code @NoHtml} ajoute une
 * <strong>défense en profondeur côté serveur</strong> : les champs texte
 * destinés à être affichés (titres, descriptions, noms...) — en particulier
 * ceux alimentés par des formulaires publics non authentifiés — sont
 * rejetés à la validation s'ils contiennent des balises HTML, des
 * gestionnaires d'évènements ({@code onerror=}, {@code onclick=}...) ou
 * des URI {@code javascript:} / {@code data:text/html}. Cela empêche un
 * contenu malveillant de persister en base et d'être rejoué plus tard vers
 * un client qui, pour une raison quelconque (bug front, export PDF,
 * back-office tiers...), ne l'échapperait pas correctement.
 * </p>
 *
 * <p>
 * Ne remplace pas l'encodage en sortie côté client — c'est un filet de
 * sécurité supplémentaire, pas la seule ligne de défense.
 * </p>
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {

    String message() default "Ce champ ne peut pas contenir de balisage HTML ou de script";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
