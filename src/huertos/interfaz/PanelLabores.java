package huertos.interfaz;

import huertos.modelo.Cultivo;
import huertos.modelo.EstadoLabor;
import huertos.modelo.Labor;
import huertos.modelo.Participante;
import huertos.modelo.TipoLabor;
import huertos.negocio.AgendaSemanal;
import huertos.negocio.Formato;
import huertos.negocio.GestorCultivos;
import huertos.negocio.GestorLabores;
import huertos.negocio.GestorParticipantes;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelLabores extends JPanel implements Actualizable {
    private static final String TODOS = "(Todos)";

    private final GestorLabores gestor;
    private final GestorParticipantes gestorParticipantes;
    private final GestorCultivos gestorCultivos;

    private final JComboBox<TipoLabor> cmbTipo = new JComboBox<>(TipoLabor.values());
    private final JComboBox<Cultivo> cmbCultivo = new JComboBox<>();
    private final JComboBox<Participante> cmbResponsable = new JComboBox<>();
    private final JTextField txtFecha = campo("txtFechaLabor", 9);
    private final JTextField txtFrecuencia = campo("txtFrecuencia", 4);

    private final JTextField txtFechaRealizacion = campo("txtFechaRealizacion", 9);
    private final JTextField txtHoras = campo("txtHoras", 4);
    private final JTextField txtObservaciones = campo("txtObservaciones", 16);

    private final JTextField txtSemana = campo("txtSemana", 9);
    private final JComboBox<Object> cmbFiltroResponsable = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroCultivo = new JComboBox<>();
    private final DefaultTableModel modeloAgenda = Componentes.modeloTabla("N.º", "Fecha", "Día", "Labor",
            "Cultivo", "Responsable", "Estado", "Horas", "Observaciones");
    private final JTable tablaAgenda = Componentes.tabla(modeloAgenda, "tablaAgenda");
    private List<Labor> filas = new ArrayList<>();
    private final JLabel lblResumen = new JLabel(" ");

    public PanelLabores(GestorLabores gestor, GestorParticipantes gestorParticipantes, GestorCultivos gestorCultivos) {
        super(new BorderLayout(6, 6));
        this.gestor = gestor;
        this.gestorParticipantes = gestorParticipantes;
        this.gestorCultivos = gestorCultivos;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cmbTipo.setName("cmbTipoLabor");
        cmbCultivo.setName("cmbCultivoLabor");
        cmbResponsable.setName("cmbResponsable");
        cmbFiltroResponsable.setName("cmbFiltroResponsable");
        cmbFiltroCultivo.setName("cmbFiltroCultivo");

        add(Componentes.encabezado("Labores del huerto",
                "Programe las tareas de cuidado, registre las que ya se hicieron y revise la agenda de la semana."),
                BorderLayout.NORTH);

        LocalDate hoy = LocalDate.now();
        txtFecha.setText(Formato.fecha(hoy));
        txtFrecuencia.setText("0");
        txtFechaRealizacion.setText(Formato.fecha(hoy));
        txtSemana.setText(Formato.fecha(hoy));

        Componentes.Formulario formProgramar = new Componentes.Formulario("Programar labor", 1)
                .agregar("Tipo de labor:", cmbTipo)
                .agregar("Cultivo:", cmbCultivo)
                .agregar("Responsable:", cmbResponsable)
                .agregar("Fecha:", txtFecha)
                .agregar("Repetir cada (días, 0 = única):", txtFrecuencia);
        JPanel programar = new JPanel(new BorderLayout());
        programar.add(formProgramar, BorderLayout.CENTER);
        programar.add(Componentes.fila(
                Componentes.boton("Programar", "btnProgramar", e -> programar()),
                Componentes.boton("Reprogramar selección", "btnReprogramar", e -> reprogramar()),
                Componentes.boton("Cancelar selección", "btnCancelarLabor", e -> cancelar())), BorderLayout.SOUTH);

        Componentes.Formulario formCumplimiento = new Componentes.Formulario(
                "Registrar labor realizada (seleccione la labor en la agenda)", 1)
                .agregar("Fecha de realización:", txtFechaRealizacion)
                .agregar("Horas trabajadas (0,5 a 8):", txtHoras)
                .agregar("Observaciones:", txtObservaciones);
        JPanel cumplimiento = new JPanel(new BorderLayout());
        cumplimiento.add(formCumplimiento, BorderLayout.NORTH);
        cumplimiento.add(Componentes.fila(Componentes.boton("Registrar labor realizada", "btnCumplimiento",
                e -> registrarCumplimiento())), BorderLayout.CENTER);

        JPanel arriba = new JPanel(new GridLayout(1, 2, 8, 0));
        arriba.add(programar);
        arriba.add(cumplimiento);

        JPanel agenda = new JPanel(new BorderLayout(0, 4));
        agenda.add(Componentes.fila(new JLabel("Semana del día:"), txtSemana, new JLabel("Responsable:"),
                cmbFiltroResponsable, new JLabel("Cultivo:"), cmbFiltroCultivo,
                Componentes.boton("Ver agenda", "btnAgenda", e -> generarAgenda())), BorderLayout.NORTH);
        agenda.add(Componentes.conBorde(tablaAgenda,
                "Agenda de la semana + labores atrasadas de semanas anteriores (en rojo)"), BorderLayout.CENTER);
        lblResumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Alertas"), BorderFactory.createEmptyBorder(2, 8, 4, 8)));
        agenda.add(lblResumen, BorderLayout.SOUTH);
        tablaAgenda.setDefaultRenderer(Object.class, new RenderizadorEstado());

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.add(arriba, BorderLayout.NORTH);
        centro.add(agenda, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        actualizar();
    }

    private void programar() {
        try {
            List<Labor> creadas = gestor.programar((TipoLabor) cmbTipo.getSelectedItem(),
                    (Cultivo) cmbCultivo.getSelectedItem(), (Participante) cmbResponsable.getSelectedItem(),
                    Formato.fecha(txtFecha.getText(), "Fecha"), txtFrecuencia.getText(), LocalDate.now());
            generarAgenda();
            String mensaje = "Se programaron " + creadas.size() + " labor(es) en estado «Pendiente».";
            if (creadas.size() > 1) {
                mensaje = mensaje + "\nDesde " + Formato.fecha(creadas.get(0).getFechaProgramada()) + " hasta "
                        + Formato.fecha(creadas.get(creadas.size() - 1).getFechaProgramada())
                        + " (fecha estimada de cosecha).";
            }
            Componentes.info(this, mensaje);
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void reprogramar() {
        try {
            Labor labor = laborSeleccionada();
            gestor.reprogramar(labor, Formato.fecha(txtFecha.getText(), "Fecha"),
                    (Participante) cmbResponsable.getSelectedItem(), LocalDate.now());
            generarAgenda();
            Componentes.info(this, "Labor N.º " + labor.getNumero() + " reprogramada para el "
                    + Formato.fecha(labor.getFechaProgramada()) + ".");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void cancelar() {
        try {
            Labor labor = laborSeleccionada();
            gestor.cancelar(labor);
            generarAgenda();
            Componentes.info(this, "Labor N.º " + labor.getNumero() + " cancelada.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void registrarCumplimiento() {
        try {
            Labor labor = laborSeleccionada();
            gestor.registrarCumplimiento(labor, Formato.fecha(txtFechaRealizacion.getText(), "Fecha de realización"),
                    txtHoras.getText(), txtObservaciones.getText(), LocalDate.now());
            txtHoras.setText("");
            txtObservaciones.setText("");
            generarAgenda();
            Componentes.info(this, "Labor N.º " + labor.getNumero() + " completada.\n"
                    + labor.getResponsable().getNombreCompleto() + " suma ahora "
                    + Formato.decimal(labor.getResponsable().getHorasAcumuladas()) + " horas acumuladas.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void generarAgenda() {
        try {
            Participante responsable = null;
            if (cmbFiltroResponsable.getSelectedItem() instanceof Participante) {
                responsable = (Participante) cmbFiltroResponsable.getSelectedItem();
            }
            Cultivo cultivo = null;
            if (cmbFiltroCultivo.getSelectedItem() instanceof Cultivo) {
                cultivo = (Cultivo) cmbFiltroCultivo.getSelectedItem();
            }
            AgendaSemanal agenda = gestor.generarAgenda(Formato.fecha(txtSemana.getText(), "Semana del día"),
                    LocalDate.now(), responsable, cultivo);
            filas = agenda.getLabores();
            modeloAgenda.setRowCount(0);
            for (Labor l : filas) {
                String horas = "";
                if (l.getEstado() == EstadoLabor.COMPLETADA) {
                    horas = Formato.decimal(l.getHorasTrabajadas());
                }
                String dia = l.getFechaProgramada().getDayOfWeek().getDisplayName(TextStyle.FULL, Formato.ECUADOR);
                modeloAgenda.addRow(new Object[]{l.getNumero(), Formato.fecha(l.getFechaProgramada()), dia,
                        l.getTipo(), l.getCultivo().getNombre(), l.getResponsable().getNombreCompleto(),
                        l.getEstado(), horas, l.getObservaciones()});
            }
            String texto = "<html>Semana del " + Formato.fecha(agenda.getLunes()) + " al "
                    + Formato.fecha(agenda.getDomingo()) + ": <b>" + filas.size() + " labores</b>, <b>"
                    + agenda.getTotalAtrasadas() + " atrasadas</b>.";
            if (agenda.getTotalAtrasadas() > 0) {
                texto = texto + "<br>Atrasadas por responsable: " + listar(agenda.getAtrasadasPorResponsable())
                        + "<br>Atrasadas por cultivo: " + listar(agenda.getAtrasadasPorCultivo());
            }
            lblResumen.setText(texto + "</html>");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private String listar(Map<String, Integer> mapa) {
        String texto = "";
        for (Map.Entry<String, Integer> e : mapa.entrySet()) {
            if (!texto.isEmpty()) {
                texto = texto + " · ";
            }
            texto = texto + e.getKey() + " (" + e.getValue() + ")";
        }
        return texto;
    }

    private Labor laborSeleccionada() {
        int fila = tablaAgenda.getSelectedRow();
        if (fila < 0 || fila >= filas.size()) {
            return null;
        }
        return filas.get(fila);
    }

    @Override
    public void actualizar() {
        cmbCultivo.removeAllItems();
        cmbFiltroCultivo.removeAllItems();
        cmbFiltroCultivo.addItem(TODOS);
        for (Cultivo c : gestorCultivos.getCultivosActivos()) {
            cmbCultivo.addItem(c);
            cmbFiltroCultivo.addItem(c);
        }
        cmbResponsable.removeAllItems();
        for (Participante p : gestorParticipantes.getActivos()) {
            cmbResponsable.addItem(p);
        }
        cmbFiltroResponsable.removeAllItems();
        cmbFiltroResponsable.addItem(TODOS);
        for (Participante p : gestorParticipantes.buscar("")) {
            cmbFiltroResponsable.addItem(p);
        }
        generarAgenda();
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
                Object estado = modeloAgenda.getValueAt(fila, 6);
                if (estado == EstadoLabor.ATRASADA) {
                    c.setForeground(Componentes.ROJO);
                } else if (estado == EstadoLabor.COMPLETADA) {
                    c.setForeground(Componentes.VERDE);
                } else if (estado == EstadoLabor.CANCELADA) {
                    c.setForeground(Color.GRAY);
                } else {
                    c.setForeground(t.getForeground());
                }
            }
            return c;
        }
    }
}
