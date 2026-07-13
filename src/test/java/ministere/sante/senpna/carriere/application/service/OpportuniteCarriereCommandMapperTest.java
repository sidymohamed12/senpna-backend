package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.exception.StatutOpportuniteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.TypeContratInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpportuniteCarriereCommandMapper — conversion type de contrat / statut")
class OpportuniteCarriereCommandMapperTest {

    OpportuniteCarriereCommandMapper sut = new OpportuniteCarriereCommandMapper();

    @Nested
    @DisplayName("versTypeContrat() / versTypeContratOptionnel()")
    class VersTypeContrat {

        @Test
        @DisplayName("valeur valide, insensible à la casse")
        void valeurValide() {
            assertThat(sut.versTypeContrat("cdi")).isEqualTo(TypeContrat.CDI);
        }

        @Test
        @DisplayName("null → TypeContratInvalideException")
        void null_leveException() {
            assertThatThrownBy(() -> sut.versTypeContrat(null)).isInstanceOf(TypeContratInvalideException.class);
        }

        @Test
        @DisplayName("valeur hors énumération → TypeContratInvalideException")
        void horsEnumeration_leveException() {
            assertThatThrownBy(() -> sut.versTypeContrat("INEXISTANT"))
                    .isInstanceOf(TypeContratInvalideException.class);
        }

        @Test
        @DisplayName("optionnel : null → null, sans exception")
        void optionnelNull_renvoieNull() {
            assertThat(sut.versTypeContratOptionnel(null)).isNull();
        }
    }

    @Nested
    @DisplayName("versStatutOptionnel()")
    class VersStatutOptionnel {

        @Test
        @DisplayName("valeur valide → statut résolu")
        void valeurValide() {
            assertThat(sut.versStatutOptionnel("OUVERT")).isEqualTo(StatutOpportunite.OUVERT);
        }

        @Test
        @DisplayName("valeur invalide → StatutOpportuniteInvalideException")
        void valeurInvalide_leveException() {
            assertThatThrownBy(() -> sut.versStatutOptionnel("INEXISTANT"))
                    .isInstanceOf(StatutOpportuniteInvalideException.class);
        }

        @Test
        @DisplayName("null → null")
        void null_renvoieNull() {
            assertThat(sut.versStatutOptionnel(null)).isNull();
        }
    }
}
