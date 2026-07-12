package ministere.sante.senpna.catalogue.infrastructure.web.exception;

import ministere.sante.senpna.catalogue.domain.exception.AucunePraPourRegionException;
import ministere.sante.senpna.catalogue.domain.exception.CatalogueAccesRefuseException;
import ministere.sante.senpna.catalogue.domain.exception.PnaCentraleIntrouvableException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CatalogueExceptionHandler — mapping des exceptions du module catalogue")
class CatalogueExceptionHandlerTest {

    CatalogueExceptionHandler sut = new CatalogueExceptionHandler();

    @Test
    @DisplayName("PnaCentraleIntrouvableException → 404 avec le type d'erreur attendu")
    void pnaCentraleIntrouvable_404() {
        ResponseEntity<Map<String, Object>> response = sut
                .handlePnaCentraleIntrouvable(new PnaCentraleIntrouvableException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "PNA_CENTRALE_NOT_CONFIGURED");
        assertThat(response.getBody()).containsEntry("status", 404);
    }

    @Test
    @DisplayName("CatalogueAccesRefuseException → 403 avec le type d'erreur attendu")
    void accesRefuse_403() {
        ResponseEntity<Map<String, Object>> response = sut
                .handleAccesRefuse(new CatalogueAccesRefuseException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("type", "CATALOGUE_ACCESS_FORBIDDEN");
    }

    @Test
    @DisplayName("AucunePraPourRegionException → 404 avec le type d'erreur attendu")
    void aucunePraPourRegion_404() {
        ResponseEntity<Map<String, Object>> response = sut
                .handleAucunePraPourRegion(new AucunePraPourRegionException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("type", "PRA_NOT_FOUND_FOR_REGION");
    }

    @Test
    @DisplayName("le message de l'exception est propagé tel quel dans le corps de la réponse")
    void messagePropage() {
        ResponseEntity<Map<String, Object>> response = sut
                .handleAucunePraPourRegion(new AucunePraPourRegionException());

        assertThat(response.getBody()).containsEntry("message", "Aucune PRA active n'est rattachée à cette région");
    }
}
