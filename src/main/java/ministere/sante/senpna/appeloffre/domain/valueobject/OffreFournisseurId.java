package ministere.sante.senpna.appeloffre.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class OffreFournisseurId extends EntityId {

    private OffreFournisseurId(UUID value) {
        super(value);
    }

    public static OffreFournisseurId of(UUID value) {
        return new OffreFournisseurId(value);
    }

    public static OffreFournisseurId of(String value) {
        return new OffreFournisseurId(UUID.fromString(value));
    }

    public static OffreFournisseurId generate() {
        return new OffreFournisseurId(UUID.randomUUID());
    }
}
