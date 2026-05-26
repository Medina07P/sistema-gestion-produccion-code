package com.sistema.dao;

import com.sistema.model.Liquidacion;
import java.util.List;

public interface ILiquidacionDAO {
    void guardar(Liquidacion liquidacion);
    List<Liquidacion> listarTodas();
    void eliminar(Long id);
    void eliminarDetalle(Long detalleId);
}