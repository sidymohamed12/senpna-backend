package ministere.sante.senpna.stock.infrastructure.persistence.repository;

import ministere.sante.senpna.stock.infrastructure.persistence.entity.StockJpaEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository <strong>en lecture seule</strong>, dédié à l'agrégation
 * catalogue — étend directement {@link Repository} (et non
 * {@code JpaRepository}) : aucune opération de sauvegarde/suppression
 * n'a de sens ici, seule la requête d'agrégation ci-dessous est exposée.
 *
 * <p>
 * La jointure {@code StockJpaEntity}/{@code LotJpaEntity} est réalisée en
 * JPQL multi-racines (pas de relation {@code @ManyToOne} entre les deux
 * entités — cf. Javadoc {@code Stock} : « jamais de relation JPA directe »
 * entre agrégats), volontairement circonscrite aux deux tables du module
 * {@code stock} : ni le nom du médicament ni les informations d'entrepôt
 * n'y figurent, elles sont enrichies plus haut, dans le module
 * {@code catalogue}.
 * </p>
 */
public interface CatalogueStockJpaRepository extends Repository<StockJpaEntity, UUID> {

    @Query("""
            SELECT s.entrepotId AS entrepotId,
                   s.medicamentId AS medicamentId,
                   SUM(s.quantiteDisponible) AS quantiteDisponible,
                   SUM(s.quantiteReservee) AS quantiteReservee,
                   COUNT(l.id) AS nombreLotsActifs,
                   MIN(l.dateExpiration) AS prochaineDateExpiration,
                   AVG(l.prixVente) AS prixVenteMoyen
            FROM StockJpaEntity s, LotJpaEntity l
            WHERE s.lotId = l.id
              AND s.entrepotId IN :entrepotIds
              AND l.statut = 'ACTIF'
              AND l.dateExpiration >= CURRENT_DATE
            GROUP BY s.entrepotId, s.medicamentId
            """)
    List<CatalogueAggregatRow> agregerParEntrepots(@Param("entrepotIds") Collection<UUID> entrepotIds);
}
