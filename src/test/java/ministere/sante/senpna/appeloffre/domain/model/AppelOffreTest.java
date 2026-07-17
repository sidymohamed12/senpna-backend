package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutAppelOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AppelOffre — agrégat de domaine")
class AppelOffreTest {

    private LigneAppelOffre ligne() {
        return LigneAppelOffre.creer(MedicamentId.generate(), "Amoxicilline 500 mg", BigDecimal.TEN, "Comprimé");
    }

    private AppelOffre appelOffreBrouillon() {
        return AppelOffre.creer(new AppelOffre.CreationCommand("AO-2026-0001", "Achat Amoxicilline", LocalDate.now().plusDays(30),
                List.of(ligne())));
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée un appel d'offres BROUILLON avec la référence nettoyée")
        void creer_succes() {
            AppelOffre appelOffre = AppelOffre.creer(new AppelOffre.CreationCommand("  AO-2026-0001  ", "Objet", LocalDate.now().plusDays(10),
                    List.of(ligne())));

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.BROUILLON);
            assertThat(appelOffre.getReference()).isEqualTo("AO-2026-0001");
            assertThat(appelOffre.getId()).isNotNull();
        }

        @Test
        @DisplayName("sans ligne → IllegalArgumentException")
        void sansLigne_leveException() {
            var date = LocalDate.now().plusDays(10);
            assertThatThrownBy(() -> AppelOffre.creer(new AppelOffre.CreationCommand("AO-1", "Objet", date, List.of())))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("date de clôture non future → IllegalArgumentException")
        void dateClotureNonFuture_leveException() {
            var now = LocalDate.now();
            var listeLigne = List.of(ligne());
            assertThatThrownBy(
                    () -> AppelOffre.creer(new AppelOffre.CreationCommand("AO-1", "Objet", now, listeLigne)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("référence vide → IllegalArgumentException")
        void referenceVide_leveException() {
            var date = LocalDate.now().plusDays(5);
            var listeLigne = List.of(ligne());
            assertThatThrownBy(
                    () -> AppelOffre.creer(new AppelOffre.CreationCommand("   ", "Objet", date, listeLigne)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("objet vide → IllegalArgumentException")
        void objetVide_leveException() {
            var date = LocalDate.now().plusDays(5);
            var listeLigne = List.of(ligne());
            assertThatThrownBy(
                    () -> AppelOffre.creer(new AppelOffre.CreationCommand("AO-1", "   ", date, listeLigne)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("publier()")
    class Publier {

        @Test
        @DisplayName("depuis BROUILLON → PUBLIE")
        void depuisBrouillon_passePublie() {
            AppelOffre appelOffre = appelOffreBrouillon();

            appelOffre.publier();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.PUBLIE);
        }

        @Test
        @DisplayName("depuis PUBLIE → TransitionStatutAppelOffreInvalideException")
        void depuisPublie_leveException() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();

            assertThatThrownBy(appelOffre::publier)
                    .isInstanceOf(TransitionStatutAppelOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("cloturer()")
    class Cloturer {

        @Test
        @DisplayName("depuis PUBLIE → CLOTURE")
        void depuisPublie_passeCloture() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();

            appelOffre.cloturer();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.CLOTURE);
        }

        @Test
        @DisplayName("depuis BROUILLON → TransitionStatutAppelOffreInvalideException")
        void depuisBrouillon_leveException() {
            AppelOffre appelOffre = appelOffreBrouillon();

            assertThatThrownBy(appelOffre::cloturer)
                    .isInstanceOf(TransitionStatutAppelOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("attribuer()")
    class Attribuer {

        @Test
        @DisplayName("depuis CLOTURE → ATTRIBUE")
        void depuisCloture_passeAttribue() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();
            appelOffre.cloturer();

            appelOffre.attribuer();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.ATTRIBUE);
        }

        @Test
        @DisplayName("depuis PUBLIE → TransitionStatutAppelOffreInvalideException")
        void depuisPublie_leveException() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();

            assertThatThrownBy(appelOffre::attribuer)
                    .isInstanceOf(TransitionStatutAppelOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("annuler()")
    class Annuler {

        @Test
        @DisplayName("depuis BROUILLON → ANNULE")
        void depuisBrouillon_passeAnnule() {
            AppelOffre appelOffre = appelOffreBrouillon();

            appelOffre.annuler();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.ANNULE);
        }

        @Test
        @DisplayName("depuis PUBLIE → ANNULE")
        void depuisPublie_passeAnnule() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();

            appelOffre.annuler();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.ANNULE);
        }

        @Test
        @DisplayName("depuis CLOTURE → ANNULE")
        void depuisCloture_passeAnnule() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();
            appelOffre.cloturer();

            appelOffre.annuler();

            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.ANNULE);
        }

        @Test
        @DisplayName("depuis ATTRIBUE → TransitionStatutAppelOffreInvalideException")
        void depuisAttribue_leveException() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();
            appelOffre.cloturer();
            appelOffre.attribuer();

            assertThatThrownBy(appelOffre::annuler)
                    .isInstanceOf(TransitionStatutAppelOffreInvalideException.class);
        }

        @Test
        @DisplayName("depuis ANNULE → TransitionStatutAppelOffreInvalideException")
        void depuisAnnule_leveException() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.annuler();

            assertThatThrownBy(appelOffre::annuler)
                    .isInstanceOf(TransitionStatutAppelOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("estOuvertALaSoumission() / dateClotureDepassee()")
    class Ouverture {

        @Test
        @DisplayName("PUBLIE avec date de clôture future → ouvert à la soumission")
        void publieDateFuture_ouvert() {
            AppelOffre appelOffre = appelOffreBrouillon();
            appelOffre.publier();

            assertThat(appelOffre.estOuvertALaSoumission()).isTrue();
            assertThat(appelOffre.dateClotureDepassee()).isFalse();
        }

        @Test
        @DisplayName("BROUILLON → jamais ouvert à la soumission")
        void brouillon_nonOuvert() {
            assertThat(appelOffreBrouillon().estOuvertALaSoumission()).isFalse();
        }

        @Test
        @DisplayName("dateClotureDepassee() → false tant que la date n'est pas dépassée")
        void dateClotureDepassee_falseSiNonDepassee() {
            assertThat(appelOffreBrouillon().dateClotureDepassee()).isFalse();
        }

        @Test
        @DisplayName("dateClotureDepassee() → true une fois la date dépassée")
        void dateClotureDepassee_trueSiDepassee() {
            LigneAppelOffre ligne = ligne();
            Instant maintenant = Instant.now();
            AppelOffre expire = AppelOffre.builder()
                .id(AppelOffreId.generate())
                .reference("AO-1")
                .objet("Objet")
                .dateCloture(LocalDate.now().minusDays(1))
                .statut(StatutAppelOffre.PUBLIE)
                .lignes(List.of(ligne))
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();

            assertThat(expire.dateClotureDepassee()).isTrue();
            assertThat(expire.estOuvertALaSoumission()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals() / hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("délègue à AggregateRoot — même identifiant → égaux")
        void memeIdentifiant_egaux() {
            AppelOffre appelOffre = appelOffreBrouillon();
            AppelOffre appelOffre2 = appelOffre;

            assertThat(appelOffre).isEqualTo(appelOffre2)
                    .hasSameHashCodeAs(appelOffre2);
        }

        @Test
        @DisplayName("identifiants différents → non égaux")
        void identifiantsDifferents_nonEgaux() {
            assertThat(appelOffreBrouillon()).isNotEqualTo(appelOffreBrouillon());
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement un appel d'offres depuis un état persisté")
        void reconstruit_etatFidele() {
            Instant maintenant = Instant.now();
            AppelOffreId id = AppelOffreId.generate();

            AppelOffre appelOffre = AppelOffre.builder()
                .id(id)
                .reference("AO-2026-0099")
                .objet("Objet")
                .dateCloture(LocalDate.now().plusDays(5))
                .statut(StatutAppelOffre.PUBLIE)
                .lignes(List.of(ligne()))
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();

            assertThat(appelOffre.getId()).isEqualTo(id);
            assertThat(appelOffre.getStatut()).isEqualTo(StatutAppelOffre.PUBLIE);
            assertThat(appelOffre.getLignes()).hasSize(1);
            assertThat(appelOffre.getCreatedAt()).isEqualTo(maintenant);
        }

        @Test
        @DisplayName("getLignes() retourne une liste non modifiable")
        void lignesNonModifiables() {
            AppelOffre appelOffre = appelOffreBrouillon();
            var ligne = ligne();
            var lignes = appelOffre.getLignes();

            assertThatThrownBy(() -> lignes.add(ligne))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
