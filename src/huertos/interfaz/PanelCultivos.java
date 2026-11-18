package huertos.interfaz;

import huertos.modelo.CausaPerdida;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.Parcela;
import huertos.modelo.TipoCultivo;
import huertos.modelo.TipoRiego;
import huertos.negocio.Formato;
import huertos.negocio.GestorCultivos;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PanelCultivos extends JPanel implements Actualizable {
    private final GestorCultivos gestor;

    private final JTextField txtCodigo = campo("txtCodigoParcela", 6);
    private final JTextField txtArea = campo("txtAreaParcela", 5);
    private final JTextField txtUbicacion = campo("txtUbicacion", 12);
    private final JComboBox<TipoRiego> cmbRiego = new JComboBox<>(TipoRiego.values());
    private final DefaultTableModel modeloParcelas = Componentes.modeloTabla(
            "Código", "Ubicación", "Riego", "Área (m²)", "Ocupada (m²)", "Libre (m²)", "Cultivos activos");
    private final JTable tablaParcelas = Componentes.tabla(modeloParcelas, "tablaParcelas");
    private List<Parcela> filasParcelas = new ArrayList<>();

    private final JComboBox<Parcela> cmbParcela = new JComboBox<>();
    private final JComboBox<TipoCultivo> cmbTipo = new JComboBox<>();
    private final JTextField txtVariedad = campo("txtVariedad", 10);
    private final JTextField txtFechaSiembra = campo("txtFechaSiembra", 9);
    private final JTextField txtAreaSiembra = campo("txtAreaSiembra", 5);

    private final DefaultTableModel modeloCultivos = Componentes.modeloTabla("ID", "Cultivo", "Parcela",
            "Siembra", "Área (m²)", "Cosecha estimada", "Kg esperados", "Estado", "Causa de pérdida");
    private final JTable tablaCultivos = Componentes.tabla(modeloCultivos, "tablaCultivos");
    private List<Cultivo> filasCultivos = new ArrayList<>();
    private final JComboBox<EstadoCultivo> cmbNuevoEstado = new JComboBox<>(new EstadoCultivo[]{
            EstadoCultivo.EN_CRECIMIENTO, EstadoCultivo.LISTO_PARA_COSECHA, EstadoCultivo.PERDIDO});
    private final JComboBox<CausaPerdida> cmbCausa = new JComboBox<>(CausaPerdida.values());

    public PanelCultivos(GestorCultivos gestor) {
        super(new BorderLayout(6, 6));
        this.gestor = gestor;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cmbRiego.setName("cmbRiego");
        cmbParcela.setName("cmbParcela");
        cmbTipo.setName("cmbTipo");
        cmbNuevoEstado.setName("cmbNuevoEstado");
        cmbCausa.setName("cmbCausa");

        add(Componentes.encabezado("Parcelas y cultivos",
                "Administre las parcelas, registre las siembras y actualice el estado de cada cultivo."),
                BorderLayout.NORTH);

        Componentes.Formulario formParcela = new Componentes.Formulario("Parcela", 2)
                .agregar("Código:", txtCodigo)
                .agregar("Área (m²):", txtArea)
                .agregar("Ubicación:", txtUbicacion)
                .agregar("Riego:", cmbRiego);
        JPanel parcelas = new JPanel(new BorderLayout(0, 4));
        JPanel arribaParcela = new JPanel(new BorderLayout());
        arribaParcela.add(formParcela, BorderLayout.CENTER);
        arribaParcela.add(Componentes.fila(
                Componentes.boton("Registrar", "btnRegistrarParcela", e -> registrarParcela()),
                Componentes.boton("Actualizar", "btnActualizarParcela", e -> actualizarParcela()),
                Componentes.boton("Eliminar", "btnEliminarParcela", e -> eliminarParcela()),
                Componentes.boton("Limpiar", "btnLimpiarParcela", e -> limpiarParcela())), BorderLayout.SOUTH);
        parcelas.add(arribaParcela, BorderLayout.NORTH);
        parcelas.add(Componentes.conBorde(tablaParcelas, "Parcelas del huerto"), BorderLayout.CENTER);

        txtFechaSiembra.setText(Formato.fecha(LocalDate.now()));
        Componentes.Formulario formSiembra = new Componentes.Formulario("Nueva siembra", 1)
                .agregar("Parcela:", cmbParcela)
                .agregar("Cultivo (catálogo):", cmbTipo)
                .agregar("Variedad:", txtVariedad)
                .agregar("Fecha de siembra:", txtFechaSiembra)
                .agregar("Área sembrada (m²):", txtAreaSiembra);
        JPanel siembra = new JPanel(new BorderLayout());
        siembra.add(formSiembra, BorderLayout.NORTH);
        siembra.add(Componentes.fila(Componentes.boton("Registrar siembra", "btnSiembra",
                e -> registrarSiembra())), BorderLayout.CENTER);

        JPanel arriba = new JPanel(new GridLayout(1, 2, 8, 0));
        arriba.add(parcelas);
        arriba.add(siembra);

        JPanel estados = new JPanel(new BorderLayout(0, 4));
        estados.add(Componentes.fila(new JLabel("Nuevo estado:"), cmbNuevoEstado, new JLabel("Causa (si se pierde):"),
                cmbCausa, Componentes.boton("Cambiar estado", "btnCambiarEstado", e -> cambiarEstado()),
                Componentes.boton("Revisar cultivos listos", "btnPropuestas", e -> revisarPropuestas())),
                BorderLayout.NORTH);
        estados.add(Componentes.conBorde(tablaCultivos, "Cultivos (seleccione uno para cambiar su estado)"),
                BorderLayout.CENTER);
        tablaCultivos.setDefaultRenderer(Object.class, new RenderizadorEstado());

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, estados);
        division.setResizeWeight(0.45);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        tablaParcelas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarParcela();
            }
        });
        actualizar();
    }

    private void registrarParcela() {
        try {
            Parcela p = gestor.registrarParcela(txtCodigo.getText(), txtArea.getText(), txtUbicacion.getText(),
                    (TipoRiego) cmbRiego.getSelectedItem());
            limpiarParcela();
            actualizar();
            Componentes.info(this, "Parcela " + p.getCodigo() + " registrada (" + Formato.decimal(p.getArea()) + " m²).");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void actualizarParcela() {
        try {
            gestor.actualizarParcela(txtCodigo.getText(), txtArea.getText(), txtUbicacion.getText(),
                    (TipoRiego) cmbRiego.getSelectedItem());
            actualizar();
            Componentes.info(this, "Parcela actualizada.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void eliminarParcela() {
        try {
            Parcela p = gestor.obtenerParcela(txtCodigo.getText());
            if (p.getCultivos().isEmpty() && !Componentes.confirmar(this, "¿Eliminar la parcela " + p.getCodigo() + "?")) {
                return;
            }
            gestor.eliminarParcela(p.getCodigo());
            limpiarParcela();
            actualizar();
            Componentes.info(this, "Parcela eliminada.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void registrarSiembra() {
        try {
            Cultivo c = gestor.registrarSiembra((Parcela) cmbParcela.getSelectedItem(),
                    (TipoCultivo) cmbTipo.getSelectedItem(), txtVariedad.getText(),
                    Formato.fecha(txtFechaSiembra.getText(), "Fecha de siembra"), txtAreaSiembra.getText(),
                    LocalDate.now());
            txtVariedad.setText("");
            txtAreaSiembra.setText("");
            actualizar();
            Componentes.info(this, "Siembra registrada: " + c.getId() + " · " + c.getNombre()
                    + "\nEstado: " + c.getEstado()
                    + "\nFecha estimada de cosecha: " + Formato.fecha(c.calcularFechaEstimadaCosecha())
                    + "\nCosecha esperada: " + Formato.decimal(c.calcularKgEsperados()) + " kg");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void cambiarEstado() {
        try {
            int fila = tablaCultivos.getSelectedRow();
            Cultivo c = null;
            if (fila >= 0) {
                c = filasCultivos.get(fila);
            }
            EstadoCultivo nuevo = (EstadoCultivo) cmbNuevoEstado.getSelectedItem();
            CausaPerdida causa = null;
            if (nuevo == EstadoCultivo.PERDIDO) {
                causa = (CausaPerdida) cmbCausa.getSelectedItem();
            }
            String mensaje = gestor.cambiarEstado(c, nuevo, causa, LocalDate.now());
            actualizar();
            Componentes.info(this, mensaje);
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void revisarPropuestas() {
        try {
            LocalDate hoy = LocalDate.now();
            List<Cultivo> propuestas = gestor.getPropuestasListoParaCosecha(hoy);
            if (propuestas.isEmpty()) {
                Componentes.info(this, "Ningún cultivo en crecimiento ha llegado todavía a su fecha estimada de cosecha.");
                return;
            }
            int aceptadas = 0;
            for (Cultivo c : propuestas) {
                if (Componentes.confirmar(this, c.getNombre() + " (" + c.getParcela().getCodigo()
                        + ") alcanzó su fecha estimada de cosecha (" + Formato.fecha(c.calcularFechaEstimadaCosecha())
                        + ").\n¿Cambiar su estado a «Listo para cosecha»?")) {
                    gestor.cambiarEstado(c, EstadoCultivo.LISTO_PARA_COSECHA, null, hoy);
                    aceptadas++;
                }
            }
            actualizar();
            Componentes.info(this, aceptadas + " cultivo(s) pasaron a «Listo para cosecha».");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void cargarParcela() {
        int fila = tablaParcelas.getSelectedRow();
        if (fila < 0 || fila >= filasParcelas.size()) {
            return;
        }
        Parcela p = filasParcelas.get(fila);
        txtCodigo.setText(p.getCodigo());
        txtCodigo.setEnabled(false);
        txtArea.setText(Formato.decimal(p.getArea()));
        txtUbicacion.setText(p.getUbicacion());
        cmbRiego.setSelectedItem(p.getTipoRiego());
    }

    private void limpiarParcela() {
        txtCodigo.setText("");
        txtCodigo.setEnabled(true);
        txtArea.setText("");
        txtUbicacion.setText("");
        cmbRiego.setSelectedIndex(0);
        tablaParcelas.clearSelection();
    }

    @Override
    public void actualizar() {
        filasParcelas = new ArrayList<>(gestor.getParcelas());
        modeloParcelas.setRowCount(0);
        cmbParcela.removeAllItems();
        for (Parcela p : filasParcelas) {
            modeloParcelas.addRow(new Object[]{p.getCodigo(), p.getUbicacion(), p.getTipoRiego(),
                    Formato.decimal(p.getArea()), Formato.decimal(p.getAreaOcupada()),
                    Formato.decimal(p.getAreaLibre()), p.contarCultivosActivos()});
            cmbParcela.addItem(p);
        }
        if (cmbTipo.getItemCount() == 0) {
            for (TipoCultivo t : gestor.getCatalogo()) {
                cmbTipo.addItem(t);
            }
        }
        LocalDate hoy = LocalDate.now();
        filasCultivos = gestor.getCultivos();
        modeloCultivos.setRowCount(0);
        for (Cultivo c : filasCultivos) {
            String estado = c.getEstado().toString();
            if (c.getEstado() == EstadoCultivo.EN_CRECIMIENTO && c.alcanzoFechaEstimada(hoy)) {
                estado = estado + " (¡ya puede estar listo!)";
            }
            String causa = "";
            if (c.getCausaPerdida() != null) {
                causa = c.getCausaPerdida().toString();
            }
            modeloCultivos.addRow(new Object[]{c.getId(), c.getNombre(), c.getParcela().getCodigo(),
                    Formato.fecha(c.getFechaSiembra()), Formato.decimal(c.getAreaSembrada()),
                    Formato.fecha(c.calcularFechaEstimadaCosecha()), Formato.decimal(c.calcularKgEsperados()),
                    estado, causa});
        }
    }

    private static JTextField campo(String nombre, int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setName(nombre);
        return campo;
    }

    private class RenderizadorEstado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            if (!seleccionada) {
                String estado = String.valueOf(modeloCultivos.getValueAt(fila, 7));
                if (estado.startsWith("Perdido")) {
                    c.setForeground(Componentes.ROJO);
                } else if (estado.startsWith("Listo") || estado.contains("¡")) {
                    c.setForeground(Componentes.NARANJA);
                } else if (estado.startsWith("Cosechado")) {
                    c.setForeground(Color.GRAY);
                } else {
                    c.setForeground(t.getForeground());
                }
            }
            return c;
        }
    }
}
