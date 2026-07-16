package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OffreFournisseur — agrégat de domaine")
class OffreFournisseurTest {

    private LigneOffre ligneOffre() {
        return LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.valueOf(2000), 30);
    }

    private OffreFournisseur offreSoumise(FournisseurId fournisseurId) {
        return OffreFournisseur.soumettre(AppelOffreId.generate(), fournisseurId, "Commentaire",
                List.of(ligneOffre()));
    }

    @Nested
    @DisplayName("soumettre()")
    class Soumettre {

        @Test
        @DisplayName("crée une offre SOUMISE")
        void soumettre_succes() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            assertThat(offre.getStatut()).isEqualTo(StatutOffre.SOUMISE);
            assertThat(offre.getId()).isNotNull();
        }

        @Test
        @DisplayName("sans ligne → IllegalArgumentException")
        void sansLigne_leveException() {

            var appelOffreId = AppelOffreId.generate();
            var fournisseurId = FournisseurId.generate();
            assertThatThrownBy(() -> OffreFournisseur.soumettre(appelOffreId, fournisseurId,
                    null, List.of())).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("retirer()")
    class Retirer {

        @Test
        @DisplayName("depuis SOUMISE → RETIREE")
        void depuisSoumise_passeRetiree() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            offre.retirer();

            assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETIREE);
        }

        @Test
        @DisplayName("depuis RETIREE → TransitionStatutOffreInvalideException")
        void depuisRetiree_leveException() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());
            offre.retirer();

            assertThatThrownBy(offre::retirer).isInstanceOf(TransitionStatutOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("retenir() / rejeter()")
    class RetenirRejeter {

        @Test
        @DisplayName("retenir() depuis SOUMISE → RETENUE")
        void retenir_depuisSoumise() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            offre.retenir();

            assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETENUE);
        }

        @Test
        @DisplayName("rejeter() depuis SOUMISE → REJETEE")
        void rejeter_depuisSoumise() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            offre.rejeter();

            assertThat(offre.getStatut()).isEqualTo(StatutOffre.REJETEE);
        }

        @Test
        @DisplayName("retenir() une offre déjà retenue → TransitionStatutOffreInvalideException")
        void retenir_depuisRetenue_leveException() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());
            offre.retenir();

            assertThatThrownBy(offre::retenir).isInstanceOf(TransitionStatutOffreInvalideException.class);
        }

        @Test
        @DisplayName("rejeter() une offre retirée → TransitionStatutOffreInvalideException")
        void rejeter_depuisRetiree_leveException() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());
            offre.retirer();

            assertThatThrownBy(offre::rejeter).isInstanceOf(TransitionStatutOffreInvalideException.class);
        }
    }

    @Nested
    @DisplayName("appartientA()")
    class AppartientA {

        @Test
        @DisplayName("même fournisseur → true")
        void memeFournisseur_true() {
            FournisseurId fournisseurId = FournisseurId.generate();
            OffreFournisseur offre = offreSoumise(fournisseurId);

            assertThat(offre.appartientA(fournisseurId)).isTrue();
        }

        @Test
        @DisplayName("fournisseur différent → false")
        void fournisseurDifferent_false() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            assertThat(offre.appartientA(FournisseurId.generate())).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une offre depuis un état persisté")
        void reconstruit_etatFidele() {
            Instant maintenant = Instant.now();
            OffreFournisseurId id = OffreFournisseurId.generate();
            AppelOffreId appelOffreId = AppelOffreId.generate();
            FournisseurId fournisseurId = FournisseurId.generate();

            OffreFournisseur offre = OffreFournisseur.reconstruct(id, appelOffreId, fournisseurId, "Com",
                    StatutOffre.RETENUE, List.of(ligneOffre()), maintenant, maintenant);

            assertThat(offre.getId()).isEqualTo(id);
            assertThat(offre.getAppelOffreId()).isEqualTo(appelOffreId);
            assertThat(offre.getFournisseurId()).isEqualTo(fournisseurId);
            assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETENUE);
        }
    }

    @Nested
    @DisplayName("equals() / hashCode()")
    class EqualsHashCode {

        @Test
        @DisplayName("délègue à AggregateRoot — même identifiant → égaux, identifiants différents → non égaux")
        void delegueAAggregateRoot() {
            OffreFournisseur offre = offreSoumise(FournisseurId.generate());

            assertThat(offre)
                    .isNotEqualTo(offreSoumise(FournisseurId.generate()))
                    .isNotNull()
                    .isEqualTo(offre);
        }
    }
}
