package ministere.sante.senpna.appeloffre.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class LigneOffreId extends EntityId {

    private LigneOffreId(UUID value) {
        super(value);
    }

    public static LigneOffreId of(UUID value) {
        return new LigneOffreId(value);
    }

    public static LigneOffreId of(String value) {
        return new LigneOffreId(UUID.fromString(value));
    }

    public static LigneOffreId generate() {
        return new LigneOffreId(UUID.randomUUID());
    }
}
