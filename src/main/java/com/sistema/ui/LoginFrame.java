package com.sistema.ui;

import com.sistema.model.Usuario;
import com.sistema.service.IUsuarioService;
import com.sistema.util.UIEstilo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana modal de inicio de sesión.
 *
 * Flujo:
 *   1. Se muestra antes que MainFrame (invocada desde main()).
 *   2. Si el usuario la cierra sin autenticar, la app termina (System.exit).
 *   3. Tras 3 intentos fallidos se bloquea 30 segundos.
 *   4. Si la autenticación es exitosa, expone getUsuarioAutenticado()
 *      para que main() pueda abrir MainFrame con el nombre del usuario.
 */
public class LoginFrame extends JDialog {

    private final IUsuarioService usuarioService;

    private final JTextField     txtUsername = new JTextField(20);
    private final JPasswordField txtPassword = new JPasswordField(20);
    private final JLabel         lblError    = new JLabel(" ");
    private final JButton        btnIngresar = new JButton("Ingresar");

    private Usuario usuarioAutenticado = null;
    private int     intentosFallidos   = 0;

    public LoginFrame(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setTitle("Iniciar sesión");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setAlwaysOnTop(true);
        buildUI();
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (usuarioAutenticado == null) System.exit(0);
            }
        });
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBorder(new EmptyBorder(32, 40, 28, 40));
        root.setBackground(UIEstilo.SUPERFICIE);

        // Encabezado
        JLabel lblTitulo = new JLabel("Sistema de Recolectores", SwingConstants.CENTER);
        lblTitulo.setFont(UIEstilo.FUENTE_TITULO);
        lblTitulo.setForeground(UIEstilo.TEXTO_PRIMARIO);
        JLabel lblSub = new JLabel("Ingrese sus credenciales para continuar", SwingConstants.CENTER);
        lblSub.setFont(UIEstilo.FUENTE_SUBTITULO);
        lblSub.setForeground(UIEstilo.TEXTO_SECUNDARIO);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.add(lblTitulo, BorderLayout.NORTH);
        header.add(lblSub,    BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(24, 0, 8, 0));
        form.setBackground(UIEstilo.SUPERFICIE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;

        UIEstilo.aplicarEstiloCampoTexto(txtUsername);
        UIEstilo.aplicarEstiloCampoTexto(txtPassword);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        lblError.setForeground(UIEstilo.ERROR);
        lblError.setFont(UIEstilo.FUENTE_SUBTITULO);
        form.add(lblError, gbc);

        gbc.gridy = 3;
        btnIngresar.setPreferredSize(new Dimension(140, 34));
        UIEstilo.aplicarEstiloBoton(btnIngresar, UIEstilo.PRIMARIO);
        form.add(btnIngresar, gbc);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);

        btnIngresar.addActionListener(e -> intentarLogin());
        txtPassword.addActionListener(e -> intentarLogin());
        txtUsername.addActionListener(e -> txtPassword.requestFocus());
    }

    private void intentarLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Complete usuario y contraseña.");
            return;
        }

        Usuario usuario = usuarioService.autenticar(username, password);

        if (usuario != null) {
            usuarioAutenticado = usuario;
            dispose();
        } else {
            intentosFallidos++;
            txtPassword.setText("");
            if (intentosFallidos >= 3) {
                bloquearTemporalmente();
            } else {
                lblError.setText("Credenciales incorrectas. Intento " + intentosFallidos + " de 3.");
            }
        }
    }

    private void bloquearTemporalmente() {
        btnIngresar.setEnabled(false);
        txtUsername.setEnabled(false);
        txtPassword.setEnabled(false);
        intentosFallidos = 0;

        final int[] seg = {30};
        Timer timer = new Timer(1000, null);
        timer.addActionListener(e -> {
            seg[0]--;
            if (seg[0] <= 0) {
                timer.stop();
                btnIngresar.setEnabled(true);
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                lblError.setText(" ");
                txtUsername.requestFocus();
            } else {
                lblError.setText("Demasiados intentos. Espere " + seg[0] + " segundos.");
            }
        });
        timer.start();
        lblError.setText("Demasiados intentos. Espere 30 segundos.");
    }

    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}