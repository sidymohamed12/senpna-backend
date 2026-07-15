package ministere.sante.senpna.commandeachat.infrastructure.web.exception;

import ministere.sante.senpna.commandeachat.domain.exception.AccesCommandeAchatRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.AccesFactureRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.CommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.LigneCommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.ReferenceCommandeAchatDejaUtiliseeException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutCommandeAchatInvalideException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutFactureInvalideException;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandeAchatExceptionHandler — mapping des exceptions du module commandeachat")
class CommandeAchatExceptionHandlerTest {

    CommandeAchatExceptionHandler sut = new CommandeAchatExceptionHandler();

    @Test
    @DisplayName("CommandeAchatIntrouvableException → 404")
    void commandeIntrouvable_404() {
        var response = sut.handleCommandeIntrouvable(new CommandeAchatIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "COMMANDE_ACHAT_NOT_FOUND");
    }

    @Test
    @DisplayName("LigneCommandeAchatIntrouvableException → 404")
    void ligneIntrouvable_404() {
        var response = sut.handleLigneIntrouvable(new LigneCommandeAchatIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "LIGNE_COMMANDE_ACHAT_NOT_FOUND");
    }

    @Test
    @DisplayName("FactureIntrouvableException → 404")
    void factureIntrouvable_404() {
        var response = sut.handleFactureIntrouvable(new FactureIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "FACTURE_NOT_FOUND");
    }

    @Test
    @DisplayName("ReferenceCommandeAchatDejaUtiliseeException → 409")
    void referenceDejaUtilisee_409() {
        var response = sut.handleReferenceDejaUtilisee(new ReferenceCommandeAchatDejaUtiliseeException("BC-2026-0001"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("type", "COMMANDE_ACHAT_REFERENCE_ALREADY_USED");
        assertThat(response.getBody().get("message").toString()).contains("BC-2026-0001");
    }

    @Test
    @DisplayName("AccesCommandeAchatRefuseException → 403")
    void accesCommandeRefuse_403() {
        var response = sut.handleAccesCommandeRefuse(new AccesCommandeAchatRefuseException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("type", "COMMANDE_ACHAT_ACCESS_DENIED");
    }

    @Test
    @DisplayName("AccesFactureRefuseException → 403")
    void accesFactureRefuse_403() {
        var response = sut.handleAccesFactureRefuse(new AccesFactureRefuseException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("type", "FACTURE_ACCESS_DENIED");
    }

    @Test
    @DisplayName("TransitionStatutCommandeAchatInvalideException → 422")
    void transitionCommandeInvalide_422() {
        var response = sut.handleTransitionCommandeInvalide(
                new TransitionStatutCommandeAchatInvalideException(StatutCommandeAchat.EN_ATTENTE_VALIDATION,
                        "réceptionner"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "COMMANDE_ACHAT_TRANSITION_INVALID");
    }

    @Test
    @DisplayName("TransitionStatutFactureInvalideException → 422")
    void transitionFactureInvalide_422() {
        var response = sut.handleTransitionFactureInvalide(
                new TransitionStatutFactureInvalideException(StatutFacture.SOUMISE, "payer"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).containsEntry("type", "FACTURE_TRANSITION_INVALID");
    }
}
