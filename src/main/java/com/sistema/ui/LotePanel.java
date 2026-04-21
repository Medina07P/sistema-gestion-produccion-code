package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.service.ILoteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de gestión de Lotes.
 * Estructura: formulario (norte) + tabla (centro) + botones (sur).
 *
 * Sólo conoce ILoteService (interfaz). Principio DIP aplicado.
 * No tiene lógica de negocio: sólo captura datos y delega al servicio.
 */
public class LotePanel extends JPanel {

    // ── Servicio (inyectado) ──────────────────────────────────
    private final ILoteService loteService;

    // ── Componentes del formulario ────────────────────────────
    private final JTextField txtCodigo      = new JTextField(10);
    private final JTextField txtDescripcion = new JTextField(25);

    // ── Tabla ─────────────────────────────────────────────────
    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private Long idSeleccionado = null;   // ID del registro seleccionado en tabla

    // ── Columnas de la tabla ──────────────────────────────────
    private static final String[] COLUMNAS = {"ID", "Código", "Descripción", "Estado"};

    public LotePanel(ILoteService loteService) {
        this.loteService = loteService;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);   // ID angosto

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(new JScrollPane(tabla),  BorderLayout.CENTER);
        add(crearPanelBotones(),     BorderLayout.SOUTH);

        // Al seleccionar fila → cargar en formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        refrescarTabla();
    }

    // ── Construcción de subpaneles ────────────────────────────

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Lote"));
        panel.add(new JLabel("Código*:"));    panel.add(txtCodigo);
        panel.add(new JLabel("Descripción:")); panel.add(txtDescripcion);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton btnGuardar    = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar    = new JButton("Limpiar");

        btnGuardar.addActionListener(e    -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnDesactivar.addActionListener(e -> desactivar());
        btnLimpiar.addActionListener(e    -> limpiar());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnDesactivar);
        panel.add(btnLimpiar);
        return panel;
    }

    // ── Acciones de los botones ───────────────────────────────

    private void guardar() {
        try {
            loteService.crear(txtCodigo.getText(), txtDescripcion.getText());
            mostrarExito("Lote guardado correctamente.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) { mostrarError("Seleccione un lote de la tabla."); return; }
        try {
            Lote lote = loteService.buscarPorId(idSeleccionado);
            loteService.actualizar(lote, txtCodigo.getText(), txtDescripcion.getText());
            mostrarExito("Lote actualizado correctamente.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void desactivar() {
        if (idSeleccionado == null) { mostrarError("Seleccione un lote de la tabla."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Desactivar este lote? No aparecerá en nuevos registros.",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            loteService.desactivar(idSeleccionado);
            mostrarExito("Lote desactivado.");
            limpiar();
            refrescarTabla();
        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void limpiar() {
        txtCodigo.setText("");
        txtDescripcion.setText("");
        idSeleccionado = null;
        tabla.clearSelection();
    }

    // ── Tabla ─────────────────────────────────────────────────

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        List<Lote> lotes = loteService.listarTodos();
        for (Lote l : lotes) {
            modeloTabla.addRow(new Object[]{
                l.getId(),
                l.getCodigo(),
                l.getDescripcion() != null ? l.getDescripcion() : "",
                l.getActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        idSeleccionado = (Long) modeloTabla.getValueAt(fila, 0);
        txtCodigo.setText((String) modeloTabla.getValueAt(fila, 1));
        txtDescripcion.setText((String) modeloTabla.getValueAt(fila, 2));
    }

    // ── Métodos públicos para acceso desde otros paneles ──────

    /** Actualiza el panel si se llama desde otra pestaña (ej: al crear un lote). */
    public void refrescar() {
        refrescarTabla();
    }

    // ── Mensajes ──────────────────────────────────────────────

    private void mostrarExito(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error de validación", JOptionPane.WARNING_MESSAGE);
    }
    private void mostrarErrorInterno(Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Error interno: " + ex.getMessage(),
            "Error del sistema", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
