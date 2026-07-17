package ministere.sante.senpna.projet.infrastructure.persistence.adapter;

import ministere.sante.senpna.projet.domain.criteria.ProjetSearchCriteria;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.projet.infrastructure.persistence.mapper.ProjetMapper;
import ministere.sante.senpna.projet.infrastructure.persistence.repository.ProjetJpaRepository;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ ProjetRepositoryAdapter.class, ProjetMapper.class })
@DisplayName("ProjetRepositoryAdapter — persistance avec H2")
class ProjetRepositoryAdapterTest {

    @Autowired
    ProjetJpaRepository projetJpaRepository;
    @Autowired
    ProjetRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Projet publieSante = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Vaccination rurale", "Desc", List.of(),
                List.of(), null));
        publieSante.publier();

        Projet brouillonSocial = Projet.creer(new Projet.CreationCommand(CategorieProjet.SOCIAL, "Cantines scolaires", "Desc", List.of(),
                List.of(), null));

        Projet archiveSante = Projet.creer(new Projet.CreationCommand(CategorieProjet.SANTE, "Ancienne campagne", "Desc", List.of(),
                List.of(), null));
        archiveSante.archiver();

        sut.save(publieSante);
        sut.save(brouillonSocial);
        sut.save(archiveSante);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("filtre par catégorie")
        void filtreParCategorie() {
            PageResult<Projet> result = sut.search(new ProjetSearchCriteria(null, CategorieProjet.SANTE, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Projet::getNom)
                    .containsExactlyInAnyOrder("Vaccination rurale", "Ancienne campagne");
        }

        @Test
        @DisplayName("filtre par statut")
        void filtreParStatut() {
            PageResult<Projet> result = sut.search(new ProjetSearchCriteria(null, null, StatutProjet.PUBLIE),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Projet::getNom).containsExactly("Vaccination rurale");
        }

        @Test
        @DisplayName("filtre par texte sur le nom")
        void filtreParTexte() {
            PageResult<Projet> result = sut.search(new ProjetSearchCriteria("cantine", null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Projet::getNom).containsExactly("Cantines scolaires");
        }

        @Test
        @DisplayName("combinaison catégorie + statut")
        void combinaisonCategorieEtStatut() {
            PageResult<Projet> result = sut.search(
                    new ProjetSearchCriteria(null, CategorieProjet.SANTE, StatutProjet.ARCHIVE),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Projet::getNom).containsExactly("Ancienne campagne");
        }

        @Test
        @DisplayName("aucun critère → tous les projets")
        void aucunCritere_tous() {
            PageResult<Projet> result = sut.search(ProjetSearchCriteria.vide(), PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("retrouve un projet persisté avec ses listes d'objectifs/impacts")
        void retrouveAvecListes() {
            Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.INNOVATION, "Nom", "Desc", List.of("O1", "O2"),
                    List.of("I1"), null));
            sut.save(projet);
            entityManager.flush();
            entityManager.clear();

            var result = sut.findById(projet.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getObjectifs()).containsExactly("O1", "O2");
        }
    }
}
