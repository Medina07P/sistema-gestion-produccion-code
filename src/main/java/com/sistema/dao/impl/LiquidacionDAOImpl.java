package com.sistema.dao.impl;

import com.sistema.dao.ILiquidacionDAO;
import com.sistema.model.Liquidacion;
import com.sistema.model.LiquidacionDetalle;
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
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(Long id) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            em.getTransaction().begin();
            Liquidacion liq = em.find(Liquidacion.class, id);
            if (liq != null) em.remove(liq);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminarDetalle(Long detalleId) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            em.getTransaction().begin();
            LiquidacionDetalle detalle = em.find(LiquidacionDetalle.class, detalleId);
            if (detalle != null) {
                Long cabeceraId = detalle.getLiquidacion().getId();
                em.remove(detalle);
                em.flush();

                Long restantes = em.createQuery(
                    "SELECT COUNT(d) FROM LiquidacionDetalle d WHERE d.liquidacion.id = :id",
                    Long.class
                ).setParameter("id", cabeceraId).getSingleResult();

                if (restantes == 0) {
                    Liquidacion cabecera = em.find(Liquidacion.class, cabeceraId);
                    if (cabecera != null) em.remove(cabecera);
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
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