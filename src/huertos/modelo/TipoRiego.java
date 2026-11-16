package huertos.modelo;

public enum TipoRiego {
    MANUAL("Manual"),
    GOTEO("Goteo");

    private final String texto;

    TipoRiego(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
