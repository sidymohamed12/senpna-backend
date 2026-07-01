package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public record Phone(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    public Phone {
        Objects.requireNonNull(value, "Le numéro de téléphone ne peut pas être null");
        value = value.trim();
        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Numéro de téléphone invalide (format international attendu, ex: +221771234567) : " + value);
        }
    }

    public static Phone of(String value) {
        return new Phone(value);
    }
}
