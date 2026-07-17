package ministere.sante.senpna.medicament.domain.model;

import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Medicament — agrégat de domaine")
class MedicamentTest {

    private static final FormeId FORME_ID = FormeId.generate();
    private static final FamilleId FAMILLE_ID = FamilleId.generate();

    private static Medicament creerParacetamol() {
        return Medicament.creer(
                new Medicament.CreationCommand("para500", "Doliprane", "Paracétamol", "500 mg", FORME_ID, FAMILLE_ID,
                        VoieAdministration.ORALE, TemperatureConservation.AMBIANTE, null, 30, false, "Sanofi", 20000,
                        200000));
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un médicament actif, avec le code normalisé en majuscules")
        void creer_succes_medicamentActif() {
            Medicament medicament = creerParacetamol();

            assertThat(medicament.isActif()).isTrue();
            assertThat(medicament.getCode()).isEqualTo("PARA500");
            assertThat(medicament.getNomCommercial()).isEqualTo("Doliprane");
            assertThat(medicament.getDci()).isEqualTo("Paracétamol");
            assertThat(medicament.getFormeId()).isEqualTo(FORME_ID);
            assertThat(medicament.getFamilleId()).isEqualTo(FAMILLE_ID);
            assertThat(medicament.getTemperatureConservation()).isEqualTo(TemperatureConservation.AMBIANTE);
            assertThat(medicament.isNecessiteOrdonnance()).isFalse();
        }

        @Test
        @DisplayName("température de conservation absente → défaut AMBIANTE")
        void creer_sansTemperature_defautAmbiante() {
            Medicament medicament = Medicament
                    .creer(new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", FORME_ID, FAMILLE_ID, null,
                            null, null, null, false, null, null, null));

            assertThat(medicament.getTemperatureConservation()).isEqualTo(TemperatureConservation.AMBIANTE);
        }

        @Test
        @DisplayName("code invalide → IllegalArgumentException")
        void creer_codeInvalide_leveException() {
            var creationCommand = new Medicament.CreationCommand("code invalide !", "Nom", "DCI", "10 mg", FORME_ID,
                    FAMILLE_ID, null, null, null, null, false, null, null, null);
            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("formeId null → NullPointerException")
        void creer_formeIdNull_leveException() {
            var creationCommand = new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", null, FAMILLE_ID, null,
                    null,
                    null, null, false, null, null, null);
            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("familleId null → NullPointerException")
        void creer_familleIdNull_leveException() {
            var creationCommand = new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", FORME_ID, null, null,
                    null,
                    null, null, false, null, null, null);
            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("délai d'approvisionnement négatif → IllegalArgumentException")
        void creer_delaiNegatif_leveException() {
            var creationCommand = new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", FORME_ID, FAMILLE_ID,
                    null,
                    null, null, -1, false, null, null, null);
            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("stock minimum supérieur au stock maximum → IllegalArgumentException")
        void creer_seuilsIncoherents_leveException() {
            var creationCommand = new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", FORME_ID, FAMILLE_ID,
                    null,
                    null, null, null, false, null, 100, 50);
            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("seuil minimum");
        }

        @Test
        @DisplayName("stock minimum négatif → IllegalArgumentException")
        void creer_stockMinimumNegatif_leveException() {
            var creationCommand = new Medicament.CreationCommand("CODE1", "Nom", "DCI", "10 mg", FORME_ID, FAMILLE_ID,
                    null,
                    null, null, null, false, null, -5, 100);

            assertThatThrownBy(() -> Medicament.creer(creationCommand))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("archiver() / desarchiver()")
    class ArchiverDesarchiver {

        @Test
        @DisplayName("archiver() puis desarchiver() → médicament de nouveau actif")
        void archiverPuisDesarchiver_redevientActif() {
            Medicament medicament = creerParacetamol();

            medicament.archiver();
            assertThat(medicament.isActif()).isFalse();

            medicament.desarchiver();
            assertThat(medicament.isActif()).isTrue();
        }

        @Test
        @DisplayName("archiver() déjà archivé est sans effet (idempotent)")
        void archiver_dejaArchive_idempotent() {
            Medicament medicament = creerParacetamol();
            medicament.archiver();
            Instant updatedAtApresArchivage = medicament.getUpdatedAt();

            medicament.archiver();

            assertThat(medicament.getUpdatedAt()).isEqualTo(updatedAtApresArchivage);
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour les informations sans toucher au code")
        void modifierInformations_succes() {
            Medicament medicament = creerParacetamol();
            FormeId nouvelleFormeId = FormeId.generate();
            FamilleId nouvelleFamilleId = FamilleId.generate();

            medicament.modifierInformations("Efferalgan", "Paracétamol", "1000 mg", nouvelleFormeId,
                    nouvelleFamilleId, VoieAdministration.ORALE, TemperatureConservation.AMBIANTE, "Programme X",
                    15, true, "UPSA", 1000, 5000);

            assertThat(medicament.getNomCommercial()).isEqualTo("Efferalgan");
            assertThat(medicament.getDosage()).isEqualTo("1000 mg");
            assertThat(medicament.getFormeId()).isEqualTo(nouvelleFormeId);
            assertThat(medicament.getFamilleId()).isEqualTo(nouvelleFamilleId);
            assertThat(medicament.isNecessiteOrdonnance()).isTrue();
            assertThat(medicament.getCode()).isEqualTo("PARA500");
        }

        @Test
        @DisplayName("seuils incohérents → IllegalArgumentException")
        void modifierInformations_seuilsIncoherents_leveException() {
            Medicament medicament = creerParacetamol();

            assertThatThrownBy(() -> medicament.modifierInformations("Nom", "DCI", "10mg", FORME_ID, FAMILLE_ID,
                    null, null, null, null, false, null, 500, 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement un médicament depuis des valeurs de persistance")
        void reconstruct_restaureEtatComplet() {
            MedicamentId id = MedicamentId.generate();
            Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
            Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");

            Medicament medicament = Medicament.builder()
                    .id(id)
                    .code("AMOX500")
                    .nomCommercial("Amoxicilline Sandoz")
                    .dci("Amoxicilline")
                    .dosage("500 mg")
                    .formeId(FORME_ID)
                    .familleId(FAMILLE_ID)
                    .voieAdministration(VoieAdministration.ORALE)
                    .temperatureConservation(TemperatureConservation.AMBIANTE)
                    .programmeSante(null)
                    .delaiApprovisionnementJours(45)
                    .necessiteOrdonnance(true)
                    .fabricant("Sandoz")
                    .stockMinimum(5000)
                    .stockMaximum(80000)
                    .actif(false)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            assertThat(medicament.getId()).isEqualTo(id);
            assertThat(medicament.isActif()).isFalse();
            assertThat(medicament.getCreatedAt()).isEqualTo(createdAt);
            assertThat(medicament.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }
}
