package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PageRequest")
class PageRequestTest {

    @Nested
    @DisplayName("constructeur canonique")
    class ConstructeurCanonique {

        @Test
        @DisplayName("accepte des valeurs valides telles quelles")
        void valeursValides() {
            PageRequest pageRequest = new PageRequest(2, 50, "nom", PageRequest.SortDirection.ASC);

            assertThat(pageRequest.page()).isEqualTo(2);
            assertThat(pageRequest.size()).isEqualTo(50);
            assertThat(pageRequest.sortBy()).isEqualTo("nom");
            assertThat(pageRequest.direction()).isEqualTo(PageRequest.SortDirection.ASC);
        }

        @Test
        @DisplayName("page négative → IllegalArgumentException")
        void pageNegative_leveException() {
            assertThatThrownBy(() -> new PageRequest(-1, 20, "nom", PageRequest.SortDirection.ASC))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("size à zéro → IllegalArgumentException")
        void sizeZero_leveException() {
            assertThatThrownBy(() -> new PageRequest(0, 0, "nom", PageRequest.SortDirection.ASC))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("size supérieure à MAX_SIZE → IllegalArgumentException")
        void sizeTropGrande_leveException() {
            assertThatThrownBy(() -> new PageRequest(0, PageRequest.MAX_SIZE + 1, "nom",
                    PageRequest.SortDirection.ASC)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("sortBy null → retombe sur DEFAULT_SORT_BY")
        void sortByNull_retombeSurDefaut() {
            PageRequest pageRequest = new PageRequest(0, 20, null, PageRequest.SortDirection.ASC);

            assertThat(pageRequest.sortBy()).isEqualTo(PageRequest.DEFAULT_SORT_BY);
        }

        @Test
        @DisplayName("sortBy blanc → retombe sur DEFAULT_SORT_BY")
        void sortByBlanc_retombeSurDefaut() {
            PageRequest pageRequest = new PageRequest(0, 20, "   ", PageRequest.SortDirection.ASC);

            assertThat(pageRequest.sortBy()).isEqualTo(PageRequest.DEFAULT_SORT_BY);
        }

        @Test
        @DisplayName("direction null → retombe sur DESC")
        void directionNull_retombeSurDesc() {
            PageRequest pageRequest = new PageRequest(0, 20, "nom", null);

            assertThat(pageRequest.direction()).isEqualTo(PageRequest.SortDirection.DESC);
        }
    }

    @Nested
    @DisplayName("of() — fabrique tolérante aux nulls")
    class Of {

        @Test
        @DisplayName("page et size null → valeurs par défaut (0, DEFAULT_SIZE)")
        void pageEtSizeNull_valeursParDefaut() {
            PageRequest pageRequest = PageRequest.of(null, null, "nom", "ASC");

            assertThat(pageRequest.page()).isZero();
            assertThat(pageRequest.size()).isEqualTo(PageRequest.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("page et size fournis → conservés")
        void pageEtSizeFournis_conserves() {
            PageRequest pageRequest = PageRequest.of(3, 10, "nom", "ASC");

            assertThat(pageRequest.page()).isEqualTo(3);
            assertThat(pageRequest.size()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("SortDirection.from()")
    class SortDirectionFrom {

        @Test
        @DisplayName("null → DESC")
        void nul_desc() {
            assertThat(PageRequest.SortDirection.from(null)).isEqualTo(PageRequest.SortDirection.DESC);
        }

        @Test
        @DisplayName("blanc → DESC")
        void blanc_desc() {
            assertThat(PageRequest.SortDirection.from("  ")).isEqualTo(PageRequest.SortDirection.DESC);
        }

        @Test
        @DisplayName("valeur invalide → DESC")
        void valeurInvalide_desc() {
            assertThat(PageRequest.SortDirection.from("SIDEWAYS")).isEqualTo(PageRequest.SortDirection.DESC);
        }

        @Test
        @DisplayName("'asc' insensible à la casse et aux espaces → ASC")
        void ascInsensibleCasse() {
            assertThat(PageRequest.SortDirection.from("  asc  ")).isEqualTo(PageRequest.SortDirection.ASC);
        }

        @Test
        @DisplayName("'DESC' → DESC")
        void desc_desc() {
            assertThat(PageRequest.SortDirection.from("DESC")).isEqualTo(PageRequest.SortDirection.DESC);
        }
    }
}
