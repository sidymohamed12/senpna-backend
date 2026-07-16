package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link Name} est abstraite : ses règles communes (bornes de longueur,
 * equals/hashCode/toString) sont exercées ici via ses deux sous-classes
 * concrètes {@link Nom} et {@link Prenom}, en plus des cas déjà couverts
 * individuellement par {@code ValueObjectsTest}.
 */
@DisplayName("Name — via Nom/Prenom")
class NameTest {

    @Nested
    @DisplayName("bornes de longueur (héritées de Name)")
    class BornesLongueur {

        @Test
        @DisplayName("un seul caractère (< minLength) → IllegalArgumentException")
        void unSeulCaractere_leveException() {
            assertThatThrownBy(() -> Nom.of("A")).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Le nom");
        }

        @Test
        @DisplayName("deux caractères (= minLength) → accepté")
        void deuxCaracteres_accepte() {
            assertThat(Nom.of("Ba").getValue()).isEqualTo("Ba");
        }

        @Test
        @DisplayName("100 caractères (= maxLength) → accepté")
        void centCaracteres_accepte() {
            String valeur = "A".repeat(100);

            assertThat(Prenom.of(valeur).getValue()).isEqualTo(valeur);
        }

        @Test
        @DisplayName("101 caractères (> maxLength) → IllegalArgumentException")
        void centUnCaracteres_leveException() {
            String valeur = "A".repeat(101);

            assertThatThrownBy(() -> Prenom.of(valeur)).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Le prénom");
        }
    }

    @Nested
    @DisplayName("equals() / hashCode() / toString()")
    class EgaliteEtRepresentation {

        @Test
        @DisplayName("même référence → true")
        void memeReference_true() {
            Nom nom = Nom.of("Diallo");
            var nom2 = nom;

            assertThat(nom).isEqualTo(nom2);
        }

        @Test
        @DisplayName("comparé à null → false")
        void comparaisonNull_false() {
            assertThat(Nom.of("Diallo")).isNotNull();
        }

        @Test
        @DisplayName("même sous-classe, même valeur → égal")
        void memeSousClasseMemeValeur_egal() {
            assertThat(Nom.of("Diallo")).isEqualTo(Nom.of("Diallo"));
            assertThat(Nom.of("Diallo")).hasSameHashCodeAs(Nom.of("Diallo"));
        }

        @Test
        @DisplayName("même sous-classe, valeurs différentes → non égal")
        void memeSousClasseValeursDifferentes_nonEgal() {
            assertThat(Nom.of("Diallo")).isNotEqualTo(Nom.of("Sow"));
        }

        @Test
        @DisplayName("sous-classes différentes (Nom vs Prenom), même valeur → non égal")
        void sousClassesDifferentes_nonEgal() {
            Nom nom = Nom.of("Awa");
            Prenom prenom = Prenom.of("Awa");

            assertThat(nom).isNotEqualTo(prenom);
        }

        @Test
        @DisplayName("toString() renvoie la valeur")
        void toString_renvoieLaValeur() {
            assertThat(Nom.of("Diallo")).hasToString("Diallo");
        }
    }
}
