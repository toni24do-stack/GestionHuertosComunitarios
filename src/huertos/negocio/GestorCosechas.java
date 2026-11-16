package huertos.negocio;

import huertos.modelo.Cosecha;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.EstadoLabor;
import huertos.modelo.Huerto;
import huertos.modelo.ItemReparto;
import huertos.modelo.Labor;
import huertos.modelo.Participante;
import huertos.modelo.Reparto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorCosechas {
    private final Huerto huerto;
    private final GestorCultivos gestorCultivos;

    public GestorCosechas(Huerto huerto, GestorCultivos gestorCultivos) {
        this.huerto = huerto;
        this.gestorCultivos = gestorCultivos;
    }

    public Cosecha registrarCosecha(Cultivo cultivo, LocalDate fecha, String kg, LocalDate hoy) {
        if (cultivo == null) {
            throw new ReglaNegocioException("Seleccione el cultivo a cosechar.");
        }
        if (cultivo.getEstado() != EstadoCultivo.LISTO_PARA_COSECHA) {
            throw new ReglaNegocioException("Solo se pueden cosechar cultivos «Listo para cosecha». "
                    + cultivo.getNombre() + " está «" + cultivo.getEstado() + "».");
        }
        if (fecha == null) {
            throw new ReglaNegocioException("El campo «Fecha de cosecha» es obligatorio.");
        }
        if (fecha.isAfter(hoy) || fecha.isBefore(cultivo.getFechaSiembra())) {
            throw new ReglaNegocioException("La fecha de cosecha debe estar entre la siembra ("
                    + Formato.fecha(cultivo.getFechaSiembra()) + ") y hoy.");
        }
        double cantidad = Formato.redondear(Formato.numero(kg, "Cantidad (kg)"));
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad cosechada debe ser mayor que 0.");
        }
        Cosecha cosecha = new Cosecha(huerto.generarCodigoCosecha(), cultivo, fecha, cantidad);
        huerto.getCosechas().add(cosecha);
        cultivo.cambiarEstado(EstadoCultivo.COSECHADO, null, fecha);
        gestorCultivos.cancelarLaboresAbiertas(cultivo);
        return cosecha;
    }

    public Reparto calcularReparto(Cosecha cosecha, String porcentajeReserva) {
        if (cosecha == null) {
            throw new ReglaNegocioException("Seleccione una cosecha.");
        }
        if (cosecha.estaRepartida()) {
            throw new ReglaNegocioException("La cosecha " + cosecha.getCodigo() + " ya fue repartida.");
        }
        double porcentaje = Formato.numero(porcentajeReserva, "Reserva del grupo (%)");
        if (porcentaje < 0 || porcentaje > 100) {
            throw new ReglaNegocioException("La reserva del grupo debe estar entre 0 y 100 %.");
        }

        List<Participante> trabajadores = new ArrayList<>();
        List<Double> horas = new ArrayList<>();
        double totalHoras = 0;
        for (Labor l : huerto.getLabores()) {
            if (l.getCultivo() == cosecha.getCultivo() && l.getEstado() == EstadoLabor.COMPLETADA) {
                int i = trabajadores.indexOf(l.getResponsable());
                if (i < 0) {
                    trabajadores.add(l.getResponsable());
                    horas.add(0.0);
                    i = trabajadores.size() - 1;
                }
                horas.set(i, horas.get(i) + l.getHorasTrabajadas());
                totalHoras = totalHoras + l.getHorasTrabajadas();
            }
        }
        if (totalHoras == 0) {
            throw new ReglaNegocioException("No hay labores completadas en " + cosecha.getCultivo().getNombre()
                    + "; no se puede repartir según las horas trabajadas.");
        }

        int totalDecimas = (int) Math.round(cosecha.getKg() * 10);
        int repartibleDecimas = (int) Math.round(totalDecimas * (100 - porcentaje) / 100);
        int reservaDecimas = totalDecimas - repartibleDecimas;

        int[] decimas = new int[trabajadores.size()];
        int asignadas = 0;
        int mayor = 0;
        for (int i = 0; i < trabajadores.size(); i++) {
            decimas[i] = (int) Math.round(repartibleDecimas * horas.get(i) / totalHoras);
            asignadas = asignadas + decimas[i];
            if (horas.get(i) > horas.get(mayor)) {
                mayor = i;
            }
        }
        decimas[mayor] = decimas[mayor] + (repartibleDecimas - asignadas);

        Reparto reparto = new Reparto(porcentaje, reservaDecimas / 10.0);
        for (int i = 0; i < trabajadores.size(); i++) {
            reparto.agregarItem(new ItemReparto(trabajadores.get(i), horas.get(i),
                    horas.get(i) / totalHoras * 100, decimas[i] / 10.0));
        }
        for (Participante p : huerto.getParticipantes()) {
            if (p.estaActivo() && !trabajadores.contains(p)) {
                reparto.agregarItem(new ItemReparto(p, 0, 0, 0));
            }
        }
        return reparto;
    }

    public void guardarReparto(Cosecha cosecha, Reparto reparto) {
        if (cosecha == null || reparto == null) {
            throw new ReglaNegocioException("Primero calcule el reparto de una cosecha.");
        }
        if (cosecha.estaRepartida()) {
            throw new ReglaNegocioException("La cosecha " + cosecha.getCodigo() + " ya fue repartida.");
        }
        cosecha.setReparto(reparto);
    }

    public ReporteProduccion generarReporte(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Ingrese las fechas de inicio y fin del período.");
        }
        if (hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        ReporteProduccion reporte = new ReporteProduccion(desde, hasta);
        for (Cosecha c : huerto.getCosechas()) {
            if (enPeriodo(c.getFecha(), desde, hasta)) {
                Cultivo cultivo = c.getCultivo();
                reporte.agregarCosecha(cultivo.getTipo().getNombre(), c.getKg(), cultivo.getAreaSembrada(),
                        cultivo.calcularKgEsperados());
            }
        }
        for (Cultivo cultivo : huerto.getCultivos()) {
            if (cultivo.getEstado() == EstadoCultivo.PERDIDO && enPeriodo(cultivo.getFechaCambioEstado(), desde, hasta)) {
                reporte.agregarPerdida(cultivo.getCausaPerdida());
            }
        }
        for (Labor l : huerto.getLabores()) {
            if (l.getEstado() == EstadoLabor.COMPLETADA && enPeriodo(l.getFechaRealizacion(), desde, hasta)) {
                reporte.sumarHoras(l.getHorasTrabajadas());
            }
        }
        return reporte;
    }

    public List<Cosecha> getCosechas() {
        return huerto.getCosechas();
    }

    private boolean enPeriodo(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }
}
