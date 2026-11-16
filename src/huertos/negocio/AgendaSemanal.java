package huertos.negocio;

import huertos.modelo.EstadoLabor;
import huertos.modelo.Labor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgendaSemanal {
    private final LocalDate lunes;
    private final LocalDate domingo;
    private final List<Labor> labores = new ArrayList<>();
    private final Map<String, Integer> atrasadasPorResponsable = new LinkedHashMap<>();
    private final Map<String, Integer> atrasadasPorCultivo = new LinkedHashMap<>();

    public AgendaSemanal(LocalDate lunes) {
        this.lunes = lunes;
        this.domingo = lunes.plusDays(6);
    }

    public void agregar(Labor labor) {
        labores.add(labor);
        if (labor.getEstado() == EstadoLabor.ATRASADA) {
            sumar(atrasadasPorResponsable, labor.getResponsable().getNombreCompleto());
            sumar(atrasadasPorCultivo, labor.getCultivo().getNombre());
        }
    }

    private void sumar(Map<String, Integer> mapa, String clave) {
        Integer actual = mapa.get(clave);
        if (actual == null) {
            actual = 0;
        }
        mapa.put(clave, actual + 1);
    }

    public LocalDate getLunes() {
        return lunes;
    }

    public LocalDate getDomingo() {
        return domingo;
    }

    public List<Labor> getLabores() {
        return labores;
    }

    public Map<String, Integer> getAtrasadasPorResponsable() {
        return atrasadasPorResponsable;
    }

    public Map<String, Integer> getAtrasadasPorCultivo() {
        return atrasadasPorCultivo;
    }

    public int getTotalAtrasadas() {
        int total = 0;
        for (Integer n : atrasadasPorResponsable.values()) {
            total = total + n;
        }
        return total;
    }
}
