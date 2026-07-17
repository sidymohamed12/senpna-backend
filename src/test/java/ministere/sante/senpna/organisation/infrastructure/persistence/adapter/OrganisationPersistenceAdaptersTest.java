package ministere.sante.senpna.organisation.infrastructure.persistence.adapter;

import ministere.sante.senpna.organisation.domain.criteria.EntrepotSearchCriteria;
import ministere.sante.senpna.organisation.domain.criteria.StructureSanitaireSearchCriteria;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.EntrepotMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.RegionMapper;
import ministere.sante.senpna.organisation.infrastructure.persistence.mapper.StructureSanitaireMapper;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.RegionProjection;
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
@Import({ RegionRepositoryAdapter.class, RegionMapper.class, RegionQueryAdapter.class,
        EntrepotRepositoryAdapter.class, EntrepotMapper.class, EntrepotQueryAdapter.class,
        StructureSanitaireRepositoryAdapter.class, StructureSanitaireMapper.class,
        StructureSanitaireQueryAdapter.class })
@DisplayName("Adaptateurs de persistence organisation — H2")
class OrganisationPersistenceAdaptersTest {

    @Autowired
    RegionRepositoryAdapter regionAdapter;
    @Autowired
    RegionQueryAdapter regionQueryAdapter;
    @Autowired
    EntrepotRepositoryAdapter entrepotAdapter;
    @Autowired
    EntrepotQueryAdapter entrepotQueryAdapter;
    @Autowired
    StructureSanitaireRepositoryAdapter structureAdapter;
    @Autowired
    StructureSanitaireQueryAdapter structureQueryAdapter;
    @Autowired
    TestEntityManager entityManager;

    Region thies;
    Region dakar;
    Entrepot praThies;
    Entrepot pnaCentral;

    @BeforeEach
    void setUp() {
        thies = Region.creer("THIES", "Thies");
        dakar = Region.creer("DAKAR", "Dakar");
        regionAdapter.save(thies);
        regionAdapter.save(dakar);

        praThies = Entrepot.creerPra(new Entrepot.CreationCommand("PRA-THIES", "PRA Thies", thies.getId(), "Adresse", "771111111"));
        entrepotAdapter.save(praThies);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("RegionRepositoryAdapter / RegionQueryAdapter")
    class RegionTests {

        @Test
        @DisplayName("existsByCode() insensible détecte les codes existants")
        void existsByCode() {
            assertThat(regionAdapter.existsByCode("THIES")).isTrue();
            assertThat(regionAdapter.existsByCode("INEXISTANT")).isFalse();
        }

        @Test
        @DisplayName("findAll() (repository) renvoie toutes les régions")
        void findAll_toutesLesRegions() {
            assertThat(regionAdapter.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("RegionQueryAdapter.findByCode() résout la projection")
        void queryAdapter_findByCode() {
            Optional<RegionProjection> result = regionQueryAdapter.findByCode("DAKAR");

            assertThat(result).isPresent();
            assertThat(result.get().nom()).isEqualTo("Dakar");
        }

        @Test
        @DisplayName("RegionQueryAdapter.existsById() reflète l'existence en base")
        void queryAdapter_existsById() {
            assertThat(regionQueryAdapter.existsById(thies.getId().getValue())).isTrue();
        }
    }

    @Nested
    @DisplayName("EntrepotRepositoryAdapter / EntrepotQueryAdapter")
    class EntrepotTests {

        @Test
        @DisplayName("search() filtre par type")
        void search_filtreParType() {
            PageResult<Entrepot> result = entrepotAdapter.search(
                    new EntrepotSearchCriteria(null, TypeEntrepot.PRA, null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(Entrepot::getCode).containsExactly("PRA-THIES");
        }

        @Test
        @DisplayName("search() filtre par région")
        void search_filtreParRegion() {
            PageResult<Entrepot> result = entrepotAdapter.search(
                    new EntrepotSearchCriteria(null, null, thies.getId().getValue(), null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).hasSize(1);
        }

        @Test
        @DisplayName("existsByCode() détecte les codes existants")
        void existsByCode() {
            assertThat(entrepotAdapter.existsByCode("PRA-THIES")).isTrue();
            assertThat(entrepotAdapter.existsByCode("INEXISTANT")).isFalse();
        }

        @Test
        @DisplayName("EntrepotQueryAdapter.findPrasActives() ne renvoie que les PRA actives")
        void queryAdapter_findPrasActives() {
            List<EntrepotProjection> result = entrepotQueryAdapter.findPrasActives();

            assertThat(result).extracting(EntrepotProjection::code).containsExactly("PRA-THIES");
        }

        @Test
        @DisplayName("EntrepotQueryAdapter.findPrasActivesParRegion() filtre par région")
        void queryAdapter_findPrasActivesParRegion() {
            List<EntrepotProjection> resultThies = entrepotQueryAdapter
                    .findPrasActivesParRegion(thies.getId().getValue());
            List<EntrepotProjection> resultDakar = entrepotQueryAdapter
                    .findPrasActivesParRegion(dakar.getId().getValue());

            assertThat(resultThies).hasSize(1);
            assertThat(resultDakar).isEmpty();
        }

        @Test
        @DisplayName("EntrepotQueryAdapter.findPnaCentraleActive() vide quand aucun PNA central n'existe")
        void queryAdapter_findPnaCentraleActive_videSiAucun() {
            assertThat(entrepotQueryAdapter.findPnaCentraleActive()).isEmpty();
        }
    }

    @Nested
    @DisplayName("StructureSanitaireRepositoryAdapter")
    class StructureSanitaireTests {

        @Test
        @DisplayName("search() filtre par région et par type")
        void search_filtreParRegionEtType() {
            StructureSanitaire structure = StructureSanitaire.creer(new StructureSanitaire.CreationCommand("PS-1", "Poste de sante 1",
                    TypeStructureSanitaire.POSTE_SANTE, thies.getId(), "District", "Adresse", "771111111",
                    "ps1@sante.sn", "Ndiaye", "Fatou"));
            structureAdapter.save(structure);
            entityManager.flush();
            entityManager.clear();

            PageResult<StructureSanitaire> result = structureAdapter.search(
                    new StructureSanitaireSearchCriteria(null, TypeStructureSanitaire.POSTE_SANTE,
                            thies.getId().getValue(), null, null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(StructureSanitaire::getCode).containsExactly("PS-1");
        }

        @Test
        @DisplayName("existsByCode() détecte les codes existants")
        void existsByCode() {
            StructureSanitaire structure = StructureSanitaire.creer(new StructureSanitaire.CreationCommand("PS-2", "Poste de sante 2",
                    TypeStructureSanitaire.POSTE_SANTE, thies.getId(), "District", "Adresse", "771111111",
                    "ps2@sante.sn", "Ndiaye", "Fatou"));
            structureAdapter.save(structure);
            entityManager.flush();
            entityManager.clear();

            assertThat(structureAdapter.existsByCode("PS-2")).isTrue();
        }

        @Test
        @DisplayName("StructureSanitaireQueryAdapter.findById() résout la projection")
        void queryAdapter_findById() {
            StructureSanitaire structure = StructureSanitaire.creer(new StructureSanitaire.CreationCommand("PS-3", "Poste de sante 3",
                    TypeStructureSanitaire.POSTE_SANTE, thies.getId(), "District", "Adresse", "771111111",
                    "ps3@sante.sn", "Ndiaye", "Fatou"));
            structureAdapter.save(structure);
            entityManager.flush();
            entityManager.clear();

            assertThat(structureQueryAdapter.findById(structure.getId().getValue())).isPresent();
        }
    }
}
