package huertos.negocio;

public class FilaProduccion {
    private final String cultivo;
    private int cosechas;
    private double kg;
    private double area;
    private double kgEsperados;

    public FilaProduccion(String cultivo) {
        this.cultivo = cultivo;
    }

    public void sumar(double kgCosechados, double areaSembrada, double esperados) {
        cosechas++;
        kg = kg + kgCosechados;
        area = area + areaSembrada;
        kgEsperados = kgEsperados + esperados;
    }

    public String getCultivo() {
        return cultivo;
    }

    public int getCosechas() {
        return cosechas;
    }

    public double getKg() {
        return kg;
    }

    public double getArea() {
        return area;
    }

    public double getKgPorM2() {
        return kg / area;
    }

    public double getRendimiento() {
        return kg / kgEsperados * 100;
    }
}
