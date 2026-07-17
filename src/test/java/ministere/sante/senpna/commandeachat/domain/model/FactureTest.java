package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutFactureInvalideException;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Facture — agrégat de domaine")
class FactureTest {

    private Facture facture(FournisseurId fournisseurId) {
        return Facture.soumettre(new Facture.SoumissionCommand(CommandeAchatId.generate(), fournisseurId, "FAC-2026-0001",
                BigDecimal.valueOf(3_500_000), LocalDate.now(), LocalDate.now().plusDays(30), "media-1"));
    }

    @Nested
    @DisplayName("soumettre()")
    class Soumettre {

        @Test
        @DisplayName("crée une facture SOUMISE")
        void soumettre_succes() {
            Facture facture = facture(FournisseurId.generate());

            assertThat(facture.getStatut()).isEqualTo(StatutFacture.SOUMISE);
            assertThat(facture.getNumeroFacture()).isEqualTo("FAC-2026-0001");
        }

        @Test
        @DisplayName("montant nul ou négatif → IllegalArgumentException")
        void montantInvalide_leveException() {

            var fournisseurId = FournisseurId.generate();
            var commandeAchatId = CommandeAchatId.generate();
            var now = LocalDate.now();

            assertThatThrownBy(() -> Facture.soumettre(new Facture.SoumissionCommand(commandeAchatId, fournisseurId,
                    "FAC-1", BigDecimal.ZERO, now, null, null)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("numéro de facture vide → IllegalArgumentException")
        void numeroFactureVide_leveException() {

            var fournisseurId = FournisseurId.generate();
            var commandeAchatId = CommandeAchatId.generate();
            var now = LocalDate.now();

            assertThatThrownBy(() -> Facture.soumettre(new Facture.SoumissionCommand(commandeAchatId, fournisseurId, "   ",
                    BigDecimal.TEN, now, null, null))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("valider() / rejeter() / marquerPayee()")
    class Transitions {

        @Test
        @DisplayName("valider() depuis SOUMISE → VALIDEE")
        void valider_depuisSoumise() {
            Facture facture = facture(FournisseurId.generate());

            facture.valider();

            assertThat(facture.getStatut()).isEqualTo(StatutFacture.VALIDEE);
        }

        @Test
        @DisplayName("rejeter() enregistre le motif et passe REJETEE")
        void rejeter_enregistreMotif() {
            Facture facture = facture(FournisseurId.generate());

            facture.rejeter("Montant incohérent");

            assertThat(facture.getStatut()).isEqualTo(StatutFacture.REJETEE);
            assertThat(facture.getMotifRejet()).isEqualTo("Montant incohérent");
        }

        @Test
        @DisplayName("marquerPayee() depuis VALIDEE → PAYEE")
        void marquerPayee_depuisValidee() {
            Facture facture = facture(FournisseurId.generate());
            facture.valider();

            facture.marquerPayee();

            assertThat(facture.getStatut()).isEqualTo(StatutFacture.PAYEE);
        }

        @Test
        @DisplayName("marquerPayee() depuis SOUMISE (sans validation) → TransitionStatutFactureInvalideException")
        void marquerPayee_depuisSoumise_leveException() {
            Facture facture = facture(FournisseurId.generate());

            assertThatThrownBy(facture::marquerPayee)
                    .isInstanceOf(TransitionStatutFactureInvalideException.class);
        }

        @Test
        @DisplayName("valider() une facture déjà validée → TransitionStatutFactureInvalideException")
        void valider_depuisValidee_leveException() {
            Facture facture = facture(FournisseurId.generate());
            facture.valider();

            assertThatThrownBy(facture::valider).isInstanceOf(TransitionStatutFactureInvalideException.class);
        }
    }

    @Nested
    @DisplayName("appartientA()")
    class AppartientA {

        @Test
        @DisplayName("même fournisseur → true")
        void memeFournisseur_true() {
            FournisseurId fournisseurId = FournisseurId.generate();
            Facture facture = facture(fournisseurId);

            assertThat(facture.appartientA(fournisseurId)).isTrue();
        }

        @Test
        @DisplayName("fournisseur différent → false")
        void fournisseurDifferent_false() {
            Facture facture = facture(FournisseurId.generate());

            assertThat(facture.appartientA(FournisseurId.generate())).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une facture depuis un état persisté")
        void reconstruit_etatFidele() {
            Instant maintenant = Instant.now();
            FactureId id = FactureId.generate();

            Facture facture = Facture.builder()
                .id(id)
                .commandeAchatId(CommandeAchatId.generate())
                .fournisseurId(FournisseurId.generate())
                .numeroFacture("FAC-2026-0099")
                .montant(BigDecimal.TEN)
                .dateEmission(LocalDate.now())
                .dateEcheance(null)
                .pieceJointeMediaId(null)
                .statut(StatutFacture.PAYEE)
                .motifRejet(null)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();

            assertThat(facture.getId()).isEqualTo(id);
            assertThat(facture.getStatut()).isEqualTo(StatutFacture.PAYEE);
        }
    }
}
