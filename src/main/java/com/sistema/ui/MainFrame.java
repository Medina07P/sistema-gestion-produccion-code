package com.sistema.ui;

import com.sistema.dao.ILiquidacionDAO;
import com.sistema.dao.ILoteDAO;
import com.sistema.dao.IPesajeDAO;
import com.sistema.dao.IRecolectorDAO;
import com.sistema.dao.IUsuarioDAO;
import com.sistema.dao.impl.LiquidacionDAOImpl;
import com.sistema.dao.impl.LoteDAOImpl;
import com.sistema.dao.impl.PesajeDAOImpl;
import com.sistema.dao.impl.RecolectorDAOImpl;
import com.sistema.dao.impl.UsuarioDAOImpl;
import com.sistema.model.Usuario;
import com.sistema.report.IReporteGenerador;
import com.sistema.report.ReporteLiquidaciones;
import com.sistema.report.ReportePorDia;
import com.sistema.report.ReportePorLote;
import com.sistema.report.ReportePorRecolector;
import com.sistema.service.*;
import com.sistema.service.IUsuarioService;
import com.sistema.service.impl.UsuarioServiceImpl;
import com.sistema.util.ConexionBD;
import com.sistema.util.UIEstilo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends JFrame {

    public MainFrame(String nombreUsuario, Usuario usuario) {
        // ── 1. Wiring: instanciar DAOs ────────────────────────
        ILoteDAO          loteDAO          = new LoteDAOImpl();
        IRecolectorDAO    recolectorDAO    = new RecolectorDAOImpl();
        IPesajeDAO        pesajeDAO        = new PesajeDAOImpl();
        ILiquidacionDAO   liquidacionDAO   = new LiquidacionDAOImpl(); // ✅ NUEVO

        // ── 2. Wiring: servicios ──────────────────────────────
        ILoteService       loteService       = new LoteService(loteDAO);
        IRecolectorService recolectorService = new RecolectorService(recolectorDAO, loteDAO);
        IPesajeService     pesajeService     = new PesajeService(pesajeDAO, recolectorDAO);
        IPagoService       pagoService       = new PagoService(pesajeDAO, liquidacionDAO); // ✅ CORREGIDO

        // ── 3. Reportes ───────────────────────────────────────
        List<IReporteGenerador> reportes = Arrays.asList(
        new ReportePorRecolector(pesajeDAO),
        new ReportePorLote(pesajeDAO),
        new ReportePorDia(pesajeDAO),
        new ReporteLiquidaciones(liquidacionDAO)  // ✅ NUEVO
    );

        // ── 4. Paneles ────────────────────────────────────────
        LotePanel       lotePanel       = new LotePanel(loteService);
        RecolectorPanel recolectorPanel = new RecolectorPanel(recolectorService, loteService);
        PesajePanel     pesajePanel     = new PesajePanel(pesajeService, recolectorService, loteService, usuario);
        PagoPanel       pagoPanel       = new PagoPanel(pagoService, usuario);   // ✅ CORREGIDO
        ReportePanel    reportePanel    = new ReportePanel(reportes);

        // ── 5. Tabs ───────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        // Font.DIALOG es compuesta: tiene fallback a Segoe UI Emoji, lo que permite emojis en tabs
        tabs.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
        tabs.setBackground(UIEstilo.FONDO);
        tabs.setForeground(UIEstilo.TEXTO_PRIMARIO);
        tabs.addTab("🌿  Lotes",        lotePanel);
        tabs.addTab("👤  Recolectores", recolectorPanel);
        tabs.addTab("⚖️  Pesajes",       pesajePanel);
        tabs.addTab("💰  Pagos",         pagoPanel);
        tabs.addTab("📊  Reportes",      reportePanel);

        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) recolectorPanel.refrescar();
            else if (idx == 2) pesajePanel.refrescar();
        });

        // ── 6. Configuración del JFrame ───────────────────────
        setTitle("Sistema de Gestión de Recolectores  —  " + nombreUsuario);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        add(crearHeader(nombreUsuario), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIEstilo.FONDO);

        // ── 7. Cierre limpio ──────────────────────────────────
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int c = JOptionPane.showConfirmDialog(MainFrame.this,
                    "¿Desea salir de la aplicación?",
                    "Confirmar salida", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) {
                    ConexionBD.cerrar();
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    private JPanel crearHeader(String nombreUsuario) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIEstilo.ENCABEZADO);
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel titulo = new JLabel("Sistema de Gestión de Recolectores");
        titulo.setForeground(UIEstilo.ENCABEZADO_TEXTO);
        titulo.setFont(UIEstilo.FUENTE_ENCABEZADO);

        JLabel subtitulo = new JLabel("Lotes · Pesajes · Liquidación · Reportes");
        subtitulo.setForeground(UIEstilo.ENCABEZADO_TEXTO);
        subtitulo.setFont(UIEstilo.FUENTE_SUBTITULO);

        // Font.DIALOG es una fuente lógica compuesta; Java le asigna Segoe UI Emoji
        // como fallback en Windows, lo que permite renderizar el emoji 👤 correctamente.
        // Las fuentes físicas como "Segoe UI" no tienen ese fallback automático.
        JLabel lblUsuario = new JLabel("👤 " + nombreUsuario);
        lblUsuario.setForeground(UIEstilo.ENCABEZADO_TEXTO);
        lblUsuario.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

        JPanel derecha = new JPanel(new GridLayout(2, 1));
        derecha.setOpaque(false);
        derecha.add(subtitulo);
        derecha.add(lblUsuario);

        header.add(titulo,  BorderLayout.CENTER);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        UIManager.put("Label.font",       UIEstilo.FUENTE_CUERPO);
        UIManager.put("Button.font",      UIEstilo.FUENTE_BOTON);
        UIManager.put("TextField.font",   UIEstilo.FUENTE_CUERPO);
        UIManager.put("ComboBox.font",    UIEstilo.FUENTE_CUERPO);
        UIManager.put("TabbedPane.font",  UIEstilo.FUENTE_NEGRITA);
        UIManager.put("Panel.background", UIEstilo.FONDO);

        SwingUtilities.invokeLater(() -> {
            try {
                IUsuarioDAO     usuarioDAO     = new UsuarioDAOImpl();
                IUsuarioService usuarioService = new UsuarioServiceImpl(usuarioDAO);

                LoginFrame loginFrame = new LoginFrame(usuarioService);
                loginFrame.setVisible(true);

                Usuario usuario = loginFrame.getUsuarioAutenticado();
                if (usuario != null) {
                    new MainFrame(usuario.getNombre(), usuario).setVisible(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Error al iniciar: " + e.getMessage() +
                    "\n\nVerifique que MySQL esté corriendo y la BD 'recolectores_db' exista.",
                    "Error de inicio", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}