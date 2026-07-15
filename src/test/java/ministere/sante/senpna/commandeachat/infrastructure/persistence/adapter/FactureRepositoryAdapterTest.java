package ministere.sante.senpna.commandeachat.infrastructure.persistence.adapter;

import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper.FactureMapper;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ FactureRepositoryAdapter.class, FactureMapper.class })
@DisplayName("FactureRepositoryAdapter — persistance avec H2")
class FactureRepositoryAdapterTest {

    @Autowired
    FactureRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    private Facture nouvelleFacture(String numero, FournisseurId fournisseurId) {
        return Facture.soumettre(CommandeAchatId.generate(), fournisseurId, numero, BigDecimal.valueOf(3_500_000),
                LocalDate.now(), LocalDate.now().plusDays(30), "media-1");
    }

    @Nested
    @DisplayName("save() / findById()")
    class SaveEtFindById {

        @Test
        @DisplayName("save() persiste la facture, findById() la retrouve")
        void save_persiste() {
            Facture facture = nouvelleFacture("FAC-2026-0001", FournisseurId.generate());

            sut.save(facture);
            entityManager.flush();
            entityManager.clear();

            Optional<Facture> result = sut.findById(facture.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getNumeroFacture()).isEqualTo("FAC-2026-0001");
            assertThat(result.get().getStatut()).isEqualTo(StatutFacture.SOUMISE);
        }

        @Test
        @DisplayName("findById() sur un identifiant inconnu → vide")
        void findById_inconnu_vide() {
            assertThat(sut.findById(FactureId.generate())).isEmpty();
        }

        @Test
        @DisplayName("save() sur une facture déjà persistée met à jour son statut")
        void save_metAJourStatut() {
            Facture facture = nouvelleFacture("FAC-2026-0002", FournisseurId.generate());
            sut.save(facture);
            entityManager.flush();
            entityManager.clear();

            Facture relue = sut.findById(facture.getId()).orElseThrow();
            relue.valider();
            sut.save(relue);
            entityManager.flush();
            entityManager.clear();

            assertThat(sut.findById(facture.getId()).orElseThrow().getStatut()).isEqualTo(StatutFacture.VALIDEE);
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @BeforeEach
        void setUp() {
            Facture validee = nouvelleFacture("FAC-2026-0010", FournisseurId.generate());
            validee.valider();
            sut.save(validee);

            sut.save(nouvelleFacture("FAC-2026-0011", FournisseurId.generate()));

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("sans filtre de statut → toutes les factures")
        void sansStatut_toutesLesFactures() {
            PageResult<Facture> result = sut.findAll(null, PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("avec filtre de statut → uniquement les factures correspondantes")
        void avecStatut_uniquementLesCorrespondantes() {
            PageResult<Facture> result = sut.findAll(StatutFacture.VALIDEE, PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).getNumeroFacture()).isEqualTo("FAC-2026-0010");
        }
    }

    @Nested
    @DisplayName("findByFournisseurId()")
    class FindByFournisseurId {

        FournisseurId fournisseurId1 = FournisseurId.generate();
        FournisseurId fournisseurId2 = FournisseurId.generate();

        @BeforeEach
        void setUp() {
            sut.save(nouvelleFacture("FAC-2026-0020", fournisseurId1));

            Facture validee = nouvelleFacture("FAC-2026-0021", fournisseurId1);
            validee.valider();
            sut.save(validee);

            sut.save(nouvelleFacture("FAC-2026-0022", fournisseurId2));

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("sans filtre de statut → toutes les factures du fournisseur")
        void sansStatut_toutesLesFacturesDuFournisseur() {
            PageResult<Facture> result = sut.findByFournisseurId(fournisseurId1, null,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("avec filtre de statut → uniquement les factures correspondantes")
        void avecStatut_uniquementLesCorrespondantes() {
            PageResult<Facture> result = sut.findByFournisseurId(fournisseurId1, StatutFacture.VALIDEE,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).getNumeroFacture()).isEqualTo("FAC-2026-0021");
        }

        @Test
        @DisplayName("un autre fournisseur ne voit pas ces factures")
        void autreFournisseur_neVoitRien() {
            PageResult<Facture> result = sut.findByFournisseurId(FournisseurId.generate(), null,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isZero();
        }
    }
}
