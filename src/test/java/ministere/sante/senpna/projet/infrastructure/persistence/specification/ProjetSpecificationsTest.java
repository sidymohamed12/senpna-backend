package ministere.sante.senpna.projet.infrastructure.persistence.specification;

import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;
import ministere.sante.senpna.projet.infrastructure.persistence.entity.ProjetJpaEntity;
import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ProjetSpecifications — construction des critères de recherche JPA")
class ProjetSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(ProjetSpecifications.recherche(null)).isNull();
        assertThat(ProjetSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge nom/description")
    void recherche_texteAvecUnderscore_echappeEtConstruitPredicat() {
        Specification<ProjetJpaEntity> specification = ProjetSpecifications.recherche("projet_pilote");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<ProjetJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> nomPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> descriptionPath = mock(Path.class);
        Predicate likeNom = mock(Predicate.class);
        Predicate likeDescription = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        String motifEchappe = "%projet\\_pilote%";

        when(root.<String>get("nom")).thenReturn(nomPath);
        when(root.<String>get("description")).thenReturn(descriptionPath);
        when(cb.lower(nomPath)).thenReturn(nomPath);
        when(cb.lower(descriptionPath)).thenReturn(descriptionPath);
        when(cb.like(nomPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeNom);
        when(cb.like(descriptionPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeDescription);
        when(cb.or(likeNom, likeDescription)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(cb).like(nomPath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(descriptionPath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("categorie() retourne null quand la catégorie est null")
    void categorie_null_retourneNull() {
        assertThat(ProjetSpecifications.categorie(null)).isNull();
    }

    @Test
    @DisplayName("statut() retourne null quand le statut est null")
    void statut_null_retourneNull() {
        assertThat(ProjetSpecifications.statut(null)).isNull();
    }

    @Test
    @DisplayName("statut() construit une égalité stricte sur le champ statut")
    void statut_valide_construitPredicat() {
        Specification<ProjetJpaEntity> specification = ProjetSpecifications.statut(StatutProjet.PUBLIE);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<ProjetJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<StatutProjet> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<StatutProjet>get("statut")).thenReturn(path);
        when(cb.equal(path, StatutProjet.PUBLIE)).thenReturn(predicate);

        assertThat(specification.toPredicate(root, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("combiner() avec tous les critères ne plante pas")
    void combiner_avecTousLesCriteres() {
        assertThat(ProjetSpecifications.combiner("projet", CategorieProjet.SANTE, StatutProjet.PUBLIE))
                .isNotNull();
    }

    @Test
    @DisplayName("combiner() sans aucun critère ne plante pas")
    void combiner_sansCriteres() {
        assertThat(ProjetSpecifications.combiner(null, null, null)).isNotNull();
    }
}
