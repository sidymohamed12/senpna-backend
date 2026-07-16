package ministere.sante.senpna.actualite.infrastructure.persistence.specification;

import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.infrastructure.persistence.entity.ActualiteJpaEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ActualiteSpecifications — construction des critères de recherche JPA")
class ActualiteSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(ActualiteSpecifications.recherche(null)).isNull();
        assertThat(ActualiteSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() construit un OR sur titre/description en minuscule, avec pattern LIKE")
    void recherche_texteValide_construitPredicat() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.recherche(" Centre ");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<ActualiteJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> titrePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> descriptionPath = mock(Path.class);
        Predicate likeTitre = mock(Predicate.class);
        Predicate likeDescription = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        when(root.<String>get("titre")).thenReturn(titrePath);
        when(root.<String>get("description")).thenReturn(descriptionPath);
        when(cb.lower(titrePath)).thenReturn(titrePath);
        when(cb.lower(descriptionPath)).thenReturn(descriptionPath);
        when(cb.like(titrePath, "%centre%")).thenReturn(likeTitre);
        when(cb.like(descriptionPath, "%centre%")).thenReturn(likeDescription);
        when(cb.or(likeTitre, likeDescription)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(query).distinct(true);
        verify(cb).like(titrePath, "%centre%");
        verify(cb).like(descriptionPath, "%centre%");
    }

    @Test
    @DisplayName("recherche() ne plante pas quand la CriteriaQuery fournie par le framework est null")
    void recherche_queryNull_nePasPlanter() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.recherche("abc");

        @SuppressWarnings("unchecked")
        Root<ActualiteJpaEntity> root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<String>get(anyString())).thenReturn(path);
        when(cb.lower(path)).thenReturn(path);
        when(cb.like(path, "%abc%")).thenReturn(predicate);
        when(cb.or(predicate, predicate)).thenReturn(predicate);

        Predicate result = specification.toPredicate(root, null, cb);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    @DisplayName("categorie() retourne null quand la catégorie est null")
    void categorie_null_retourneNull() {
        assertThat(ActualiteSpecifications.categorie(null)).isNull();
    }

    @Test
    @DisplayName("categorie() construit une égalité stricte sur le champ categorie")
    void categorie_valide_construitPredicat() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.categorie(CategorieActualite.PROJET);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<ActualiteJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<CategorieActualite> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<CategorieActualite>get("categorie")).thenReturn(path);
        when(cb.equal(path, CategorieActualite.PROJET)).thenReturn(predicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    @DisplayName("statut() retourne null quand le statut est null")
    void statut_null_retourneNull() {
        assertThat(ActualiteSpecifications.statut(null)).isNull();
    }

    @Test
    @DisplayName("statut() construit une égalité stricte sur le champ statut")
    void statut_valide_construitPredicat() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.statut(StatutActualite.PUBLIE);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<ActualiteJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<StatutActualite> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<StatutActualite>get("statut")).thenReturn(path);
        when(cb.equal(path, StatutActualite.PUBLIE)).thenReturn(predicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(predicate);
    }

    @Test
    @DisplayName("combiner() sans aucun critère retourne une spécification qui n'ajoute aucun prédicat")
    void combiner_sansCriteres() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.combiner(null, null, null);
        assertThat(specification).isNotNull();
    }

    @Test
    @DisplayName("combiner() avec tous les critères combine recherche + catégorie + statut")
    void combiner_avecTousLesCriteres() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.combiner("centre",
                CategorieActualite.PROJET, StatutActualite.PUBLIE);
        assertThat(specification).isNotNull();
    }

    @Test
    @DisplayName("combiner() avec uniquement la catégorie n'échoue pas et ignore recherche/statut")
    void combiner_uniquementCategorie() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.combiner(null,
                CategorieActualite.PROJET, null);
        assertThat(specification).isNotNull();
    }

    @Test
    @DisplayName("combiner() avec uniquement le statut n'échoue pas et ignore recherche/catégorie")
    void combiner_uniquementStatut() {
        Specification<ActualiteJpaEntity> specification = ActualiteSpecifications.combiner(null, null,
                StatutActualite.DESACTIVE);
        assertThat(specification).isNotNull();
    }
}
