package huertos.modelo;

import java.util.ArrayList;
import java.util.List;

public class Parcela {
    private final String codigo;
    private double area;
    private String ubicacion;
    private TipoRiego tipoRiego;
    private final List<Cultivo> cultivos = new ArrayList<>();

    public Parcela(String codigo, double area, String ubicacion, TipoRiego tipoRiego) {
        this.codigo = codigo;
        this.area = area;
        this.ubicacion = ubicacion;
        this.tipoRiego = tipoRiego;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public TipoRiego getTipoRiego() {
        return tipoRiego;
    }

    public void setTipoRiego(TipoRiego tipoRiego) {
        this.tipoRiego = tipoRiego;
    }

    public List<Cultivo> getCultivos() {
        return cultivos;
    }

    public void agregarCultivo(Cultivo cultivo) {
        cultivos.add(cultivo);
    }

    public double getAreaOcupada() {
        double ocupada = 0;
        for (Cultivo c : cultivos) {
            if (c.getEstado().esActivo()) {
                ocupada = ocupada + c.getAreaSembrada();
            }
        }
        return ocupada;
    }

    public double getAreaLibre() {
        return area - getAreaOcupada();
    }

    public int contarCultivosActivos() {
        int total = 0;
        for (Cultivo c : cultivos) {
            if (c.getEstado().esActivo()) {
                total++;
            }
        }
        return total;
    }

    @Override
    public String toString() {
        return codigo + " – " + ubicacion;
    }
}
