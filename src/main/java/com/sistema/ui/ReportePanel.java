package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Usuario;
import com.sistema.report.IReporteGenerador;
import com.sistema.util.UIEstilo;
import com.sistema.util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel de Reportes.
 *
 * ── POLIMORFISMO EN ACCIÓN ────────────────────────────────────
 * Este panel recibe una List<IReporteGenerador> y es completamente
 * agnóstico del tipo de reporte que contiene.
 *
 * Cuando el usuario selecciona un reporte del JComboBox y pulsa
 * "Generar", este panel simplemente llama:
 *
 *   reporteSeleccionado.getTitulo()
 *   reporteSeleccionado.getColumnas()
 *   reporteSeleccionado.generarDatos(inicio, fin)
 *
 * Si mañana se agrega un cuarto tipo de reporte (ReportePorTemporada),
 * sólo hay que registrarlo en MainFrame. Este panel NO cambia. (OCP ✓)
 *
 * El botón "Eliminar Registro" solo se habilita cuando el reporte
 * seleccionado implementa soportaEliminar() = true y hay fila seleccionada.
 * Solo el usuario admin puede ejecutar la eliminación.
 */
public class ReportePanel extends JPanel {

    private final List<IReporteGenerador> reportes;
    private final Usuario                 usuarioActual;

    private final JComboBox<IReporteGenerador> cmbReporte = new JComboBox<>();
    private final JTextField txtInicio = new JTextField(12);
    private final JTextField txtFin    = new JTextField(12);

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private final JLabel            lblInfo    = new JLabel(" ");
    private final JButton           btnEliminar = new JButton("Eliminar Registro");

    public ReportePanel(List<IReporteGenerador> reportes, Usuario usuarioActual) {
        this.reportes      = reportes;
        this.usuarioActual = usuarioActual;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIEstilo.FONDO);

        modeloTabla = new DefaultTableModel(new String[]{"—"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UIEstilo.aplicarEstiloTabla(tabla);

        UIEstilo.aplicarEstiloComboBox(cmbReporte);
        UIEstilo.aplicarEstiloCampoTexto(txtInicio);
        UIEstilo.aplicarEstiloCampoTexto(txtFin);

        lblInfo.setFont(UIEstilo.FUENTE_SUBTITULO);
        lblInfo.setForeground(UIEstilo.TEXTO_SECUNDARIO);

        btnEliminar.setEnabled(false);
        UIEstilo.aplicarEstiloBoton(btnEliminar, UIEstilo.PELIGRO);

        // Fechas por defecto: mes actual
        txtInicio.setText(LocalDate.now().withDayOfMonth(1).toString());
        txtFin.setText(LocalDate.now().toString());

        // Poblar combo con polimorfismo:
        // cada IReporteGenerador.getTitulo() es el texto que aparece en el combo
        for (IReporteGenerador r : reportes) cmbReporte.addItem(r);
        cmbReporte.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value instanceof IReporteGenerador) {
                    setText(((IReporteGenerador) value).getTitulo());
                }
                return this;
            }
        });

        // Deshabilitar Eliminar cuando cambia el tipo de reporte
        cmbReporte.addActionListener(e -> actualizarBotonEliminar());

        // Habilitar Eliminar solo cuando el reporte lo soporta y hay fila seleccionada
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarBotonEliminar();
        });

        JScrollPane scroll = new JScrollPane(tabla);
        UIEstilo.aplicarEstiloScrollPane(scroll);

        add(crearPanelFiltros(), BorderLayout.NORTH);
        add(scroll,              BorderLayout.CENTER);
        add(lblInfo,             BorderLayout.SOUTH);
    }

    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(UIEstilo.crearBordeTitulado("Filtros del Reporte"));
        panel.setBackground(UIEstilo.FONDO);

        JButton btnGenerar = new JButton("Generar Reporte");
        UIEstilo.aplicarEstiloBoton(btnGenerar, UIEstilo.PRIMARIO);
        btnGenerar.addActionListener(e -> generarReporte());
        btnEliminar.addActionListener(e -> eliminarRegistro());

        panel.add(new JLabel("Tipo de Reporte:"));          panel.add(cmbReporte);
        panel.add(new JLabel("Desde (AAAA-MM-DD):"));        panel.add(txtInicio);
        panel.add(new JLabel("Hasta (AAAA-MM-DD):"));        panel.add(txtFin);
        panel.add(btnGenerar);
        panel.add(btnEliminar);
        return panel;
    }

    // ── Lógica del panel ──────────────────────────────────────

    private void actualizarBotonEliminar() {
        IReporteGenerador reporte = (IReporteGenerador) cmbReporte.getSelectedItem();
        boolean soporta  = reporte != null && reporte.soportaEliminar();
        boolean hayFila  = tabla.getSelectedRow() >= 0;
        btnEliminar.setEnabled(soporta && hayFila);
    }

    private void generarReporte() {
        IReporteGenerador reporte = (IReporteGenerador) cmbReporte.getSelectedItem();
        if (reporte == null) return;

        try {
            LocalDate inicio = Validador.parsearFecha(txtInicio.getText(), "fecha inicio");
            LocalDate fin    = Validador.parsearFecha(txtFin.getText(),    "fecha fin");
            Validador.rangoFechas(inicio, fin);

            // ── Polimorfismo: mismo código para cualquier tipo de reporte ──
            String[]       columnas = reporte.getColumnas();
            List<Object[]> datos    = reporte.generarDatos(inicio, fin);

            // Reconfigurar columnas de la tabla según el reporte seleccionado
            modeloTabla.setColumnCount(0);
            for (String col : columnas) modeloTabla.addColumn(col);

            modeloTabla.setRowCount(0);
            for (Object[] fila : datos) modeloTabla.addRow(fila);

            lblInfo.setText("  " + reporte.getTitulo() + " — "
                + datos.size() + " registros  |  Período: " + inicio + " → " + fin);

            actualizarBotonEliminar();

            if (datos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No hay datos en el período seleccionado.",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error interno: " + ex.getMessage(),
                "Error del sistema", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void eliminarRegistro() {
        if (!"admin".equalsIgnoreCase(usuarioActual.getUsername())) {
            JOptionPane.showMessageDialog(this,
                "Solo el usuario admin puede eliminar liquidaciones.",
                "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int fila = tabla.getSelectedRow();
        if (fila < 0) return;

        IReporteGenerador reporte = (IReporteGenerador) cmbReporte.getSelectedItem();
        if (reporte == null || !reporte.soportaEliminar()) return;

        Object id = modeloTabla.getValueAt(fila, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Eliminar el detalle del recolector seleccionado (ID " + id + ")?\n" +
            "Si es el último detalle de la liquidación, también se eliminará la liquidación.\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) return;

        try {
            reporte.eliminar(id);
            JOptionPane.showMessageDialog(this,
                "Detalle eliminado correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            generarReporte();
        } catch (NegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error interno: " + ex.getMessage(),
                "Error del sistema", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
