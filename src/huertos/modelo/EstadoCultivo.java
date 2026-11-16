package huertos.modelo;

public enum EstadoCultivo {
    SEMBRADO("Sembrado"),
    EN_CRECIMIENTO("En crecimiento"),
    LISTO_PARA_COSECHA("Listo para cosecha"),
    COSECHADO("Cosechado"),
    PERDIDO("Perdido");

    private final String texto;

    EstadoCultivo(String texto) {
        this.texto = texto;
    }

    public boolean esActivo() {
        return this != COSECHADO && this != PERDIDO;
    }

    public boolean puedeCambiarA(EstadoCultivo nuevo) {
        if (nuevo == PERDIDO) {
            return esActivo();
        }
        if (this == SEMBRADO) {
            return nuevo == EN_CRECIMIENTO;
        }
        if (this == EN_CRECIMIENTO) {
            return nuevo == LISTO_PARA_COSECHA;
        }
        if (this == LISTO_PARA_COSECHA) {
            return nuevo == COSECHADO;
        }
        return false;
    }

    @Override
    public String toString() {
        return texto;
    }
}
