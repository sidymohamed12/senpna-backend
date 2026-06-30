package ministere.sante.senpna.auth.domain.valueobject;

import ministere.sante.senpna.shared.domain.valueobject.Name;

public final class Prenom extends Name {

    private Prenom(String value) {
        super(value);
    }

    public static Prenom of(String value) {
        return new Prenom(value);
    }

    @Override
    protected String getFieldLabel() {
        return "Le prénom";
    }
}
