package ministere.sante.senpna.projet.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class ProjetId extends EntityId {

    private ProjetId(UUID value) {
        super(value);
    }

    public static ProjetId of(UUID value) {
        return new ProjetId(value);
    }

    public static ProjetId of(String value) {
        return new ProjetId(UUID.fromString(value));
    }

    public static ProjetId generate() {
        return new ProjetId(UUID.randomUUID());
    }
}
