package ministere.sante.senpna.commandeachat.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AvisExpedition — value object")
class AvisExpeditionTest {

    @Test
    @DisplayName("crée un avis d'expédition valide")
    void of_succes() {
        AvisExpedition avis = AvisExpedition.of(LocalDate.now(), "DHL", "TRACK-123", LocalDate.now().plusDays(5));

        assertThat(avis.getTransporteur()).isEqualTo("DHL");
        assertThat(avis.getNumeroSuivi()).isEqualTo("TRACK-123");
    }

    @Test
    @DisplayName("date d'expédition null → NullPointerException")
    void dateExpeditionNull_leveException() {
        var now = LocalDate.now();
        assertThatThrownBy(() -> AvisExpedition.of(null, "DHL", "TRACK-123", now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("date de livraison estimée avant la date d'expédition → IllegalArgumentException")
    void dateLivraisonAvantExpedition_leveException() {
        LocalDate expedition = LocalDate.now();
        var dateAvant = expedition.minusDays(1);

        assertThatThrownBy(() -> AvisExpedition.of(expedition, "DHL", "TRACK-123", dateAvant))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("transporteur et numéro de suivi optionnels")
    void transporteurEtSuiviOptionnels() {
        AvisExpedition avis = AvisExpedition.of(LocalDate.now(), null, null, null);

        assertThat(avis.getTransporteur()).isNull();
        assertThat(avis.getNumeroSuivi()).isNull();
    }

    @Test
    @DisplayName("equals()/hashCode() basés sur la valeur")
    void equalsHashCode_baseSurValeur() {
        LocalDate date = LocalDate.now();
        AvisExpedition avis1 = AvisExpedition.of(date, "DHL", "T1", null);
        AvisExpedition avis2 = AvisExpedition.of(date, "DHL", "T1", null);

        assertThat(avis1).isEqualTo(avis2)
                .hasSameHashCodeAs(avis2);
    }
}
