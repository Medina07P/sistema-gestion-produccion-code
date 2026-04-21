package com.sistema.dao.impl;

import com.sistema.dao.IRecolectorDAO;
import com.sistema.model.Recolector;
import com.sistema.util.ConexionBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Implementación concreta del DAO de Recolectores.
 * Hereda CRUD de GenericDAOImpl, agrega consultas específicas.
 */
public class RecolectorDAOImpl extends GenericDAOImpl<Recolector, Long> implements IRecolectorDAO {

    public RecolectorDAOImpl() {
        super(Recolector.class);
    }

    @Override
    public Recolector buscarPorCedula(String cedula) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            TypedQuery<Recolector> query = em.createQuery(
                "SELECT r FROM Recolector r WHERE r.cedula = :cedula", Recolector.class
            );
            query.setParameter("cedula", cedula);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Recolector> listarActivos() {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Recolector r WHERE r.activo = true ORDER BY r.nombre ASC",
                Recolector.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Recolector> listarTodos() {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Recolector r ORDER BY r.nombre ASC", Recolector.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
