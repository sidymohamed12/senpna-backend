package ministere.sante.senpna.shared.domain.valueobject;

/**
 * Paramètres de pagination — objet de domaine indépendant de tout framework.
 *
 * <p>
 * Les use cases et ports ne doivent jamais dépendre de {@code org.springframework.data.domain.Pageable} :
 * cela ferait fuiter un détail d'infrastructure (Spring Data) dans le domaine, en violation de la règle de
 * dépendance de la Clean Architecture. La traduction vers/depuis les types Spring Data est effectuée
 * exclusivement dans la couche infrastructure (adapters de persistance).
 * </p>
 */
public record PageRequest(int page, int size, String sortBy, SortDirection direction) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Le numéro de page ne peut pas être négatif");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("La taille de page doit être comprise entre 1 et " + MAX_SIZE);
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = DEFAULT_SORT_BY;
        }
        if (direction == null) {
            direction = SortDirection.DESC;
        }
    }

    /**
     * Fabrique tolérante aux valeurs nulles — utilisée depuis la couche web où
     * les paramètres de requête (query params) sont optionnels.
     */
    public static PageRequest of(Integer page, Integer size, String sortBy, String direction) {
        return new PageRequest(
                page != null ? page : 0,
                size != null ? size : DEFAULT_SIZE,
                sortBy,
                SortDirection.from(direction));
    }

    public enum SortDirection {
        ASC, DESC;

        public static SortDirection from(String value) {
            if (value == null || value.isBlank()) {
                return DESC;
            }
            try {
                return SortDirection.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return DESC;
            }
        }
    }
}
