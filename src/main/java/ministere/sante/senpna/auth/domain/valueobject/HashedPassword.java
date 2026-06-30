package ministere.sante.senpna.auth.domain.valueobject;

import java.util.Objects;

public record HashedPassword(String value) {

    public HashedPassword {
        Objects.requireNonNull(value, "Le hash de mot de passe ne peut pas être null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Le hash de mot de passe ne peut pas être vide");
        }
    }

    public static HashedPassword of(String hash) {
        return new HashedPassword(hash);
    }
}
