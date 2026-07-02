package ministere.sante.senpna.shared.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Mapping <strong>partiel</strong> et en lecture/écriture ciblée de la
 * table {@code users}, limité aux colonnes d'affectation organisationnelle
 * ({@code entrepot_id}, {@code structure_sanitaire_id}).
 *
 * <p>
 * L'agrégat {@code User} complet (identité, authentification, rôles) est
 * la propriété exclusive des modules {@code auth}/{@code utilisateurs}.
 * Cette entité — volontairement placée dans {@code shared} car utilisée
 * aussi bien par {@code utilisateurs} (affectation atomique à la
 * création) que par {@code organisation} (affectation ultérieure) — agit
 * uniquement sur ces deux colonnes via des requêtes ciblées
 * ({@code UPDATE ... SET ...}), jamais via un {@code INSERT} (elle n'est
 * donc jamais instanciée ni persistée directement, seulement lue/mise à
 * jour pour un utilisateur déjà existant).
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAffectationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "entrepot_id")
    private UUID entrepotId;

    @Column(name = "structure_sanitaire_id")
    private UUID structureSanitaireId;
}
