package ministere.sante.senpna.stock.application.usecase.lot;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.application.service.LotOwnershipGuard;
import ministere.sante.senpna.stock.domain.command.LotCommands.BloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.DebloquerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.GetLotQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.ModifierPrixLotCommand;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLotUseCaseImpl et transitions de statut d'un lot")
class LotUseCasesTest {

    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    LotDetailAssembler lotDetailAssembler;
    @Mock
    LotOwnershipGuard lotOwnershipGuard;

    UUID id;
    Lot lot;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        lot = Lot.creer("LOT-001", MedicamentId.generate(), FournisseurId.generate(),
                LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(6), new BigDecimal("10.00"),
                new BigDecimal("15.00"));
    }

    @Nested
    @DisplayName("GetLotUseCaseImpl")
    class Get {

        GetLotUseCaseImpl sut;

        @BeforeEach
        void setUp() {
            sut = new GetLotUseCaseImpl(lotRepositoryPort, lotDetailAssembler, lotOwnershipGuard);
        }

        @Test
        @DisplayName("vérifie l'accès au lot après l'avoir trouvé")
        void verifieAccesLot() {
            when(lotRepositoryPort.findById(LotId.of(id))).thenReturn(Optional.of(lot));

            sut.obtenir(new GetLotQuery(id));

            verify(lotOwnershipGuard).verifierAccesLot(lot.getId());
        }

        @Test
        @DisplayName("introuvable → LotIntrouvableException")
        void introuvable_leveException() {
            when(lotRepositoryPort.findById(LotId.of(id))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.obtenir(new GetLotQuery(id))).isInstanceOf(LotIntrouvableException.class);
        }
    }

    @Nested
    @DisplayName("BloquerLotUseCaseImpl / DebloquerLotUseCaseImpl")
    class BloquerDebloquer {

        @Test
        @DisplayName("bloquer() passe le lot à BLOQUE")
        void bloquer_passeBloque() {
            when(lotRepositoryPort.findById(LotId.of(id))).thenReturn(Optional.of(lot));
            when(lotRepositoryPort.save(lot)).thenReturn(lot);

            new BloquerLotUseCaseImpl(lotRepositoryPort, lotDetailAssembler, lotOwnershipGuard)
                    .bloquer(new BloquerLotCommand(id));

            assertThat(lot.getStatut()).isEqualTo(StatutLot.BLOQUE);
        }

        @Test
        @DisplayName("debloquer() remet le lot ACTIF")
        void debloquer_remetActif() {
            lot.bloquer();
            when(lotRepositoryPort.findById(LotId.of(id))).thenReturn(Optional.of(lot));
            when(lotRepositoryPort.save(lot)).thenReturn(lot);

            new DebloquerLotUseCaseImpl(lotRepositoryPort, lotDetailAssembler, lotOwnershipGuard)
                    .debloquer(new DebloquerLotCommand(id));

            assertThat(lot.getStatut()).isEqualTo(StatutLot.ACTIF);
        }
    }

    @Nested
    @DisplayName("ModifierPrixLotUseCaseImpl")
    class ModifierPrix {

        @Test
        @DisplayName("modifie le prix d'achat et de vente du lot")
        void modifiePrix() {
            when(lotRepositoryPort.findById(LotId.of(id))).thenReturn(Optional.of(lot));
            when(lotRepositoryPort.save(lot)).thenReturn(lot);

            new ModifierPrixLotUseCaseImpl(lotRepositoryPort, lotDetailAssembler, lotOwnershipGuard)
                    .modifierPrix(new ModifierPrixLotCommand(id, new BigDecimal("12.00"), new BigDecimal("18.00")));

            assertThat(lot.getPrixVente()).isEqualByComparingTo("18.00");
        }
    }
}
