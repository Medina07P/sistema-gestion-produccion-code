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
import com.sistema.service.impl.UsuarioServiceImpl;
import com.sistema.util.ConexionBD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Ventana principal de la aplicación.
 *
 * ── RESPONSABILIDADES ─────────────────────────────────────────
 *  1. Inicializar la aplicación con Look & Feel del sistema.
 *  2. Realizar el "cableado" (Dependency Injection manual):
 *       DAOs → Servicios → Paneles
 *     Sin frameworks: se ve claramente quién depende de quién.
 *  3. Componer la UI: JTabbedPane con un panel por módulo.
 *  4. Cerrar la conexión a la BD al salir de la aplicación.
 *
 * ── INYECCIÓN DE DEPENDENCIAS (Manual) ────────────────────────
 * Este es el único lugar donde se instancian implementaciones
 * concretas (LoteDAOImpl, etc.). El resto del sistema trabaja
 * sólo con interfaces. Principio DIP en toda su expresión.
 *
 * ── POLIMORFISMO (Reportes) ───────────────────────────────────
 * Los tres reportes se registran como List<IReporteGenerador>.
 * ReportePanel los trata a todos igual vía la interfaz común.
 */
public class MainFrame extends JFrame {

    public MainFrame(String nombreUsuario) {
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
        PesajePanel     pesajePanel     = new PesajePanel(pesajeService, recolectorService, loteService);
        PagoPanel       pagoPanel       = new PagoPanel(pagoService);
        ReportePanel    reportePanel    = new ReportePanel(reportes);

        // ── 5. Composición del JTabbedPane ────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("🌿  Lotes",        lotePanel);
        tabs.addTab("👤  Recolectores", recolectorPanel);
        tabs.addTab("⚖️  Pesajes",       pesajePanel);
        tabs.addTab("💰  Pagos",         pagoPanel);
        tabs.addTab("📊  Reportes",      reportePanel);

        // Al cambiar de pestaña → refrescar datos del panel destino
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) {
                recolectorPanel.refrescar();
            } else if (idx == 2) {
                pesajePanel.refrescar();
            }
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

        // Muestra el nombre del usuario autenticado en la esquina derecha
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
                IUsuarioDAO         usuarioDAO     = new UsuarioDAOImpl();
                UsuarioServiceImpl  usuarioService = new UsuarioServiceImpl(usuarioDAO);

                // 2. Mostrar ventana de login (modal — bloquea hasta cerrarse)
                LoginFrame loginFrame = new LoginFrame(usuarioService);
                loginFrame.setVisible(true);

                // 3. Si autenticó correctamente, abrir la aplicación principal
                Usuario usuario = loginFrame.getUsuarioAutenticado();
                if (usuario != null) {
                    new MainFrame(usuario.getNombre()).setVisible(true);
                }
                // Si usuario == null, LoginFrame ya llamó System.exit(0)

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