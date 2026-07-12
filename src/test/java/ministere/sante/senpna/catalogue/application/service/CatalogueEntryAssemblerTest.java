package ministere.sante.senpna.catalogue.application.service;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogue;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogueInterPra;
import ministere.sante.senpna.shared.domain.port.out.ConditionnementQueryPort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogueEntryAssembler — assemblage des pages de catalogue")
class CatalogueEntryAssemblerTest {

        @Mock
        FournisseurCachePort fournisseurCachePort;

        @Mock
        ConditionnementQueryPort conditionnementQueryPort;

        @Mock
        MedicamentQueryPort medicamentQueryPort;

        CatalogueEntryAssembler sut;

        UUID medicamentA;
        UUID medicamentB;
        UUID fournisseurX;
        UUID entrepot1;
        UUID entrepot2;
        UUID regionThies;

        @BeforeEach
        void setUp() {
                sut = new CatalogueEntryAssembler(fournisseurCachePort, conditionnementQueryPort);

                medicamentA = UUID.randomUUID();
                medicamentB = UUID.randomUUID();
                fournisseurX = UUID.randomUUID();
                entrepot1 = UUID.randomUUID();
                entrepot2 = UUID.randomUUID();
                regionThies = UUID.randomUUID();

                when(conditionnementQueryPort.findAllVendablesByMedicamentIdIn(anyCollection())).thenReturn(List.of());
        }

        private MedicamentProjection medicament(UUID id, String code, String nom, String dci) {
                return new MedicamentProjection(id, code, nom, dci, "Antalgiques", "Sanofi", true);
        }

        private StockAgregeProjection ligneStock(UUID entrepotId, UUID medicamentId, BigDecimal disponible,
                        BigDecimal reservee, UUID fournisseurId) {
                return new StockAgregeProjection(entrepotId, medicamentId, disponible, reservee, 1,
                                LocalDate.now().plusMonths(3), new BigDecimal("100.00"), fournisseurId);
        }

        @Nested
        @DisplayName("assembler() — catalogue simple")
        class Assembler {

                @Test
                @DisplayName("enrichit chaque ligne de stock avec le médicament et trie par nom commercial")
                void enrichitEtTrie() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem"),
                                        medicament(medicamentB, "MED-B", "Amoxicilline", "Amoxicilline")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("50"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("30"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, null, null, null);

                        assertThat(page.content()).extracting(LigneCatalogue::nomCommercial)
                                        .containsExactly("Amoxicilline", "Zolpidem");
                        assertThat(page.totalElements()).isEqualTo(2);
                }

                @Test
                @DisplayName("ligne de stock sans médicament résolu → exclue silencieusement")
                void ligneSansMedicament_exclue() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("50"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("30"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, null, null, null);

                        assertThat(page.content()).extracting(LigneCatalogue::medicamentId)
                                        .containsExactly(medicamentA);
                }

                @Test
                @DisplayName("filtre \"ruptureUniquement\" ne garde que les lignes en rupture")
                void filtreRuptureUniquement() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem"),
                                        medicament(medicamentB, "MED-B", "Amoxicilline", "Amoxicilline")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, BigDecimal.ZERO, BigDecimal.ZERO,
                                                        fournisseurX), // en rupture
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("30"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, true, null, null);

                        assertThat(page.content()).extracting(LigneCatalogue::medicamentId)
                                        .containsExactly(medicamentA);
                }

                @Test
                @DisplayName("recherche textuelle filtre sur nom commercial, DCI ou code, insensible à la casse")
                void rechercheTextuelle() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem"),
                                        medicament(medicamentB, "MED-B", "Amoxicilline", "Amoxicilline")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, "amox", null, null, null);

                        assertThat(page.content()).extracting(LigneCatalogue::nomCommercial)
                                        .containsExactly("Amoxicilline");
                }

                @Test
                @DisplayName("recherche vide ou blanche → aucun filtre appliqué")
                void rechercheVide_aucunFiltre() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, "   ", null, null, null);

                        assertThat(page.content()).hasSize(1);
                }

                @Test
                @DisplayName("résout le nom du fournisseur via le cache — absent du cache → null")
                void resoutNomFournisseur() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));
                        when(fournisseurCachePort.findById(fournisseurX))
                                        .thenReturn(Optional.of(
                                                        new FournisseurProjection(fournisseurX, "Pharma Plus", true)));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, null, null, null);

                        assertThat(page.content().get(0).fournisseurNom()).isEqualTo("Pharma Plus");
                }

                @Test
                @DisplayName("fournisseurId null sur la ligne de stock → nom fournisseur null, pas d'appel au cache")
                void fournisseurIdNull_pasDAppelAuCache() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        null));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, null, null, null);

                        assertThat(page.content().get(0).fournisseurNom()).isNull();
                }

                @Test
                @DisplayName("pagination : page et taille demandées sont respectées")
                void pagination() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "AAA", "AAA"),
                                        medicament(medicamentB, "MED-B", "BBB", "BBB")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CataloguePage page = sut.assembler(lignes, medicamentQueryPort, null, null, 1, 1);

                        assertThat(page.content()).extracting(LigneCatalogue::nomCommercial).containsExactly("BBB");
                        assertThat(page.page()).isEqualTo(1);
                        assertThat(page.size()).isEqualTo(1);
                        assertThat(page.totalElements()).isEqualTo(2);
                }

                @Test
                @DisplayName("liste de lignes vide → page vide sans erreur")
                void listeVide() {
                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of());

                        CataloguePage page = sut.assembler(List.of(), medicamentQueryPort, null, null, null, null);

                        assertThat(page.content()).isEmpty();
                        assertThat(page.totalElements()).isZero();
                }
        }

        @Nested
        @DisplayName("assemblerInterPra() — catalogue inter-PRA")
        class AssemblerInterPra {

                @Test
                @DisplayName("regroupe les disponibilités par médicament, ventilées par PRA, triées par quantité décroissante")
                void regroupeParMedicamentEtParPra() {
                        EntrepotProjection pra1 = new EntrepotProjection(entrepot1, "PRA-DAKAR", "PRA Dakar", "PRA",
                                        regionThies,
                                        true);
                        EntrepotProjection pra2 = new EntrepotProjection(entrepot2, "PRA-THIES", "PRA Thiès", "PRA",
                                        regionThies,
                                        true);

                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot2, medicamentA, new BigDecimal("40"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CatalogueInterPraPage page = sut.assemblerInterPra(lignes, List.of(pra1, pra2),
                                        medicamentQueryPort,
                                        null, null, null, null, null);

                        assertThat(page.content()).hasSize(1);
                        LigneCatalogueInterPra ligne = page.content().get(0);
                        assertThat(ligne.quantiteTotaleReseau()).isEqualByComparingTo("50");
                        assertThat(ligne.disponibilites()).extracting(d -> d.codeEntrepot())
                                        .containsExactly("PRA-THIES", "PRA-DAKAR"); // 40 avant 10
                }

                @Test
                @DisplayName("filtre par medicamentId demandé → ignore les autres médicaments")
                void filtrePartMedicamentIdDemande() {
                        EntrepotProjection pra1 = new EntrepotProjection(entrepot1, "PRA-DAKAR", "PRA Dakar", "PRA",
                                        regionThies,
                                        true);

                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem"),
                                        medicament(medicamentB, "MED-B", "Amoxicilline", "Amoxicilline")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CatalogueInterPraPage page = sut.assemblerInterPra(lignes, List.of(pra1), medicamentQueryPort,
                                        null, medicamentA, null, null, null);

                        assertThat(page.content()).extracting(LigneCatalogueInterPra::medicamentId)
                                        .containsExactly(medicamentA);
                }

                @Test
                @DisplayName("ligne dont l'entrepôt n'est pas une PRA active → exclue")
                void entrepotHorsPrasActives_exclu() {
                        EntrepotProjection pra1 = new EntrepotProjection(entrepot1, "PRA-DAKAR", "PRA Dakar", "PRA",
                                        regionThies,
                                        true);

                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot2, medicamentA, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CatalogueInterPraPage page = sut.assemblerInterPra(lignes, List.of(pra1), medicamentQueryPort,
                                        null, null, null, null, null);

                        assertThat(page.content()).isEmpty();
                }

                @Test
                @DisplayName("filtre ruptureUniquement sur la quantité totale réseau")
                void filtreRuptureUniquement() {
                        EntrepotProjection pra1 = new EntrepotProjection(entrepot1, "PRA-DAKAR", "PRA Dakar", "PRA",
                                        regionThies,
                                        true);

                        when(medicamentQueryPort.findAllById(anyCollection())).thenReturn(List.of(
                                        medicament(medicamentA, "MED-A", "Zolpidem", "Zolpidem"),
                                        medicament(medicamentB, "MED-B", "Amoxicilline", "Amoxicilline")));

                        List<StockAgregeProjection> lignes = List.of(
                                        ligneStock(entrepot1, medicamentA, BigDecimal.ZERO, BigDecimal.ZERO,
                                                        fournisseurX),
                                        ligneStock(entrepot1, medicamentB, new BigDecimal("10"), BigDecimal.ZERO,
                                                        fournisseurX));

                        CatalogueInterPraPage page = sut.assemblerInterPra(lignes, List.of(pra1), medicamentQueryPort,
                                        null, null, true, null, null);

                        assertThat(page.content()).extracting(LigneCatalogueInterPra::medicamentId)
                                        .containsExactly(medicamentA);
                }
        }
}
