package com.sistema.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class UIEstilo {

    public static final Color PRIMARIO         = new Color(79, 70, 229);
    public static final Color SECUNDARIO       = new Color(34, 197, 94);
    public static final Color PELIGRO          = new Color(239, 68, 68);
    public static final Color ADVERTENCIA      = new Color(245, 158, 11);
    public static final Color NEUTRO           = new Color(156, 163, 175);
    public static final Color FONDO            = new Color(249, 250, 251);
    public static final Color SUPERFICIE       = Color.WHITE;
    public static final Color TEXTO_PRIMARIO   = new Color(17, 24, 39);
    public static final Color TEXTO_SECUNDARIO = new Color(107, 114, 128);
    public static final Color BORDE            = new Color(229, 231, 235);
    public static final Color ENCABEZADO       = new Color(30, 41, 59);
    public static final Color ENCABEZADO_TEXTO = new Color(226, 232, 240);
    public static final Color FILA_ALTERNA     = new Color(243, 244, 246);
    public static final Color SELECCION        = new Color(238, 242, 255);
    public static final Color ERROR            = new Color(239, 68, 68);

    public static final Font FUENTE_CUERPO     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FUENTE_TITULO     = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FUENTE_SUBTITULO  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_ENCABEZADO = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FUENTE_BOTON      = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FUENTE_TABLA_CAB  = new Font("Segoe UI", Font.BOLD,  12);

    private UIEstilo() {}

    public static void aplicarEstiloBoton(JButton btn, Color fondo) {
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFont(FUENTE_BOTON);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void aplicarEstiloTabla(JTable tabla) {
        tabla.setFont(FUENTE_CUERPO);
        tabla.setRowHeight(28);
        tabla.setGridColor(BORDE);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TEXTO_PRIMARIO);
        tabla.setBackground(SUPERFICIE);
        tabla.setForeground(TEXTO_PRIMARIO);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        header.setReorderingAllowed(false);
        // Nimbus ignora setBackground/setForeground en JTableHeader;
        // se necesita un renderer propio para forzar los colores.
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBackground(ENCABEZADO);
                setForeground(ENCABEZADO_TEXTO);
                setFont(FUENTE_TABLA_CAB);
                setOpaque(true);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, PRIMARIO),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
                ));
                return this;
            }
        });

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    setBackground(SELECCION);
                    setForeground(TEXTO_PRIMARIO);
                } else {
                    setBackground(row % 2 == 0 ? SUPERFICIE : FILA_ALTERNA);
                    setForeground(TEXTO_PRIMARIO);
                }
                setFont(FUENTE_CUERPO);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    public static void aplicarEstiloCampoTexto(JTextField campo) {
        campo.setFont(FUENTE_CUERPO);
        campo.setForeground(TEXTO_PRIMARIO);
        campo.setBackground(SUPERFICIE);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDE, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
    }

    public static void aplicarEstiloComboBox(JComboBox<?> combo) {
        combo.setFont(FUENTE_CUERPO);
        combo.setBackground(SUPERFICIE);
        combo.setForeground(TEXTO_PRIMARIO);
    }

    public static Border crearBordeTitulado(String titulo) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDE, 1),
            titulo
        );
        border.setTitleFont(FUENTE_NEGRITA);
        border.setTitleColor(TEXTO_SECUNDARIO);
        return border;
    }

    public static void aplicarEstiloScrollPane(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1));
        scroll.getViewport().setBackground(SUPERFICIE);
    }
}
