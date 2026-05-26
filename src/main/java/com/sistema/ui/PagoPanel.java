package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Pago;
import com.sistema.model.Usuario;
import com.sistema.service.IPagoService;
import com.sistema.util.UIEstilo;
import com.sistema.util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PagoPanel extends JPanel {

    private final IPagoService pagoService;
    private final Usuario      usuarioActual;  // ✅ NUEVO

    private final JTextField txtInicio = new JTextField(12);
    private final JTextField txtFin    = new JTextField(12);
    private final JTextField txtPrecio = new JTextField(10);
    private final JLabel     lblTotal  = new JLabel("Total general: —");

    // ✅ NUEVO: estado del último cálculo para poder guardarlo
    private List<Pago> ultimoCalculo = new ArrayList<>();
    private LocalDate  ultimoInicio;
    private LocalDate  ultimoFin;
    private double     ultimoPrecio;

    // ✅ NUEVO: botón guardar, deshabilitado hasta que se calcule
    private final JButton btnGuardar = new JButton("Guardar Liquidación");

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;

    private static final String[] COLUMNAS =
        {"Recolector", "Cédula", "Total Kilos", "Precio x Kilo", "Total Pago"};

    private static final NumberFormat FMT_MONEDA =
        NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    // ✅ NUEVO: constructor recibe usuarioActual
    public PagoPanel(IPagoService pagoService, Usuario usuarioActual) {
        this.pagoService   = pagoService;
        this.usuarioActual = usuarioActual;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIEstilo.FONDO);

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIEstilo.aplicarEstiloTabla(tabla);

        UIEstilo.aplicarEstiloCampoTexto(txtInicio);
        UIEstilo.aplicarEstiloCampoTexto(txtFin);
        UIEstilo.aplicarEstiloCampoTexto(txtPrecio);

        txtInicio.setText(LocalDate.now().withDayOfMonth(1).toString());
        txtFin.setText(LocalDate.now().toString());
        btnGuardar.setEnabled(false);  // solo se activa tras calcular

        JScrollPane scroll = new JScrollPane(tabla);
        UIEstilo.aplicarEstiloScrollPane(scroll);

        add(crearPanelParametros(), BorderLayout.NORTH);
        add(scroll,                 BorderLayout.CENTER);
        add(crearPanelTotal(),      BorderLayout.SOUTH);
    }

    private JPanel crearPanelParametros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(UIEstilo.crearBordeTitulado("Parámetros de Liquidación"));
        panel.setBackground(UIEstilo.FONDO);

        JButton btnCalcular = new JButton("Calcular Pagos");
        UIEstilo.aplicarEstiloBoton(btnCalcular, UIEstilo.SECUNDARIO);
        btnCalcular.addActionListener(e -> calcular());

        UIEstilo.aplicarEstiloBoton(btnGuardar, UIEstilo.PRIMARIO);
        btnGuardar.addActionListener(e -> guardar());

        panel.add(new JLabel("Desde* (AAAA-MM-DD):")); panel.add(txtInicio);
        panel.add(new JLabel("Hasta* (AAAA-MM-DD):")); panel.add(txtFin);
        panel.add(new JLabel("Precio por Kilo* ($):")); panel.add(txtPrecio);
        panel.add(btnCalcular);
        panel.add(btnGuardar);  // ✅ NUEVO
        return panel;
    }

    private JPanel crearPanelTotal() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(UIEstilo.FONDO);
        lblTotal.setFont(UIEstilo.FUENTE_NEGRITA);
        lblTotal.setForeground(UIEstilo.PRIMARIO);
        panel.add(lblTotal);
        return panel;
    }

    // ── Lógica del panel ──────────────────────────────────────

    private void calcular() {
        btnGuardar.setEnabled(false);
        ultimoCalculo = new ArrayList<>();
        try {
            LocalDate inicio = Validador.parsearFecha(txtInicio.getText(), "fecha inicio");
            LocalDate fin    = Validador.parsearFecha(txtFin.getText(),    "fecha fin");
            double precio    = Validador.parsearDouble(txtPrecio.getText(), "precio por kilo");
            Validador.rangoFechas(inicio, fin);

            List<Pago> pagos = pagoService.calcularPagos(inicio, fin, precio);

            ultimoInicio  = inicio;
            ultimoFin     = fin;
            ultimoPrecio  = precio;
            ultimoCalculo = pagos;
            mostrarPagos(pagos, precio);
            btnGuardar.setEnabled(true);

        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    // ✅ NUEVO: guarda la liquidación ya calculada
    private void guardar() {
        int c = JOptionPane.showConfirmDialog(this,
            "¿Guardar esta liquidación en el historial?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            pagoService.guardarLiquidacion(
                ultimoInicio, ultimoFin, ultimoPrecio, ultimoCalculo, usuarioActual
            );
            btnGuardar.setEnabled(false);  // evitar doble guardado
            JOptionPane.showMessageDialog(this,
                "Liquidación guardada correctamente.",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            mostrarErrorInterno(ex);
        }
    }

    private void mostrarPagos(List<Pago> pagos, double precio) {
        modeloTabla.setRowCount(0);
        double sumaTotal = 0;
        for (Pago p : pagos) {
            modeloTabla.addRow(new Object[]{
                p.getRecolector().getNombre(),
                p.getRecolector().getCedula(),
                String.format("%.2f kg", p.getTotalKilos()),
                FMT_MONEDA.format(precio),
                FMT_MONEDA.format(p.getTotalPago())
            });
            sumaTotal += p.getTotalPago();
        }
        lblTotal.setText("Total general a pagar: " + FMT_MONEDA.format(sumaTotal)
            + "  (" + pagos.size() + " recolectores)");
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