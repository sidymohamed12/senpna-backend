package ministere.sante.senpna.medicament.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.criteria.ConditionnementSearchCriteria;
import ministere.sante.senpna.medicament.domain.criteria.FamilleSearchCriteria;
import ministere.sante.senpna.medicament.domain.criteria.FormeSearchCriteria;
import ministere.sante.senpna.medicament.domain.criteria.MedicamentSearchCriteria;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.ConditionnementMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FamilleMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.FormeMapper;
import ministere.sante.senpna.medicament.infrastructure.persistence.mapper.MedicamentMapper;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ FamilleRepositoryAdapter.class, FamilleMapper.class, FamilleQueryAdapter.class,
        FormeRepositoryAdapter.class, FormeMapper.class, FormeQueryAdapter.class,
        MedicamentRepositoryAdapter.class, MedicamentMapper.class, MedicamentQueryAdapter.class,
        ConditionnementRepositoryAdapter.class, ConditionnementMapper.class, ConditionnementQueryAdapter.class })
@DisplayName("Adaptateurs de persistence medicament — H2")
class MedicamentPersistenceAdaptersTest {

    @Autowired
    FamilleRepositoryAdapter familleAdapter;
    @Autowired
    FamilleQueryAdapter familleQueryAdapter;
    @Autowired
    FormeRepositoryAdapter formeAdapter;
    @Autowired
    FormeQueryAdapter formeQueryAdapter;
    @Autowired
    MedicamentRepositoryAdapter medicamentAdapter;
    @Autowired
    MedicamentQueryAdapter medicamentQueryAdapter;
    @Autowired
    ConditionnementRepositoryAdapter conditionnementAdapter;
    @Autowired
    ConditionnementQueryAdapter conditionnementQueryAdapter;
    @Autowired
    TestEntityManager entityManager;

    Famille famille;
    Forme forme;
    Medicament medicament;

    @BeforeEach
    void setUp() {
        famille = Famille.creer("ANTIBIO", "Antibiotiques", null);
        forme = Forme.creer("COMP", "Comprimé", null);
        familleAdapter.save(famille);
        formeAdapter.save(forme);

        medicament = Medicament.creer(new Medicament.CreationCommand("MED-1", "Zolpidem", "Zolpidem", "10mg", forme.getId(), famille.getId(),
                VoieAdministration.ORALE, null, null, null, false, "Sanofi", null, null));
        medicamentAdapter.save(medicament);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("FamilleRepositoryAdapter / FamilleQueryAdapter")
    class FamilleTests {

        @Test
        @DisplayName("existsByCodeIgnoreCase() insensible à la casse")
        void existsByCodeIgnoreCase() {
            assertThat(familleAdapter.existsByCodeIgnoreCase("antibio")).isTrue();
        }

        @Test
        @DisplayName("existsByCodeIgnoreCaseAndIdNot() exclut l'identifiant donné")
        void existsByCodeIgnoreCaseAndIdNot() {
            assertThat(familleAdapter.existsByCodeIgnoreCaseAndIdNot("ANTIBIO", famille.getId())).isFalse();
        }

        @Test
        @DisplayName("search() filtre par statut actif")
        void search_filtreParActif() {
            PageResult<Famille> result = familleAdapter.search(new FamilleSearchCriteria(null, true),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Famille::getCode).containsExactly("ANTIBIO");
        }

        @Test
        @DisplayName("findAll() (query) renvoie une projection par famille")
        void queryAdapter_findAll() {
            List<FamilleProjection> result = familleQueryAdapter.findAll();

            assertThat(result).extracting(FamilleProjection::code).containsExactly("ANTIBIO");
        }
    }

    @Nested
    @DisplayName("FormeRepositoryAdapter / FormeQueryAdapter")
    class FormeTests {

        @Test
        @DisplayName("search() filtre par texte")
        void search_filtreParTexte() {
            PageResult<Forme> result = formeAdapter.search(new FormeSearchCriteria("comp", null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Forme::getCode).containsExactly("COMP");
        }

        @Test
        @DisplayName("findAll() (query) renvoie une projection par forme")
        void queryAdapter_findAll() {
            List<FormeProjection> result = formeQueryAdapter.findAll();

            assertThat(result).extracting(FormeProjection::code).containsExactly("COMP");
        }
    }

    @Nested
    @DisplayName("MedicamentRepositoryAdapter / MedicamentQueryAdapter")
    class MedicamentTests {

        @Test
        @DisplayName("search() filtre par famille")
        void search_filtreParFamille() {
            PageResult<Medicament> result = medicamentAdapter.search(
                    new MedicamentSearchCriteria(null, famille.getId().getValue(), null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Medicament::getCode).containsExactly("MED-1");
        }

        @Test
        @DisplayName("existsByCodeIgnoreCase() insensible à la casse")
        void existsByCodeIgnoreCase() {
            assertThat(medicamentAdapter.existsByCodeIgnoreCase("med-1")).isTrue();
        }

        @Test
        @DisplayName("findAllById() (query) renvoie une projection par médicament trouvé")
        void queryAdapter_findAllById() {
            List<MedicamentProjection> result = medicamentQueryAdapter
                    .findAllById(Set.of(medicament.getId().getValue()));

            assertThat(result).extracting(MedicamentProjection::code).containsExactly("MED-1");
        }
    }

    @Nested
    @DisplayName("ConditionnementRepositoryAdapter / ConditionnementQueryAdapter")
    class ConditionnementTests {

        @Test
        @DisplayName("search() filtre par médicament, existsUniteBaseByMedicamentId() détecte l'unité de base")
        void search_etExistsUniteBase() {
            Conditionnement c = Conditionnement.creer(new Conditionnement.CreationCommand(medicament.getId(), "Boite de 10", 1, BigDecimal.ONE, true,
                    BigDecimal.TEN, new BigDecimal("15")));
            conditionnementAdapter.save(c);
            entityManager.flush();
            entityManager.clear();

            PageResult<Conditionnement> result = conditionnementAdapter.search(
                    new ConditionnementSearchCriteria(medicament.getId().getValue(), null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Conditionnement::getNom).containsExactly("Boite de 10");
            assertThat(conditionnementAdapter.existsUniteBaseByMedicamentId(medicament.getId())).isTrue();
        }

        @Test
        @DisplayName("findAllVendablesByMedicamentIdIn() (query) renvoie une projection par conditionnement actif")
        void queryAdapter_findAllVendables() {
            Conditionnement c = Conditionnement.creer(new Conditionnement.CreationCommand(medicament.getId(), "Boite de 10", 1, BigDecimal.ONE, true,
                    BigDecimal.TEN, new BigDecimal("15")));
            conditionnementAdapter.save(c);
            entityManager.flush();
            entityManager.clear();

            var result = conditionnementQueryAdapter
                    .findAllVendablesByMedicamentIdIn(Set.of(medicament.getId().getValue()));

            assertThat(result).hasSize(1);
        }
    }
}
