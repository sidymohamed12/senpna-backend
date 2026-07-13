package ministere.sante.senpna.stock.application.usecase.lot;

import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.exception.lot.NumeroLotDejaUtiliseException;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreerLotUseCaseImpl — création d'un lot")
class CreerLotUseCaseImplTest {

    @Mock
    LotRepositoryPort lotRepositoryPort;
    @Mock
    MedicamentRepositoryPort medicamentRepositoryPort;
    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    LotDetailAssembler lotDetailAssembler;
    @Mock
    EntrepotScopeGuard entrepotScopeGuard;

    CreerLotUseCaseImpl sut;

    private CreerLotCommand commande() {
        return new CreerLotCommand("LOT-001", UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6), new BigDecimal("10.00"), new BigDecimal("15.00"));
    }

    @BeforeEach
    void setUp() {
        sut = new CreerLotUseCaseImpl(lotRepositoryPort, medicamentRepositoryPort, fournisseurRepositoryPort,
                lotDetailAssembler, entrepotScopeGuard);
    }

    @Test
    @DisplayName("vérifie que l'acteur est national AVANT toute autre opération")
    void verifieActeurNationalEnPremier() {
        org.mockito.Mockito.doThrow(new RuntimeException("acces refuse")).when(entrepotScopeGuard)
                .verifierActeurNational();

        assertThatThrownBy(() -> sut.creer(commande())).isInstanceOf(RuntimeException.class);

        verify(medicamentRepositoryPort, never()).findById(any());
    }

    @Test
    @DisplayName("médicament introuvable → MedicamentIntrouvableException")
    void medicamentIntrouvable_leveException() {
        when(medicamentRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.creer(commande())).isInstanceOf(MedicamentIntrouvableException.class);
    }

    @Test
    @DisplayName("fournisseur introuvable → FournisseurIntrouvableException")
    void fournisseurIntrouvable_leveException() {
        when(medicamentRepositoryPort.findById(any())).thenReturn(Optional.of(org.mockito.Mockito.mock(Medicament.class)));
        when(fournisseurRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.creer(commande())).isInstanceOf(FournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("numéro de lot déjà utilisé pour ce médicament → NumeroLotDejaUtiliseException")
    void numeroLotDejaUtilise_leveException() {
        Medicament medicament = org.mockito.Mockito.mock(Medicament.class);
        when(medicament.getId()).thenReturn(ministere.sante.senpna.medicament.domain.valueobject.MedicamentId.generate());
        when(medicamentRepositoryPort.findById(any())).thenReturn(Optional.of(medicament));
        when(fournisseurRepositoryPort.findById(any())).thenReturn(Optional.of(org.mockito.Mockito.mock(Fournisseur.class)));
        when(lotRepositoryPort.existsByMedicamentIdAndNumeroLotIgnoreCase(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> sut.creer(commande())).isInstanceOf(NumeroLotDejaUtiliseException.class);

        verify(lotRepositoryPort, never()).save(any());
    }
}
