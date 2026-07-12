package ministere.sante.senpna.utilisateurs.infrastructure.web.exception;

import ministere.sante.senpna.utilisateurs.domain.exception.ActeurNonAffecteException;
import ministere.sante.senpna.utilisateurs.domain.exception.AutoDesactivationInterditeException;
import ministere.sante.senpna.utilisateurs.domain.exception.ConflitTypeEntrepotException;
import ministere.sante.senpna.utilisateurs.domain.exception.CreationRoleReserveeException;
import ministere.sante.senpna.utilisateurs.domain.exception.DernierRoleException;
import ministere.sante.senpna.utilisateurs.domain.exception.EmailDejaUtiliseException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotNonApplicableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.GestionUtilisateurInterditeException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleDejaAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.RoleNonAssigneException;
import ministere.sante.senpna.utilisateurs.domain.exception.TypeEntrepotIncompatibleException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UtilisateursExceptionHandler — mapping des exceptions du module utilisateurs")
class UtilisateursExceptionHandlerTest {

    UtilisateursExceptionHandler sut = new UtilisateursExceptionHandler();

    @Test
    @DisplayName("RoleIntrouvableException → 404")
    void roleIntrouvable_404() {
        assertThat(sut.handleRoleIntrouvable(new RoleIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("EntrepotIntrouvableException → 404")
    void entrepotIntrouvable_404() {
        assertThat(sut.handleEntrepotIntrouvable(new EntrepotIntrouvableException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("RoleNonAssigneException → 404")
    void roleNonAssigne_404() {
        assertThat(sut.handleRoleNonAssigne(new RoleNonAssigneException()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("EmailDejaUtiliseException → 409")
    void emailDejaUtilise_409() {
        assertThat(sut.handleEmailDejaUtilise(new EmailDejaUtiliseException("x@y.z")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("RoleDejaAssigneException → 409")
    void roleDejaAssigne_409() {
        assertThat(sut.handleRoleDejaAssigne(new RoleDejaAssigneException()).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("GestionUtilisateurInterditeException → 403")
    void gestionInterdite_403() {
        assertThat(sut.handleGestionInterdite(new GestionUtilisateurInterditeException()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("CreationRoleReserveeException → 422")
    void creationRoleReservee_422() {
        assertThat(sut.handleCreationRoleReservee(new CreationRoleReserveeException("GESTIONNAIRE_STRUCTURE"))
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("EntrepotInactifException → 422")
    void entrepotInactif_422() {
        assertThat(sut.handleEntrepotInactif(new EntrepotInactifException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("EntrepotNonApplicableException → 400")
    void entrepotNonApplicable_400() {
        assertThat(sut.handleEntrepotNonApplicable(new EntrepotNonApplicableException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("EntrepotRequisException → 400")
    void entrepotRequis_400() {
        assertThat(sut.handleEntrepotRequis(new EntrepotRequisException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("ConflitTypeEntrepotException → 400")
    void conflitTypeEntrepot_400() {
        assertThat(sut.handleConflitTypeEntrepot(new ConflitTypeEntrepotException()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("TypeEntrepotIncompatibleException → 422")
    void typeEntrepotIncompatible_422() {
        assertThat(sut.handleTypeEntrepotIncompatible(new TypeEntrepotIncompatibleException("PRA"))
                .getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("DernierRoleException → 422")
    void dernierRole_422() {
        assertThat(sut.handleDernierRole(new DernierRoleException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("AutoDesactivationInterditeException → 422")
    void autoDesactivationInterdite_422() {
        assertThat(sut.handleAutoDesactivationInterdite(new AutoDesactivationInterditeException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("ActeurNonAffecteException → 422")
    void acteurNonAffecte_422() {
        assertThat(sut.handleActeurNonAffecte(new ActeurNonAffecteException()).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("le corps de la réponse porte le code d'erreur métier de l'exception")
    void corpsPorteCodeErreur() {
        assertThat(sut.handleRoleIntrouvable(new RoleIntrouvableException()).getBody())
                .containsEntry("type", "ROLE_NOT_FOUND");
    }
}
