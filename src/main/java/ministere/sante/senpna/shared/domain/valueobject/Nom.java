package ministere.sante.senpna.shared.domain.valueobject;


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
