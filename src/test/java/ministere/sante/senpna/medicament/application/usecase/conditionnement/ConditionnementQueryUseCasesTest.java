package ministere.sante.senpna.medicament.application.usecase.conditionnement;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
@DisplayName("Use cases restants de Conditionnement (Get, List, Desarchive, Update)")
class ConditionnementQueryUseCasesTest {

        @Mock
        ConditionnementRepositoryPort conditionnementRepositoryPort;

        ConditionnementDetailAssembler assembler = new ConditionnementDetailAssembler();

        private Conditionnement conditionnement() {
                return Conditionnement.creer(new Conditionnement.CreationCommand(MedicamentId.generate(), "Boite de 10", 1, BigDecimal.ONE, true,
                                BigDecimal.TEN, BigDecimal.TEN));
        }

        @Nested
        @DisplayName("GetConditionnementUseCaseImpl")
        class Get {

                @Test
                @DisplayName("introuvable → ConditionnementIntrouvableException")
                void introuvable_leveException() {
                        when(conditionnementRepositoryPort.findById(any())).thenReturn(Optional.empty());

                        var sut = new GetConditionnementUseCaseImpl(conditionnementRepositoryPort, assembler);
                        var getConditionnementQuery = new GetConditionnementQuery(UUID.randomUUID());
                        assertThatThrownBy(() -> sut
                                        .obtenir(getConditionnementQuery))
                                        .isInstanceOf(ConditionnementIntrouvableException.class);
                }
        }

        @Nested
        @DisplayName("ListConditionnementsUseCaseImpl")
        class List_ {

                @Test
                @DisplayName("aucun résultat → page vide")
                void aucunResultat_pageVide() {
                        when(conditionnementRepositoryPort.search(any(), any()))
                                        .thenReturn(PageResult.of(List.of(), 0, 20, 0));

                        ConditionnementPage page = new ListConditionnementsUseCaseImpl(conditionnementRepositoryPort,
                                        assembler)
                                        .lister(new ListConditionnementsQuery(null, null, 0, 20, null, null));

                        assertThat(page.content()).isEmpty();
                }
        }

        @Nested
        @DisplayName("DesarchiveConditionnementUseCaseImpl")
        class Desarchive {

                @Test
                @DisplayName("réactive un conditionnement archivé")
                void reactive() {
                        Conditionnement c = conditionnement();
                        c.archiver();
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(c.getId().getValue())))
                                        .thenReturn(Optional.of(c));
                        when(conditionnementRepositoryPort.save(c)).thenReturn(c);

                        new DesarchiveConditionnementUseCaseImpl(conditionnementRepositoryPort, assembler)
                                        .desarchiver(new DesarchiveConditionnementCommand(c.getId().getValue()));

                        assertThat(c.isActif()).isTrue();
                }
        }

        @Nested
        @DisplayName("UpdateConditionnementUseCaseImpl")
        class Update {

                UpdateConditionnementUseCaseImpl sut;
                Conditionnement c;

                @BeforeEach
                void setUp() {
                        sut = new UpdateConditionnementUseCaseImpl(conditionnementRepositoryPort, assembler);
                        c = conditionnement();
                }

                private UpdateConditionnementCommand commande() {
                        return new UpdateConditionnementCommand(c.getId().getValue(), "Nouveau nom", 2, BigDecimal.TEN,
                                        false,
                                        BigDecimal.ONE, BigDecimal.ONE);
                }

                @Test
                @DisplayName("niveau déjà utilisé par un autre conditionnement → NiveauConditionnementDejaUtiliseException")
                void niveauDejaUtilise_leveException() {
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(c.getId().getValue())))
                                        .thenReturn(Optional.of(c));
                        // niveauDejaUtilise_leveException()
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(true);

                        var command = commande();
                        assertThatThrownBy(() -> sut.modifier(command))
                                        .isInstanceOf(NiveauConditionnementDejaUtiliseException.class);
                }

                @Test
                @DisplayName("nom déjà utilisé → NomConditionnementDejaUtiliseException")
                void nomDejaUtilise_leveException() {
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(c.getId().getValue())))
                                        .thenReturn(Optional.of(c));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCaseAndIdNot(any(), any(),
                                        any()))
                                        .thenReturn(true);

                        var command = commande();
                        assertThatThrownBy(() -> sut.modifier(command))
                                        .isInstanceOf(NomConditionnementDejaUtiliseException.class);
                }

                @Test
                @DisplayName("estUniteBase=true alors qu'une autre unité de base existe déjà → UniteBaseDejaDefinieException")
                void uniteBaseDejaDefinie_leveException() {
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(c.getId().getValue())))
                                        .thenReturn(Optional.of(c));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsUniteBaseByMedicamentIdAndIdNot(any(), any()))
                                        .thenReturn(true);

                        UpdateConditionnementCommand commandeUniteBase = new UpdateConditionnementCommand(
                                        c.getId().getValue(),
                                        "Nom", 2, BigDecimal.TEN, true, BigDecimal.ONE, BigDecimal.ONE);

                        assertThatThrownBy(() -> sut.modifier(commandeUniteBase))
                                        .isInstanceOf(UniteBaseDejaDefinieException.class);
                }

                @Test
                @DisplayName("modification valide → contenu mis à jour")
                void modificationValide_metAJour() {
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(c.getId().getValue())))
                                        .thenReturn(Optional.of(c));
                        // modificationValide_metAJour()
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(false);
                        // modificationValide_metAJour()
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveauAndIdNot(any(), anyInt(),
                                        any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.save(c)).thenReturn(c);

                        sut.modifier(commande());

                        assertThat(c.getNom()).isEqualTo("Nouveau nom");
                }
        }
}
