package ministere.sante.senpna.shared.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Implémentation de {@link NoHtml}.
 *
 * <p>
 * Détecte : toute balise HTML ({@code <...>}), les attributs
 * gestionnaires d'évènements ({@code on*=}), ainsi que les URI
 * {@code javascript:} et {@code data:text/html}. Une valeur {@code null}
 * ou vide est toujours valide — la présence/obligation du champ relève
 * des annotations {@code @NotBlank}/{@code @NotNull} dédiées.
 * </p>
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    private static final Pattern HTML_TAG = Pattern.compile("<\\s*[a-zA-Z!/]");
    private static final Pattern EVENT_HANDLER = Pattern.compile("(?i)\\bon\\w+\\s*=");
    private static final Pattern SCRIPT_URI = Pattern.compile("(?i)(javascript|vbscript):|data:text/html");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !HTML_TAG.matcher(value).find()
                && !EVENT_HANDLER.matcher(value).find()
                && !SCRIPT_URI.matcher(value).find();
    }
}
