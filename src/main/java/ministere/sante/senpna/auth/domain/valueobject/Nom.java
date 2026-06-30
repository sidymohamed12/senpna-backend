package ministere.sante.senpna.auth.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.Name;

public final class Nom extends Name {

    private Nom(String value) {
        super(value);
    }

    public static Nom of(String value) {
        return new Nom(value);
    }

    @Override
    protected String getFieldLabel() {
        return "Le nom";
    }
}
