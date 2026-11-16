package huertos.negocio;

public final class ValidadorCedula {
    private ValidadorCedula() {
    }

    public static boolean esValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            return false;
        }
        int tercerDigito = cedula.charAt(2) - '0';
        if (tercerDigito >= 6) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int digito = cedula.charAt(i) - '0';
            int coeficiente;
            if (i % 2 == 0) {
                coeficiente = 2;
            } else {
                coeficiente = 1;
            }
            int producto = digito * coeficiente;
            if (producto > 9) {
                producto = producto - 9;
            }
            suma = suma + producto;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }
}
