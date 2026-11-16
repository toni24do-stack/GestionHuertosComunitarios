package huertos.modelo;

public enum RolHuerto {
    PARTICIPANTE("Participante"),
    COORDINADOR("Coordinador/a");

    private final String texto;

    RolHuerto(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
