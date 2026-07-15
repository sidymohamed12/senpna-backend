package ministere.sante.senpna.commandeachat.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.util.UUID;

public final class CommandeAchatId extends EntityId {

    private CommandeAchatId(UUID value) {
        super(value);
    }

    public static CommandeAchatId of(UUID value) {
        return new CommandeAchatId(value);
    }

    public static CommandeAchatId of(String value) {
        return new CommandeAchatId(UUID.fromString(value));
    }

    public static CommandeAchatId generate() {
        return new CommandeAchatId(UUID.randomUUID());
    }
}
