package huertos.modelo;

public class TipoCultivo {
    private final String nombre;
    private final int diasCiclo;
    private final double rendimientoKgM2;

    public TipoCultivo(String nombre, int diasCiclo, double rendimientoKgM2) {
        this.nombre = nombre;
        this.diasCiclo = diasCiclo;
        this.rendimientoKgM2 = rendimientoKgM2;
    }

    public String getNombre() {
        return nombre;
    }

    public int getDiasCiclo() {
        return diasCiclo;
    }

    public double getRendimientoKgM2() {
        return rendimientoKgM2;
    }

    @Override
    public String toString() {
        return nombre + " (" + diasCiclo + " días)";
    }
}
