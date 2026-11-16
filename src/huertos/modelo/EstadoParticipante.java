package huertos.modelo;

public enum EstadoParticipante {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String texto;

    EstadoParticipante(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
