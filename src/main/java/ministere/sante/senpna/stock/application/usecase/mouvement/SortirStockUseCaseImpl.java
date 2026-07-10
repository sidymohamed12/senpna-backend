package ministere.sante.senpna.stock.application.usecase.mouvement;

import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.StockDetailAssembler;
import ministere.sante.senpna.stock.domain.command.StockCommands.SortieStockCommand;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockDetail;
import ministere.sante.senpna.stock.domain.exception.lot.LotIntrouvableException;
import ministere.sante.senpna.stock.domain.exception.lot.LotNonDisponibleException;
import ministere.sante.senpna.stock.domain.exception.stock.StockIntrouvableException;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.model.Stock;
import ministere.sante.senpna.stock.domain.port.in.mouvement.SortirStockUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.port.out.StockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Sortie de stock : diminue la quantité disponible d'une ligne de stock et
 * enregistre le {@link MouvementStock} correspondant.
 *
 * <p>
 * Les sorties d'expédition ({@code SORTIE_TRANSFERT}, {@code SORTIE_STRUCTURE})
 * exigent un lot disponible ({@code ACTIF}, non expiré — cf. modèle métier
 * complémentaire §2 « Lots »). Les sorties correctives ({@code PERTE},
 * {@code CASSE}, {@code VOL}, {@code PEREMPTION}, {@code AJUSTEMENT},
 * {@code INVENTAIRE}, {@code DON}) s'appliquent quel que soit le statut du
 * lot — elles peuvent précisément servir à retirer un lot bloqué ou expiré
 * du stock physique.
 * </p>
 */
@Service
public class SortirStockUseCaseImpl implements SortirStockUseCase {

    private static final Set<TypeMouvement> TYPES_EXPEDITION = EnumSet.of(TypeMouvement.SORTIE_TRANSFERT,
            TypeMouvement.SORTIE_STRUCTURE);

    private final StockRepositoryPort stockRepositoryPort;
    private final LotRepositoryPort lotRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final MouvementStockRepositoryPort mouvementStockRepositoryPort;
    private final StockDetailAssembler stockDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public SortirStockUseCaseImpl(StockRepositoryPort stockRepositoryPort, LotRepositoryPort lotRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort, MouvementStockRepositoryPort mouvementStockRepositoryPort,
            StockDetailAssembler stockDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.stockRepositoryPort = stockRepositoryPort;
        this.lotRepositoryPort = lotRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.mouvementStockRepositoryPort = mouvementStockRepositoryPort;
        this.stockDetailAssembler = stockDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional
    public StockDetail sortir(SortieStockCommand command) {
        // Seul l'entrepôt source (celui qui expédie/perd la marchandise) doit
        // être le sien : l'entrepôt destination d'un transfert appartient à
        // l'acteur qui traitera sa propre réception, de son côté.
        entrepotScopeGuard.verifierEcritureAutorisee(command.entrepotId());

        Lot lot = lotRepositoryPort.findById(LotId.of(command.lotId())).orElseThrow(LotIntrouvableException::new);

        Entrepot entrepotSource = entrepotRepositoryPort.findById(EntrepotId.of(command.entrepotId()))
                .orElseThrow(EntrepotIntrouvableException::new);

        TypeMouvement type = parseType(command.typeMouvement());

        if (TYPES_EXPEDITION.contains(type) && !lot.peutEtreReserveOuExpedie()) {
            throw new LotNonDisponibleException(lot.getNumeroLot());
        }

        EntrepotId entrepotDestinationId = command.entrepotDestinationId() != null
                ? EntrepotId.of(command.entrepotDestinationId())
                : null;

        Stock stock = stockRepositoryPort.findByEntrepotIdAndLotIdForUpdate(entrepotSource.getId(), lot.getId())
                .orElseThrow(StockIntrouvableException::new);

        if (command.depuisReservation()) {
            stock.sortirDepuisReservation(command.quantite());
        } else {
            stock.sortir(command.quantite());
        }
        Stock saved = stockRepositoryPort.save(stock);

        MouvementStock mouvement = MouvementStock.creer(type, SensMouvement.SORTIE, entrepotSource.getId(),
                entrepotDestinationId, command.commandeId(), lot.getId(), lot.getMedicamentId(), command.quantite(),
                command.referenceDocument(), command.motif(), command.utilisateurId());
        mouvementStockRepositoryPort.save(mouvement);

        return stockDetailAssembler.assembler(saved);
    }

    private static TypeMouvement parseType(String typeMouvement) {
        Objects.requireNonNull(typeMouvement, "Le type de mouvement est obligatoire");
        try {
            return TypeMouvement.valueOf(typeMouvement.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Type de mouvement invalide : " + typeMouvement, "TYPE_MOUVEMENT_INVALID");
        }
    }
}
