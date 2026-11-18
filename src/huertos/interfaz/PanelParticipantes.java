package huertos.interfaz;

import huertos.modelo.EstadoParticipante;
import huertos.modelo.Participante;
import huertos.modelo.RolHuerto;
import huertos.negocio.Formato;
import huertos.negocio.GestorParticipantes;
import huertos.negocio.ResumenParticipacion;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PanelParticipantes extends JPanel implements Actualizable {
    private final GestorParticipantes gestor;

    private final JTextField txtCedula = campo("txtCedula", 10);
    private final JTextField txtNombres = campo("txtNombres", 14);
    private final JTextField txtApellidos = campo("txtApellidos", 14);
    private final JTextField txtTelefono = campo("txtTelefono", 10);
    private final JComboBox<RolHuerto> cmbRol = new JComboBox<>(RolHuerto.values());
    private final JTextField txtFechaIngreso = campo("txtFechaIngreso", 9);
    private final JTextField txtBuscar = campo("txtBuscar", 16);

    private final DefaultTableModel modelo = Componentes.modeloTabla(
            "Cédula", "Nombre completo", "Rol", "Teléfono", "Ingreso", "Estado", "Horas acumuladas");
    private final JTable tabla = Componentes.tabla(modelo, "tablaParticipantes");
    private List<Participante> filas = new ArrayList<>();

    private final JTextField txtDesde = campo("txtDesde", 9);
    private final JTextField txtHasta = campo("txtHasta", 9);
    private final DefaultTableModel modeloParticipacion = Componentes.modeloTabla(
            "Participante", "Labores completadas", "Horas trabajadas", "% de participación", "Estado");
    private final JLabel lblParticipacion = new JLabel(" ");

    public PanelParticipantes(GestorParticipantes gestor) {
        super(new BorderLayout(6, 6));
        this.gestor = gestor;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cmbRol.setName("cmbRol");

        add(Componentes.encabezado("Participantes del huerto",
                "Registre a las personas del huerto, actualice sus datos y revise cuántas horas ha trabajado cada una."), BorderLayout.NORTH);

        Componentes.Formulario formulario = new Componentes.Formulario("Datos del participante", 3)
                .agregar("Cédula:", txtCedula)
                .agregar("Nombres:", txtNombres)
                .agregar("Apellidos:", txtApellidos)
                .agregar("Teléfono:", txtTelefono)
                .agregar("Rol en el huerto:", cmbRol)
                .agregar("Fecha de ingreso:", txtFechaIngreso);
        JPanel botones = Componentes.fila(
                Componentes.boton("Registrar", "btnRegistrar", e -> registrar()),
                Componentes.boton("Actualizar", "btnActualizar", e -> actualizarDatos()),
                Componentes.boton("Eliminar", "btnEliminar", e -> eliminar()),
                Componentes.boton("Activar / desactivar", "btnEstado", e -> cambiarEstado()),
                Componentes.boton("Limpiar", "btnLimpiar", e -> limpiar()),
                new JLabel("   Buscar por cédula o nombre:"), txtBuscar,
                Componentes.boton("Buscar", "btnBuscar", e -> actualizar()));
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(botones, BorderLayout.SOUTH);
        JPanel registro = new JPanel(new BorderLayout(0, 6));
        registro.add(arriba, BorderLayout.NORTH);
        registro.add(Componentes.conBorde(tabla, "Participantes del huerto (seleccione una fila para editarla)"),
                BorderLayout.CENTER);

        LocalDate hoy = LocalDate.now();
        txtDesde.setText(Formato.fecha(hoy.minusDays(60)));
        txtHasta.setText(Formato.fecha(hoy));
        JPanel participacion = new JPanel(new BorderLayout(0, 4));
        participacion.add(Componentes.fila(new JLabel("Desde:"), txtDesde, new JLabel("Hasta:"), txtHasta,
                Componentes.boton("Calcular participación", "btnParticipacion", e -> calcularParticipacion()),
                lblParticipacion), BorderLayout.NORTH);
        participacion.add(Componentes.conBorde(Componentes.tabla(modeloParticipacion, "tablaParticipacion"),
                "Participación en el período (horas de labores completadas)"), BorderLayout.CENTER);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, registro, participacion);
        division.setResizeWeight(0.6);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        limpiar();
        actualizar();
    }

    private void registrar() {
        try {
            LocalDate hoy = LocalDate.now();
            Participante p = gestor.registrar(txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                    txtTelefono.getText(), (RolHuerto) cmbRol.getSelectedItem(),
                    Formato.fecha(txtFechaIngreso.getText(), "Fecha de ingreso"), hoy);
            limpiar();
            actualizar();
            Componentes.info(this, "Participante registrado: " + p.getNombreCompleto()
                    + "\nEstado: " + p.getEstado() + " · Horas acumuladas: 0");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void actualizarDatos() {
        try {
            gestor.actualizar(txtCedula.getText(), txtNombres.getText(), txtApellidos.getText(),
                    txtTelefono.getText(), (RolHuerto) cmbRol.getSelectedItem());
            actualizar();
            Componentes.info(this, "Datos actualizados.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void eliminar() {
        try {
            Participante p = gestor.obtener(txtCedula.getText());
            if (gestor.motivoNoEliminable(p) == null
                    && !Componentes.confirmar(this, "¿Eliminar a " + p.getNombreCompleto() + "?")) {
                return;
            }
            gestor.eliminar(p.getCedula());
            limpiar();
            actualizar();
            Componentes.info(this, "Participante eliminado.");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void cambiarEstado() {
        try {
            Participante p = gestor.obtener(txtCedula.getText());
            EstadoParticipante nuevo = EstadoParticipante.INACTIVO;
            if (!p.estaActivo()) {
                nuevo = EstadoParticipante.ACTIVO;
            }
            gestor.cambiarEstado(p.getCedula(), nuevo);
            actualizar();
            Componentes.info(this, p.getNombreCompleto() + " ahora está «" + nuevo + "».");
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void calcularParticipacion() {
        try {
            LocalDate hoy = LocalDate.now();
            List<ResumenParticipacion> resultado = gestor.calcularParticipacion(
                    Formato.fecha(txtDesde.getText(), "Desde"), Formato.fecha(txtHasta.getText(), "Hasta"), hoy);
            modeloParticipacion.setRowCount(0);
            double total = 0;
            for (ResumenParticipacion r : resultado) {
                modeloParticipacion.addRow(new Object[]{r.getParticipante().getNombreCompleto(), r.getNumeroLabores(),
                        Formato.decimal(r.getHoras()), Formato.porcentaje(r.getPorcentaje()),
                        r.getParticipante().getEstado()});
                total = total + r.getHoras();
            }
            lblParticipacion.setText("   Total del grupo: " + Formato.decimal(total) + " h   ·   Quien pasa "
                    + GestorParticipantes.DIAS_INACTIVIDAD + " días sin completar labores queda como «Inactivo».");
            actualizar();
        } catch (RuntimeException ex) {
            Componentes.error(this, ex);
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= filas.size()) {
            return;
        }
        Participante p = filas.get(fila);
        txtCedula.setText(p.getCedula());
        txtCedula.setEnabled(false);
        txtNombres.setText(p.getNombres());
        txtApellidos.setText(p.getApellidos());
        txtTelefono.setText(p.getTelefono());
        cmbRol.setSelectedItem(p.getRol());
        txtFechaIngreso.setText(Formato.fecha(p.getFechaIngreso()));
        txtFechaIngreso.setEnabled(false);
    }

    private void limpiar() {
        txtCedula.setText("");
        txtCedula.setEnabled(true);
        txtNombres.setText("");
        txtApellidos.setText("");
        txtTelefono.setText("");
        cmbRol.setSelectedIndex(0);
        txtFechaIngreso.setText(Formato.fecha(LocalDate.now()));
        txtFechaIngreso.setEnabled(true);
        tabla.clearSelection();
    }

    @Override
    public void actualizar() {
        filas = gestor.buscar(txtBuscar.getText());
        modelo.setRowCount(0);
        for (Participante p : filas) {
            modelo.addRow(new Object[]{p.getCedula(), p.getNombreCompleto(), p.getRol(), p.getTelefono(),
                    Formato.fecha(p.getFechaIngreso()), p.getEstado(), Formato.decimal(p.getHorasAcumuladas())});
        }
    }

    private static JTextField campo(String nombre, int columnas) {
        JTextField campo = new JTextField(columnas);
        campo.setName(nombre);
        return campo;
    }
}
