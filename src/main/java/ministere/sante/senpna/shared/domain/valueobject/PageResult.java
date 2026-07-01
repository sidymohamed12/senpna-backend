package ministere.sante.senpna.shared.domain.valueobject;

import java.util.List;

/**
 * Résultat paginé générique — retourné par les ports de persistence,
 * indépendant de tout framework (voir {@link PageRequest}).
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / (double) size);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    public boolean isFirst() {
        return page <= 0;
    }

    public boolean isLast() {
        return page >= totalPages - 1;
    }
}
