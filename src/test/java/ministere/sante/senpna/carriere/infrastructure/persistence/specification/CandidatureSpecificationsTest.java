package ministere.sante.senpna.carriere.infrastructure.persistence.specification;

import ministere.sante.senpna.carriere.infrastructure.persistence.entity.CandidatureJpaEntity;
import ministere.sante.senpna.shared.infrastructure.util.LikePatternEscaper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CandidatureSpecifications — construction des critères de recherche JPA")
class CandidatureSpecificationsTest {

    @Test
    @DisplayName("opportunite() retourne null quand l'id est null")
    void opportunite_null_retourneNull() {
        assertThat(CandidatureSpecifications.opportunite(null)).isNull();
    }

    @Test
    @DisplayName("opportunite() construit une égalité stricte sur opportuniteId")
    void opportunite_valide_construitPredicat() {
        UUID id = UUID.randomUUID();
        Specification<CandidatureJpaEntity> specification = CandidatureSpecifications.opportunite(id);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<CandidatureJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<UUID> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<UUID>get("opportuniteId")).thenReturn(path);
        when(cb.equal(path, id)).thenReturn(predicate);

        assertThat(specification.toPredicate(root, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(CandidatureSpecifications.recherche(null)).isNull();
        assertThat(CandidatureSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge nomComplet/email")
    void recherche_texteAvecWildcard_echappeEtConstruitPredicat() {
        // Champ alimenté par le formulaire public de candidature — cas
        // d'usage le plus sensible pour l'échappement LIKE.
        Specification<CandidatureJpaEntity> specification = CandidatureSpecifications.recherche("diallo_a%");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<CandidatureJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> nomPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> emailPath = mock(Path.class);
        Predicate likeNom = mock(Predicate.class);
        Predicate likeEmail = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        String motifEchappe = "%diallo\\_a\\%%";

        when(root.<String>get("nomComplet")).thenReturn(nomPath);
        when(root.<String>get("email")).thenReturn(emailPath);
        when(cb.lower(nomPath)).thenReturn(nomPath);
        when(cb.lower(emailPath)).thenReturn(emailPath);
        when(cb.like(nomPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeNom);
        when(cb.like(emailPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeEmail);
        when(cb.or(likeNom, likeEmail)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(cb).like(nomPath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(emailPath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("combiner() avec tous les critères ne plante pas")
    void combiner_avecTousLesCriteres() {
        assertThat(CandidatureSpecifications.combiner(UUID.randomUUID(), "diallo")).isNotNull();
    }

    @Test
    @DisplayName("combiner() sans aucun critère ne plante pas")
    void combiner_sansCriteres() {
        assertThat(CandidatureSpecifications.combiner(null, null)).isNotNull();
    }
}
