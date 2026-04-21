package com.sistema.dao;

import java.util.List;

/**
 * Interfaz genérica para operaciones CRUD comunes.
 *
 * ── POLIMORFISMO ──────────────────────────────────────────────
 * Todos los DAOs del sistema implementan (directa o indirectamente)
 * esta interfaz con su tipo específico:
 *
 *   IGenericDAO<Lote,       Long>  → ILoteDAO       → LoteDAOImpl
 *   IGenericDAO<Recolector, Long>  → IRecolectorDAO → RecolectorDAOImpl
 *   IGenericDAO<Pesaje,     Long>  → IPesajeDAO     → PesajeDAOImpl
 *
 * Beneficio: cualquier componente que reciba un IGenericDAO<T,ID>
 * puede operar sobre él sin conocer la implementación concreta.
 *
 * Principio SOLID: DIP — los servicios dependen de esta abstracción,
 * no de las implementaciones concretas.
 *
 * @param <T>  Tipo de la entidad (Lote, Recolector, Pesaje…)
 * @param <ID> Tipo de la clave primaria (generalmente Long)
 */
public interface IGenericDAO<T, ID> {

    /** Persiste una nueva entidad en la base de datos. */
    void guardar(T entidad);

    /** Busca una entidad por su clave primaria. Retorna null si no existe. */
    T buscarPorId(ID id);

    /** Retorna todas las entidades del tipo T. */
    List<T> listarTodos();

    /** Actualiza una entidad existente (hace merge en JPA). */
    void actualizar(T entidad);

    /** Elimina la entidad con el ID dado. No hace nada si no existe. */
    void eliminar(ID id);
}
