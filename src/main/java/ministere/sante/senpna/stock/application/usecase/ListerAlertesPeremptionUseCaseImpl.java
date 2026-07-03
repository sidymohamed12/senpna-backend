package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.application.service.LotDetailAssembler;
import ministere.sante.senpna.stock.domain.command.LotCommands.AlertePeremptionQuery;
import ministere.sante.senpna.stock.domain.command.LotCommands.LotPage;
import ministere.sante.senpna.stock.domain.criteria.AlertePeremptionCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.ListerAlertesPeremptionUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Alertes de péremption (cf. doc. métier §17) — remonte les lots
 * {@code ACTIF} expirant dans les {@code horizonJours} à venir (paliers
 * usuels : 365, 180, 90, 30 jours pour 12/6/3/1 mois).
 */
@Service
public class ListerAlertesPeremptionUseCaseImpl implements ListerAlertesPeremptionUseCase {

    private final LotRepositoryPort lotRepositoryPort;
    private final LotDetailAssembler lotDetailAssembler;

    public ListerAlertesPeremptionUseCaseImpl(LotRepositoryPort lotRepositoryPort,
            LotDetailAssembler lotDetailAssembler) {
        this.lotRepositoryPort = lotRepositoryPort;
        this.lotDetailAssembler = lotDetailAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public LotPage lister(AlertePeremptionQuery query) {
        if (query.horizonJours() <= 0) {
            throw new IllegalArgumentException("L'horizon en jours doit être strictement positif");
        }

        LocalDate dateLimite = LocalDate.now().plusDays(query.horizonJours());
        AlertePeremptionCriteria criteria = new AlertePeremptionCriteria(dateLimite, query.medicamentId());
        PageRequest pageRequest = PageRequest.of(query.page(), query.size(), "dateExpiration", "ASC");

        PageResult<Lot> result = lotRepositoryPort.findExpirantAvant(criteria, pageRequest);

        return new LotPage(
                result.content().stream().map(lotDetailAssembler::assembler).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
