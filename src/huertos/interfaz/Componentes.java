package huertos.interfaz;

import huertos.negocio.ReglaNegocioException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

public final class Componentes {
    public static final Color VERDE = new Color(46, 125, 50);
    public static final Color VERDE_CLARO = new Color(232, 245, 233);
    public static final Color ROJO = new Color(198, 40, 40);
    public static final Color NARANJA = new Color(230, 120, 0);

    private Componentes() {
    }

    public static JPanel encabezado(String titulo, String detalle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(VERDE_CLARO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, VERDE),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        lblTitulo.setForeground(VERDE.darker());
        JLabel lblDetalle = new JLabel(detalle);
        lblDetalle.setForeground(Color.DARK_GRAY);
        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(lblDetalle, BorderLayout.SOUTH);
        return panel;
    }

    public static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    public static JTable tabla(DefaultTableModel modelo, String nombre) {
        JTable tabla = new JTable(modelo);
        tabla.setName(nombre);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFillsViewportHeight(true);
        return tabla;
    }

    public static JScrollPane conBorde(JComponent componente, String titulo) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(BorderFactory.createTitledBorder(titulo));
        return scroll;
    }

    public static JButton boton(String texto, String nombre, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setName(nombre);
        boton.addActionListener(accion);
        return boton;
    }

    public static JPanel fila(JComponent... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        for (JComponent c : componentes) {
            panel.add(c);
        }
        return panel;
    }

    public static void error(Component padre, Exception e) {
        String mensaje;
        if (e instanceof ReglaNegocioException) {
            mensaje = e.getMessage();
        } else {
            mensaje = "Ocurrió un error inesperado: " + e.getMessage();
        }
        JOptionPane.showMessageDialog(padre, mensaje, "No se pudo completar la operación", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Operación realizada", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    public static class Formulario extends JPanel {
        private final int paresPorFila;
        private int fila = 0;
        private int columna = 0;

        public Formulario(String titulo, int paresPorFila) {
            super(new GridBagLayout());
            this.paresPorFila = paresPorFila;
            if (titulo != null) {
                setBorder(BorderFactory.createTitledBorder(titulo));
            }
        }

        public Formulario agregar(String etiqueta, JComponent campo) {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(3, 6, 3, 6);
            c.gridy = fila;
            c.gridx = columna * 2;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(etiqueta), c);
            c.gridx = columna * 2 + 1;
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            add(campo, c);
            columna++;
            if (columna == paresPorFila) {
                columna = 0;
                fila++;
            }
            return this;
        }
    }
}
