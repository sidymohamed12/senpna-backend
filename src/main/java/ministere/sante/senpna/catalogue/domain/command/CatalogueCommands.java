package ministere.sante.senpna.catalogue.domain.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Requêtes et résultats du module {@code catalogue} — regroupés dans une
 * seule classe utilitaire, comme {@code StockCommands} / {@code LotCommands}
 * dans le module {@code stock}, pour garder les ports {@code in} lisibles.
 *
 * <p>
 * Aucun de ces enregistrements ne correspond à une table persistée : le
 * catalogue est une <strong>vue calculée</strong> sur les données déjà
 * possédées par {@code stock} (lignes de stock, lots), {@code medicament}
 * (référentiel national) et {@code organisation} (entrepôts, régions).
 * </p>
 */
public final class CatalogueCommands {

        private CatalogueCommands() {
        }

        // ── Requêtes ─────────────────────────────────────────────────────────

        /**
         * Consultation du catalogue PNA — cf. règle « visible que par les PRA et le PNA
         * ».
         */
        public record ConsulterCatalogueNationalQuery(String recherche, Boolean ruptureUniquement, Integer page,
                        Integer size) {
        }

        /**
         * Consultation du catalogue inter-PRA — cf. règle « visible par tous
         * les PRA et PNA ». {@code medicamentId}, s'il est fourni, restreint
         * la recherche de disponibilités à un seul médicament (cas d'usage
         * principal : une PRA en rupture cherche qui, parmi les autres PRA,
         * peut lui céder un transfert — cf. doc. flows §CAS 3).
         */
        public record ConsulterCatalogueInterPraQuery(String recherche, UUID medicamentId, Boolean ruptureUniquement,
                        Integer page, Integer size) {
        }

        /**
         * Consultation du catalogue régional d'une PRA — cf. règle « visible
         * que par les structures sanitaires appartenant à leur région ».
         * {@code regionId} n'est pris en compte que pour un acteur nationnal
         * (PNA) ; pour tout autre acteur (structure sanitaire ou PRA), la
         * région est résolue automatiquement à partir de son affectation et
         * ce champ est ignoré (cf. {@code CatalogueAccessGuard}).
         */
        public record ConsulterCatalogueRegionalQuery(UUID regionId, String recherche, Boolean ruptureUniquement,
                        Integer page, Integer size) {
        }

        // ── Résultats ────────────────────────────────────────────────────────

        /**
         * Conditionnement achetable pour un médicament (niveau d'emballage
         * avec son prix) — cf. règle « on ne vend pas par comprimé... mais
         * par boîte ou carton ». Seuls les conditionnements ayant un prix
         * défini sont listés ; c'est cette liste qui permet à l'acheteur de
         * choisir dans quelle unité commander.
         */
        public record ConditionnementCatalogue(UUID id, String nom, int niveau, BigDecimal quantiteUniteBase,
                        BigDecimal prixAchat, BigDecimal prixVente) {
        }

        /**
         * Ligne de catalogue « simple » — un entrepôt, un médicament, une
         * disponibilité.
         */
        public record LigneCatalogue(UUID medicamentId, String code, String nomCommercial, String dci,
                        String familleNom, String fabricant, String fournisseurNom,
                        BigDecimal quantiteDisponibleALaVente,
                        int nombreLotsActifs, LocalDate prochaineDateExpiration, boolean enRupture,
                        List<ConditionnementCatalogue> conditionnements) {
        }

        public record CataloguePage(List<LigneCatalogue> content, int page, int size, long totalElements,
                        int totalPages) {
        }

        /**
         * Disponibilité d'un médicament dans une PRA donnée (bloc du catalogue
         * inter-PRA).
         */
        public record DisponibilitePra(UUID entrepotId, String codeEntrepot, String nomEntrepot, UUID regionId,
                        String fournisseurNom, BigDecimal quantiteDisponibleALaVente,
                        LocalDate prochaineDateExpiration) {
        }

        /**
         * Ligne de catalogue inter-PRA — un médicament, ventilé par PRA disposant d'une
         * disponibilité.
         */
        public record LigneCatalogueInterPra(UUID medicamentId, String code, String nomCommercial, String dci,
                        String familleNom, String fabricant, BigDecimal quantiteTotaleReseau,
                        List<ConditionnementCatalogue> conditionnements, List<DisponibilitePra> disponibilites) {
        }

        public record CatalogueInterPraPage(List<LigneCatalogueInterPra> content, int page, int size,
                        long totalElements, int totalPages) {
        }
}
