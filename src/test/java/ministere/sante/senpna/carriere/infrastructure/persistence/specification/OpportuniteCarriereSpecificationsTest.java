package ministere.sante.senpna.carriere.infrastructure.persistence.specification;

import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.carriere.infrastructure.persistence.entity.OpportuniteCarriereJpaEntity;
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

@DisplayName("OpportuniteCarriereSpecifications — construction des critères de recherche JPA")
class OpportuniteCarriereSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(OpportuniteCarriereSpecifications.recherche(null)).isNull();
        assertThat(OpportuniteCarriereSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge titre/nomEntreprise/lieu")
    void recherche_texteAvecWildcard_echappeEtConstruitPredicat() {
        Specification<OpportuniteCarriereJpaEntity> specification = OpportuniteCarriereSpecifications
                .recherche("pharmacien%");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<OpportuniteCarriereJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> titrePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> entreprisePath = mock(Path.class);
        @SuppressWarnings("unchecked")
        Path<String> lieuPath = mock(Path.class);
        Predicate likeTitre = mock(Predicate.class);
        Predicate likeEntreprise = mock(Predicate.class);
        Predicate likeLieu = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        String motifEchappe = "%pharmacien\\%%";

        when(root.<String>get("titre")).thenReturn(titrePath);
        when(root.<String>get("nomEntreprise")).thenReturn(entreprisePath);
        when(root.<String>get("lieu")).thenReturn(lieuPath);
        when(cb.lower(titrePath)).thenReturn(titrePath);
        when(cb.lower(entreprisePath)).thenReturn(entreprisePath);
        when(cb.lower(lieuPath)).thenReturn(lieuPath);
        when(cb.like(titrePath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeTitre);
        when(cb.like(entreprisePath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeEntreprise);
        when(cb.like(lieuPath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeLieu);
        when(cb.or(likeTitre, likeEntreprise, likeLieu)).thenReturn(orPredicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(orPredicate);
        verify(cb).like(titrePath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(entreprisePath, motifEchappe, LikePatternEscaper.escapeChar());
        verify(cb).like(lieuPath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("typeContrat() retourne null quand le type est null")
    void typeContrat_null_retourneNull() {
        assertThat(OpportuniteCarriereSpecifications.typeContrat(null)).isNull();
    }

    @Test
    @DisplayName("statut() retourne null quand le statut est null")
    void statut_null_retourneNull() {
        assertThat(OpportuniteCarriereSpecifications.statut(null)).isNull();
    }

    @Test
    @DisplayName("visiblePubliquement() restreint aux statuts OUVERT/EN_COURS et date limite non dépassée")
    void visiblePubliquement_construitPredicat() {
        Specification<OpportuniteCarriereJpaEntity> specification = OpportuniteCarriereSpecifications
                .visiblePubliquement();
        assertThat(specification).isNotNull();
    }

    @Test
    @DisplayName("combiner() en mode public ignore le paramètre statut au profit de visiblePubliquement()")
    void combiner_modePublic() {
        assertThat(OpportuniteCarriereSpecifications.combiner("pharmacien", TypeContrat.CDI, null, true))
                .isNotNull();
    }

    @Test
    @DisplayName("combiner() en mode admin applique le statut demandé")
    void combiner_modeAdmin() {
        assertThat(OpportuniteCarriereSpecifications.combiner(null, null, StatutOpportunite.BROUILLON, false))
                .isNotNull();
    }
}
