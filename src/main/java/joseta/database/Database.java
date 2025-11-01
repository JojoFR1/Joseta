package joseta.database;

import jakarta.persistence.*;
import joseta.annotations.*;
import joseta.database.entities.*;
import joseta.utils.Log;
import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.tool.schema.*;
import org.reflections.*;

import java.util.*;

public class Database {
    private static SessionFactory sessionFactory;
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param url The database URL. Cannot be null or blank. Must be a PostgreSQL server.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String url) {
        return initialize(entitiesPath, user, password, url, false);
    }
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param url The database URL. Cannot be null or blank. Must be a PostgreSQL server.
     * @param showSql Whether to show SQL statements in the logs.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String url, boolean showSql) {
        if (entitiesPath == null || entitiesPath.isEmpty()) {
            Log.err("Entities package path is not provided.");
            return false;
        }
        
        Reflections reflections = new Reflections(entitiesPath);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);
        
        if (classes.isEmpty()) {
            Log.warn("No entity classes found in the specified path: " + entitiesPath);
            return false;
        }
        
        if (user == null || user.isBlank()) {
            Log.err("Database user ('"+ user +"') is not provided.");
            return false;
        }
        if (password == null || password.isBlank()) Log.warn("Database password is not provided.");
        
        if (url == null || url.isBlank()) {
            Log.err("Database URL ('"+ url +"') is not provided.");
            return false;
        }
        
        HibernatePersistenceConfiguration configuration = new HibernatePersistenceConfiguration("DiscordDatabase")
            .managedClasses(classes)
            .jdbcCredentials(user, password)
            .jdbcUrl("jdbc:postgresql://" + url)
            .jdbcPoolSize(16)
            .schemaToolingAction(Action.UPDATE)
            .showSql(showSql, true, true);

        sessionFactory = configuration.createEntityManagerFactory();
        
        if (sessionFactory == null) {
            Log.err("SessionFactory creation failed.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Open a new {@link Session}.
     *
     * @return The created session.
     *
     * @throws IllegalStateException If the database has not been initialized yet.
     * @throws HibernateException Indicates a problem opening the session; pretty rare here.
     */
    public static Session getSession() {
        if (sessionFactory == null) throw new IllegalStateException("Database not initialized. Call Database.initialize(...) first.");
        return sessionFactory.openSession();
    }
    
    /**
     * Create (or insert) new objects in the database.
     *
     * @param objects The objects to create.
     */
    public static void create(Object... objects) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            for (Object object : objects) session.persist(object);
            transaction.commit();
        }
    }
    
    /**
     * Retrieve an object from the database by its type and primary key.
     *
     * @param clazz The class type.
     * @param id The primary key.
     *
     * @param <T> The object type.
     *
     * @return A fully-fetched persistent instance or null.
     */
    public static <T> T get(Class<T> clazz, Object id) {
        try (Session session = getSession()) { return session.find(clazz, id); }
    }
    
    /**
     * Update an existing object in the database.
     *
     * @param object The object to update.
     *
     * @param <T> The object type.
     *
     * @return The updated persistent object.
     */
    public static <T> T update(T object) {
        return createOrUpdate(object);
    }
    
    /**
     * Create or update an object in the database.
     *
     * @param object The object to create or update.
     *
     * @param <T> The object type.
     *
     * @return The updated persistent object.
     */
    public static <T> T createOrUpdate(T object) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            T persistent = session.merge(object);
            transaction.commit();
            return persistent;
        }
    }
    
    /**
     * Delete an object from the database.
     *
     * @param object The object to delete.
     */
    public static void delete(Object object) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            if(!session.contains(object)) object = session.merge(object);
            session.remove(object);
            transaction.commit();
        }
    }
}
