package ministere.sante.senpna.appeloffre.infrastructure.persistence.adapter;

import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper.AppelOffreMapper;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({ AppelOffreRepositoryAdapter.class, AppelOffreMapper.class })
@DisplayName("AppelOffreRepositoryAdapter — persistance avec H2")
class AppelOffreRepositoryAdapterTest {

    @Autowired
    AppelOffreRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    private LigneAppelOffre ligne(String designation) {
        return LigneAppelOffre.creer(MedicamentId.generate(), designation, BigDecimal.TEN, "Comprimé");
    }

    private AppelOffre nouvelAppelOffre(String reference, String objet) {
        return AppelOffre.creer(reference, objet, LocalDate.now().plusDays(10), List.of(ligne("Amoxicilline")));
    }

    @Nested
    @DisplayName("save() / findById()")
    class SaveEtFindById {

        @Test
        @DisplayName("save() persiste l'agrégat et ses lignes, findById() les recompose")
        void save_persisteEtRecompose() {
            AppelOffre appelOffre = nouvelAppelOffre("AO-2026-0001", "Achat Amoxicilline");

            sut.save(appelOffre);
            entityManager.flush();
            entityManager.clear();

            Optional<AppelOffre> result = sut.findById(appelOffre.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getReference()).isEqualTo("AO-2026-0001");
            assertThat(result.get().getLignes()).hasSize(1);
            assertThat(result.get().getLignes().get(0).getDesignation()).isEqualTo("Amoxicilline");
        }

        @Test
        @DisplayName("findById() sur un identifiant inconnu → vide")
        void findById_inconnu_vide() {
            assertThat(sut.findById(AppelOffreId.generate())).isEmpty();
        }

        @Test
        @DisplayName("save() sur le même identifiant remplace intégralement l'ancien jeu de lignes")
        void save_remplaceLesLignes() {
            AppelOffreId id = AppelOffreId.generate();
            Instant maintenant = Instant.now();

            AppelOffre premiereVersion = AppelOffre.reconstruct(id, "AO-2026-0002", "Objet",
                    LocalDate.now().plusDays(10), StatutAppelOffre.BROUILLON, List.of(ligne("Paracétamol")),
                    maintenant, maintenant);
            sut.save(premiereVersion);
            entityManager.flush();
            entityManager.clear();

            AppelOffre deuxiemeVersion = AppelOffre.reconstruct(id, "AO-2026-0002", "Objet",
                    LocalDate.now().plusDays(10), StatutAppelOffre.BROUILLON,
                    List.of(ligne("Amoxicilline"), ligne("Ceftriaxone")), maintenant, maintenant);
            sut.save(deuxiemeVersion);
            entityManager.flush();
            entityManager.clear();

            AppelOffre relu = sut.findById(id).orElseThrow();
            assertThat(relu.getLignes()).hasSize(2);
            assertThat(relu.getLignes()).extracting(LigneAppelOffre::getDesignation)
                    .containsExactlyInAnyOrder("Amoxicilline", "Ceftriaxone");
        }
    }

    @Nested
    @DisplayName("existsByReferenceIgnoreCase()")
    class ExistsByReference {

        @Test
        @DisplayName("insensible à la casse")
        void insensibleCasse() {
            sut.save(nouvelAppelOffre("AO-2026-0003", "Objet"));
            entityManager.flush();

            assertThat(sut.existsByReferenceIgnoreCase("ao-2026-0003")).isTrue();
            assertThat(sut.existsByReferenceIgnoreCase("AO-2026-9999")).isFalse();
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @BeforeEach
        void setUp() {
            AppelOffre publie = nouvelAppelOffre("AO-2026-0010", "Achat Amoxicilline");
            publie.publier();
            sut.save(publie);

            sut.save(nouvelAppelOffre("AO-2026-0011", "Achat Paracétamol"));

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("filtre par statut")
        void filtreParStatut() {
            PageResult<AppelOffre> result = sut.search(
                    new AppelOffreSearchCriteria(null, StatutAppelOffre.PUBLIE), PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(AppelOffre::getReference).containsExactly("AO-2026-0010");
        }

        @Test
        @DisplayName("filtre par texte sur la référence ou l'objet, insensible à la casse")
        void filtreParTexte() {
            PageResult<AppelOffre> result = sut.search(new AppelOffreSearchCriteria("paracétamol", null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(AppelOffre::getReference).containsExactly("AO-2026-0011");
        }

        @Test
        @DisplayName("aucun critère → tous les appels d'offres")
        void aucunCritere_tousLesAppelsOffres() {
            PageResult<AppelOffre> result = sut.search(new AppelOffreSearchCriteria(null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("recherche texte vide (blanche) → traitée comme absente, aucun filtrage")
        void rechercheBlanche_traiteeCommeAbsente() {
            PageResult<AppelOffre> result = sut.search(new AppelOffreSearchCriteria("   ", null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("texte ET statut combinés → seuls les AO satisfaisant les deux critères")
        void texteEtStatutCombines() {
            PageResult<AppelOffre> result = sut.search(
                    new AppelOffreSearchCriteria("Amoxicilline", StatutAppelOffre.PUBLIE),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(AppelOffre::getReference).containsExactly("AO-2026-0010");
        }

        @Test
        @DisplayName("texte ET statut combinés, sans correspondance commune → aucun résultat")
        void texteEtStatutCombines_sansCorrespondance() {
            PageResult<AppelOffre> result = sut.search(
                    new AppelOffreSearchCriteria("Paracétamol", StatutAppelOffre.PUBLIE),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isZero();
        }

        @Test
        @DisplayName("champ de tri non autorisé → repli silencieux sur createdAt")
        void triNonAutorise_repliSurCreatedAt() {
            PageResult<AppelOffre> result = sut.search(new AppelOffreSearchCriteria(null, null),
                    PageRequest.of(0, 20, "champInexistant;DROP TABLE", "ASC"));

            assertThat(result.totalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findPubliesAvecClotureDepassee()")
    class FindPubliesAvecClotureDepassee {

        @Test
        @DisplayName("retourne les AO publiés dont la date de clôture est dépassée")
        void retourneLesAoExpires() {
            LigneAppelOffre ligne = ligne("Amoxicilline");
            Instant maintenant = Instant.now();
            AppelOffre expire = AppelOffre.reconstruct(AppelOffreId.generate(), "AO-EXPIRE", "Objet",
                    LocalDate.now().minusDays(1), StatutAppelOffre.PUBLIE, List.of(ligne), maintenant, maintenant);
            sut.save(expire);

            AppelOffre nonExpire = nouvelAppelOffre("AO-NON-EXPIRE", "Objet");
            nonExpire.publier();
            sut.save(nonExpire);

            entityManager.flush();
            entityManager.clear();

            List<AppelOffre> result = sut.findPubliesAvecClotureDepassee();

            assertThat(result).extracting(AppelOffre::getReference).containsExactly("AO-EXPIRE");
        }

        @Test
        @DisplayName("un AO en BROUILLON avec date dépassée n'est jamais retourné")
        void brouillonDatePassee_jamaisRetourne() {
            LigneAppelOffre ligne = ligne("Amoxicilline");
            Instant maintenant = Instant.now();
            AppelOffre brouillonPasse = AppelOffre.reconstruct(AppelOffreId.generate(), "AO-BROUILLON", "Objet",
                    LocalDate.now().minusDays(1), StatutAppelOffre.BROUILLON, List.of(ligne), maintenant, maintenant);
            sut.save(brouillonPasse);
            entityManager.flush();
            entityManager.clear();

            assertThat(sut.findPubliesAvecClotureDepassee()).isEmpty();
        }
    }
}
