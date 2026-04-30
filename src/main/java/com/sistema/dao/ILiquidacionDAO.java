package com.sistema.dao;

import com.sistema.model.Liquidacion;
import java.util.List;

public interface ILiquidacionDAO {
    void guardar(Liquidacion liquidacion);
    List<Liquidacion> listarTodas();
}