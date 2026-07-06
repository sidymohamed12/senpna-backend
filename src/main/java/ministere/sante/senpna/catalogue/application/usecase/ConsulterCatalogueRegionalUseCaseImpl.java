package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;
import ministere.sante.senpna.catalogue.domain.exception.AucunePraPourRegionException;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueRegionalUseCase;
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
import java.util.UUID;

/**
 * Catalogue régional (cf. doc. produit « Catalogue » — bullet 3) : stock
 * agrégé par médicament de la PRA d'une région donnée. Visible uniquement
 * par les structures sanitaires de cette région — une structure sanitaire
 * ne peut jamais consulter le catalogue d'une autre région que la sienne
 * (cf. {@link CatalogueAccessGuard#resoudreRegionPourCatalogueRegional(UUID)},
 * qui ignore silencieusement la région demandée pour tout acteur non
 * national et la remplace par la sienne). Un acteur PRA consultant ce
 * catalogue s'y voit, par le même mécanisme, systématiquement ramené à sa
 * propre région ; seul un acteur PNA peut cibler explicitement une région
 * précise, à des fins de supervision nationale.
 *
 * <p>
 * Hypothèse : une seule PRA active par région (cf. doc. flows « Chaque
 * PRA et PNA disposent de leur propre entrepôt ») — si plusieurs PRA
 * actives existaient pour une même région, la première trouvée serait
 * utilisée. Ne dépend que de ports {@code shared} (voir
 * {@link ConsulterCatalogueNationalUseCaseImpl}), jamais des modules
 * {@code organisation}, {@code stock} ou {@code medicament} directement.
 * </p>
 */
@Service
public class ConsulterCatalogueRegionalUseCaseImpl implements ConsulterCatalogueRegionalUseCase {

    private static final String PREFIX = "catalogue:regional:";

    private final CatalogueAccessGuard catalogueAccessGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StockAgregeQueryPort stockAgregeQueryPort;
    private final MedicamentQueryPort medicamentQueryPort;
    private final CatalogueEntryAssembler catalogueEntryAssembler;
    private final JsonCacheSupport cache;
    private final AppProperties appProperties;

    public ConsulterCatalogueRegionalUseCaseImpl(CatalogueAccessGuard catalogueAccessGuard,
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
    public CataloguePage consulter(ConsulterCatalogueRegionalQuery query) {
        // La région effective est résolue AVANT toute lecture de cache : c'est
        // elle (jamais la région demandée) qui doit segmenter la clé, sous
        // peine de fuite du catalogue d'une région vers une autre (cf.
        // CatalogueAccessGuard : la région demandée est ignorée pour tout
        // acteur non national).
        UUID regionEffective = catalogueAccessGuard.resoudreRegionPourCatalogueRegional(query.regionId());

        String cleCache = cleCache(regionEffective, query);
        return cache.get(cleCache, CataloguePage.class)
                .orElseGet(() -> assembler(regionEffective, query, cleCache));
    }

    private CataloguePage assembler(UUID regionEffective, ConsulterCatalogueRegionalQuery query, String cleCache) {
        List<EntrepotProjection> pras = entrepotQueryPort.findPrasActivesParRegion(regionEffective);
        EntrepotProjection pra = pras.stream().findFirst().orElseThrow(AucunePraPourRegionException::new);

        List<StockAgregeProjection> lignes = stockAgregeQueryPort.rechercherParEntrepot(pra.id());

        CataloguePage page = catalogueEntryAssembler.assembler(lignes, medicamentQueryPort, query.recherche(),
                query.ruptureUniquement(), query.page(), query.size());

        cache.put(cleCache, page, appProperties.cache().catalogueTtl());
        return page;
    }

    private String cleCache(UUID regionEffective, ConsulterCatalogueRegionalQuery query) {
        return PREFIX + regionEffective + ":" + query.recherche() + ":" + query.ruptureUniquement() + ":"
                + query.page() + ":" + query.size();
    }
}
