package com.sistema.dao.impl;

import com.sistema.dao.ILiquidacionDAO;
import com.sistema.model.Liquidacion;
import com.sistema.util.ConexionBD;
import jakarta.persistence.EntityManager;
import java.util.List;

public class LiquidacionDAOImpl implements ILiquidacionDAO {

    @Override
    public void guardar(Liquidacion liquidacion) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(liquidacion);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    @Override
public List<Liquidacion> listarTodas() {
    EntityManager em = ConexionBD.getEntityManager();
    try {
        return em.createQuery(
            "SELECT l FROM Liquidacion l " +
            "JOIN FETCH l.usuario " +
            "JOIN FETCH l.detalles d " +
            "JOIN FETCH d.recolector " +
            "ORDER BY l.fechaRegistro DESC",
            Liquidacion.class
        ).getResultList();
    } finally {
        em.close();
    }
}
}