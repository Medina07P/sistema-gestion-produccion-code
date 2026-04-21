package com.sistema.service;

import com.sistema.model.Usuario;

public interface IUsuarioService {
    Usuario autenticar(String username, String password);
    String  hashPassword(String plainPassword);
}