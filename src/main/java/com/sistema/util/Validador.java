package com.sistema.util;

import com.sistema.exception.NegocioException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Clase utilitaria con métodos de validación reutilizables.
 *
 * Principio SOLID:
 *   - SRP: Solo valida datos, no hace nada más.
 *   - DRY: Centraliza validaciones usadas en múltiples servicios.
 *
 * Todos los métodos son estáticos porque no necesitan estado.
 * Lanzan NegocioException con mensajes claros para el usuario.
 */
public class Validador {

    private Validador() {}  // No instanciable

    // ── Cadenas ──────────────────────────────────────────────────

    public static void requerido(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new NegocioException("El campo '" + nombreCampo + "' es obligatorio.");
        }
    }

    public static void longitudMaxima(String valor, int max, String nombreCampo) {
        if (valor != null && valor.trim().length() > max) {
            throw new NegocioException(
                "El campo '" + nombreCampo + "' no puede superar " + max + " caracteres."
            );
        }
    }

    // ── Números ──────────────────────────────────────────────────

    public static void positivo(double valor, String nombreCampo) {
        if (valor <= 0) {
            throw new NegocioException(
                "El campo '" + nombreCampo + "' debe ser un número mayor a 0."
            );
        }
    }

    public static double parsearDouble(String texto, String nombreCampo) {
        requerido(texto, nombreCampo);
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new NegocioException(
                "El campo '" + nombreCampo + "' debe ser un número válido (ej: 50.5)."
            );
        }
    }

    // ── Fechas ───────────────────────────────────────────────────

    public static LocalDate parsearFecha(String texto, String nombreCampo) {
        requerido(texto, nombreCampo);
        try {
            return LocalDate.parse(texto.trim());  // Formato ISO: YYYY-MM-DD
        } catch (DateTimeParseException e) {
            throw new NegocioException(
                "El campo '" + nombreCampo + "' debe tener formato AAAA-MM-DD (ej: 2025-04-28)."
            );
        }
    }

    public static void rangoFechas(LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new NegocioException(
                "La fecha de inicio no puede ser posterior a la fecha de fin."
            );
        }
    }

    // ── Cédula ───────────────────────────────────────────────────

    public static void cedula(String cedula) {
        requerido(cedula, "cédula");
        if (!cedula.trim().matches("[0-9]{5,15}")) {
            throw new NegocioException(
                "La cédula debe contener entre 5 y 15 dígitos numéricos."
            );
        }
    }
}
