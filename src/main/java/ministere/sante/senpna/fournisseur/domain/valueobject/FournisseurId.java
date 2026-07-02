package ministere.sante.senpna.fournisseur.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class FournisseurId extends EntityId {

    private FournisseurId(UUID value) {
        super(value);
    }

    public static FournisseurId of(UUID value) {
        return new FournisseurId(value);
    }

    public static FournisseurId of(String value) {
        return new FournisseurId(UUID.fromString(value));
    }

    public static FournisseurId generate() {
        return new FournisseurId(UUID.randomUUID());
    }
}
