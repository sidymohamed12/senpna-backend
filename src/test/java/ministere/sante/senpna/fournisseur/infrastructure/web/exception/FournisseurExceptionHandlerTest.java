package ministere.sante.senpna.fournisseur.infrastructure.web.exception;

import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FournisseurExceptionHandler — mapping des exceptions du module fournisseur")
class FournisseurExceptionHandlerTest {

    FournisseurExceptionHandler sut = new FournisseurExceptionHandler();

    @Test
    @DisplayName("FournisseurIntrouvableException → 404")
    void fournisseurIntrouvable_404() {
        var response = sut.handleFournisseurIntrouvable(new FournisseurIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "FOURNISSEUR_NOT_FOUND");
    }

    @Test
    @DisplayName("NomFournisseurDejaUtiliseException → 409")
    void nomDejaUtilise_409() {
        var response = sut.handleNomDejaUtilise(new NomFournisseurDejaUtiliseException("Pharma Plus"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("type", "FOURNISSEUR_NOM_ALREADY_USED");
    }
}
