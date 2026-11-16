package huertos.interfaz;

import huertos.modelo.Huerto;
import huertos.negocio.Formato;
import huertos.negocio.GestorCosechas;
import huertos.negocio.GestorCultivos;
import huertos.negocio.GestorLabores;
import huertos.negocio.GestorParticipantes;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;

public class VentanaPrincipal extends JFrame {
    public VentanaPrincipal(Huerto huerto) {
        super("Gestión de huertos comunitarios – Programación II");

        GestorParticipantes gestorParticipantes = new GestorParticipantes(huerto);
        GestorCultivos gestorCultivos = new GestorCultivos(huerto);
        GestorLabores gestorLabores = new GestorLabores(huerto);
        GestorCosechas gestorCosechas = new GestorCosechas(huerto, gestorCultivos);

        final JTabbedPane pestanas = new JTabbedPane();
        pestanas.setName("pestanas");
        pestanas.addTab("M1 · Participantes", new PanelParticipantes(gestorParticipantes));
        pestanas.addTab("M2 · Parcelas y cultivos", new PanelCultivos(gestorCultivos));
        pestanas.addTab("M3 · Labores", new PanelLabores(gestorLabores, gestorParticipantes, gestorCultivos));
        pestanas.addTab("M4 · Cosechas y reparto", new PanelCosechas(gestorCosechas, gestorCultivos));
        pestanas.setFont(pestanas.getFont().deriveFont(Font.BOLD, 13f));
        pestanas.addChangeListener(e -> {
            Actualizable pantalla = (Actualizable) pestanas.getSelectedComponent();
            pantalla.actualizar();
        });

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(Componentes.VERDE);
        barraSuperior.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        JLabel titulo = new JLabel("Huerto comunitario «" + huerto.getNombre() + "»");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        JLabel fecha = new JLabel("Hoy: " + Formato.fecha(LocalDate.now()));
        fecha.setForeground(Color.WHITE);
        barraSuperior.add(titulo, BorderLayout.WEST);
        barraSuperior.add(fecha, BorderLayout.EAST);

        JLabel barraEstado = new JLabel("  Datos de ejemplo cargados al iniciar. Los datos se guardan en memoria "
                + "mientras la aplicación está abierta.");
        barraEstado.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        barraEstado.setForeground(Color.DARK_GRAY);

        setLayout(new BorderLayout());
        add(barraSuperior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
    }
}
