package huertos.modelo;

import java.time.LocalDate;

public class Participante extends Persona {
    private RolHuerto rol;
    private EstadoParticipante estado;
    private final LocalDate fechaIngreso;
    private double horasAcumuladas;

    public Participante(String cedula, String nombres, String apellidos, String telefono,
                        RolHuerto rol, LocalDate fechaIngreso) {
        super(cedula, nombres, apellidos, telefono);
        this.rol = rol;
        this.fechaIngreso = fechaIngreso;
        this.estado = EstadoParticipante.ACTIVO;
        this.horasAcumuladas = 0;
    }

    public RolHuerto getRol() {
        return rol;
    }

    public void setRol(RolHuerto rol) {
        this.rol = rol;
    }

    public EstadoParticipante getEstado() {
        return estado;
    }

    public void setEstado(EstadoParticipante estado) {
        this.estado = estado;
    }

    public boolean estaActivo() {
        return estado == EstadoParticipante.ACTIVO;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public double getHorasAcumuladas() {
        return horasAcumuladas;
    }

    public void sumarHoras(double horas) {
        horasAcumuladas = horasAcumuladas + horas;
    }

    @Override
    public String getDescripcion() {
        return rol + " (" + estado + ")";
    }
}
