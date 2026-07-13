package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.exception.CiviliteInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CandidatureCommandMapper — conversion de la civilité")
class CandidatureCommandMapperTest {

    CandidatureCommandMapper sut = new CandidatureCommandMapper();

    @Test
    @DisplayName("valeur simple valide")
    void valeurSimple() {
        assertThat(sut.versCivilite("m")).isEqualTo(Civilite.M);
    }

    @Test
    @DisplayName("valeur avec point (\"M.\") est normalisée")
    void valeurAvecPoint_normalisee() {
        assertThat(sut.versCivilite("M.")).isEqualTo(Civilite.M);
        assertThat(sut.versCivilite("Mme.")).isEqualTo(Civilite.MME);
    }

    @Test
    @DisplayName("null → CiviliteInvalideException")
    void null_leveException() {
        assertThatThrownBy(() -> sut.versCivilite(null)).isInstanceOf(CiviliteInvalideException.class);
    }

    @Test
    @DisplayName("vide/blanc → CiviliteInvalideException")
    void vide_leveException() {
        assertThatThrownBy(() -> sut.versCivilite("  ")).isInstanceOf(CiviliteInvalideException.class);
    }

    @Test
    @DisplayName("valeur hors énumération → CiviliteInvalideException")
    void horsEnumeration_leveException() {
        assertThatThrownBy(() -> sut.versCivilite("DR")).isInstanceOf(CiviliteInvalideException.class);
    }
}
