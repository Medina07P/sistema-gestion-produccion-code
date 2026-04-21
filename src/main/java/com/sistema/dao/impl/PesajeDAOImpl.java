package com.sistema.dao.impl;

import com.sistema.dao.IPesajeDAO;
import com.sistema.model.Pesaje;
import com.sistema.util.ConexionBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementación concreta del DAO de Pesajes.
 * Contiene las consultas más importantes del sistema: reportes y pagos.
 *
 * Todas las consultas usan parámetros nombrados (:inicio, :fin)
 * → Hibernate genera PreparedStatements → SQL Injection imposible.
 */
public class PesajeDAOImpl extends GenericDAOImpl<Pesaje, Long> implements IPesajeDAO {

    public PesajeDAOImpl() {
        super(Pesaje.class);
    }

    /**
     * Pesajes en un rango de fechas (inclusivo en ambos extremos).
     * JOIN FETCH para cargar recolector y lote en una sola consulta (evita N+1).
     */
    @Override
    public List<Pesaje> buscarPorPeriodo(LocalDate inicio, LocalDate fin) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            TypedQuery<Pesaje> query = em.createQuery(
                "SELECT p FROM Pesaje p " +
                "JOIN FETCH p.recolector " +
                "JOIN FETCH p.lote " +
                "WHERE p.fecha BETWEEN :inicio AND :fin " +
                "ORDER BY p.fecha ASC, p.recolector.nombre ASC",
                Pesaje.class
            );
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Suma de kilos agrupada por recolector en un período.
     * Retorna: [nombreRecolector (String), cedula (String), totalKilos (Double)]
     * Usado por: ReportePorRecolector y PagoService.
     */
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

    /**
     * Suma de kilos agrupada por lote en un período.
     * Retorna: [codigoLote (String), descripcionLote (String), totalKilos (Double)]
     * Usado por: ReportePorLote.
     */
    @Override
    public List<Object[]> totalKilosPorLote(LocalDate inicio, LocalDate fin) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                "SELECT l.codigo, l.descripcion, SUM(p.kilos) " +
                "FROM Pesaje p JOIN p.lote l " +
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
