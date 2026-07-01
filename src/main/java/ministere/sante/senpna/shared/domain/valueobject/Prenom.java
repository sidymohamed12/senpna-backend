package ministere.sante.senpna.shared.domain.valueobject;


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
