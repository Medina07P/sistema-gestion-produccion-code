package com.sistema.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Singleton que administra el EntityManagerFactory de JPA/Hibernate.
 *
 * ¿Por qué Singleton?
 *   - Crear un EntityManagerFactory es costoso (conecta a la BD, carga mappings).
 *   - Solo debe existir UNA instancia en toda la aplicación.
 *   - Todos los DAOs llaman a ConexionBD.getEntityManager() para obtener
 *     un EntityManager nuevo (ligero, uno por operación).
 *
 * El nombre "sistemaRecolectoresPU" debe coincidir exactamente con
 * el atributo 'name' del persistence-unit en persistence.xml.
 */
public class ConexionBD {

    private static final String PERSISTENCE_UNIT = "sistemaRecolectoresPU";
    private static EntityManagerFactory factory;

    // Constructor privado: nadie puede instanciar esta clase
    private ConexionBD() {}

    /**
     * Retorna la única instancia del EntityManagerFactory.
     * Lazy initialization con sincronización básica.
     */
    public static synchronized EntityManagerFactory getFactory() {
        if (factory == null || !factory.isOpen()) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
        return factory;
    }

    /**
     * Crea y retorna un nuevo EntityManager.
     * Cada operación de DAO debe crear el suyo y cerrarlo al terminar.
     */
    public static EntityManager getEntityManager() {
        return getFactory().createEntityManager();
    }

    /**
     * Cierra la fábrica al apagar la aplicación.
     * Llamar desde el shutdown hook de MainFrame.
     */
    public static void cerrar() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}
