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
            Fournisseur fournisseur = Fournisseur
                    .creer(new Fournisseur.CreationCommand("  Pharma Plus  ", "Dakar", "+221771234567",
                            "contact@pharmaplus.sn", "Awa Fall"));

            assertThat(fournisseur.isActif()).isTrue();
            assertThat(fournisseur.getNom()).isEqualTo("Pharma Plus");
            assertThat(fournisseur.getId()).isNotNull();
        }

        @Test
        @DisplayName("nom null → NullPointerException")
        void nomNull_leveException() {
            var creationCommand = new Fournisseur.CreationCommand(null, "Dakar", null, null, null);
            assertThatThrownBy(() -> Fournisseur.creer(creationCommand))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("nom vide ou blanc → IllegalArgumentException")
        void nomVide_leveException() {
            var creationCommand = new Fournisseur.CreationCommand("   ", "Dakar", null, null, null);
            assertThatThrownBy(() -> Fournisseur.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom de plus de 150 caractères → IllegalArgumentException")
        void nomTropLong_leveException() {
            String nomTropLong = "A".repeat(151);
            var creationCommand = new Fournisseur.CreationCommand(nomTropLong, null, null, null, null);
            assertThatThrownBy(() -> Fournisseur.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom de exactement 150 caractères → accepté")
        void nomExactement150_accepte() {
            String nom = "A".repeat(150);
            var creationCommand = new Fournisseur.CreationCommand(nom, null, null, null, null);
            assertThat(Fournisseur.creer(creationCommand).getNom()).hasSize(150);
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour tous les champs et l'horodatage de mise à jour")
        void metAJourChampsEtHorodatage() {
            var creationCommand = new Fournisseur.CreationCommand("Ancien nom", "Ancienne adresse", "111", "a@a.sn",
                    "X");
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);
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
            var creationCommand = new Fournisseur.CreationCommand("Nom initial", "Adresse", null, null, null);
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);

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
            var creationCommand = new Fournisseur.CreationCommand("Nom", null, null, null, null);
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);

            fournisseur.desactiver();

            assertThat(fournisseur.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer un fournisseur déjà actif → idempotent, ne lève pas d'erreur")
        void activerDejaActif_idempotent() {
            var creationCommand = new Fournisseur.CreationCommand("Nom", null, null, null, null);
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);

            fournisseur.activer();

            assertThat(fournisseur.isActif()).isTrue();
        }

        @Test
        @DisplayName("désactiver un fournisseur déjà inactif → idempotent")
        void desactiverDejaInactif_idempotent() {
            var creationCommand = new Fournisseur.CreationCommand("Nom", null, null, null, null);
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);
            fournisseur.desactiver();

            fournisseur.desactiver();

            assertThat(fournisseur.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer un fournisseur inactif → repasse actif")
        void activerInactif_repasseActif() {
            var creationCommand = new Fournisseur.CreationCommand("Nom", null, null, null, null);
            Fournisseur fournisseur = Fournisseur.creer(creationCommand);
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
            Fournisseur fournisseur = Fournisseur.builder()
                    .id(ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId.generate())
                    .nom("Nom")
                    .adresse("Adresse")
                    .telephone("Tel")
                    .email("Email")
                    .contactPrincipal("Contact")
                    .actif(false)
                    .createdAt(maintenant)
                    .updatedAt(maintenant)
                    .build();

            assertThat(fournisseur.getNom()).isEqualTo("Nom");
            assertThat(fournisseur.isActif()).isFalse();
            assertThat(fournisseur.getCreatedAt()).isEqualTo(maintenant);
        }
    }
}
