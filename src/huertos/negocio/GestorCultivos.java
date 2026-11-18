package huertos.negocio;

import huertos.modelo.CausaPerdida;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.Huerto;
import huertos.modelo.Labor;
import huertos.modelo.Parcela;
import huertos.modelo.TipoCultivo;
import huertos.modelo.TipoRiego;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorCultivos {
    private final Huerto huerto;

    public GestorCultivos(Huerto huerto) {
        this.huerto = huerto;
    }

    public Parcela registrarParcela(String codigo, String area, String ubicacion, TipoRiego riego) {
        String cod = Formato.texto(codigo, "Código").toUpperCase();
        if (buscarParcela(cod) != null) {
            throw new ReglaNegocioException("Ya existe una parcela con el código " + cod + ".");
        }
        double metros = validarArea(area);
        String ubi = Formato.texto(ubicacion, "Ubicación");
        if (riego == null) {
            throw new ReglaNegocioException("Seleccione el tipo de riego.");
        }
        Parcela p = new Parcela(cod, metros, ubi, riego);
        huerto.getParcelas().add(p);
        return p;
    }

    public void actualizarParcela(String codigo, String area, String ubicacion, TipoRiego riego) {
        Parcela p = obtenerParcela(codigo);
        double metros = validarArea(area);
        if (metros < p.getAreaOcupada()) {
            throw new ReglaNegocioException("El área no puede ser menor que la ocupada por los cultivos activos ("
                    + Formato.decimal(p.getAreaOcupada()) + " m²).");
        }
        p.setArea(metros);
        p.setUbicacion(Formato.texto(ubicacion, "Ubicación"));
        p.setTipoRiego(riego);
    }

    public void eliminarParcela(String codigo) {
        Parcela p = obtenerParcela(codigo);
        if (p.contarCultivosActivos() > 0) {
            throw new ReglaNegocioException("No se puede eliminar la parcela " + p.getCodigo() + " porque tiene "
                    + p.contarCultivosActivos() + " cultivo(s) activo(s).");
        }
        if (!p.getCultivos().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar la parcela " + p.getCodigo()
                    + " porque tiene historial de cultivos (cosechas o pérdidas registradas).");
        }
        huerto.getParcelas().remove(p);
    }

    public Parcela buscarParcela(String codigo) {
        for (Parcela p : huerto.getParcelas()) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        return null;
    }

    public Parcela obtenerParcela(String codigo) {
        String cod = Formato.texto(codigo, "Código");
        Parcela p = buscarParcela(cod);
        if (p == null) {
            throw new ReglaNegocioException("No existe la parcela " + cod.toUpperCase() + ".");
        }
        return p;
    }

    public List<Parcela> getParcelas() {
        return huerto.getParcelas();
    }

    public Cultivo registrarSiembra(Parcela parcela, TipoCultivo tipo, String variedad, LocalDate fechaSiembra,
                                    String area, LocalDate hoy) {
        if (parcela == null) {
            throw new ReglaNegocioException("Seleccione la parcela.");
        }
        if (tipo == null) {
            throw new ReglaNegocioException("Seleccione el cultivo del catálogo.");
        }
        String var = Formato.texto(variedad, "Variedad");
        if (fechaSiembra == null) {
            throw new ReglaNegocioException("El campo «Fecha de siembra» es obligatorio.");
        }
        if (fechaSiembra.isAfter(hoy)) {
            throw new ReglaNegocioException("La fecha de siembra no puede ser posterior a hoy.");
        }
        double metros = validarArea(area);
        if (metros > parcela.getAreaLibre()) {
            throw new ReglaNegocioException("No hay área libre suficiente en la parcela " + parcela.getCodigo()
                    + ": se piden " + Formato.decimal(metros) + " m² y solo quedan "
                    + Formato.decimal(parcela.getAreaLibre()) + " m² libres.");
        }
        Cultivo c = new Cultivo(huerto.generarIdCultivo(), parcela, tipo, var, fechaSiembra, metros);
        parcela.agregarCultivo(c);
        return c;
    }

    public String cambiarEstado(Cultivo cultivo, EstadoCultivo nuevo, CausaPerdida causa, LocalDate hoy) {
        if (cultivo == null) {
            throw new ReglaNegocioException("Seleccione un cultivo.");
        }
        if (nuevo == null) {
            throw new ReglaNegocioException("Seleccione el nuevo estado.");
        }
        if (nuevo == EstadoCultivo.COSECHADO) {
            throw new ReglaNegocioException("El estado «Cosechado» se asigna al registrar la cosecha en la pestaña «Cosechas y reparto».");
        }
        if (!cultivo.getEstado().puedeCambiarA(nuevo)) {
            throw new ReglaNegocioException("Transición no válida: un cultivo «" + cultivo.getEstado()
                    + "» no puede pasar a «" + nuevo + "».");
        }
        if (nuevo == EstadoCultivo.PERDIDO && causa == null) {
            throw new ReglaNegocioException("Para marcar el cultivo como perdido debe indicar la causa.");
        }
        if (nuevo != EstadoCultivo.PERDIDO) {
            causa = null;
        }
        cultivo.cambiarEstado(nuevo, causa, hoy);
        String mensaje = cultivo.getNombre() + " pasó a «" + nuevo + "».";
        if (nuevo == EstadoCultivo.PERDIDO) {
            int canceladas = cancelarLaboresAbiertas(cultivo);
            mensaje = mensaje + "\nCausa: " + causa + ". Se liberaron " + Formato.decimal(cultivo.getAreaSembrada())
                    + " m² de la parcela " + cultivo.getParcela().getCodigo() + " y se cancelaron "
                    + canceladas + " labor(es) pendiente(s).";
        }
        return mensaje;
    }

    public List<Cultivo> getPropuestasListoParaCosecha(LocalDate hoy) {
        List<Cultivo> propuestas = new ArrayList<>();
        for (Cultivo c : huerto.getCultivos()) {
            if (c.getEstado() == EstadoCultivo.EN_CRECIMIENTO && c.alcanzoFechaEstimada(hoy)) {
                propuestas.add(c);
            }
        }
        return propuestas;
    }

    public int cancelarLaboresAbiertas(Cultivo cultivo) {
        int canceladas = 0;
        for (Labor l : huerto.getLabores()) {
            if (l.getCultivo() == cultivo && l.estaAbierta()) {
                l.cancelar();
                canceladas++;
            }
        }
        return canceladas;
    }

    public List<Cultivo> getCultivos() {
        return huerto.getCultivos();
    }

    public List<Cultivo> getCultivosActivos() {
        List<Cultivo> activos = new ArrayList<>();
        for (Cultivo c : huerto.getCultivos()) {
            if (c.getEstado().esActivo()) {
                activos.add(c);
            }
        }
        return activos;
    }

    public List<Cultivo> getCultivosEnEstado(EstadoCultivo estado) {
        List<Cultivo> lista = new ArrayList<>();
        for (Cultivo c : huerto.getCultivos()) {
            if (c.getEstado() == estado) {
                lista.add(c);
            }
        }
        return lista;
    }

    public List<TipoCultivo> getCatalogo() {
        return huerto.getCatalogo();
    }

    private double validarArea(String area) {
        double metros = Formato.numero(area, "Área (m²)");
        if (metros <= 0) {
            throw new ReglaNegocioException("El área debe ser mayor que 0.");
        }
        return metros;
    }
}
