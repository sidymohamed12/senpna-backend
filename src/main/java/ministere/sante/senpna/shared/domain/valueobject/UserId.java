package ministere.sante.senpna.shared.domain.valueobject;


import java.util.UUID;

public final class UserId extends EntityId {

    private UserId(UUID value) {
        super(value);
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
