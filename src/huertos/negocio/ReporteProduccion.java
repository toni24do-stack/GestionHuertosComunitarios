package huertos.negocio;

import huertos.modelo.CausaPerdida;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteProduccion {
    private final LocalDate desde;
    private final LocalDate hasta;
    private final List<FilaProduccion> filas = new ArrayList<>();
    private final Map<CausaPerdida, Integer> causasPerdida = new LinkedHashMap<>();
    private int cultivosCosechados;
    private int cultivosPerdidos;
    private double horasTrabajadas;

    public ReporteProduccion(LocalDate desde, LocalDate hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public void agregarCosecha(String cultivo, double kg, double area, double kgEsperados) {
        FilaProduccion fila = null;
        for (FilaProduccion f : filas) {
            if (f.getCultivo().equals(cultivo)) {
                fila = f;
            }
        }
        if (fila == null) {
            fila = new FilaProduccion(cultivo);
            filas.add(fila);
        }
        fila.sumar(kg, area, kgEsperados);
        cultivosCosechados++;
    }

    public void agregarPerdida(CausaPerdida causa) {
        cultivosPerdidos++;
        Integer actual = causasPerdida.get(causa);
        if (actual == null) {
            actual = 0;
        }
        causasPerdida.put(causa, actual + 1);
    }

    public void sumarHoras(double horas) {
        horasTrabajadas = horasTrabajadas + horas;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public List<FilaProduccion> getFilas() {
        return filas;
    }

    public Map<CausaPerdida, Integer> getCausasPerdida() {
        return causasPerdida;
    }

    public int getCultivosCosechados() {
        return cultivosCosechados;
    }

    public int getCultivosPerdidos() {
        return cultivosPerdidos;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public double getKgTotales() {
        double total = 0;
        for (FilaProduccion f : filas) {
            total = total + f.getKg();
        }
        return total;
    }

    public double getPorcentajePerdidos() {
        int terminados = cultivosCosechados + cultivosPerdidos;
        if (terminados == 0) {
            return 0;
        }
        return cultivosPerdidos * 100.0 / terminados;
    }
}
