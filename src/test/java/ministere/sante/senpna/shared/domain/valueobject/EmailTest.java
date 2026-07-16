package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email")
class EmailTest {

    @Test
    @DisplayName("of() accepte une adresse valide et la normalise (trim + minuscules)")
    void of_adresseValide_normalisee() {
        Email email = Email.of("  Awa.Diallo@Example.COM  ");

        assertThat(email.value()).isEqualTo("awa.diallo@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = { "awa.diallo@example.com", "a@b.co", "prenom.nom+tag@sous.domaine.example.org" })
    @DisplayName("accepte les formats d'e-mail valides")
    void of_formatsValides(String valeur) {
        assertThat(Email.of(valeur)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "pas-un-email", "manque-arobase.com", "@manque-local.com", "manque-domaine@",
            "espace dans@example.com", "double@@example.com" })
    @DisplayName("rejette les formats invalides")
    void of_formatsInvalides_leveException(String valeur) {
        assertThatThrownBy(() -> Email.of(valeur)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valeur null → NullPointerException")
    void of_null_leveNpe() {
        assertThatThrownBy(() -> Email.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("égalité structurelle sur la valeur normalisée")
    void egalite_structurelle() {
        Email a = Email.of("Awa@Example.com");
        Email b = Email.of("awa@example.com");

        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
    }
}
