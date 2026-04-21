package com.sistema.ui;

import com.sistema.exception.NegocioException;
import com.sistema.model.Pago;
import com.sistema.service.IPagoService;
import com.sistema.util.Validador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Panel de Cálculo de Pagos / Liquidación.
 *
 * El operador ingresa: fecha inicio, fecha fin y precio por kilo.
 * El sistema calcula el pago de cada recolector y muestra el total general.
 */
public class PagoPanel extends JPanel {

    private final IPagoService pagoService;

    private final JTextField txtInicio = new JTextField(12);
    private final JTextField txtFin    = new JTextField(12);
    private final JTextField txtPrecio = new JTextField(10);
    private final JLabel     lblTotal  = new JLabel("Total general: —");

    private final DefaultTableModel modeloTabla;
    private final JTable            tabla;

    private static final String[] COLUMNAS =
        {"Recolector", "Cédula", "Total Kilos", "Precio x Kilo", "Total Pago"};

    private static final NumberFormat FMT_MONEDA =
        NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final NumberFormat FMT_NUM =
        NumberFormat.getNumberInstance(Locale.getDefault());

    public PagoPanel(IPagoService pagoService) {
        this.pagoService = pagoService;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Fechas por defecto: primer día del mes actual → hoy
        txtInicio.setText(LocalDate.now().withDayOfMonth(1).toString());
        txtFin.setText(LocalDate.now().toString());

        add(crearPanelParametros(), BorderLayout.NORTH);
        add(new JScrollPane(tabla),  BorderLayout.CENTER);
        add(crearPanelTotal(),       BorderLayout.SOUTH);
    }

    private JPanel crearPanelParametros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Parámetros de Liquidación"));

        JButton btnCalcular = new JButton("Calcular Pagos");
        btnCalcular.setBackground(new Color(39, 174, 96));
        btnCalcular.setForeground(Color.WHITE);
        btnCalcular.setFont(btnCalcular.getFont().deriveFont(Font.BOLD));
        btnCalcular.addActionListener(e -> calcular());

        panel.add(new JLabel("Desde* (AAAA-MM-DD):"));  panel.add(txtInicio);
        panel.add(new JLabel("Hasta* (AAAA-MM-DD):"));  panel.add(txtFin);
        panel.add(new JLabel("Precio por Kilo* ($):"));  panel.add(txtPrecio);
        panel.add(btnCalcular);
        return panel;
    }

    private JPanel crearPanelTotal() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(new Color(30, 100, 200));
        panel.add(lblTotal);
        return panel;
    }

    // ── Lógica del panel ──────────────────────────────────────

    private void calcular() {
        try {
            LocalDate inicio = Validador.parsearFecha(txtInicio.getText(), "fecha inicio");
            LocalDate fin    = Validador.parsearFecha(txtFin.getText(),    "fecha fin");
            double precio    = Validador.parsearDouble(txtPrecio.getText(), "precio por kilo");
            Validador.rangoFechas(inicio, fin);

            List<Pago> pagos = pagoService.calcularPagos(inicio, fin, precio);
            mostrarPagos(pagos, precio);

        } catch (NegocioException ex) {
            mostrarError(ex.getMessage());
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
