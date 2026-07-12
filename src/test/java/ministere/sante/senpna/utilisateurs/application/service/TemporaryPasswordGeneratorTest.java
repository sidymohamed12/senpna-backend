package ministere.sante.senpna.utilisateurs.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TemporaryPasswordGenerator — génération de mots de passe temporaires")
class TemporaryPasswordGeneratorTest {

    TemporaryPasswordGenerator sut = new TemporaryPasswordGenerator();

    @RepeatedTest(20)
    @DisplayName("génère toujours un mot de passe de 14 caractères")
    void longueurToujours14() {
        assertThat(sut.generer()).hasSize(14);
    }

    @RepeatedTest(20)
    @DisplayName("contient toujours au moins une majuscule, une minuscule, un chiffre et un caractère spécial")
    void contientChaqueCategorie() {
        String motDePasse = sut.generer();

        assertThat(motDePasse).containsPattern("[A-Z]")
                .containsPattern("[a-z]")
                .containsPattern("[0-9]")
                .matches(".*[!@#$%^&*\\-_=+].*");
    }

    @Test
    @DisplayName("exclut les caractères ambigus à l'oral (I, O, l, 0, 1)")
    void excludesCaracteresAmbigus() {
        for (int i = 0; i < 50; i++) {
            String motDePasse = sut.generer();
            assertThat(motDePasse).doesNotContain("I", "O", "l", "0", "1");
        }
    }

    @Test
    @DisplayName("génère des mots de passe différents à chaque appel")
    void genereMotsDePasseDifferents() {
        Set<String> generes = new HashSet<>();
        IntStream.range(0, 30).forEach(i -> generes.add(sut.generer()));

        assertThat(generes).hasSize(30);
    }
}
