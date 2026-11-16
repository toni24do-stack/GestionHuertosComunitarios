package huertos.modelo;

import java.time.LocalDate;

public class Labor {
    private final int numero;
    private final TipoLabor tipo;
    private final Cultivo cultivo;
    private Participante responsable;
    private LocalDate fechaProgramada;
    private EstadoLabor estado;
    private LocalDate fechaRealizacion;
    private double horasTrabajadas;
    private String observaciones = "";

    public Labor(int numero, TipoLabor tipo, Cultivo cultivo, Participante responsable, LocalDate fechaProgramada) {
        this.numero = numero;
        this.tipo = tipo;
        this.cultivo = cultivo;
        this.responsable = responsable;
        this.fechaProgramada = fechaProgramada;
        this.estado = EstadoLabor.PENDIENTE;
    }

    public int getNumero() {
        return numero;
    }

    public TipoLabor getTipo() {
        return tipo;
    }

    public Cultivo getCultivo() {
        return cultivo;
    }

    public Participante getResponsable() {
        return responsable;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public EstadoLabor getEstado() {
        return estado;
    }

    public LocalDate getFechaRealizacion() {
        return fechaRealizacion;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public boolean estaAbierta() {
        return estado.estaAbierta();
    }

    public boolean actualizarAtraso(LocalDate hoy) {
        if (estado == EstadoLabor.PENDIENTE && fechaProgramada.isBefore(hoy)) {
            estado = EstadoLabor.ATRASADA;
            return true;
        }
        return false;
    }

    public void registrarCumplimiento(LocalDate fecha, double horas, String observaciones) {
        this.fechaRealizacion = fecha;
        this.horasTrabajadas = horas;
        this.observaciones = observaciones;
        this.estado = EstadoLabor.COMPLETADA;
        responsable.sumarHoras(horas);
    }

    public void reprogramar(LocalDate nuevaFecha, Participante nuevoResponsable, LocalDate hoy) {
        this.fechaProgramada = nuevaFecha;
        this.responsable = nuevoResponsable;
        this.estado = EstadoLabor.PENDIENTE;
        actualizarAtraso(hoy);
    }

    public void cancelar() {
        this.estado = EstadoLabor.CANCELADA;
    }
}
