package com.sistema.dao.impl;

import com.sistema.dao.IUsuarioDAO;
import com.sistema.model.Usuario;
import com.sistema.util.ConexionBD;

import javax.persistence.*;

public class UsuarioDAOImpl extends GenericDAOImpl<Usuario, Long> implements IUsuarioDAO {

    public UsuarioDAOImpl() {
        super(Usuario.class);
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :username AND u.activo = true",
                    Usuario.class)
                .setParameter("username", username)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}