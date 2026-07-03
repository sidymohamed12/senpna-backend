package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueInterPraUseCase;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;

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

    private final CatalogueAccessGuard catalogueAccessGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StockAgregeQueryPort stockAgregeQueryPort;
    private final MedicamentQueryPort medicamentQueryPort;
    private final CatalogueEntryAssembler catalogueEntryAssembler;

    public ConsulterCatalogueInterPraUseCaseImpl(CatalogueAccessGuard catalogueAccessGuard,
            EntrepotQueryPort entrepotQueryPort, StockAgregeQueryPort stockAgregeQueryPort,
            MedicamentQueryPort medicamentQueryPort, CatalogueEntryAssembler catalogueEntryAssembler) {
        this.catalogueAccessGuard = catalogueAccessGuard;
        this.entrepotQueryPort = entrepotQueryPort;
        this.stockAgregeQueryPort = stockAgregeQueryPort;
        this.medicamentQueryPort = medicamentQueryPort;
        this.catalogueEntryAssembler = catalogueEntryAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogueInterPraPage consulter(ConsulterCatalogueInterPraQuery query) {
        catalogueAccessGuard.verifierActeurPnaOuPra();

        List<EntrepotProjection> prasActives = entrepotQueryPort.findPrasActives();
        if (prasActives.isEmpty()) {
            int size = query.size() != null ? query.size() : PageRequest.DEFAULT_SIZE;
            return new CatalogueInterPraPage(List.of(), 0, size, 0, 0);
        }

        Set<UUID> entrepotIds = prasActives.stream().map(EntrepotProjection::id).collect(Collectors.toSet());
        List<StockAgregeProjection> lignes = stockAgregeQueryPort.rechercherParEntrepots(entrepotIds);

        return catalogueEntryAssembler.assemblerInterPra(lignes, prasActives, medicamentQueryPort,
                query.recherche(), query.medicamentId(), query.ruptureUniquement(), query.page(), query.size());
    }
}
