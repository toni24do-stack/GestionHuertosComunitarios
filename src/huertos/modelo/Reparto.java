package huertos.modelo;

import java.util.ArrayList;
import java.util.List;

public class Reparto {
    private final double porcentajeReserva;
    private final double kgReserva;
    private final List<ItemReparto> items = new ArrayList<>();

    public Reparto(double porcentajeReserva, double kgReserva) {
        this.porcentajeReserva = porcentajeReserva;
        this.kgReserva = kgReserva;
    }

    public double getPorcentajeReserva() {
        return porcentajeReserva;
    }

    public double getKgReserva() {
        return kgReserva;
    }

    public List<ItemReparto> getItems() {
        return items;
    }

    public void agregarItem(ItemReparto item) {
        items.add(item);
    }

    public double getKgRepartidos() {
        double total = 0;
        for (ItemReparto item : items) {
            total = total + item.getKg();
        }
        return total;
    }

    public boolean incluye(Participante participante) {
        for (ItemReparto item : items) {
            if (item.getParticipante() == participante && item.getKg() > 0) {
                return true;
            }
        }
        return false;
    }
}
