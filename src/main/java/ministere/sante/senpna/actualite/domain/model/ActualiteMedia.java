package ministere.sante.senpna.actualite.domain.model;

import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import java.util.Objects;
import java.util.UUID;

public final class ActualiteMedia {

    private static final int URL_MAX_LENGTH = 1000;

    private final UUID id;
    private final TypeMedia type;
    private final String url;
    private final int ordre;

    private ActualiteMedia(UUID id, TypeMedia type, String url, int ordre) {
        this.id = Objects.requireNonNull(id, "L'identifiant du média ne peut pas être null");
        this.type = Objects.requireNonNull(type, "Le type de média est obligatoire");
        this.url = validerUrl(url);
        this.ordre = ordre;
    }

    public static ActualiteMedia creer(TypeMedia type, String url, int ordre) {
        return new ActualiteMedia(UUID.randomUUID(), type, url, ordre);
    }

    public static ActualiteMedia reconstruct(UUID id, TypeMedia type, String url, int ordre) {
        return new ActualiteMedia(id, type, url, ordre);
    }

    private static String validerUrl(String url) {
        Objects.requireNonNull(url, "L'URL du média ne peut pas être null");
        String trimmed = url.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("L'URL du média ne peut pas être vide");
        }
        if (trimmed.length() > URL_MAX_LENGTH) {
            throw new IllegalArgumentException("L'URL du média ne peut pas dépasser " + URL_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public TypeMedia getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public int getOrdre() {
        return ordre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ActualiteMedia that = (ActualiteMedia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
