package ministere.sante.senpna.catalogue.application.usecase;

import ministere.sante.senpna.catalogue.application.service.CatalogueAccessGuard;
import ministere.sante.senpna.catalogue.application.service.CatalogueEntryAssembler;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.exception.PnaCentraleIntrouvableException;
import ministere.sante.senpna.catalogue.domain.port.in.ConsulterCatalogueNationalUseCase;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.port.out.StockAgregeQueryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;

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
 */
@Service
public class ConsulterCatalogueNationalUseCaseImpl implements ConsulterCatalogueNationalUseCase {

    private final CatalogueAccessGuard catalogueAccessGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StockAgregeQueryPort stockAgregeQueryPort;
    private final MedicamentQueryPort medicamentQueryPort;
    private final CatalogueEntryAssembler catalogueEntryAssembler;

    public ConsulterCatalogueNationalUseCaseImpl(CatalogueAccessGuard catalogueAccessGuard,
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
    public CataloguePage consulter(ConsulterCatalogueNationalQuery query) {
        catalogueAccessGuard.verifierActeurPnaOuPra();

        EntrepotProjection pnaCentrale = entrepotQueryPort.findPnaCentraleActive()
                .orElseThrow(PnaCentraleIntrouvableException::new);

        List<StockAgregeProjection> lignes = stockAgregeQueryPort.rechercherParEntrepot(pnaCentrale.id());

        return catalogueEntryAssembler.assembler(lignes, medicamentQueryPort, query.recherche(),
                query.ruptureUniquement(), query.page(), query.size());
    }
}
