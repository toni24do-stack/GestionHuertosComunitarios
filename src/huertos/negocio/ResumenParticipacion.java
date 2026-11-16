package huertos.negocio;

import huertos.modelo.Participante;

public class ResumenParticipacion {
    private final Participante participante;
    private final int numeroLabores;
    private final double horas;
    private final double porcentaje;

    public ResumenParticipacion(Participante participante, int numeroLabores, double horas, double porcentaje) {
        this.participante = participante;
        this.numeroLabores = numeroLabores;
        this.horas = horas;
        this.porcentaje = porcentaje;
    }

    public Participante getParticipante() {
        return participante;
    }

    public int getNumeroLabores() {
        return numeroLabores;
    }

    public double getHoras() {
        return horas;
    }

    public double getPorcentaje() {
        return porcentaje;
    }
}
