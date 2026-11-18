package huertos.interfaz;

import huertos.modelo.CausaPerdida;
import huertos.modelo.Cosecha;
import huertos.modelo.Cultivo;
import huertos.modelo.EstadoCultivo;
import huertos.modelo.ItemReparto;
import huertos.modelo.Reparto;
import huertos.negocio.FilaProduccion;
import huertos.negocio.Formato;
import huertos.negocio.GestorCosechas;
import huertos.negocio.GestorCultivos;
import huertos.negocio.ReporteProduccion;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelCosechas extends JPanel implements Actualizable {
    private final GestorCosechas gestor;
    private final GestorCultivos gestorCultivos;

    private final JComboBox<Cultivo> cmbCultivo = new JComboBox<>();
    private final JTextField txtFecha = campo("txtFechaCosecha", 9);
    private final JTextField txtKg = campo("txtKg", 5);
    private final DefaultTableModel modeloCosechas = Componentes.modeloTabla("Código", "Cultivo", "Parcela",
            "Fecha", "Cosechado (kg)", "Esperado (kg)", "Rendimiento", "kg/m²", "Reparto");
    private final JTable tablaCosechas = Componentes.tabla(modeloCosechas, "tablaCosechas");
    private List<Cosecha> filas = new ArrayList<>();

    private final JTextField txtReserva = campo("txtReserva", 4);
    private final DefaultTableModel modeloReparto = Componentes.modeloTabla("Participante", "Horas", "%", "Kg asignados");
    private final JLabel lblReparto = new JLabel(" ");
    private Reparto repartoCalculado;
    private Cosecha cosechaCalculada;

    private final JTextField txtDesde = campo("txtDesdeReporte", 9);
    private final JTextField txtHasta = campo("txtHastaReporte", 9);
    private final DefaultTableModel modeloReporte = Componentes.modeloTabla("Cultivo", "Cosechas", "Kg",
            "Área (m²)", "kg/m²", "Rendimiento");
    private final JLabel lblIndicadores = new JLabel(" ");

    public PanelCosechas(GestorCosechas gestor, GestorCultivos gestorCultivos) {
        super(new BorderLayout(6, 6));
        this.gestor = gestor;
        this.gestorCultivos = gestorCultivos;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cmbCultivo.setName("cmbCultivoCosecha");

        add(Componentes.encabezado("Módulo 4 · Gestión de cosechas y reparto  –  Responsable: Joseph Vásquez (Pruebas)",
                "RF-10 Registrar cosecha   ·   RF-11 Calcular el reparto de la cosecha   ·   "
                        + "RF-12 Generar el reporte de producción"), BorderLayout.NORTH);

        LocalDate hoy = LocalDate.now();
        txtFecha.setText(Formato.fecha(hoy));
        txtReserva.setText("10");
        txtDesde.setText(Formato.fecha(hoy.minusDays(90)));
        txtHasta.setText(Formato.fecha(hoy));

        JPanel cosechas = new JPanel(new BorderLayout(0, 4));
        cosechas.add(Componentes.fila(new JLabel("Cultivo listo para cosecha:"), cmbCultivo,
                new JLabel("Fecha:"), txtFecha, new JLabel("Cantidad (kg):"), txtKg,
                Componentes.boton("Registrar cosecha (RF-10)", "btnCosecha", e -> registrarCosecha())),
                BorderLayout.NORTH);
        cosechas.add(Componentes.conBorde(tablaCosechas, "Cosechas registradas (seleccione una para repartirla)"),
                BorderLayout.CENTER);

        JPanel reparto = new JPanel(new BorderLayout(0, 4));
        reparto.add(Componentes.fila(new JLabel("Reserva del grupo (%):"), txtReserva,
                Componentes.boton("Calcular reparto (RF-11)", "btnCalcularReparto", e -> calcularReparto()),
                Componentes.boton("Guardar reparto", "btnGuardarReparto", e -> guardarReparto())), BorderLayout.NORTH);
        reparto.add(Componentes.conBorde(Componentes.tabla(modeloReparto, "tablaReparto"),
                "RF-11 · Reparto proporcional a las horas trabajadas en el cultivo"), BorderLayout.CENTER);
        reparto.add(lblReparto, BorderLayout.SOUTH);

        JPanel reporte = new JPanel(new BorderLayout(0, 4));
        reporte.add(Componentes.fila(new JLabel("Desde:"), txtDesde, new JLabel("Hasta:"), txtHasta,
                Componentes.boton("Generar reporte (RF-12)", "btnReporte", e -> generarReporte())), BorderLayout.NORTH);
        reporte.add(Componentes.conBorde(Componentes.tabla(modeloReporte, "tablaReporte"),
                "RF-12 · Producción por cultivo"), BorderLayout.CENTER);
        lblIndicadores.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("RF-12 · Indicadores"), BorderFactory.createEmptyBorder(2, 8, 4, 8)));
        reporte.add(lblIndicadores, BorderLayout.SOUTH);

        JPanel abajo = new JPanel(new GridLayout(1, 2, 8, 0));
        abajo.add(reparto);
        abajo.add(reporte);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cosechas, abajo);
        division.setResizeWeight(0.4);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        actualizar();
        generarReporte();
    }

    private void registrarCosecha() {
        try {
            Cosecha c = gestor.registrarCosecha((Cultivo) cmbCultivo.getSelectedItem(),
                    Formato.fecha(txtFecha.getText(), "Fecha de cosecha"), txtKg.getText(), LocalDate.now());
            txtKg.setText("");
            actualizar();
            generarReporte();
            Componentes.info(this, "Cosecha " + c.getCodigo() + " registrada: " + Formato.decimal(c.getKg()) + " kg de "
                    + c.getCultivo().getNombre() + ".\nRendimiento: " + Formato.porcentaje(c.calcularRendimiento())
                    + " de lo esperado (" + Formato.decimal(c.getCultivo().calcularKgEsperados()) + " kg).\n"
                    + "El cultivo pasó a «Cosechado» y se liberaron "
                    + Formato.decimal(c.getCultivo().getAreaSembrada()) + " m² de la parcela.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void calcularReparto() {
        try {
            Cosecha cosecha = null;
            int fila = tablaCosechas.getSelectedRow();
            if (fila >= 0) {
                cosecha = filas.get(fila);
            }
            repartoCalculado = gestor.calcularReparto(cosecha, txtReserva.getText());
            cosechaCalculada = cosecha;
            mostrarReparto(cosecha, repartoCalculado);
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void guardarReparto() {
        try {
            gestor.guardarReparto(cosechaCalculada, repartoCalculado);
            Componentes.info(this, "Reparto de la cosecha " + cosechaCalculada.getCodigo() + " guardado.");
            repartoCalculado = null;
            cosechaCalculada = null;
            actualizar();
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void mostrarReparto(Cosecha cosecha, Reparto reparto) {
        modeloReparto.setRowCount(0);
        for (ItemReparto item : reparto.getItems()) {
            modeloReparto.addRow(new Object[]{item.getParticipante().getNombreCompleto(), Formato.decimal(item.getHoras()),
                    Formato.porcentaje(item.getPorcentaje()), Formato.decimal(item.getKg())});
        }
        lblReparto.setText("<html>" + cosecha.getCodigo() + " · " + cosecha.getCultivo().getNombre() + ": "
                + Formato.decimal(cosecha.getKg()) + " kg = reserva del grupo <b>" + Formato.decimal(reparto.getKgReserva())
                + " kg</b> (" + Formato.decimal(reparto.getPorcentajeReserva()) + " %) + repartido <b>"
                + Formato.decimal(reparto.getKgRepartidos()) + " kg</b></html>");
    }

    private void generarReporte() {
        try {
            ReporteProduccion r = gestor.generarReporte(Formato.fecha(txtDesde.getText(), "Desde"),
                    Formato.fecha(txtHasta.getText(), "Hasta"));
            modeloReporte.setRowCount(0);
            for (FilaProduccion f : r.getFilas()) {
                modeloReporte.addRow(new Object[]{f.getCultivo(), f.getCosechas(), Formato.decimal(f.getKg()),
                        Formato.decimal(f.getArea()), Formato.decimal(f.getKgPorM2()), Formato.porcentaje(f.getRendimiento())});
            }
            String causas = "ninguna";
            if (!r.getCausasPerdida().isEmpty()) {
                causas = "";
                for (Map.Entry<CausaPerdida, Integer> e : r.getCausasPerdida().entrySet()) {
                    if (!causas.isEmpty()) {
                        causas = causas + ", ";
                    }
                    causas = causas + e.getKey() + " (" + e.getValue() + ")";
                }
            }
            lblIndicadores.setText("<html>Total cosechado: <b>" + Formato.decimal(r.getKgTotales()) + " kg</b>"
                    + " · Cultivos cosechados: " + r.getCultivosCosechados() + " · perdidos: " + r.getCultivosPerdidos()
                    + "<br>Cultivos perdidos: <b>" + Formato.porcentaje(r.getPorcentajePerdidos()) + "</b> · causas: "
                    + causas + "<br>Horas trabajadas en el período: <b>" + Formato.decimal(r.getHorasTrabajadas())
                    + " h</b></html>");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    @Override
    public void actualizar() {
        cmbCultivo.removeAllItems();
        for (Cultivo c : gestorCultivos.getCultivosEnEstado(EstadoCultivo.LISTO_PARA_COSECHA)) {
            cmbCultivo.addItem(c);
        }
        filas = gestor.getCosechas();
        modeloCosechas.setRowCount(0);
        for (Cosecha c : filas) {
            String reparto = "Pendiente";
            if (c.estaRepartida()) {
                reparto = "Repartida";
            }
            modeloCosechas.addRow(new Object[]{c.getCodigo(), c.getCultivo().getNombre(),
                    c.getCultivo().getParcela().getCodigo(), Formato.fecha(c.getFecha()), Formato.decimal(c.getKg()),
                    Formato.decimal(c.getCultivo().calcularKgEsperados()), Formato.porcentaje(c.calcularRendimiento()),
                    Formato.decimal(c.calcularKgPorM2()), reparto});
        }
    }

    private static JTextField campo(String nombre, int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setName(nombre);
        return campo;
    }
}
