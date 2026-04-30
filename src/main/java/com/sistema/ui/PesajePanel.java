package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Pesaje;
import com.sistema.model.Recolector;
import com.sistema.model.Usuario;
import com.sistema.service.ILoteService;
import com.sistema.service.IPesajeService;
import com.sistema.service.IRecolectorService;

import com.sistema.util.UIEstilo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PesajePanel extends JPanel {

    private final IPesajeService     pesajeService;
    private final IRecolectorService recolectorService;
    private final ILoteService       loteService;
    private final Usuario            usuarioActual;

    private final JComboBox<Recolector> cmbRecolector = new JComboBox<>();
    private final JTextField            txtFecha      = new JTextField(12);
    private final JTextField            txtKilos      = new JTextField(8);
    // ✅ ELIMINADO: txtPrecioPorKilo — el precio se maneja en Pagos
    // ✅ ELIMINADO: cmbLote — el lote viene del recolector

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;
    private Long idSeleccionado = null;

    private static final String[] COLUMNAS = {"ID", "Recolector", "Lote", "Fecha", "Kilos", "Registrado por"};

    public PesajePanel(IPesajeService pesajeService,
                       IRecolectorService recolectorService,
                       ILoteService loteService,
                       Usuario usuarioActual) {
        this.pesajeService     = pesajeService;
        this.recolectorService = recolectorService;
        this.loteService       = loteService;
        this.usuarioActual     = usuarioActual;

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

        UIEstilo.aplicarEstiloComboBox(cmbRecolector);
        UIEstilo.aplicarEstiloCampoTexto(txtFecha);
        UIEstilo.aplicarEstiloCampoTexto(txtKilos);

        txtFecha.setText(LocalDate.now().toString());

        JScrollPane scroll = new JScrollPane(tabla);
        UIEstilo.aplicarEstiloScrollPane(scroll);

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(scroll,                 BorderLayout.CENTER);
        add(crearPanelBotones(),    BorderLayout.SOUTH);

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
        panel.setBorder(UIEstilo.crearBordeTitulado("Registro de Pesaje"));
        panel.setBackground(UIEstilo.FONDO);
        panel.add(new JLabel("Recolector*:"));         panel.add(cmbRecolector);
        // ✅ ELIMINADO: Lote — se obtiene automáticamente del recolector
        panel.add(new JLabel("Fecha* (AAAA-MM-DD):")); panel.add(txtFecha);
        panel.add(new JLabel("Kilos*:"));              panel.add(txtKilos);
        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        panel.setBackground(UIEstilo.FONDO);
        JButton btnRegistrar = new JButton("Registrar Pesaje");
        JButton btnEliminar  = new JButton("Eliminar");
        JButton btnLimpiar   = new JButton("Limpiar");

        UIEstilo.aplicarEstiloBoton(btnRegistrar, UIEstilo.PRIMARIO);
        UIEstilo.aplicarEstiloBoton(btnEliminar,  UIEstilo.PELIGRO);
        UIEstilo.aplicarEstiloBoton(btnLimpiar,   UIEstilo.NEUTRO);

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
            Long recolId = (r != null) ? r.getId() : null;

            // ✅ CORREGIDO: sin loteId ni precioPorKilo
            pesajeService.registrar(recolId, txtFecha.getText(), txtKilos.getText(), usuarioActual);

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
        // ✅ ELIMINADO: cmbLote.setSelectedIndex(0)
        idSeleccionado = null;
        tabla.clearSelection();
    }

    // ── Tabla y combos ────────────────────────────────────────

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        for (Pesaje p : pesajeService.listarTodos()) {
            String registradoPor = (p.getRegistradoPor() != null)
                ? p.getRegistradoPor().getNombre() : "—";
            // ✅ El lote se obtiene navegando por el recolector
            String lote = (p.getLote() != null) ? p.getLote().getCodigo() : "—";
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getRecolector().getNombre(),
                lote,
                p.getFecha().toString(),
                String.format("%.2f", p.getKilos()),
                registradoPor
            });
        }
    }

    private void cargarCombos() {
        cmbRecolector.removeAllItems();
        cmbRecolector.addItem(null);
        for (Recolector r : recolectorService.listarActivos()) cmbRecolector.addItem(r);
        // ✅ ELIMINADO: carga de cmbLote
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