package ministere.sante.senpna.catalogue.application.service;

import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConditionnementCatalogue;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.DisponibilitePra;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogue;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogueInterPra;
import ministere.sante.senpna.shared.domain.port.out.ConditionnementQueryPort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.port.out.MedicamentQueryPort;
import ministere.sante.senpna.shared.domain.projection.ConditionnementProjection;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.FournisseurProjection;
import ministere.sante.senpna.shared.domain.projection.MedicamentProjection;
import ministere.sante.senpna.shared.domain.projection.StockAgregeProjection;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assemble les projections de stock agrégées ({@link StockAgregeProjection},
 * port {@code shared}) avec le référentiel médicament
 * ({@link MedicamentProjection}), les fournisseurs
 * ({@link FournisseurCachePort})
 * et les conditionnements achetables ({@link ConditionnementQueryPort}) pour
 * produire les pages de catalogue exposées par les use cases — ne dépend
 * que de {@code shared}, jamais des modules {@code stock}, {@code medicament},
 * {@code fournisseur} ou {@code organisation} directement.
 *
 * <h3>Pourquoi une pagination en mémoire</h3>
 * <p>
 * L'agrégation par entrepôt ne peut pas être filtrée par texte au niveau
 * SQL : le nom commercial et la DCI appartiennent au référentiel
 * médicament, agrégat indépendant des lignes de stock (cf. Javadoc
 * {@code Stock} : « jamais de relation JPA directe » entre agrégats). Le
 * nombre de médicaments actifs d'un référentiel national reste, en
 * pratique, de l'ordre de quelques centaines à quelques milliers de
 * lignes : filtrer et paginer en mémoire après enrichissement est donc un
 * compromis pragmatique, largement préférable à la complexité d'une vue
 * matérialisée pour ce volume de données.
 * </p>
 */
@Component
public class CatalogueEntryAssembler {

        private final FournisseurCachePort fournisseurCachePort;
        private final ConditionnementQueryPort conditionnementQueryPort;

        public CatalogueEntryAssembler(FournisseurCachePort fournisseurCachePort,
                        ConditionnementQueryPort conditionnementQueryPort) {
                this.fournisseurCachePort = fournisseurCachePort;
                this.conditionnementQueryPort = conditionnementQueryPort;
        }

        /** Assemble une page de catalogue « simple » (un seul entrepôt). */
        public CataloguePage assembler(List<StockAgregeProjection> lignes, MedicamentQueryPort medicamentQueryPort,
                        String recherche, Boolean ruptureUniquement, Integer pageDemandee, Integer sizeDemande) {

                Map<UUID, MedicamentProjection> medicaments = chargerMedicaments(
                                lignes.stream().map(StockAgregeProjection::medicamentId), medicamentQueryPort);
                Map<UUID, List<ConditionnementCatalogue>> conditionnementsParMedicament = chargerConditionnements(
                                medicaments.keySet());

                List<LigneCatalogue> toutes = lignes.stream()
                                .filter(ligne -> medicaments.containsKey(ligne.medicamentId()))
                                .map(ligne -> versLigneCatalogue(ligne, medicaments.get(ligne.medicamentId()),
                                                conditionnementsParMedicament.getOrDefault(ligne.medicamentId(),
                                                                List.of())))
                                .filter(ligne -> correspondRecherche(ligne.nomCommercial(), ligne.dci(), ligne.code(),
                                                recherche))
                                .filter(ligne -> !Boolean.TRUE.equals(ruptureUniquement) || ligne.enRupture())
                                .sorted(Comparator.comparing(LigneCatalogue::nomCommercial,
                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                PageResult<LigneCatalogue> page = paginerEnMemoire(toutes, pageDemandee, sizeDemande);
                return new CataloguePage(page.content(), page.page(), page.size(), page.totalElements(),
                                page.totalPages());
        }

        /**
         * Assemble une page de catalogue inter-PRA (plusieurs entrepôts, ventilé par
         * PRA).
         */
        public CatalogueInterPraPage assemblerInterPra(List<StockAgregeProjection> lignes,
                        List<EntrepotProjection> prasActives, MedicamentQueryPort medicamentQueryPort, String recherche,
                        UUID medicamentIdDemande, Boolean ruptureUniquement, Integer pageDemandee,
                        Integer sizeDemande) {

                Map<UUID, EntrepotProjection> prasParId = prasActives.stream()
                                .collect(Collectors.toMap(EntrepotProjection::id, Function.identity()));

                Map<UUID, MedicamentProjection> medicaments = chargerMedicaments(
                                lignes.stream().map(StockAgregeProjection::medicamentId), medicamentQueryPort);
                Map<UUID, List<ConditionnementCatalogue>> conditionnementsParMedicament = chargerConditionnements(
                                medicaments.keySet());

                Map<UUID, List<StockAgregeProjection>> parMedicament = lignes.stream()
                                .filter(ligne -> medicaments.containsKey(ligne.medicamentId()))
                                .filter(ligne -> prasParId.containsKey(ligne.entrepotId()))
                                .filter(ligne -> medicamentIdDemande == null
                                                || medicamentIdDemande.equals(ligne.medicamentId()))
                                .collect(Collectors.groupingBy(StockAgregeProjection::medicamentId));

                List<LigneCatalogueInterPra> toutes = parMedicament.entrySet().stream()
                                .map(entry -> versLigneCatalogueInterPra(medicaments.get(entry.getKey()),
                                                entry.getValue(),
                                                prasParId,
                                                conditionnementsParMedicament.getOrDefault(entry.getKey(), List.of())))
                                .filter(ligne -> correspondRecherche(ligne.nomCommercial(), ligne.dci(), ligne.code(),
                                                recherche))
                                .filter(ligne -> !Boolean.TRUE.equals(ruptureUniquement)
                                                || ligne.quantiteTotaleReseau().signum() <= 0)
                                .sorted(Comparator.comparing(LigneCatalogueInterPra::nomCommercial,
                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                PageResult<LigneCatalogueInterPra> page = paginerEnMemoire(toutes, pageDemandee, sizeDemande);
                return new CatalogueInterPraPage(
                                page.content(), page.page(), page.size(), page.totalElements(), page.totalPages());
        }

        // ── Assemblage unitaire ──────────────────────────────────────────────

        private LigneCatalogue versLigneCatalogue(StockAgregeProjection ligne, MedicamentProjection medicament,
                        List<ConditionnementCatalogue> conditionnements) {
                return new LigneCatalogue(
                                medicament.id(),
                                medicament.code(),
                                medicament.nomCommercial(),
                                medicament.dci(),
                                medicament.familleNom(),
                                medicament.fabricant(),
                                resoudreNomFournisseur(ligne.fournisseurId()),
                                ligne.quantiteDisponibleALaVente(),
                                ligne.nombreLotsActifs(),
                                ligne.prochaineDateExpiration(),
                                ligne.enRupture(),
                                conditionnements);
        }

        private LigneCatalogueInterPra versLigneCatalogueInterPra(MedicamentProjection medicament,
                        List<StockAgregeProjection> lignesParPra, Map<UUID, EntrepotProjection> prasParId,
                        List<ConditionnementCatalogue> conditionnements) {

                List<DisponibilitePra> disponibilites = lignesParPra.stream()
                                .map(ligne -> {
                                        EntrepotProjection pra = prasParId.get(ligne.entrepotId());
                                        return new DisponibilitePra(
                                                        pra.id(),
                                                        pra.code(),
                                                        pra.nom(),
                                                        pra.regionId(),
                                                        resoudreNomFournisseur(ligne.fournisseurId()),
                                                        ligne.quantiteDisponibleALaVente(),
                                                        ligne.prochaineDateExpiration());
                                })
                                .sorted(Comparator.comparing(DisponibilitePra::quantiteDisponibleALaVente).reversed())
                                .toList();

                BigDecimal quantiteTotale = disponibilites.stream()
                                .map(DisponibilitePra::quantiteDisponibleALaVente)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new LigneCatalogueInterPra(
                                medicament.id(),
                                medicament.code(),
                                medicament.nomCommercial(),
                                medicament.dci(),
                                medicament.familleNom(),
                                medicament.fabricant(),
                                quantiteTotale,
                                conditionnements,
                                disponibilites);
        }

        // ── Utilitaires ──────────────────────────────────────────────────────

        private Map<UUID, MedicamentProjection> chargerMedicaments(Stream<UUID> medicamentIds,
                        MedicamentQueryPort medicamentQueryPort) {
                List<UUID> ids = medicamentIds.distinct().toList();
                return medicamentQueryPort.findAllById(ids).stream()
                                .collect(Collectors.toMap(MedicamentProjection::id, Function.identity()));
        }

        private Map<UUID, List<ConditionnementCatalogue>> chargerConditionnements(Collection<UUID> medicamentIds) {
                return conditionnementQueryPort.findAllVendablesByMedicamentIdIn(medicamentIds).stream()
                                .collect(Collectors.groupingBy(ConditionnementProjection::medicamentId,
                                                Collectors.mapping(this::versConditionnementCatalogue,
                                                                Collectors.toList())));
        }

        private ConditionnementCatalogue versConditionnementCatalogue(ConditionnementProjection conditionnement) {
                return new ConditionnementCatalogue(
                                conditionnement.id(),
                                conditionnement.nom(),
                                conditionnement.niveau(),
                                conditionnement.quantiteUniteBase(),
                                conditionnement.prixAchat(),
                                conditionnement.prixVente());
        }

        private String resoudreNomFournisseur(UUID fournisseurId) {
                if (fournisseurId == null) {
                        return null;
                }
                return fournisseurCachePort.findById(fournisseurId).map(FournisseurProjection::nom).orElse(null);
        }

        private boolean correspondRecherche(String nomCommercial, String dci, String code, String recherche) {
                if (recherche == null || recherche.isBlank()) {
                        return true;
                }
                String motif = recherche.trim().toLowerCase(Locale.ROOT);
                return contient(nomCommercial, motif) || contient(dci, motif) || contient(code, motif);
        }

        private boolean contient(String valeur, String motif) {
                return valeur != null && valeur.toLowerCase(Locale.ROOT).contains(motif);
        }

        private <T> PageResult<T> paginerEnMemoire(List<T> tousLesElements, Integer pageDemandee, Integer sizeDemande) {
                int page = pageDemandee != null && pageDemandee >= 0 ? pageDemandee : 0;
                int size = sizeDemande != null && sizeDemande > 0
                                ? Math.min(sizeDemande, PageRequest.MAX_SIZE)
                                : PageRequest.DEFAULT_SIZE;

                int debut = Math.min(page * size, tousLesElements.size());
                int fin = Math.min(debut + size, tousLesElements.size());

                return PageResult.of(tousLesElements.subList(debut, fin), page, size, tousLesElements.size());
        }
}
