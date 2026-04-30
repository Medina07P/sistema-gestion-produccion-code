package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Lote;
import com.sistema.service.ILoteService;

import com.sistema.util.UIEstilo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LotePanel extends JPanel {

    private final ILoteService loteService;

    private final JTextField txtCodigo      = new JTextField(10);
    private final JTextField txtDescripcion = new JTextField(25);

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private Long idSeleccionado = null;

    private static final String[] COLUMNAS = {"ID", "Código", "Descripción", "Estado"};

    public LotePanel(ILoteService loteService) {
        this.loteService = loteService;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIEstilo.FONDO);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        UIEstilo.aplicarEstiloTabla(tabla);

        UIEstilo.aplicarEstiloCampoTexto(txtCodigo);
        UIEstilo.aplicarEstiloCampoTexto(txtDescripcion);

        JScrollPane scroll = new JScrollPane(tabla);
        UIEstilo.aplicarEstiloScrollPane(scroll);

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(scroll,                 BorderLayout.CENTER);
        add(crearPanelBotones(),    BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        refrescarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(UIEstilo.crearBordeTitulado("Datos del Lote"));
        panel.setBackground(UIEstilo.FONDO);
        panel.add(new JLabel("Código*:"));     panel.add(txtCodigo);
        panel.add(new JLabel("Descripción:")); panel.add(txtDescripcion);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panel.setBackground(UIEstilo.FONDO);
        JButton btnGuardar    = new JButton("Guardar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnActivar    = new JButton("Activar");
        JButton btnDesactivar = new JButton("Desactivar");
        JButton btnLimpiar    = new JButton("Limpiar");

        UIEstilo.aplicarEstiloBoton(btnGuardar,    UIEstilo.PRIMARIO);
        UIEstilo.aplicarEstiloBoton(btnActualizar, UIEstilo.PRIMARIO);
        UIEstilo.aplicarEstiloBoton(btnActivar,    UIEstilo.SECUNDARIO);
        UIEstilo.aplicarEstiloBoton(btnDesactivar, UIEstilo.ADVERTENCIA);
        UIEstilo.aplicarEstiloBoton(btnLimpiar,    UIEstilo.NEUTRO);

        btnGuardar.addActionListener(e    -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivar.addActionListener(e    -> activar());
        btnDesactivar.addActionListener(e -> desactivar());
        btnLimpiar.addActionListener(e    -> limpiar());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnActivar);
        panel.add(btnDesactivar);
        panel.add(btnLimpiar);
        return panel;
    }

    // ── Acciones ──────────────────────────────────────────────

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

    private void activar() {                                 // ← NUEVO
        if (idSeleccionado == null) { mostrarError("Seleccione un lote de la tabla."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Activar este lote?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            loteService.activar(idSeleccionado);
            mostrarExito("Lote activado correctamente.");
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
        for (Lote l : loteService.listarTodos()) {
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

    public void refrescar() { refrescarTabla(); }

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