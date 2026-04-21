package com.sistema.exception;

/**
 * Excepción de dominio del negocio.
 *
 * Se lanza desde la capa de servicio cuando se viola una regla
 * de negocio (datos inválidos, duplicados, estados incorrectos).
 *
 * La capa de UI la captura y muestra el mensaje al usuario
 * sin exponer stacktraces internos.
 *
 * Principio SOLID: SRP — sólo representa errores de negocio.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }

    public NegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
