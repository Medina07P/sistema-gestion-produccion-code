package com.sistema.dao;

import com.sistema.model.Recolector;
import java.util.List;

/**
 * Interfaz específica del DAO de Recolectores.
 * Agrega consultas propias de la entidad Recolector.
 */
public interface IRecolectorDAO extends IGenericDAO<Recolector, Long> {

    /** Busca un recolector por cédula. Retorna null si no existe. */
    Recolector buscarPorCedula(String cedula);

    /** Retorna sólo los recolectores con estado activo = true. */
    List<Recolector> listarActivos();
}
