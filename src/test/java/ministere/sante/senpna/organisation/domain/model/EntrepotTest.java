package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Entrepot — agrégat de domaine")
class EntrepotTest {

    private static final RegionId REGION_ID = RegionId.generate();

    @Nested
    @DisplayName("creerPra()")
    class CreerPra {

        @Test
        @DisplayName("crée une PRA active, de type PRA, rattachée à la région donnée")
        void creerPra_succes_praActive() {
            Entrepot pra = Entrepot.creerPra("pra-thies", "PRA Thiès", REGION_ID, "Route de Dakar", "+221771234567");

            assertThat(pra.isActif()).isTrue();
            assertThat(pra.getType()).isEqualTo(TypeEntrepot.PRA);
            assertThat(pra.estPra()).isTrue();
            assertThat(pra.getCode()).isEqualTo("PRA-THIES");
            assertThat(pra.getRegionId()).isEqualTo(REGION_ID);
            assertThat(pra.getResponsableUserId()).isNull();
        }

        @Test
        @DisplayName("région null → IllegalArgumentException (une PRA doit être rattachée à une région)")
        void creerPra_sansRegion_leveException() {
            assertThatThrownBy(() -> Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("région");
        }

        @Test
        @DisplayName("code invalide → IllegalArgumentException")
        void creerPra_codeInvalide_leveException() {
            assertThatThrownBy(() -> Entrepot.creerPra("code invalide!", "Nom", REGION_ID, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom vide → IllegalArgumentException")
        void creerPra_nomVide_leveException() {
            assertThatThrownBy(() -> Entrepot.creerPra("PRA-DAKAR", " ", REGION_ID, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour nom, adresse, téléphone et région")
        void modifierInformations_succes_metAJourChamps() {
            Entrepot pra = Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", REGION_ID, "Ancienne adresse", "+221700000000");
            RegionId nouvelleRegion = RegionId.generate();

            pra.modifierInformations("PRA Dakar Nord", "Nouvelle adresse", "+221711111111", nouvelleRegion);

            assertThat(pra.getNom()).isEqualTo("PRA Dakar Nord");
            assertThat(pra.getAdresse()).isEqualTo("Nouvelle adresse");
            assertThat(pra.getTelephone()).isEqualTo("+221711111111");
            assertThat(pra.getRegionId()).isEqualTo(nouvelleRegion);
        }

        @Test
        @DisplayName("région null fournie → conserve la région existante (pas d'écrasement accidentel)")
        void modifierInformations_regionNull_conserveRegionExistante() {
            Entrepot pra = Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);

            pra.modifierInformations("PRA Dakar", null, null, null);

            assertThat(pra.getRegionId()).isEqualTo(REGION_ID);
        }
    }

    @Nested
    @DisplayName("activer() / desactiver()")
    class ActiverDesactiver {

        @Test
        @DisplayName("desactiver() une PRA active la rend inactive")
        void desactiver_praActive_devientInactive() {
            Entrepot pra = Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);

            pra.desactiver();

            assertThat(pra.isActif()).isFalse();
        }

        @Test
        @DisplayName("activer() une PRA déjà active est idempotent")
        void activer_dejaActive_idempotent() {
            Entrepot pra = Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);
            Instant updatedAtInitial = pra.getUpdatedAt();

            pra.activer();

            assertThat(pra.isActif()).isTrue();
            assertThat(pra.getUpdatedAt()).isEqualTo(updatedAtInitial);
        }
    }

    @Nested
    @DisplayName("affecterResponsable()")
    class AffecterResponsable {

        @Test
        @DisplayName("affecte un responsable à l'entrepôt")
        void affecterResponsable_succes() {
            Entrepot pra = Entrepot.creerPra("PRA-DAKAR", "PRA Dakar", REGION_ID, null, null);
            UUID responsableId = UUID.randomUUID();

            pra.affecterResponsable(responsableId);

            assertThat(pra.getResponsableUserId()).isEqualTo(responsableId);
        }
    }

    @Nested
    @DisplayName("estPra()")
    class EstPra {

        @Test
        @DisplayName("un entrepôt reconstruit de type PNA_CENTRAL n'est pas une PRA")
        void estPra_pnaCentral_retourneFalse() {
            Entrepot pnaCentral = Entrepot.reconstruct(EntrepotId.generate(), "PNA-CENTRAL", "PNA Centrale",
                    TypeEntrepot.PNA_CENTRAL, null, null, null, null, true, Instant.now(), Instant.now());

            assertThat(pnaCentral.estPra()).isFalse();
        }

        @Test
        @DisplayName("une PRA reconstruite est bien une PRA")
        void estPra_pra_retourneTrue() {
            Entrepot pra = Entrepot.reconstruct(EntrepotId.generate(), "PRA-DAKAR", "PRA Dakar", TypeEntrepot.PRA,
                    REGION_ID, null, null, null, true, Instant.now(), Instant.now());

            assertThat(pra.estPra()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement un entrepôt PNA_CENTRAL sans région")
        void reconstruct_pnaCentral_sansRegion_succes() {
            Entrepot pnaCentral = Entrepot.reconstruct(EntrepotId.generate(), "PNA-CENTRAL", "PNA Centrale",
                    TypeEntrepot.PNA_CENTRAL, null, null, null, null, true, Instant.now(), Instant.now());

            assertThat(pnaCentral.getRegionId()).isNull();
            assertThat(pnaCentral.getType()).isEqualTo(TypeEntrepot.PNA_CENTRAL);
        }

        @Test
        @DisplayName("reconstruit une PRA sans région → IllegalArgumentException (invariant vérifié aussi au reconstruct)")
        void reconstruct_praSansRegion_leveException() {
            var generate = EntrepotId.generate();
            var now = Instant.now();
            var now2 = Instant.now();
            assertThatThrownBy(() -> Entrepot.reconstruct(generate, "PRA-X", "PRA X", TypeEntrepot.PRA, null, null, null, null, true, now, now2))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
