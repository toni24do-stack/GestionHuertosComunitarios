package huertos.negocio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

public final class Formato {
    public static final Locale ECUADOR = Locale.forLanguageTag("es-EC");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Formato() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ReglaNegocioException("El campo «" + campo + "» es obligatorio.");
        }
        return valor.trim();
    }

    public static double numero(String valor, String campo) {
        String texto = texto(valor, campo);
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo «" + campo + "» debe ser un número (por ejemplo: 2,5).");
        }
    }

    public static int entero(String valor, String campo) {
        String texto = texto(valor, campo);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo «" + campo + "» debe ser un número entero (por ejemplo: 7).");
        }
    }

    public static LocalDate fecha(String valor, String campo) {
        String texto = texto(valor, campo);
        try {
            return LocalDate.parse(texto, FECHA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("El campo «" + campo + "» debe tener el formato dd/mm/aaaa.");
        }
    }

    public static String fecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FECHA);
    }

    public static String decimal(double valor) {
        return String.format(ECUADOR, "%.1f", valor);
    }

    public static String porcentaje(double valor) {
        return String.format(ECUADOR, "%.1f", valor) + " %";
    }

    public static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
