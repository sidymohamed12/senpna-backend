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
 * La jointure {@code stocks}/{@code lots} est volontairement circonscrite
 * aux deux tables du module {@code stock} : ni le nom du médicament ni
 * les informations d'entrepôt ou de fournisseur n'y figurent, seul leur
 * identifiant est renvoyé — l'enrichissement (noms) se fait plus haut,
 * dans le module {@code catalogue}, via les projections {@code shared}.
 * </p>
 *
 * <p>
 * <strong>Requête native (Postgres)</strong>, pas du JPQL : la ventilation
 * par médicament doit à la fois (a) sommer les quantités de tous les lots
 * actifs — une agrégation classique — et (b) retenir le fournisseur du
 * lot le plus proche de la péremption (FEFO) — une colonne non agrégée,
 * fonctionnellement dépendante du tri, que JPQL ne sait pas exprimer sans
 * fonction fenêtrée. Le CTE {@code fefo} isole ce second besoin via
 * {@code DISTINCT ON}, spécifique à Postgres.
 * </p>
 */
public interface CatalogueStockJpaRepository extends Repository<StockJpaEntity, UUID> {

  @Query(value = """
      WITH agg AS (
          SELECT s.entrepot_id AS entrepot_id,
                 s.medicament_id AS medicament_id,
                 SUM(s.quantite_disponible) AS quantite_disponible,
                 SUM(s.quantite_reservee) AS quantite_reservee,
                 COUNT(l.id) AS nombre_lots_actifs,
                 MIN(l.date_expiration) AS prochaine_date_expiration,
                 AVG(l.prix_vente) AS prix_vente_moyen
          FROM stocks s
          JOIN lots l ON s.lot_id = l.id
          WHERE s.entrepot_id IN (:entrepotIds)
            AND l.statut = 'ACTIF'
            AND l.date_expiration >= CURRENT_DATE
          GROUP BY s.entrepot_id, s.medicament_id
      ),
      fefo AS (
          SELECT DISTINCT ON (s.entrepot_id, s.medicament_id)
                 s.entrepot_id AS entrepot_id,
                 s.medicament_id AS medicament_id,
                 l.fournisseur_id AS fournisseur_id
          FROM stocks s
          JOIN lots l ON s.lot_id = l.id
          WHERE s.entrepot_id IN (:entrepotIds)
            AND l.statut = 'ACTIF'
            AND l.date_expiration >= CURRENT_DATE
          ORDER BY s.entrepot_id, s.medicament_id, l.date_expiration ASC
      )
      SELECT agg.entrepot_id           AS "entrepotId",
             agg.medicament_id         AS "medicamentId",
             agg.quantite_disponible   AS "quantiteDisponible",
             agg.quantite_reservee     AS "quantiteReservee",
             agg.nombre_lots_actifs    AS "nombreLotsActifs",
             agg.prochaine_date_expiration AS "prochaineDateExpiration",
             agg.prix_vente_moyen      AS "prixVenteMoyen",
             fefo.fournisseur_id       AS "fournisseurId"
      FROM agg
      JOIN fefo ON agg.entrepot_id = fefo.entrepot_id AND agg.medicament_id = fefo.medicament_id
      """, nativeQuery = true)
  List<CatalogueAggregatRow> agregerParEntrepots(@Param("entrepotIds") Collection<UUID> entrepotIds);
}
