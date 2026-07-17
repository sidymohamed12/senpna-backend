package ministere.sante.senpna.appeloffre.infrastructure.persistence.adapter;

import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper.OffreFournisseurMapper;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({ OffreFournisseurRepositoryAdapter.class, OffreFournisseurMapper.class })
@DisplayName("OffreFournisseurRepositoryAdapter — persistance avec H2")
class OffreFournisseurRepositoryAdapterTest {

    @Autowired
    OffreFournisseurRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    private LigneOffre ligne() {
        return LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.valueOf(2000), 15);
    }

    private OffreFournisseur nouvelleOffre(AppelOffreId appelOffreId, FournisseurId fournisseurId) {
        return OffreFournisseur.soumettre(new OffreFournisseur.SoumissionCommand(appelOffreId, fournisseurId, "Commentaire", List.of(ligne())));
    }

    @Nested
    @DisplayName("save() / findById()")
    class SaveEtFindById {

        @Test
        @DisplayName("save() persiste l'agrégat et ses lignes, findById() les recompose")
        void save_persisteEtRecompose() {
            OffreFournisseur offre = nouvelleOffre(AppelOffreId.generate(), FournisseurId.generate());

            sut.save(offre);
            entityManager.flush();
            entityManager.clear();

            Optional<OffreFournisseur> result = sut.findById(offre.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getStatut()).isEqualTo(StatutOffre.SOUMISE);
            assertThat(result.get().getLignes()).hasSize(1);
            assertThat(result.get().getLignes().get(0).getPrixUnitaire()).isEqualByComparingTo("2000");
        }

        @Test
        @DisplayName("findById() sur un identifiant inconnu → vide")
        void findById_inconnu_vide() {
            assertThat(sut.findById(OffreFournisseurId.generate())).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByAppelOffreIdAndFournisseurIdAndStatut()")
    class ExistsByAppelOffreIdAndFournisseurIdAndStatut {

        @Test
        @DisplayName("vrai uniquement pour la combinaison AO + fournisseur + statut exacts")
        void vraiPourCombinaisonExacte() {
            AppelOffreId appelOffreId = AppelOffreId.generate();
            FournisseurId fournisseurId = FournisseurId.generate();
            sut.save(nouvelleOffre(appelOffreId, fournisseurId));
            entityManager.flush();

            assertThat(sut.existsByAppelOffreIdAndFournisseurIdAndStatut(appelOffreId, fournisseurId,
                    StatutOffre.SOUMISE)).isTrue();
            assertThat(sut.existsByAppelOffreIdAndFournisseurIdAndStatut(appelOffreId, fournisseurId,
                    StatutOffre.RETENUE)).isFalse();
            assertThat(sut.existsByAppelOffreIdAndFournisseurIdAndStatut(AppelOffreId.generate(), fournisseurId,
                    StatutOffre.SOUMISE)).isFalse();
        }

        @Test
        @DisplayName("après retrait de l'offre, elle n'est plus comptée comme SOUMISE")
        void apresRetrait_neCompteplusCommeSoumise() {
            AppelOffreId appelOffreId = AppelOffreId.generate();
            FournisseurId fournisseurId = FournisseurId.generate();
            OffreFournisseur offre = nouvelleOffre(appelOffreId, fournisseurId);
            offre.retirer();
            sut.save(offre);
            entityManager.flush();

            assertThat(sut.existsByAppelOffreIdAndFournisseurIdAndStatut(appelOffreId, fournisseurId,
                    StatutOffre.SOUMISE)).isFalse();
        }
    }

    @Nested
    @DisplayName("findByAppelOffreId() / findByFournisseurId()")
    class Recherches {

        AppelOffreId appelOffreId1 = AppelOffreId.generate();
        AppelOffreId appelOffreId2 = AppelOffreId.generate();
        FournisseurId fournisseurId1 = FournisseurId.generate();
        FournisseurId fournisseurId2 = FournisseurId.generate();

        @BeforeEach
        void setUp() {
            sut.save(nouvelleOffre(appelOffreId1, fournisseurId1));
            sut.save(nouvelleOffre(appelOffreId1, fournisseurId2));

            OffreFournisseur offreRetenue = nouvelleOffre(appelOffreId2, fournisseurId1);
            offreRetenue.retenir();
            sut.save(offreRetenue);

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("findByAppelOffreId() retourne toutes les offres soumises pour cet AO")
        void findByAppelOffreId_retourneLesOffresDeLAo() {
            PageResult<OffreFournisseur> result = sut.findByAppelOffreId(appelOffreId1, PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.content()).extracting(o -> o.getFournisseurId())
                    .containsExactlyInAnyOrder(fournisseurId1, fournisseurId2);
        }

        @Test
        @DisplayName("findByFournisseurId() sans filtre de statut retourne toutes les offres du fournisseur")
        void findByFournisseurId_sansStatut() {
            PageResult<OffreFournisseur> result = sut.findByFournisseurId(fournisseurId1, null,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("findByFournisseurId() avec filtre de statut ne retourne que les offres correspondantes")
        void findByFournisseurId_avecStatut() {
            PageResult<OffreFournisseur> result = sut.findByFournisseurId(fournisseurId1, StatutOffre.RETENUE,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).getAppelOffreId()).isEqualTo(appelOffreId2);
        }
    }
}
