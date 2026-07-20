package ministere.sante.senpna.appeloffre.infrastructure.persistence.specification;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;
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

@DisplayName("AppelOffreSpecifications — construction des critères de recherche JPA")
class AppelOffreSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(AppelOffreSpecifications.recherche(null)).isNull();
        assertThat(AppelOffreSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge reference/objet")
    void recherche_texteAvecWildcard_echappeEtConstruitPredicat() {
        // "%" dans la saisie utilisateur ne doit PAS être traité comme un
        // joker LIKE — cf. LikePatternEscaper.
        Specification<AppelOffreJpaEntity> specification = AppelOffreSpecifications.recherche("100%");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<AppelOffreJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> referencePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> objetPath = mock(Path.class);
        Predicate likeReference = mock(Predicate.class);
        Predicate likeObjet = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        String motifEchappe = "%100\\%%";

        when(root.<String>get("reference")).thenReturn(referencePath);
        when(root.<String>get("objet")).thenReturn(objetPath);
        when(cb.lower(referencePath)).thenReturn(referencePath);
        when(cb.lower(objetPath)).thenReturn(objetPath);
        when(cb.like(referencePath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeReference);
        when(cb.like(objetPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeObjet);
        when(cb.or(likeReference, likeObjet)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(cb).like(referencePath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(objetPath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("statut() retourne null quand le statut est null")
    void statut_null_retourneNull() {
        assertThat(AppelOffreSpecifications.statut(null)).isNull();
    }

    @Test
    @DisplayName("statut() construit une égalité stricte sur le champ statut")
    void statut_valide_construitPredicat() {
        Specification<AppelOffreJpaEntity> specification = AppelOffreSpecifications.statut(StatutAppelOffre.PUBLIE);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<AppelOffreJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<StatutAppelOffre> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<StatutAppelOffre>get("statut")).thenReturn(path);
        when(cb.equal(path, StatutAppelOffre.PUBLIE)).thenReturn(predicate);

        assertThat(specification.toPredicate(root, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("combiner() sans critère ne plante pas")
    void combiner_sansCriteres() {
        assertThat(AppelOffreSpecifications.combiner(null, null)).isNotNull();
    }

    @Test
    @DisplayName("combiner() avec texte et statut combine les deux prédicats")
    void combiner_avecTousLesCriteres() {
        assertThat(AppelOffreSpecifications.combiner("appel", StatutAppelOffre.PUBLIE)).isNotNull();
    }
}
