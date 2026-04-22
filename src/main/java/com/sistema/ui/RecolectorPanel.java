package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.model.Recolector;
import com.sistema.service.ILoteService;
import com.sistema.service.IRecolectorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RecolectorPanel extends JPanel {

    private final IRecolectorService recolectorService;
    private final ILoteService       loteService;

    private final JTextField      txtNombre = new JTextField(20);
    private final JTextField      txtCedula = new JTextField(15);
    private final JComboBox<Lote> cmbLote   = new JComboBox<>();

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private Long idSeleccionado = null;

    private static final String[] COLUMNAS = {"ID", "Nombre", "Cédula", "Lote", "Estado"};

    public RecolectorPanel(IRecolectorService recolectorService, ILoteService loteService) {
        this.recolectorService = recolectorService;
        this.loteService       = loteService;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla),  BorderLayout.CENTER);
        add(crearPanelBotones(),     BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarLotesEnCombo();
        refrescarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Recolector"));
        panel.add(new JLabel("Nombre*:")); panel.add(txtNombre);
        panel.add(new JLabel("Cédula*:")); panel.add(txtCedula);
        panel.add(new JLabel("Lote:"));    panel.add(cmbLote);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton btnGuardar    = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnActivar    = new JButton("Activar");      // ← NUEVO
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar    = new JButton("Limpiar");

        btnGuardar.addActionListener(e    -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivar.addActionListener(e    -> activar());     // ← NUEVO
        btnDesactivar.addActionListener(e -> desactivar());
        btnLimpiar.addActionListener(e    -> limpiar());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnActivar);                               // ← NUEVO
        panel.add(btnDesactivar);
        panel.add(btnLimpiar);
        return panel;
    }

    // ── Acciones ──────────────────────────────────────────────

    private void guardar() {
        try {
            Long loteId = getLoteIdSeleccionado();
            recolectorService.crear(txtNombre.getText(), txtCedula.getText(), loteId);
            mostrarExito("Recolector guardado correctamente.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) { mostrarError("Seleccione un recolector."); return; }
        try {
            Recolector r = recolectorService.buscarPorId(idSeleccionado);
            Long loteId  = getLoteIdSeleccionado();
            recolectorService.actualizar(r, txtNombre.getText(), txtCedula.getText(), loteId);
            mostrarExito("Recolector actualizado.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void activar() {                                 // ← NUEVO
        if (idSeleccionado == null) { mostrarError("Seleccione un recolector."); return; }
        int c = JOptionPane.showConfirmDialog(this,
            "¿Activar este recolector?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            recolectorService.activar(idSeleccionado);
            mostrarExito("Recolector activado correctamente.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void desactivar() {
        if (idSeleccionado == null) { mostrarError("Seleccione un recolector."); return; }
        int c = JOptionPane.showConfirmDialog(this,
            "¿Desactivar este recolector?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            recolectorService.desactivar(idSeleccionado);
            mostrarExito("Recolector desactivado.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        txtCedula.setText("");
        cmbLote.setSelectedIndex(0);
        idSeleccionado = null;
        tabla.clearSelection();
    }

    // ── Tabla ─────────────────────────────────────────────────

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Recolector r : recolectorService.listarTodos()) {
            modeloTabla.addRow(new Object[]{
                r.getId(), r.getNombre(), r.getCedula(),
                r.getLote() != null ? r.getLote().getCodigo() : "—",
                r.getActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (Long) modeloTabla.getValueAt(fila, 0);
        txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
        txtCedula.setText((String) modeloTabla.getValueAt(fila, 2));
        String codigoLote = (String) modeloTabla.getValueAt(fila, 3);
        for (int i = 0; i < cmbLote.getItemCount(); i++) {
            Lote l = cmbLote.getItemAt(i);
            if (l != null && l.getCodigo().equals(codigoLote)) {
                cmbLote.setSelectedIndex(i); break;
            }
        }
    }

    private void cargarLotesEnCombo() {
        cmbLote.removeAllItems();
        cmbLote.addItem(null);
        for (Lote l : loteService.listarActivos()) cmbLote.addItem(l);
    }

    private Long getLoteIdSeleccionado() {
        Lote lote = (Lote) cmbLote.getSelectedItem();
        return (lote != null) ? lote.getId() : null;
    }

    public void refrescar() {
        cargarLotesEnCombo();
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