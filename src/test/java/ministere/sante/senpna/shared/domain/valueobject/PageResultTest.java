package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageResult")
class PageResultTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("calcule le nombre total de pages par arrondi supérieur")
        void calculeTotalPagesParArrondiSuperieur() {
            PageResult<String> result = PageResult.of(List.of("a", "b"), 0, 20, 21);

            assertThat(result.totalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("totalElements exactement divisible par size → pas de page supplémentaire")
        void totalElementsDivisibleParSize() {
            PageResult<String> result = PageResult.of(List.of(), 0, 20, 40);

            assertThat(result.totalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("size à zéro → totalPages à zéro, sans division par zéro")
        void sizeZero_totalPagesZero() {
            PageResult<String> result = PageResult.of(List.of(), 0, 0, 10);

            assertThat(result.totalPages()).isZero();
        }

        @Test
        @DisplayName("aucun élément → totalPages à zéro")
        void aucunElement_totalPagesZero() {
            PageResult<String> result = PageResult.of(List.of(), 0, 20, 0);

            assertThat(result.totalPages()).isZero();
        }
    }

    @Nested
    @DisplayName("isFirst()")
    class IsFirst {

        @Test
        @DisplayName("page à zéro → true")
        void pageZero_true() {
            assertThat(PageResult.of(List.of(), 0, 20, 40).isFirst()).isTrue();
        }

        @Test
        @DisplayName("page supérieure à zéro → false")
        void pageSuperieure_false() {
            assertThat(PageResult.of(List.of(), 1, 20, 40).isFirst()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLast()")
    class IsLast {

        @Test
        @DisplayName("dernière page → true")
        void dernierePage_true() {
            assertThat(PageResult.of(List.of(), 1, 20, 40).isLast()).isTrue();
        }

        @Test
        @DisplayName("page intermédiaire → false")
        void pageIntermediaire_false() {
            assertThat(PageResult.of(List.of(), 0, 20, 60).isLast()).isFalse();
        }
    }
}
