package ministere.sante.senpna.fournisseur.infrastructure.persistence.specification;

import ministere.sante.senpna.fournisseur.infrastructure.persistence.entity.FournisseurJpaEntity;
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

@DisplayName("FournisseurSpecifications — construction des critères de recherche JPA")
class FournisseurSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(FournisseurSpecifications.recherche(null)).isNull();
        assertThat(FournisseurSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge nom/email/telephone")
    void recherche_texteAvecWildcard_echappeEtConstruitPredicat() {
        Specification<FournisseurJpaEntity> specification = FournisseurSpecifications.recherche("pharma_sn%");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<FournisseurJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> nomPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> emailPath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> telephonePath = mock(Path.class);
        Predicate likeNom = mock(Predicate.class);
        Predicate likeEmail = mock(Predicate.class);
        Predicate likeTelephone = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        String motifEchappe = "%pharma\\_sn\\%%";

        when(root.<String>get("nom")).thenReturn(nomPath);
        when(root.<String>get("email")).thenReturn(emailPath);
        when(root.<String>get("telephone")).thenReturn(telephonePath);
        when(cb.lower(nomPath)).thenReturn(nomPath);
        when(cb.lower(emailPath)).thenReturn(emailPath);
        when(cb.lower(telephonePath)).thenReturn(telephonePath);
        when(cb.like(nomPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeNom);
        when(cb.like(emailPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeEmail);
        when(cb.like(telephonePath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeTelephone);
        when(cb.or(likeNom, likeEmail, likeTelephone)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(cb).like(nomPath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(emailPath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(telephonePath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("actif() retourne null quand le paramètre est null")
    void actif_null_retourneNull() {
        assertThat(FournisseurSpecifications.actif(null)).isNull();
    }

    @Test
    @DisplayName("actif() construit une égalité stricte sur le champ actif")
    void actif_valide_construitPredicat() {
        Specification<FournisseurJpaEntity> specification = FournisseurSpecifications.actif(true);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<FournisseurJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Boolean> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<Boolean>get("actif")).thenReturn(path);
        when(cb.equal(path, true)).thenReturn(predicate);

        assertThat(specification.toPredicate(root, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("combiner() avec tous les critères ne plante pas")
    void combiner_avecTousLesCriteres() {
        assertThat(FournisseurSpecifications.combiner("pharma", true)).isNotNull();
    }

    @Test
    @DisplayName("combiner() sans aucun critère ne plante pas")
    void combiner_sansCriteres() {
        assertThat(FournisseurSpecifications.combiner(null, null)).isNotNull();
    }
}
