package com.sistema.dao.impl;

import com.sistema.dao.IGenericDAO;
import com.sistema.util.ConexionBD;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * Implementación abstracta del DAO genérico.
 *
 * ── POLIMORFISMO ──────────────────────────────────────────────
 * Esta clase abstracta implementa las 5 operaciones CRUD una sola vez.
 * Las subclases concretas (LoteDAOImpl, RecolectorDAOImpl, PesajeDAOImpl)
 * heredan ese comportamiento y SÓLO agregan sus métodos específicos.
 *
 * Sin este patrón, cada DAO repetiría el mismo código de guardar(),
 * buscarPorId(), listarTodos(), actualizar() y eliminar().
 *
 * Principio SOLID:
 *   - SRP: cada método hace exactamente una operación de persistencia.
 *   - DRY: código CRUD escrito una sola vez, reutilizado en 3 DAOs.
 *
 * @param <T>  Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public abstract class GenericDAOImpl<T, ID> implements IGenericDAO<T, ID> {

    /** Clase del tipo T, necesaria para em.find() y las queries JPQL. */
    private final Class<T> tipo;

    protected GenericDAOImpl(Class<T> tipo) {
        this.tipo = tipo;
    }

    // ── CRUD compartido por todos los DAOs ────────────────────

    @Override
    public void guardar(T entidad) {
        EntityManager em = ConexionBD.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidad);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error al guardar " + tipo.getSimpleName() + ": " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public T buscarPorId(ID id) {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            return em.find(tipo, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<T> listarTodos() {
        EntityManager em = ConexionBD.getEntityManager();
        try {
            String jpql = "SELECT e FROM " + tipo.getSimpleName() + " e";
            return em.createQuery(jpql, tipo).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void actualizar(T entidad) {
        EntityManager em = ConexionBD.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(entidad);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error al actualizar " + tipo.getSimpleName() + ": " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public void eliminar(ID id) {
        EntityManager em = ConexionBD.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entidad = em.find(tipo, id);
            if (entidad != null) {
                em.remove(entidad);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error al eliminar " + tipo.getSimpleName() + ": " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ── Acceso protegido para subclases ───────────────────────

    /** Retorna la clase del tipo T (útil para queries en subclases). */
    protected Class<T> getTipo() {
        return tipo;
    }
}
