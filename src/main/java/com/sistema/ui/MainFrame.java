package com.sistema.ui;

import com.sistema.dao.ILoteDAO;
import com.sistema.dao.IPesajeDAO;
import com.sistema.dao.IRecolectorDAO;
import com.sistema.dao.IUsuarioDAO;
import com.sistema.dao.impl.LoteDAOImpl;
import com.sistema.dao.impl.PesajeDAOImpl;
import com.sistema.dao.impl.RecolectorDAOImpl;
import com.sistema.dao.impl.UsuarioDAOImpl;
import com.sistema.model.Usuario;
import com.sistema.report.IReporteGenerador;
import com.sistema.report.ReportePorDia;
import com.sistema.report.ReportePorLote;
import com.sistema.report.ReportePorRecolector;
import com.sistema.service.*;
import com.sistema.service.IUsuarioService;
import com.sistema.service.impl.UsuarioServiceImpl;
import com.sistema.util.ConexionBD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

public class MainFrame extends JFrame {

    public MainFrame(String nombreUsuario, Usuario usuario) {
        // ── 1. Wiring: instanciar DAOs (implementaciones concretas) ──
        ILoteDAO       loteDAO       = new LoteDAOImpl();
        IRecolectorDAO recolectorDAO = new RecolectorDAOImpl();
        IPesajeDAO     pesajeDAO     = new PesajeDAOImpl();

        // ── 2. Wiring: servicios dependen de interfaces de DAO (DIP) ──
        ILoteService       loteService       = new LoteService(loteDAO);
        IRecolectorService recolectorService = new RecolectorService(recolectorDAO, loteDAO);
        IPesajeService     pesajeService     = new PesajeService(pesajeDAO, recolectorDAO, loteDAO);
        IPagoService       pagoService       = new PagoService(pesajeDAO);

        // ── 3. Reportes: polimorfismo sobre IReporteGenerador ─────────
        List<IReporteGenerador> reportes = Arrays.asList(
            new ReportePorRecolector(pesajeDAO),
            new ReportePorLote(pesajeDAO),
            new ReportePorDia(pesajeDAO)
        );

        // ── 4. Paneles (capa UI) ──────────────────────────────────────
        LotePanel       lotePanel       = new LotePanel(loteService);
        RecolectorPanel recolectorPanel = new RecolectorPanel(recolectorService, loteService);
        PesajePanel     pesajePanel     = new PesajePanel(pesajeService, recolectorService, loteService, usuario);
        PagoPanel       pagoPanel       = new PagoPanel(pagoService);
        ReportePanel    reportePanel    = new ReportePanel(reportes);

        // ── 5. Composición del JTabbedPane ────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
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

        // ── 6. Configuración del JFrame ───────────────────────────────
        setTitle("Sistema de Gestión de Recolectores  —  " + nombreUsuario);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        add(crearHeader(nombreUsuario), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        // ── 7. Cierre limpio de la BD al salir ────────────────────────
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
        header.setBackground(new Color(27, 79, 114));
        header.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        JLabel titulo = new JLabel("Sistema de Gestión de Recolectores");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));

        JLabel subtitulo = new JLabel("Lotes · Pesajes · Liquidación · Reportes");
        subtitulo.setForeground(new Color(133, 193, 233));
        subtitulo.setFont(subtitulo.getFont().deriveFont(12f));

        JLabel lblUsuario = new JLabel("👤 " + nombreUsuario);
        lblUsuario.setForeground(new Color(133, 193, 233));
        lblUsuario.setFont(lblUsuario.getFont().deriveFont(12f));

        JPanel derecha = new JPanel(new GridLayout(2, 1));
        derecha.setOpaque(false);
        derecha.add(subtitulo);
        derecha.add(lblUsuario);

        header.add(titulo,  BorderLayout.CENTER);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    // ── Punto de entrada de la aplicación ────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Construir dependencias del login
                IUsuarioDAO     usuarioDAO     = new UsuarioDAOImpl();
                IUsuarioService usuarioService = new UsuarioServiceImpl(usuarioDAO);

                // 2. Mostrar ventana de login (modal — bloquea hasta cerrarse)
                LoginFrame loginFrame = new LoginFrame(usuarioService);
                loginFrame.setVisible(true);

                // 3. Si autenticó correctamente, abrir la aplicación principal
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