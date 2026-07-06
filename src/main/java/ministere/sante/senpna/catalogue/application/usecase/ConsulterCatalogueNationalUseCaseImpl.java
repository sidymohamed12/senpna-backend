package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.exception.PnaCentraleIntrouvableException;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueNationalUseCase;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catalogue de la PNA (cf. doc. produit « Catalogue » — bullet 1) : stock
 * agrégé par médicament de l'entrepôt PNA centrale, visible uniquement par
 * les acteurs PNA et PRA (cf.
 * {@link CatalogueAccessGuard#verifierActeurPnaOuPra()}).
 *
 * <p>
 * Ne dépend que de ports {@code shared} — {@link EntrepotQueryPort} (pour
 * localiser la PNA centrale), {@link StockAgregeQueryPort} (pour
 * l'agrégation de stock) et {@link MedicamentQueryPort} (pour
 * l'enrichissement) — jamais des modules {@code organisation},
 * {@code stock} ou {@code medicament} directement.
 * </p>
 *
 * <h3>Cache</h3>
 * <p>
 * La page assemblée est mise en cache Redis à une clé dérivée des
 * paramètres de la requête ({@code catalogue:national:<recherche>:
 * <rupture>:<page>:<size>}) avec un TTL court
 * ({@code app.cache.catalogue-ttl}, 1 min par défaut) — jamais au
 * démarrage. Le catalogue est une vue calculée sur un très grand nombre
 * de lignes de stock (cf. Javadoc {@code CatalogueEntryAssembler}) qui
 * bougent en continu (entrées/sorties/réservations) : plutôt que
 * d'invalider explicitement cette clé à chaque mouvement de stock d'un
 * médicament de la PNA (ce qui exigerait de coupler {@code stock} à
 * {@code catalogue}, contraire à l'indépendance des agrégats), on accepte
 * une fenêtre de fraîcheur d'une minute — un compromis pragmatique
 * classique pour une vue agrégée à fort trafic en lecture.
 * </p>
 */
@Service
public class ConsulterCatalogueNationalUseCaseImpl implements ConsulterCatalogueNationalUseCase {

    private static final String PREFIX = "catalogue:national:";

    private final CatalogueAccessGuard catalogueAccessGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StockAgregeQueryPort stockAgregeQueryPort;
    private final MedicamentQueryPort medicamentQueryPort;
    private final CatalogueEntryAssembler catalogueEntryAssembler;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public ConsulterCatalogueNationalUseCaseImpl(CatalogueAccessGuard catalogueAccessGuard,
            EntrepotQueryPort entrepotQueryPort, StockAgregeQueryPort stockAgregeQueryPort,
            MedicamentQueryPort medicamentQueryPort, CatalogueEntryAssembler catalogueEntryAssembler,
            JsonCacheSupport cache, AppProperties appProperties) {
        this.catalogueAccessGuard = catalogueAccessGuard;
        this.entrepotQueryPort = entrepotQueryPort;
        this.stockAgregeQueryPort = stockAgregeQueryPort;
        this.medicamentQueryPort = medicamentQueryPort;
        this.catalogueEntryAssembler = catalogueEntryAssembler;
        this.cache = cache;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public CataloguePage consulter(ConsulterCatalogueNationalQuery query) {
        catalogueAccessGuard.verifierActeurPnaOuPra();

        String cleCache = cleCache(query);
        return cache.get(cleCache, CataloguePage.class)
                .orElseGet(() -> assembler(query, cleCache));
    }

    private CataloguePage assembler(ConsulterCatalogueNationalQuery query, String cleCache) {
        EntrepotProjection pnaCentrale = entrepotQueryPort.findPnaCentraleActive()
                .orElseThrow(PnaCentraleIntrouvableException::new);

        List<StockAgregeProjection> lignes = stockAgregeQueryPort.rechercherParEntrepot(pnaCentrale.id());

        CataloguePage page = catalogueEntryAssembler.assembler(lignes, medicamentQueryPort, query.recherche(),
                query.ruptureUniquement(), query.page(), query.size());

        cache.put(cleCache, page, appProperties.cache().catalogueTtl());
        return page;
    }

    private String cleCache(ConsulterCatalogueNationalQuery query) {
        return PREFIX + query.recherche() + ":" + query.ruptureUniquement() + ":" + query.page() + ":"
                + query.size();
    }
}
