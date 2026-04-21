package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.model.Usuario;
import com.sistema.service.ILoteService;
import com.sistema.service.IPesajeService;
import com.sistema.service.IRecolectorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel de registro de Pesajes.
 * Permite registrar kilos diarios asociando recolector, lote, fecha
 * y el usuario autenticado que realiza el registro.
 */
public class PesajePanel extends JPanel {

    private final IPesajeService     pesajeService;
    private final IRecolectorService recolectorService;
    private final ILoteService       loteService;
    private final Usuario            usuarioActual;   // ← NUEVO: usuario autenticado

    private final JComboBox<Recolector> cmbRecolector = new JComboBox<>();
    private final JComboBox<Lote>       cmbLote       = new JComboBox<>();
    private final JTextField            txtFecha      = new JTextField(12);
    private final JTextField            txtKilos      = new JTextField(8);

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private Long idSeleccionado = null;

    private static final String[] COLUMNAS = {"ID", "Recolector", "Lote", "Fecha", "Kilos", "Registrado por"};

    public PesajePanel(IPesajeService pesajeService,
                       IRecolectorService recolectorService,
                       ILoteService loteService,
                       Usuario usuarioActual) {          // ← NUEVO parámetro
        this.pesajeService     = pesajeService;
        this.recolectorService = recolectorService;
        this.loteService       = loteService;
        this.usuarioActual     = usuarioActual;          // ← NUEVO

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);

        // Fecha por defecto = hoy
        txtFecha.setText(LocalDate.now().toString());

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla),  BorderLayout.CENTER);
        add(crearPanelBotones(),     BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                if (fila >= 0) idSeleccionado = (Long) modeloTabla.getValueAt(fila, 0);
            }
        });

        cargarCombos();
        refrescarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Registro de Pesaje"));
        panel.add(new JLabel("Recolector*:"));          panel.add(cmbRecolector);
        panel.add(new JLabel("Lote*:"));                panel.add(cmbLote);
        panel.add(new JLabel("Fecha* (AAAA-MM-DD):"));  panel.add(txtFecha);
        panel.add(new JLabel("Kilos*:"));               panel.add(txtKilos);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton btnRegistrar = new JButton("Registrar Pesaje");
        JButton btnEliminar  = new JButton("Eliminar");
        JButton btnLimpiar   = new JButton("Limpiar");

        btnRegistrar.addActionListener(e -> registrar());
        btnEliminar.addActionListener(e  -> eliminar());
        btnLimpiar.addActionListener(e   -> limpiar());

        panel.add(btnRegistrar); panel.add(btnEliminar); panel.add(btnLimpiar);
        return panel;
    }

    // ── Acciones ──────────────────────────────────────────────

    private void registrar() {
        try {
            Recolector r = (Recolector) cmbRecolector.getSelectedItem();
            Lote l       = (Lote)       cmbLote.getSelectedItem();
            Long recolId = (r != null) ? r.getId() : null;
            Long loteId  = (l != null) ? l.getId() : null;

            // ← CAMBIO: se pasa usuarioActual para registrar quién hizo el pesaje
            pesajeService.registrar(recolId, loteId, txtFecha.getText(), txtKilos.getText(), usuarioActual);

            mostrarExito("Pesaje registrado correctamente.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) { mostrarError("Seleccione un pesaje de la tabla."); return; }
        int c = JOptionPane.showConfirmDialog(this,
            "¿Eliminar este pesaje?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            pesajeService.eliminar(idSeleccionado);
            mostrarExito("Pesaje eliminado.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void limpiar() {
        txtFecha.setText(LocalDate.now().toString());
        txtKilos.setText("");
        cmbRecolector.setSelectedIndex(0);
        cmbLote.setSelectedIndex(0);
        idSeleccionado = null;
        tabla.clearSelection();
    }

    // ── Tabla y combos ────────────────────────────────────────

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Pesaje p : pesajeService.listarTodos()) {
            // ← CAMBIO: columna extra que muestra quién registró el pesaje
            String registradoPor = (p.getRegistradoPor() != null)
                ? p.getRegistradoPor().getNombre()
                : "—";
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getRecolector().getNombre(),
                p.getLote().getCodigo(),
                p.getFecha().toString(),
                String.format("%.2f", p.getKilos()),
                registradoPor                          // ← NUEVO
            });
        }
    }

    private void cargarCombos() {
        cmbRecolector.removeAllItems();
        cmbRecolector.addItem(null);
        for (Recolector r : recolectorService.listarActivos()) cmbRecolector.addItem(r);

        cmbLote.removeAllItems();
        cmbLote.addItem(null);
        for (Lote l : loteService.listarActivos()) cmbLote.addItem(l);
    }

    public void refrescar() {
        cargarCombos();
        refrescarTabla();
    }

    private void mostrarExito(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error de validación", JOptionPane.WARNING_MESSAGE);
    }
    private void mostrarErrorInterno(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error interno: " + ex.getMessage(),
            "Error del sistema", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}