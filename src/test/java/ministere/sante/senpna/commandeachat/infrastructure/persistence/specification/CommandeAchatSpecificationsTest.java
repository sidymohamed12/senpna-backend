package ministere.sante.senpna.commandeachat.infrastructure.persistence.specification;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;
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

@DisplayName("CommandeAchatSpecifications — construction des critères de recherche JPA")
class CommandeAchatSpecificationsTest {

    @Test
    @DisplayName("recherche() retourne null quand le texte est null ou vide")
    void recherche_texteVide_retourneNull() {
        assertThat(CommandeAchatSpecifications.recherche(null)).isNull();
        assertThat(CommandeAchatSpecifications.recherche("   ")).isNull();
    }

    @Test
    @DisplayName("recherche() échappe les métacaractères LIKE et interroge reference")
    void recherche_texteAvecWildcard_echappeEtConstruitPredicat() {
        Specification<CommandeAchatJpaEntity> specification = CommandeAchatSpecifications.recherche("BC_2026%");
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<CommandeAchatJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> referencePath = mock(Path.class);
        Predicate likeReference = mock(Predicate.class);

        String motifEchappe = "%bc\\_2026\\%%";

        when(root.<String>get("reference")).thenReturn(referencePath);
        when(cb.lower(referencePath)).thenReturn(referencePath);
        when(cb.like(referencePath, motifEchappe, LikePatternEscaper.escapeChar())).thenReturn(likeReference);

        Predicate result = specification.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(likeReference);
        verify(cb).like(referencePath, motifEchappe, LikePatternEscaper.escapeChar());
    }

    @Test
    @DisplayName("statut() retourne null quand le statut est null")
    void statut_null_retourneNull() {
        assertThat(CommandeAchatSpecifications.statut(null)).isNull();
    }

    @Test
    @DisplayName("statut() construit une égalité stricte sur le champ statut")
    void statut_valide_construitPredicat() {
        Specification<CommandeAchatJpaEntity> specification = CommandeAchatSpecifications
                .statut(StatutCommandeAchat.VALIDEE);
        assertThat(specification).isNotNull();

        @SuppressWarnings("unchecked")
        Root<CommandeAchatJpaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<StatutCommandeAchat> path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.<StatutCommandeAchat>get("statut")).thenReturn(path);
        when(cb.equal(path, StatutCommandeAchat.VALIDEE)).thenReturn(predicate);

        assertThat(specification.toPredicate(root, query, cb)).isEqualTo(predicate);
    }

    @Test
    @DisplayName("combiner() avec tous les critères ne plante pas")
    void combiner_avecTousLesCriteres() {
        assertThat(CommandeAchatSpecifications.combiner("BC-2026", StatutCommandeAchat.VALIDEE)).isNotNull();
    }

    @Test
    @DisplayName("combiner() sans aucun critère ne plante pas")
    void combiner_sansCriteres() {
        assertThat(CommandeAchatSpecifications.combiner(null, null)).isNotNull();
    }
}
