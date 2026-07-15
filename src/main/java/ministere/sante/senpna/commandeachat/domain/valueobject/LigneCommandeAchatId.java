package ministere.sante.senpna.commandeachat.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class LigneCommandeAchatId extends EntityId {

    private LigneCommandeAchatId(UUID value) {
        super(value);
    }

    public static LigneCommandeAchatId of(UUID value) {
        return new LigneCommandeAchatId(value);
    }

    public static LigneCommandeAchatId of(String value) {
        return new LigneCommandeAchatId(UUID.fromString(value));
    }

    public static LigneCommandeAchatId generate() {
        return new LigneCommandeAchatId(UUID.randomUUID());
    }
}
