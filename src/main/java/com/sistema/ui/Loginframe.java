package com.sistema.service.impl;

import com.sistema.dao.IUsuarioDAO;
import com.sistema.model.Usuario;
import com.sistema.service.IUsuarioService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioDAO usuarioDAO;

    public UsuarioServiceImpl(IUsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public Usuario autenticar(String username, String password) {
        if (username == null || username.isBlank()) return null;
        if (password == null || password.isBlank()) return null;

        String  hash    = hashPassword(password);
        Usuario usuario = usuarioDAO.buscarPorUsername(username.trim());

        if (usuario == null) return null;
        if (!usuario.getPassword().equals(hash)) return null;

        return usuario;
    }

    @Override
    public String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}