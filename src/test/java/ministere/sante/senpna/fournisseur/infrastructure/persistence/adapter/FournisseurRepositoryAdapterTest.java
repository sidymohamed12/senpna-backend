package ministere.sante.senpna.fournisseur.infrastructure.persistence.adapter;

import ministere.sante.senpna.fournisseur.domain.criteria.FournisseurSearchCriteria;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.mapper.FournisseurMapper;
import ministere.sante.senpna.fournisseur.infrastructure.persistence.repository.FournisseurJpaRepository;
import ministere.sante.senpna.shared.domain.port.out.FournisseurQueryPort;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ FournisseurRepositoryAdapter.class, FournisseurQueryAdapter.class, FournisseurMapper.class })
@DisplayName("FournisseurRepositoryAdapter / FournisseurQueryAdapter — persistance avec H2")
class FournisseurRepositoryAdapterTest {

    @Autowired
    FournisseurJpaRepository fournisseurJpaRepository;
    @Autowired
    FournisseurRepositoryAdapter sut;
    @Autowired
    FournisseurQueryPort queryAdapter;
    @Autowired
    TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Fournisseur actif1 = Fournisseur.creer("Pharma Plus", "Dakar", "+221771111111", "a@p.sn", "Awa");
        Fournisseur actif2 = Fournisseur.creer("MediSupply", "Thiès", "+221772222222", "b@m.sn", "Bo");
        Fournisseur inactif = Fournisseur.creer("Ancien Fournisseur", "Kaolack", "+221773333333", "c@a.sn", "Ci");
        inactif.desactiver();

        sut.save(actif1);
        sut.save(actif2);
        sut.save(inactif);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("findById() / existsByNomIgnoreCase*")
    class RecherchesUnitaires {

        @Test
        @DisplayName("findById() retrouve un fournisseur persisté")
        void findById_retrouve() {
            Fournisseur nouveau = Fournisseur.creer("Nouveau", null, null, null, null);
            sut.save(nouveau);

            Optional<Fournisseur> result = sut.findById(nouveau.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getNom()).isEqualTo("Nouveau");
        }

        @Test
        @DisplayName("findById() sur un identifiant inconnu → vide")
        void findById_inconnu_vide() {
            assertThat(sut.findById(FournisseurId.generate())).isEmpty();
        }

        @Test
        @DisplayName("existsByNomIgnoreCase() est insensible à la casse")
        void existsByNomIgnoreCase_insensibleCasse() {
            assertThat(sut.existsByNomIgnoreCase("PHARMA PLUS")).isTrue();
            assertThat(sut.existsByNomIgnoreCase("inexistant")).isFalse();
        }

        @Test
        @DisplayName("existsByNomIgnoreCaseAndIdNot() exclut l'identifiant donné")
        void existsByNomIgnoreCaseAndIdNot_excludId() {
            Fournisseur pharmaPlus = fournisseurJpaRepository.findAll().stream()
                    .filter(e -> e.getNom().equals("Pharma Plus"))
                    .findFirst()
                    .map(e -> sut.findById(FournisseurId.of(e.getId())).orElseThrow())
                    .orElseThrow();

            // le même fournisseur, exclu par son propre id → pas de conflit
            assertThat(sut.existsByNomIgnoreCaseAndIdNot("Pharma Plus", pharmaPlus.getId())).isFalse();
            // un autre id existant avec ce nom → conflit
            assertThat(sut.existsByNomIgnoreCaseAndIdNot("Pharma Plus", FournisseurId.generate())).isTrue();
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("filtre par statut actif")
        void filtreParStatutActif() {
            PageResult<Fournisseur> result = sut.search(new FournisseurSearchCriteria(null, true),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Fournisseur::getNom)
                    .containsExactlyInAnyOrder("Pharma Plus", "MediSupply");
        }

        @Test
        @DisplayName("filtre par texte sur le nom, insensible à la casse")
        void filtreParTexteNom() {
            PageResult<Fournisseur> result = sut.search(new FournisseurSearchCriteria("medi", null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Fournisseur::getNom).containsExactly("MediSupply");
        }

        @Test
        @DisplayName("aucun critère → tous les fournisseurs")
        void aucunCritere_tousLesFournisseurs() {
            PageResult<Fournisseur> result = sut.search(FournisseurSearchCriteria.vide(),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("champ de tri non autorisé → repli silencieux sur createdAt")
        void triNonAutorise_repliSurCreatedAt() {
            PageResult<Fournisseur> result = sut.search(FournisseurSearchCriteria.vide(),
                    PageRequest.of(0, 20, "champInexistant;DROP TABLE", "ASC"));

            // ne lève pas d'exception malgré le champ non blanc-listé
            assertThat(result.totalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("tri par nom ascendant")
        void triParNomAscendant() {
            PageResult<Fournisseur> result = sut.search(FournisseurSearchCriteria.vide(),
                    PageRequest.of(0, 20, "nom", "ASC"));

            assertThat(result.content()).extracting(Fournisseur::getNom)
                    .containsExactly("Ancien Fournisseur", "MediSupply", "Pharma Plus");
        }
    }

    @Nested
    @DisplayName("FournisseurQueryAdapter.findAll()")
    class QueryAdapterFindAll {

        @Test
        @DisplayName("renvoie une projection pour chaque fournisseur, actifs et inactifs confondus")
        void renvoieProjectionPourChaqueFournisseur() {
            List<FournisseurProjection> projections = queryAdapter.findAll();

            assertThat(projections).hasSize(3);
            assertThat(projections).extracting(FournisseurProjection::actif)
                    .containsExactlyInAnyOrder(true, true, false);
        }
    }
}
