package com.sistema.dao.impl;

import com.sistema.dao.ILoteDAO;
import com.sistema.model.Lote;
import com.sistema.util.ConexionBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Implementación concreta del DAO de Lotes.
 *
 * Hereda el CRUD completo de GenericDAOImpl y agrega
 * únicamente la consulta específica de Lote: buscarPorCodigo().
 *
 * Todas las consultas usan JPQL (no SQL nativo) → Hibernate
 * genera PreparedStatements automáticamente → cero riesgo de SQL Injection.
 */
public class LoteDAOImpl extends GenericDAOImpl<Lote, Long> implements ILoteDAO {

    public LoteDAOImpl() {
        super(Lote.class);
    }

    @Override
    public Lote buscarPorCodigo(String codigo) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            TypedQuery<Lote> query = em.createQuery(
                "SELECT l FROM Lote l WHERE l.codigo = :codigo", Lote.class
            );
            query.setParameter("codigo", codigo);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;   // No existe el código → retornamos null
        } finally {
            em.close();
        }
    }

    /**
     * Sobreescribimos listarTodos() para ordenar por código.
     * Principio OCP: extendemos el comportamiento sin tocar GenericDAOImpl.
     */
    @Override
    public List<Lote> listarTodos() {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.createQuery(
                "SELECT l FROM Lote l ORDER BY l.codigo ASC", Lote.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
