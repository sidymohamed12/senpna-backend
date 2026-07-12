package ministere.sante.senpna.shared.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Value Objects — shared domain")
class ValueObjectsTest {

    // ══════════════════════════════════════════════════════════════════════
    // HashedPassword
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("HashedPassword")
    class HashedPasswordTest {

        @Test
        @DisplayName("accepte un hash valide")
        void of_hashValide() {
            String hash = "$2a$12$somevalidhashXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
            HashedPassword hp = HashedPassword.of(hash);
            assertThat(hp.value()).isEqualTo(hash);
        }

        @Test
        @DisplayName("lève NullPointerException si null")
        void of_null_leve_npe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> HashedPassword.of(null));
        }

        @Test
        @DisplayName("lève IllegalArgumentException si vide")
        void of_vide_leve_iae() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HashedPassword.of(""));
        }

        @Test
        @DisplayName("lève IllegalArgumentException si blanc")
        void of_blanc_leve_iae() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HashedPassword.of("   "));
        }

        @Test
        @DisplayName("égalité structurelle sur la valeur")
        void egalite_structurelle() {
            HashedPassword hp1 = HashedPassword.of("$2a$12$hash");
            HashedPassword hp2 = HashedPassword.of("$2a$12$hash");
            assertThat(hp1).isEqualTo(hp2);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Phone
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Phone")
    class PhoneTest {

        @ParameterizedTest(name = "{0}")
        @ValueSource(strings = {
                "+221771234567", // Sénégal mobile
                "+221338207000", // Sénégal fixe
                "+33612345678", // France
                "+12015551234", // USA
        })
        @DisplayName("accepte les formats internationaux valides")
        void of_formatsValides(String numero) {
            assertThatNoException().isThrownBy(() -> Phone.of(numero));
            assertThat(Phone.of(numero).value()).isEqualTo(numero);
        }

        @ParameterizedTest(name = "[{index}] \"{0}\"")
        @ValueSource(strings = {
                "0771234567", // sans préfixe +
                "+0771234567", // + suivi de 0
                "221771234567", // sans +
                "+221", // trop court
                "+221771234567890123", // trop long
                "",
                "abc",
        })
        @DisplayName("rejette les formats invalides")
        void of_formatsInvalides(String numero) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Phone.of(numero));
        }

        @Test
        @DisplayName("lève NullPointerException si null")
        void of_null_leve_npe() {
            assertThatNullPointerException().isThrownBy(() -> Phone.of(null));
        }

        @Test
        @DisplayName("trim les espaces avant validation — le numéro est accepté et la valeur est normalisée")
        void of_trimmeAvantValidation() {
            // Phone trimme value avant la validation du pattern :
            // " +221771234567 " → "+221771234567" (valide)
            Phone phone = Phone.of("  +221771234567  ");
            assertThat(phone.value()).isEqualTo("+221771234567");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Nom
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Nom")
    class NomTest {

        @Test
        @DisplayName("accepte une valeur non vide")
        void of_valeurValide() {
            Nom nom = Nom.of("Diallo");
            assertThat(nom.getValue()).isEqualTo("Diallo");
        }

        @Test
        @DisplayName("lève une exception si null")
        void of_null_leve_exception() {
            assertThatException().isThrownBy(() -> Nom.of(null));
        }

        @Test
        @DisplayName("lève une exception si vide")
        void of_vide_leve_exception() {
            assertThatException().isThrownBy(() -> Nom.of(""));
        }

        @Test
        @DisplayName("lève une exception si blanc")
        void of_blanc_leve_exception() {
            assertThatException().isThrownBy(() -> Nom.of("   "));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Prenom
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Prenom")
    class PrenomTest {

        @Test
        @DisplayName("accepte une valeur non vide")
        void of_valeurValide() {
            Prenom prenom = Prenom.of("Mamadou");
            assertThat(prenom.getValue()).isEqualTo("Mamadou");
        }

        @Test
        @DisplayName("lève une exception si null")
        void of_null_leve_exception() {
            assertThatException().isThrownBy(() -> Prenom.of(null));
        }

        @Test
        @DisplayName("lève une exception si blank")
        void of_blank_leve_exception() {
            assertThatException().isThrownBy(() -> Prenom.of("  "));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UserId
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("UserId")
    class UserIdTest {

        @Test
        @DisplayName("of(UUID) encapsule l'UUID")
        void of_uuid() {
            java.util.UUID uuid = java.util.UUID.randomUUID();
            UserId id = UserId.of(uuid);
            assertThat(id.getValue()).isEqualTo(uuid);
        }

        @Test
        @DisplayName("of(String) parse l'UUID depuis une chaîne")
        void of_string() {
            String uuidStr = "11111111-1111-1111-1111-111111111111";
            UserId id = UserId.of(uuidStr);
            assertThat(id.getValue().toString()).hasToString(uuidStr);
        }

        @Test
        @DisplayName("generate() produit un UserId non null unique à chaque appel")
        void generate_unique() {
            UserId id1 = UserId.generate();
            UserId id2 = UserId.generate();
            assertThat(id1).isNotNull();
            assertThat(id1.getValue()).isNotEqualTo(id2.getValue());
        }
    }
}
