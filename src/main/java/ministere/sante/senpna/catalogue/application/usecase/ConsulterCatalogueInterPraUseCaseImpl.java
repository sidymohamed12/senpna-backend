package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueInterPraUseCase;
import ministere.sante.senpna.config.AppProperties;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.infrastructure.cache.JsonCacheSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Catalogue inter-PRA (cf. doc. produit « Catalogue » — bullet 2) :
 * disponibilités de toutes les PRA actives, ventilées par PRA, pour
 * faciliter l'arbitrage d'un transfert ou d'un échange (cf. doc. flows
 * §CAS 3 « Une PRA demande un transfert à une autre PRA »). Visible par
 * tous les acteurs PNA et PRA, quelle que soit leur propre région — une
 * PRA en rupture doit pouvoir voir les disponibilités de n'importe quelle
 * autre région.
 *
 * <p>
 * Ne dépend que de ports {@code shared} (voir
 * {@link ConsulterCatalogueNationalUseCaseImpl}), jamais des modules
 * {@code organisation}, {@code stock} ou {@code medicament} directement.
 * </p>
 */
@Service
public class ConsulterCatalogueInterPraUseCaseImpl implements ConsulterCatalogueInterPraUseCase {

    private static final String PREFIX = "catalogue:inter-pra:";

    private final CatalogueAccessGuard catalogueAccessGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StockAgregeQueryPort stockAgregeQueryPort;
    private final MedicamentQueryPort medicamentQueryPort;
    private final CatalogueEntryAssembler catalogueEntryAssembler;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public ConsulterCatalogueInterPraUseCaseImpl(CatalogueAccessGuard catalogueAccessGuard,
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
    public CatalogueInterPraPage consulter(ConsulterCatalogueInterPraQuery query) {
        catalogueAccessGuard.verifierActeurPnaOuPra();

        String cleCache = cleCache(query);
        return cache.get(cleCache, CatalogueInterPraPage.class)
                .orElseGet(() -> assembler(query, cleCache));
    }

    private CatalogueInterPraPage assembler(ConsulterCatalogueInterPraQuery query, String cleCache) {
        List<EntrepotProjection> prasActives = entrepotQueryPort.findPrasActives();
        if (prasActives.isEmpty()) {
            int size = query.size() != null ? query.size() : PageRequest.DEFAULT_SIZE;
            return new CatalogueInterPraPage(List.of(), 0, size, 0, 0);
        }

        Set<UUID> entrepotIds = prasActives.stream().map(EntrepotProjection::id).collect(Collectors.toSet());
        List<StockAgregeProjection> lignes = stockAgregeQueryPort.rechercherParEntrepots(entrepotIds);

        CatalogueInterPraPage page = catalogueEntryAssembler.assemblerInterPra(lignes, prasActives,
                medicamentQueryPort, query.recherche(), query.medicamentId(), query.ruptureUniquement(),
                query.page(), query.size());

        cache.put(cleCache, page, appProperties.cache().catalogueTtl());
        return page;
    }

    private String cleCache(ConsulterCatalogueInterPraQuery query) {
        return PREFIX + query.recherche() + ":" + query.medicamentId() + ":" + query.ruptureUniquement() + ":"
                + query.page() + ":" + query.size();
    }
}
