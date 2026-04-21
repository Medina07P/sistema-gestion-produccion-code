package com.sistema.dao;

import com.sistema.model.Lote;

/**
 * Interfaz específica del DAO de Lotes.
 * Extiende IGenericDAO para heredar las operaciones CRUD
 * y agrega métodos propios de la entidad Lote.
 *
 * Principio OCP: si se necesita una nueva consulta de lotes,
 * se agrega aquí sin modificar IGenericDAO.
 */
public interface ILoteDAO extends IGenericDAO<Lote, Long> {

    /** Busca un lote por su código único (ej: "A1"). */
    Lote buscarPorCodigo(String codigo);
}
