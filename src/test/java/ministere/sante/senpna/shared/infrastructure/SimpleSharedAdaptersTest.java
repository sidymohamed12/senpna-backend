package ministere.sante.senpna.shared.infrastructure;

import ministere.sante.senpna.shared.infrastructure.mail.LoggingAccountMailAdapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("RandomUuidGeneratorAdapter / LoggingAccountMailAdapter")
class SimpleSharedAdaptersTest {

    @Test
    @DisplayName("RandomUuidGeneratorAdapter.generate() renvoie un UUID valide, différent à chaque appel")
    void generate_uuidValideEtDifferent() {
        RandomUuidGeneratorAdapter sut = new RandomUuidGeneratorAdapter();

        Set<UUID> generes = new HashSet<>();
        IntStream.range(0, 20).forEach(i -> generes.add(sut.generate()));

        assertThat(generes).hasSize(20);
        generes.forEach(u -> assertThat(u).isNotNull());
    }

    @Test
    @DisplayName("LoggingAccountMailAdapter.envoyerIdentifiantsCompte() journalise sans lever d'exception")
    void loggingAccountMail_neLeveRien() {
        LoggingAccountMailAdapter sut = new LoggingAccountMailAdapter();

        assertThatCode(() -> sut.envoyerIdentifiantsCompte("a@a.sn", "Diallo", "Awa", "Mdp@Temp1234!"))
                .doesNotThrowAnyException();
    }
}
