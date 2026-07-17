package ministere.sante.senpna.commandeachat.infrastructure.persistence.adapter;

import ministere.sante.senpna.commandeachat.domain.criteria.CommandeAchatSearchCriteria;
import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper.CommandeAchatMapper;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Tag("integration")
@Import({ CommandeAchatRepositoryAdapter.class, CommandeAchatMapper.class })
@DisplayName("CommandeAchatRepositoryAdapter — persistance avec H2")
class CommandeAchatRepositoryAdapterTest {

    @Autowired
    CommandeAchatRepositoryAdapter sut;
    @Autowired
    TestEntityManager entityManager;

    private LigneCommandeAchat ligne() {
        return LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.generate(), ConditionnementId.generate(), BigDecimal.TEN,
                BigDecimal.valueOf(200_000)));
    }

    private CommandeAchat nouvelleCommande(String reference, FournisseurId fournisseurId) {
        return CommandeAchat.creer(new CommandeAchat.CreationCommand(reference, fournisseurId, EntrepotId.generate(), List.of(ligne()), "Commentaire"));
    }

    @Nested
    @DisplayName("save() / findById()")
    class SaveEtFindById {

        @Test
        @DisplayName("save() persiste l'agrégat et ses lignes, findById() les recompose")
        void save_persisteEtRecompose() {
            CommandeAchat commande = nouvelleCommande("BC-2026-0001", FournisseurId.generate());

            sut.save(commande);
            entityManager.flush();
            entityManager.clear();

            Optional<CommandeAchat> result = sut.findById(commande.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getReference()).isEqualTo("BC-2026-0001");
            assertThat(result.get().getLignes()).hasSize(1);
            assertThat(result.get().getStatut()).isEqualTo(StatutCommandeAchat.EN_ATTENTE_VALIDATION);
        }

        @Test
        @DisplayName("save() persiste l'avis d'expédition et les informations de lot renseignées sur les lignes")
        void save_persisteAvisExpeditionEtLot() {
            CommandeAchat commande = nouvelleCommande("BC-2026-0002", FournisseurId.generate());
            commande.validerInterne();
            commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
            commande.genererAvisExpedition(
                    AvisExpedition.of(LocalDate.now(), "DHL", "T-1", LocalDate.now().plusDays(5)),
                    List.of(new CommandeAchat.InfoExpeditionLigne(commande.getLignes().get(0).getId(), "LOT-A001",
                            LocalDate.now(), LocalDate.now().plusYears(2), "https://cdn/cert.pdf", BigDecimal.TEN)));

            sut.save(commande);
            entityManager.flush();
            entityManager.clear();

            CommandeAchat relue = sut.findById(commande.getId()).orElseThrow();
            assertThat(relue.getStatut()).isEqualTo(StatutCommandeAchat.EXPEDIEE);
            assertThat(relue.getAvisExpedition()).isNotNull();
            assertThat(relue.getAvisExpedition().getTransporteur()).isEqualTo("DHL");
            assertThat(relue.getLignes().get(0).getNumeroLot()).isEqualTo("LOT-A001");
            assertThat(relue.getLignes().get(0).getQuantiteExpediee()).isEqualByComparingTo("10");
        }

        @Test
        @DisplayName("findById() sur un identifiant inconnu → vide")
        void findById_inconnu_vide() {
            assertThat(sut.findById(CommandeAchatId.generate())).isEmpty();
        }

        @Test
        @DisplayName("save() sur le même identifiant remplace intégralement l'ancien jeu de lignes")
        void save_remplaceLesLignes() {
            CommandeAchat commande = nouvelleCommande("BC-2026-0003", FournisseurId.generate());
            sut.save(commande);
            entityManager.flush();
            entityManager.clear();

            CommandeAchat relue = sut.findById(commande.getId()).orElseThrow();
            relue.validerInterne();
            sut.save(relue);
            entityManager.flush();
            entityManager.clear();

            CommandeAchat finale = sut.findById(commande.getId()).orElseThrow();
            assertThat(finale.getLignes()).hasSize(1);
            assertThat(finale.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
        }
    }

    @Nested
    @DisplayName("existsByReferenceIgnoreCase()")
    class ExistsByReference {

        @Test
        @DisplayName("insensible à la casse")
        void insensibleCasse() {
            sut.save(nouvelleCommande("BC-2026-0004", FournisseurId.generate()));
            entityManager.flush();

            assertThat(sut.existsByReferenceIgnoreCase("bc-2026-0004")).isTrue();
            assertThat(sut.existsByReferenceIgnoreCase("BC-2026-9999")).isFalse();
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @BeforeEach
        void setUp() {
            CommandeAchat validee = nouvelleCommande("BC-2026-0010", FournisseurId.generate());
            validee.validerInterne();
            sut.save(validee);

            sut.save(nouvelleCommande("BC-2026-0011", FournisseurId.generate()));

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("filtre par statut")
        void filtreParStatut() {
            PageResult<CommandeAchat> result = sut.search(
                    new CommandeAchatSearchCriteria(null, StatutCommandeAchat.VALIDEE),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(CommandeAchat::getReference).containsExactly("BC-2026-0010");
        }

        @Test
        @DisplayName("filtre par texte sur la référence")
        void filtreParTexte() {
            PageResult<CommandeAchat> result = sut.search(new CommandeAchatSearchCriteria("0011", null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.content()).extracting(CommandeAchat::getReference).containsExactly("BC-2026-0011");
        }

        @Test
        @DisplayName("aucun critère → toutes les commandes")
        void aucunCritere_toutesLesCommandes() {
            PageResult<CommandeAchat> result = sut.search(new CommandeAchatSearchCriteria(null, null),
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("champ de tri non autorisé → repli silencieux sur createdAt")
        void triNonAutorise_repliSurCreatedAt() {
            PageResult<CommandeAchat> result = sut.search(new CommandeAchatSearchCriteria(null, null),
                    PageRequest.of(0, 20, "champInexistant", "ASC"));

            assertThat(result.totalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findByFournisseurId()")
    class FindByFournisseurId {

        FournisseurId fournisseurId1 = FournisseurId.generate();
        FournisseurId fournisseurId2 = FournisseurId.generate();

        @BeforeEach
        void setUp() {
            sut.save(nouvelleCommande("BC-2026-0020", fournisseurId1));

            CommandeAchat validee = nouvelleCommande("BC-2026-0021", fournisseurId1);
            validee.validerInterne();
            sut.save(validee);

            sut.save(nouvelleCommande("BC-2026-0022", fournisseurId2));

            entityManager.flush();
            entityManager.clear();
        }

        @Test
        @DisplayName("sans filtre de statut → toutes les commandes du fournisseur")
        void sansStatut_toutesLesCommandesDuFournisseur() {
            PageResult<CommandeAchat> result = sut.findByFournisseurId(fournisseurId1, null,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("avec filtre de statut → uniquement les commandes correspondantes")
        void avecStatut_uniquementLesCorrespondantes() {
            PageResult<CommandeAchat> result = sut.findByFournisseurId(fournisseurId1,
                    StatutCommandeAchat.VALIDEE, PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.content().get(0).getReference()).isEqualTo("BC-2026-0021");
        }

        @Test
        @DisplayName("un autre fournisseur ne voit pas ces commandes")
        void autreFournisseur_neVoitRien() {
            PageResult<CommandeAchat> result = sut.findByFournisseurId(FournisseurId.generate(), null,
                    PageRequest.of(0, 20, null, null));

            assertThat(result.totalElements()).isZero();
        }
    }
}
