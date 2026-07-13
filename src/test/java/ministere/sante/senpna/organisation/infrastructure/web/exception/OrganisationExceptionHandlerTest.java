package ministere.sante.senpna.organisation.infrastructure.web.exception;

import ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException;
import ministere.sante.senpna.organisation.domain.exception.CodeEntrepotDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.CodeRegionDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.CodeStructureSanitaireDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrganisationExceptionHandler — mapping des exceptions du module organisation")
class OrganisationExceptionHandlerTest {

    OrganisationExceptionHandler sut = new OrganisationExceptionHandler();

    @Test
    @DisplayName("RegionIntrouvableException → 404")
    void regionIntrouvable_404() {
        assertThat(sut.handleRegionIntrouvable(new RegionIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("StructureSanitaireIntrouvableException → 404")
    void structureIntrouvable_404() {
        assertThat(sut.handleStructureIntrouvable(new StructureSanitaireIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("EntrepotIntrouvableException → 404")
    void entrepotIntrouvable_404() {
        assertThat(sut.handleEntrepotIntrouvable(new EntrepotIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("CodeRegionDejaUtiliseException → 409")
    void codeRegionDejaUtilise_409() {
        assertThat(sut.handleCodeRegionDejaUtilise(new CodeRegionDejaUtiliseException("THIES")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("CodeStructureSanitaireDejaUtiliseException → 409")
    void codeStructureDejaUtilise_409() {
        assertThat(sut.handleCodeStructureDejaUtilise(new CodeStructureSanitaireDejaUtiliseException("PS-1"))
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("CodeEntrepotDejaUtiliseException → 409")
    void codeEntrepotDejaUtilise_409() {
        assertThat(sut.handleCodeEntrepotDejaUtilise(new CodeEntrepotDejaUtiliseException("PRA-1")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("AccesRegionRefuseException → 403")
    void accesRegionRefuse_403() {
        assertThat(sut.handleAccesRegionRefuse(new AccesRegionRefuseException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("RegionInactiveException → 422")
    void regionInactive_422() {
        assertThat(sut.handleRegionInactive(new RegionInactiveException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("EntrepotInactifException → 422")
    void entrepotInactif_422() {
        assertThat(sut.handleEntrepotInactif(new EntrepotInactifException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("StructureSanitaireNonValideeException → 422")
    void structureNonValidee_422() {
        assertThat(sut.handleStructureNonValidee(new StructureSanitaireNonValideeException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("DemandeAdhesionDejaTraiteeException → 422")
    void demandeAdhesionDejaTraitee_422() {
        assertThat(sut.handleDemandeAdhesionDejaTraitee(new DemandeAdhesionDejaTraiteeException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("TypeEntrepotInvalideException → 422")
    void typeEntrepotInvalide_422() {
        assertThat(sut.handleTypeEntrepotInvalide(new TypeEntrepotInvalideException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
