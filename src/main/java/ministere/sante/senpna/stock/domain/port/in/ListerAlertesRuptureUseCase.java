package ministere.sante.senpna.stock.domain.port.in;

import ministere.sante.senpna.stock.domain.command.StockCommands.ListStocksQuery;
import ministere.sante.senpna.stock.domain.command.StockCommands.StockPage;

/**
 * Alertes de rupture / seuil minimum atteint (cf. doc. métier §17) —
 * variante en lecture seule de {@link ListStocksUseCase} qui force le
 * filtre {@code ruptureUniquement} (ou {@code seuilAtteintUniquement}
 * selon la requête) pour offrir un point d'entrée dédié et explicite aux
 * tableaux de bord et notifications.
 */
public interface ListerAlertesRuptureUseCase {
    StockPage lister(ListStocksQuery query);
}
