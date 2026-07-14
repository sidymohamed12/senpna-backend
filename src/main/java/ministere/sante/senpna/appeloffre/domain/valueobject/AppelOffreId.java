package ministere.sante.senpna.appeloffre.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class AppelOffreId extends EntityId {

    private AppelOffreId(UUID value) {
        super(value);
    }

    public static AppelOffreId of(UUID value) {
        return new AppelOffreId(value);
    }

    public static AppelOffreId of(String value) {
        return new AppelOffreId(UUID.fromString(value));
    }

    public static AppelOffreId generate() {
        return new AppelOffreId(UUID.randomUUID());
    }
}
