package huertos.modelo;

public class ItemReparto {
    private final Participante participante;
    private final double horas;
    private final double porcentaje;
    private final double kg;

    public ItemReparto(Participante participante, double horas, double porcentaje, double kg) {
        this.participante = participante;
        this.horas = horas;
        this.porcentaje = porcentaje;
        this.kg = kg;
    }

    public Participante getParticipante() {
        return participante;
    }

    public double getHoras() {
        return horas;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public double getKg() {
        return kg;
    }
}
