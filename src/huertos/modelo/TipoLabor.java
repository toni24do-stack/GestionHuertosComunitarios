package huertos.modelo;

public enum TipoLabor {
    RIEGO("Riego"),
    DESHIERBE("Deshierbe"),
    ABONADO("Abonado"),
    CONTROL_DE_PLAGAS("Control de plagas"),
    PODA("Poda");

    private final String texto;

    TipoLabor(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
