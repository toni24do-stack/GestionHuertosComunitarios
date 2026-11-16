package huertos.modelo;

import java.time.LocalDate;

public class Cosecha {
    private final String codigo;
    private final Cultivo cultivo;
    private final LocalDate fecha;
    private final double kg;
    private Reparto reparto;

    public Cosecha(String codigo, Cultivo cultivo, LocalDate fecha, double kg) {
        this.codigo = codigo;
        this.cultivo = cultivo;
        this.fecha = fecha;
        this.kg = kg;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getKg() {
        return kg;
    }

    public Reparto getReparto() {
        return reparto;
    }

    public void setReparto(Reparto reparto) {
        this.reparto = reparto;
    }

    public boolean estaRepartida() {
        return reparto != null;
    }

    public double calcularRendimiento() {
        return kg / cultivo.calcularKgEsperados() * 100;
    }

    public double calcularKgPorM2() {
        return kg / cultivo.getAreaSembrada();
    }

    @Override
    public String toString() {
        return codigo + " · " + cultivo.getNombre();
    }
}
