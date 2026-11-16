package huertos.modelo;

public enum CausaPerdida {
    PLAGA("Plaga"),
    FALTA_DE_RIEGO("Falta de riego"),
    CLIMA("Clima"),
    OTRA("Otra");

    private final String texto;

    CausaPerdida(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
