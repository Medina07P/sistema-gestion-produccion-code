package com.sistema.dao.impl;

import com.sistema.dao.IPesajeDAO;
import com.sistema.model.Pesaje;
import com.sistema.util.ConexionBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

public class PesajeDAOImpl extends GenericDAOImpl<Pesaje, Long> implements IPesajeDAO {

    public PesajeDAOImpl() {
        super(Pesaje.class);
    }

    @Override
    public List<Pesaje> buscarPorPeriodo(LocalDate inicio, LocalDate fin) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            TypedQuery<Pesaje> query = em.createQuery(
                "SELECT p FROM Pesaje p " +
                "JOIN FETCH p.recolector r " +
                // ✅ CORREGIDO: el lote se obtiene desde el recolector
                "JOIN FETCH r.lote " +
                "WHERE p.fecha BETWEEN :inicio AND :fin " +
                "ORDER BY p.fecha ASC, r.nombre ASC",
                Pesaje.class
            );
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> totalKilosPorRecolector(LocalDate inicio, LocalDate fin) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r.nombre, r.cedula, SUM(p.kilos) " +
                "FROM Pesaje p JOIN p.recolector r " +
                "WHERE p.fecha BETWEEN :inicio AND :fin " +
                "GROUP BY r.id, r.nombre, r.cedula " +
                "ORDER BY SUM(p.kilos) DESC",
                Object[].class
            )
            .setParameter("inicio", inicio)
            .setParameter("fin", fin)
            .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Object[]> totalKilosPorLote(LocalDate inicio, LocalDate fin) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                // ✅ CORREGIDO: lote se accede via recolector
                "SELECT l.codigo, l.descripcion, SUM(p.kilos) " +
                "FROM Pesaje p JOIN p.recolector r JOIN r.lote l " +
                "WHERE p.fecha BETWEEN :inicio AND :fin " +
                "GROUP BY l.id, l.codigo, l.descripcion " +
                "ORDER BY l.codigo ASC",
                Object[].class
            )
            .setParameter("inicio", inicio)
            .setParameter("fin", fin)
            .getResultList();
        } finally {
            em.close();
        }
    }
}