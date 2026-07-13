package ministere.sante.senpna.medicament.infrastructure.web.exception;

import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.DerniereUniteBaseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.exception.famille.CodeFamilleDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleInactiveException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.forme.CodeFormeDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeInactiveException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.medicament.CodeMedicamentDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentInactifException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MedicamentExceptionHandler — mapping des exceptions du module medicament")
class MedicamentExceptionHandlerTest {

    MedicamentExceptionHandler sut = new MedicamentExceptionHandler();

    @Test
    @DisplayName("FamilleIntrouvableException / FormeIntrouvableException / ConditionnementIntrouvableException"
            + " / MedicamentIntrouvableException → 404")
    void introuvables_404() {
        assertThat(sut.handleFamilleIntrouvable(new FamilleIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(sut.handleFormeIntrouvable(new FormeIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(sut.handleConditionnementIntrouvable(new ConditionnementIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(sut.handleMedicamentIntrouvable(new MedicamentIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("codes déjà utilisés (Famille/Forme/Medicament) → 409")
    void codesDejaUtilises_409() {
        assertThat(sut.handleCodeFamilleDejaUtilise(new CodeFamilleDejaUtiliseException("X")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(sut.handleCodeFormeDejaUtilise(new CodeFormeDejaUtiliseException("X")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(sut.handleCodeMedicamentDejaUtilise(new CodeMedicamentDejaUtiliseException("X")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("nom/niveau de conditionnement déjà utilisés → 409")
    void nomEtNiveauConditionnementDejaUtilises_409() {
        assertThat(sut.handleNomConditionnementDejaUtilise(new NomConditionnementDejaUtiliseException("X"))
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(sut.handleNiveauConditionnementDejaUtilise(new NiveauConditionnementDejaUtiliseException(1))
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("états inactifs / règles unité de base → 422")
    void inactifsEtUniteBase_422() {
        assertThat(sut.handleFamilleInactive(new FamilleInactiveException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(sut.handleFormeInactive(new FormeInactiveException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(sut.handleMedicamentInactif(new MedicamentInactifException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(sut.handleDerniereUniteBase(new DerniereUniteBaseException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(sut.handleUniteBaseDejaDefinie(new UniteBaseDejaDefinieException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
