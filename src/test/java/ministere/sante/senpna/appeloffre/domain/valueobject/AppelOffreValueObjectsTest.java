package ministere.sante.senpna.appeloffre.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Couvre la surcharge {@code of(String)} de chaque identifiant typé du
 * module — jamais exercée ailleurs, le reste des tests utilisant
 * exclusivement {@code generate()} ou {@code of(UUID)}. Le comportement
 * de parsing lui-même (délégation à {@link
 * ministere.sante.senpna.shared.domain.valueobject.EntityId}) est déjà
 * couvert génériquement par {@code EntityIdTest}.
 */
@DisplayName("Identifiants typés du module appeloffre — surcharge of(String)")
class AppelOffreValueObjectsTest {

    @Test
    @DisplayName("AppelOffreId.of(String) parse l'UUID")
    void appelOffreId_ofString() {
        UUID uuid = UUID.randomUUID();
        assertThat(AppelOffreId.of(uuid.toString()).getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("LigneAppelOffreId.of(String) parse l'UUID")
    void ligneAppelOffreId_ofString() {
        UUID uuid = UUID.randomUUID();
        assertThat(LigneAppelOffreId.of(uuid.toString()).getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("OffreFournisseurId.of(String) parse l'UUID")
    void offreFournisseurId_ofString() {
        UUID uuid = UUID.randomUUID();
        assertThat(OffreFournisseurId.of(uuid.toString()).getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("LigneOffreId.of(String) parse l'UUID")
    void ligneOffreId_ofString() {
        UUID uuid = UUID.randomUUID();
        assertThat(LigneOffreId.of(uuid.toString()).getValue()).isEqualTo(uuid);
    }
}
