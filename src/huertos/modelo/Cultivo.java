package huertos.modelo;

import java.time.LocalDate;

public class Cultivo {
    private final String id;
    private final Parcela parcela;
    private final TipoCultivo tipo;
    private final String variedad;
    private final LocalDate fechaSiembra;
    private final double areaSembrada;
    private EstadoCultivo estado;
    private CausaPerdida causaPerdida;
    private LocalDate fechaCambioEstado;

    public Cultivo(String id, Parcela parcela, TipoCultivo tipo, String variedad,
                   LocalDate fechaSiembra, double areaSembrada) {
        this.id = id;
        this.parcela = parcela;
        this.tipo = tipo;
        this.variedad = variedad;
        this.fechaSiembra = fechaSiembra;
        this.areaSembrada = areaSembrada;
        this.estado = EstadoCultivo.SEMBRADO;
        this.fechaCambioEstado = fechaSiembra;
    }

    public String getId() {
        return id;
    }

    public Parcela getParcela() {
        return parcela;
    }

    public TipoCultivo getTipo() {
        return tipo;
    }

    public String getVariedad() {
        return variedad;
    }

    public LocalDate getFechaSiembra() {
        return fechaSiembra;
    }

    public double getAreaSembrada() {
        return areaSembrada;
    }

    public EstadoCultivo getEstado() {
        return estado;
    }

    public CausaPerdida getCausaPerdida() {
        return causaPerdida;
    }

    public LocalDate getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public LocalDate calcularFechaEstimadaCosecha() {
        return fechaSiembra.plusDays(tipo.getDiasCiclo());
    }

    public double calcularKgEsperados() {
        return areaSembrada * tipo.getRendimientoKgM2();
    }

    public boolean alcanzoFechaEstimada(LocalDate hoy) {
        return !hoy.isBefore(calcularFechaEstimadaCosecha());
    }

    public void cambiarEstado(EstadoCultivo nuevo, CausaPerdida causa, LocalDate fecha) {
        this.estado = nuevo;
        this.causaPerdida = causa;
        this.fechaCambioEstado = fecha;
    }

    public String getNombre() {
        return tipo.getNombre() + " " + variedad;
    }

    @Override
    public String toString() {
        return id + " · " + getNombre() + " (" + parcela.getCodigo() + ")";
    }
}
