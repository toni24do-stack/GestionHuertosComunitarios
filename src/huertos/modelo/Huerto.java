package huertos.modelo;

import java.util.ArrayList;
import java.util.List;

public class Huerto {
    private final String nombre;
    private final List<Participante> participantes = new ArrayList<>();
    private final List<Parcela> parcelas = new ArrayList<>();
    private final List<TipoCultivo> catalogo = new ArrayList<>();
    private final List<Labor> labores = new ArrayList<>();
    private final List<Cosecha> cosechas = new ArrayList<>();
    private int contadorCultivos = 0;
    private int contadorLabores = 0;
    private int contadorCosechas = 0;

    public Huerto(String nombre) {
        this.nombre = nombre;
        catalogo.add(new TipoCultivo("Rábano", 30, 1.5));
        catalogo.add(new TipoCultivo("Lechuga", 60, 2.5));
        catalogo.add(new TipoCultivo("Acelga", 70, 3.0));
        catalogo.add(new TipoCultivo("Remolacha", 80, 2.5));
        catalogo.add(new TipoCultivo("Zanahoria", 90, 3.0));
        catalogo.add(new TipoCultivo("Brócoli", 90, 1.5));
        catalogo.add(new TipoCultivo("Cebolla", 120, 2.5));
        catalogo.add(new TipoCultivo("Tomate riñón", 120, 6.0));
    }

    public String getNombre() {
        return nombre;
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public List<Parcela> getParcelas() {
        return parcelas;
    }

    public List<TipoCultivo> getCatalogo() {
        return catalogo;
    }

    public List<Labor> getLabores() {
        return labores;
    }

    public List<Cosecha> getCosechas() {
        return cosechas;
    }

    public List<Cultivo> getCultivos() {
        List<Cultivo> lista = new ArrayList<>();
        for (Parcela p : parcelas) {
            lista.addAll(p.getCultivos());
        }
        return lista;
    }

    public TipoCultivo buscarTipoCultivo(String nombreCultivo) {
        for (TipoCultivo t : catalogo) {
            if (t.getNombre().equalsIgnoreCase(nombreCultivo)) {
                return t;
            }
        }
        return null;
    }

    public String generarIdCultivo() {
        contadorCultivos++;
        return String.format("C-%03d", contadorCultivos);
    }

    public int generarNumeroLabor() {
        contadorLabores++;
        return contadorLabores;
    }

    public String generarCodigoCosecha() {
        contadorCosechas++;
        return String.format("CO-%03d", contadorCosechas);
    }
}
