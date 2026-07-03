package ministere.sante.senpna.stock.application.usecase;

import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.in.MarquerLotsExpiresUseCase;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Marque {@code EXPIRE} tous les lots {@code ACTIF} dont la date
 * d'expiration est dépassée (cf. modèle métier complémentaire §2
 * « Péremption » : « Les produits expirés doivent être automatiquement
 * marqués comme non distribuables »).
 */
@Service
public class MarquerLotsExpiresUseCaseImpl implements MarquerLotsExpiresUseCase {

    private static final Logger log = LoggerFactory.getLogger(MarquerLotsExpiresUseCaseImpl.class);

    private final LotRepositoryPort lotRepositoryPort;

    public MarquerLotsExpiresUseCaseImpl(LotRepositoryPort lotRepositoryPort) {
        this.lotRepositoryPort = lotRepositoryPort;
    }

    @Override
    @Transactional
    public int marquerExpires() {
        List<Lot> lotsExpires = lotRepositoryPort.findActifsExpires();

        for (Lot lot : lotsExpires) {
            lot.marquerExpire();
            lotRepositoryPort.save(lot);
        }

        if (!lotsExpires.isEmpty()) {
            log.info("[Péremption] {} lot(s) marqué(s) EXPIRE", lotsExpires.size());
        }

        return lotsExpires.size();
    }
}
