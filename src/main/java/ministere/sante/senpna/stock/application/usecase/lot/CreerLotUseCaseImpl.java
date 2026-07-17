package ministere.sante.senpna.stock.application.usecase.lot;

import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.CreerLotCommand;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotDetail;
import ministere.sante.senpna.stock.domain.exception.lot.NumeroLotDejaUtiliseException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.lot.CreerLotUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enregistre un nouveau lot dans le référentiel.
 * N'affecte aucune quantité de stock — la mise en stock effective d'un
 * lot se fait via {@code EntreeStockUseCase}, séparément, afin de garder
 * une responsabilité unique par use case.
 */
@Service
public class CreerLotUseCaseImpl implements CreerLotUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final MedicamentRepositoryPort medicamentRepositoryPort;
    private final FournisseurRepositoryPort fournisseurRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public CreerLotUseCaseImpl(LotRepositoryPort lotRepositoryPort, MedicamentRepositoryPort medicamentRepositoryPort,
            FournisseurRepositoryPort fournisseurRepositoryPort, LotDetailAssembler lotDetailAssembler,
            EntrepotScopeGuard entrepotScopeGuard) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.medicamentRepositoryPort = medicamentRepositoryPort;
        this.fournisseurRepositoryPort = fournisseurRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public LotDetail creer(CreerLotCommand command) {
        // Seule la PNA achète auprès des fournisseurs (cf. modèle métier
        // complémentaire §2 « Approvisionnements ») : une PRA ne crée jamais
        // de nouveau lot, elle en reçoit par transfert (EntreeStockUseCase).
        entrepotScopeGuard.verifierActeurNational();

        Medicament medicament = medicamentRepositoryPort.findById(MedicamentId.of(command.medicamentId()))
                .orElseThrow(MedicamentIntrouvableException::new);

        Fournisseur fournisseur = fournisseurRepositoryPort.findById(FournisseurId.of(command.fournisseurId()))
                .orElseThrow(FournisseurIntrouvableException::new);

        if (lotRepositoryPort.existsByMedicamentIdAndNumeroLotIgnoreCase(medicament.getId(),
                command.numeroLot().trim())) {
            throw new NumeroLotDejaUtiliseException(command.numeroLot());
        }

        Lot lot = Lot.creer(new Lot.CreationCommand(command.numeroLot(), medicament.getId(), fournisseur.getId(), command.dateFabrication(),
                command.dateExpiration(), command.prixAchat(), command.prixVente()));

        Lot saved = lotRepositoryPort.save(lot);
        return lotDetailAssembler.assembler(saved);
    }
}
