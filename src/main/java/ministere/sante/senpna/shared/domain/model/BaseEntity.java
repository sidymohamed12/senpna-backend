package ministere.sante.senpna.shared.domain.model;

import ministere.sante.senpna.shared.domain.valueobject.EntityId;

import java.time.Instant;
import java.util.Objects;

public abstract class BaseEntity {

    private final EntityId id;
    private final Instant createdAt;
    private Instant updatedAt;

    protected BaseEntity(EntityId id) {
        Objects.requireNonNull(id, "L'identifiant ne peut pas être null");
        this.id = id;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    protected BaseEntity(EntityId id, Instant createdAt, Instant updatedAt) {
        Objects.requireNonNull(id, "L'identifiant ne peut pas être null");
        Objects.requireNonNull(createdAt, "createdAt ne peut pas être null");
        Objects.requireNonNull(updatedAt, "updatedAt ne peut pas être null");
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    protected void markUpdated() {
        this.updatedAt = Instant.now();
    }

    public EntityId getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ── Equals / HashCode — basés uniquement sur l'identifiant ───────────

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
