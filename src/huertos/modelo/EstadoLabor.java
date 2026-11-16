package huertos.modelo;

public enum EstadoLabor {
    PENDIENTE("Pendiente"),
    ATRASADA("Atrasada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String texto;

    EstadoLabor(String texto) {
        this.texto = texto;
    }

    public boolean estaAbierta() {
        return this == PENDIENTE || this == ATRASADA;
    }

    @Override
    public String toString() {
        return texto;
    }
}
