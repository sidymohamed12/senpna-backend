package ministere.sante.senpna.fournisseur.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Fournisseur — agrégat de domaine")
class FournisseurTest {

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un fournisseur actif, avec le nom nettoyé des espaces")
        void creer_succes() {
            Fournisseur fournisseur = Fournisseur.creer("  Pharma Plus  ", "Dakar", "+221771234567",
                    "contact@pharmaplus.sn", "Awa Fall");

            assertThat(fournisseur.isActif()).isTrue();
            assertThat(fournisseur.getNom()).isEqualTo("Pharma Plus");
            assertThat(fournisseur.getId()).isNotNull();
        }

        @Test
        @DisplayName("nom null → NullPointerException")
        void nomNull_leveException() {
            assertThatThrownBy(() -> Fournisseur.creer(null, "Dakar", null, null, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nom vide ou blanc → IllegalArgumentException")
        void nomVide_leveException() {
            assertThatThrownBy(() -> Fournisseur.creer("   ", "Dakar", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom de plus de 150 caractères → IllegalArgumentException")
        void nomTropLong_leveException() {
            String nomTropLong = "A".repeat(151);

            assertThatThrownBy(() -> Fournisseur.creer(nomTropLong, null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom de exactement 150 caractères → accepté")
        void nomExactement150_accepte() {
            String nom = "A".repeat(150);

            assertThat(Fournisseur.creer(nom, null, null, null, null).getNom()).hasSize(150);
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour tous les champs et l'horodatage de mise à jour")
        void metAJourChampsEtHorodatage() {
            Fournisseur fournisseur = Fournisseur.creer("Ancien nom", "Ancienne adresse", "111", "a@a.sn", "X");
            var updatedAtAvant = fournisseur.getUpdatedAt();

            fournisseur.modifierInformations("Nouveau nom", "Nouvelle adresse", "222", "b@b.sn", "Y");

            assertThat(fournisseur.getNom()).isEqualTo("Nouveau nom");
            assertThat(fournisseur.getAdresse()).isEqualTo("Nouvelle adresse");
            assertThat(fournisseur.getTelephone()).isEqualTo("222");
            assertThat(fournisseur.getEmail()).isEqualTo("b@b.sn");
            assertThat(fournisseur.getContactPrincipal()).isEqualTo("Y");
            assertThat(fournisseur.getUpdatedAt()).isAfterOrEqualTo(updatedAtAvant);
        }

        @Test
        @DisplayName("nom invalide → IllegalArgumentException, aucun champ modifié")
        void nomInvalide_leveExceptionSansModifier() {
            Fournisseur fournisseur = Fournisseur.creer("Nom initial", "Adresse", null, null, null);

            assertThatThrownBy(() -> fournisseur.modifierInformations("   ", "X", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(fournisseur.getNom()).isEqualTo("Nom initial");
        }
    }

    @Nested
    @DisplayName("activer() / desactiver()")
    class ActiverDesactiver {

        @Test
        @DisplayName("désactiver un fournisseur actif → passe à inactif")
        void desactiver_passeInactif() {
            Fournisseur fournisseur = Fournisseur.creer("Nom", null, null, null, null);

            fournisseur.desactiver();

            assertThat(fournisseur.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer un fournisseur déjà actif → idempotent, ne lève pas d'erreur")
        void activerDejaActif_idempotent() {
            Fournisseur fournisseur = Fournisseur.creer("Nom", null, null, null, null);

            fournisseur.activer();

            assertThat(fournisseur.isActif()).isTrue();
        }

        @Test
        @DisplayName("désactiver un fournisseur déjà inactif → idempotent")
        void desactiverDejaInactif_idempotent() {
            Fournisseur fournisseur = Fournisseur.creer("Nom", null, null, null, null);
            fournisseur.desactiver();

            fournisseur.desactiver();

            assertThat(fournisseur.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer un fournisseur inactif → repasse actif")
        void activerInactif_repasseActif() {
            Fournisseur fournisseur = Fournisseur.creer("Nom", null, null, null, null);
            fournisseur.desactiver();

            fournisseur.activer();

            assertThat(fournisseur.isActif()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement un fournisseur depuis un état persisté")
        void reconstruit_etatFidele() {
            java.time.Instant maintenant = java.time.Instant.now();
            Fournisseur fournisseur = Fournisseur.reconstruct(
                    ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId.generate(),
                    "Nom", "Adresse", "Tel", "Email", "Contact", false, maintenant, maintenant);

            assertThat(fournisseur.getNom()).isEqualTo("Nom");
            assertThat(fournisseur.isActif()).isFalse();
            assertThat(fournisseur.getCreatedAt()).isEqualTo(maintenant);
        }
    }
}
