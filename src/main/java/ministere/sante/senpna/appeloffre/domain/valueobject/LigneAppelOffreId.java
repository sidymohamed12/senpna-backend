package ministere.sante.senpna.appeloffre.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class LigneAppelOffreId extends EntityId {

    private LigneAppelOffreId(UUID value) {
        super(value);
    }

    public static LigneAppelOffreId of(UUID value) {
        return new LigneAppelOffreId(value);
    }

    public static LigneAppelOffreId of(String value) {
        return new LigneAppelOffreId(UUID.fromString(value));
    }

    public static LigneAppelOffreId generate() {
        return new LigneAppelOffreId(UUID.randomUUID());
    }
}
