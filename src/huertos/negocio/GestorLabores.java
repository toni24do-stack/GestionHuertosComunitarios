package huertos.negocio;

import huertos.modelo.Cultivo;
import huertos.modelo.EstadoLabor;
import huertos.modelo.Huerto;
import huertos.modelo.Labor;
import huertos.modelo.Participante;
import huertos.modelo.TipoLabor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorLabores {
    public static final double HORAS_MINIMAS = 0.5;
    public static final double HORAS_MAXIMAS = 8;
    public static final int FRECUENCIA_MAXIMA = 30;

    private final Huerto huerto;

    public GestorLabores(Huerto huerto) {
        this.huerto = huerto;
    }

    public List<Labor> programar(TipoLabor tipo, Cultivo cultivo, Participante responsable, LocalDate fecha,
                                 String frecuencia, LocalDate hoy) {
        if (tipo == null) {
            throw new ReglaNegocioException("Seleccione el tipo de labor.");
        }
        validarCultivoYResponsable(cultivo, responsable);
        if (fecha == null) {
            throw new ReglaNegocioException("El campo «Fecha» es obligatorio.");
        }
        if (fecha.isBefore(cultivo.getFechaSiembra())) {
            throw new ReglaNegocioException("La fecha no puede ser anterior a la siembra del cultivo ("
                    + Formato.fecha(cultivo.getFechaSiembra()) + ").");
        }
        int cadaDias = 0;
        if (frecuencia != null && !frecuencia.trim().isEmpty()) {
            cadaDias = Formato.entero(frecuencia, "Frecuencia (días)");
        }
        if (cadaDias < 0 || cadaDias > FRECUENCIA_MAXIMA) {
            throw new ReglaNegocioException("La frecuencia debe estar entre 0 (labor única) y "
                    + FRECUENCIA_MAXIMA + " días.");
        }
        List<Labor> creadas = new ArrayList<>();
        if (cadaDias == 0) {
            creadas.add(crear(tipo, cultivo, responsable, fecha, hoy));
        } else {
            LocalDate fin = cultivo.calcularFechaEstimadaCosecha();
            if (fecha.isAfter(fin)) {
                throw new ReglaNegocioException("Una labor recurrente debe empezar antes de la fecha estimada de "
                        + "cosecha (" + Formato.fecha(fin) + ").");
            }
            LocalDate dia = fecha;
            while (!dia.isAfter(fin)) {
                creadas.add(crear(tipo, cultivo, responsable, dia, hoy));
                dia = dia.plusDays(cadaDias);
            }
        }
        return creadas;
    }

    private Labor crear(TipoLabor tipo, Cultivo cultivo, Participante responsable, LocalDate fecha, LocalDate hoy) {
        Labor labor = new Labor(huerto.generarNumeroLabor(), tipo, cultivo, responsable, fecha);
        labor.actualizarAtraso(hoy);
        huerto.getLabores().add(labor);
        return labor;
    }

    public void reprogramar(Labor labor, LocalDate nuevaFecha, Participante responsable, LocalDate hoy) {
        validarAbierta(labor);
        validarCultivoYResponsable(labor.getCultivo(), responsable);
        if (nuevaFecha == null) {
            throw new ReglaNegocioException("El campo «Fecha» es obligatorio.");
        }
        if (nuevaFecha.isBefore(hoy)) {
            throw new ReglaNegocioException("Al reprogramar, la nueva fecha debe ser hoy o posterior.");
        }
        labor.reprogramar(nuevaFecha, responsable, hoy);
    }

    public void cancelar(Labor labor) {
        validarAbierta(labor);
        labor.cancelar();
    }

    public void registrarCumplimiento(Labor labor, LocalDate fecha, String horas, String observaciones,
                                      LocalDate hoy) {
        validarAbierta(labor);
        if (fecha == null) {
            throw new ReglaNegocioException("El campo «Fecha de realización» es obligatorio.");
        }
        if (fecha.isAfter(hoy)) {
            throw new ReglaNegocioException("No se puede registrar una labor con fecha futura.");
        }
        if (fecha.isBefore(labor.getCultivo().getFechaSiembra())) {
            throw new ReglaNegocioException("La fecha de realización no puede ser anterior a la siembra.");
        }
        double h = Formato.numero(horas, "Horas trabajadas");
        if (h < HORAS_MINIMAS || h > HORAS_MAXIMAS) {
            throw new ReglaNegocioException("Las horas trabajadas deben estar entre 0,5 y 8.");
        }
        String obs = "";
        if (observaciones != null) {
            obs = observaciones.trim();
        }
        labor.registrarCumplimiento(fecha, Formato.redondear(h), obs);
    }

    public int actualizarAtrasadas(LocalDate hoy) {
        int cambios = 0;
        for (Labor l : huerto.getLabores()) {
            if (l.actualizarAtraso(hoy)) {
                cambios++;
            }
        }
        return cambios;
    }

    public AgendaSemanal generarAgenda(LocalDate dia, LocalDate hoy, Participante filtroResponsable,
                                       Cultivo filtroCultivo) {
        if (dia == null) {
            throw new ReglaNegocioException("Ingrese un día de la semana a consultar.");
        }
        actualizarAtrasadas(hoy);
        LocalDate lunes = dia.with(DayOfWeek.MONDAY);
        AgendaSemanal agenda = new AgendaSemanal(lunes);

        List<Labor> seleccion = new ArrayList<>();
        for (Labor l : huerto.getLabores()) {
            boolean enSemana = !l.getFechaProgramada().isBefore(lunes) && !l.getFechaProgramada().isAfter(agenda.getDomingo());
            boolean atrasadaAnterior = l.getEstado() == EstadoLabor.ATRASADA
                    && l.getFechaProgramada().isBefore(lunes);
            boolean pasaFiltros = (filtroResponsable == null || l.getResponsable() == filtroResponsable)
                    && (filtroCultivo == null || l.getCultivo() == filtroCultivo);
            if ((enSemana || atrasadaAnterior) && pasaFiltros) {
                int posicion = 0;
                while (posicion < seleccion.size()
                        && !seleccion.get(posicion).getFechaProgramada().isAfter(l.getFechaProgramada())) {
                    posicion++;
                }
                seleccion.add(posicion, l);
            }
        }
        for (Labor l : seleccion) {
            agenda.agregar(l);
        }
        return agenda;
    }

    public List<Labor> getLabores() {
        return huerto.getLabores();
    }

    private void validarAbierta(Labor labor) {
        if (labor == null) {
            throw new ReglaNegocioException("Seleccione una labor de la agenda.");
        }
        if (!labor.estaAbierta()) {
            throw new ReglaNegocioException("La labor N.º " + labor.getNumero() + " está «" + labor.getEstado()
                    + "»; solo se pueden modificar labores pendientes o atrasadas.");
        }
    }

    private void validarCultivoYResponsable(Cultivo cultivo, Participante responsable) {
        if (cultivo == null) {
            throw new ReglaNegocioException("Seleccione el cultivo.");
        }
        if (!cultivo.getEstado().esActivo()) {
            throw new ReglaNegocioException("No se pueden programar labores para un cultivo «"
                    + cultivo.getEstado() + "».");
        }
        if (responsable == null) {
            throw new ReglaNegocioException("Seleccione el responsable.");
        }
        if (!responsable.estaActivo()) {
            throw new ReglaNegocioException(responsable.getNombreCompleto()
                    + " está «Inactivo»; solo se asignan labores a participantes activos.");
        }
    }
}
