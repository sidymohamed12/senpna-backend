package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.EntrepotScopeGuard;
import ministere.sante.senpna.stock.application.service.MouvementDetailAssembler;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.ListMouvementsQuery;
import ministere.sante.senpna.stock.domain.command.MouvementStockCommands.MouvementPage;
import ministere.sante.senpna.stock.domain.criteria.MouvementSearchCriteria;
import ministere.sante.senpna.stock.domain.model.MouvementStock;
import ministere.sante.senpna.stock.domain.port.in.mouvement.ListMouvementsUseCase;
import ministere.sante.senpna.stock.domain.port.out.MouvementStockRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.SensMouvement;
import ministere.sante.senpna.stock.domain.valueobject.TypeMouvement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Historique des mouvements de stock (cf. doc. métier §13 et §18) —
 * consultation, filtrage et recherche, avec traçabilité complète
 * (utilisateur, date, type, entrepôts).
 */
@Service
public class ListMouvementsUseCaseImpl implements ListMouvementsUseCase {

    private final MouvementStockRepositoryPort mouvementStockRepositoryPort;
    private final MouvementDetailAssembler mouvementDetailAssembler;
    private final EntrepotScopeGuard entrepotScopeGuard;

    public ListMouvementsUseCaseImpl(MouvementStockRepositoryPort mouvementStockRepositoryPort,
            MouvementDetailAssembler mouvementDetailAssembler, EntrepotScopeGuard entrepotScopeGuard) {
        this.mouvementStockRepositoryPort = mouvementStockRepositoryPort;
        this.mouvementDetailAssembler = mouvementDetailAssembler;
        this.entrepotScopeGuard = entrepotScopeGuard;
    }

    @Override
    @Transactional(readOnly = true)
    public MouvementPage lister(ListMouvementsQuery query) {
        // PRA : toujours ramené à son propre entrepôt (source OU destination —
        // cf. MouvementStockSpecifications.entrepotId). PNA : filtre libre.
        UUID entrepotIdEffectif = entrepotScopeGuard.entrepotIdPourLecture(query.entrepotId());

        MouvementSearchCriteria criteria = new MouvementSearchCriteria(
                query.lotId(),
                query.medicamentId(),
                entrepotIdEffectif,
                parseType(query.typeMouvement()),
                parseSens(query.sens()),
                query.utilisateurId(),
                query.dateDebut(),
                query.dateFin());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(),
                query.sortBy() != null ? query.sortBy() : "dateMouvement", query.sortDirection());

        PageResult<MouvementStock> result = mouvementStockRepositoryPort.search(criteria, pageRequest);

        return new MouvementPage(
                result.content().stream().map(mouvementDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    private static TypeMouvement parseType(String typeMouvement) {
        if (typeMouvement == null || typeMouvement.isBlank()) {
            return null;
        }
        try {
            return TypeMouvement.valueOf(typeMouvement.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Type de mouvement invalide : " + typeMouvement, "TYPE_MOUVEMENT_INVALID");
        }
    }

    private static SensMouvement parseSens(String sens) {
        if (sens == null || sens.isBlank()) {
            return null;
        }
        try {
            return SensMouvement.valueOf(sens.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Sens de mouvement invalide : " + sens, "SENS_MOUVEMENT_INVALID");
        }
    }
}
