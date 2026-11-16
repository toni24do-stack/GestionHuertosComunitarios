package huertos.interfaz;

import huertos.modelo.Huerto;
import huertos.negocio.DatosEjemplo;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.LocalDate;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("es-EC"));
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                usarAspectoNimbus();
                Huerto huerto = DatosEjemplo.crearHuerto(LocalDate.now());
                new VentanaPrincipal(huerto).setVisible(true);
            }
        });
    }

    private static void usarAspectoNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }
    }
}
