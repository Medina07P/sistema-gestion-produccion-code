package com.sistema.dao;

import com.sistema.model.Usuario;

public interface IUsuarioDAO extends IGenericDAO<Usuario, Long> {
    Usuario buscarPorUsername(String username);
}