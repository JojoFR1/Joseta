package joseta.database;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import joseta.annotations.*;
import joseta.database.entities.*;
import joseta.utils.Log;
import joseta.utils.function.Consumer2;
import joseta.utils.function.Function2;
import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.SelectionQuery;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.tool.schema.*;
import org.reflections.*;

import java.util.*;

/**
 * Database utility class for managing database connections and operations.
 * <p>
 * This class provides methods to initialize the database connection, create sessions,
 * and perform CRUD operations on entities.
 */
public class Database {
    private static SessionFactory sessionFactory;
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param host The database host. Cannot be null or blank.
     * @param database The database name. Cannot be null or blank.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String host, String database) {
        return initialize(entitiesPath, user, password, host, "5432", database, false);
    }
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param host The database host. Cannot be null or blank.
     * @param database The database name. Cannot be null or blank.
     * @param showSql Whether to show SQL statements in the logs.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String host, String database, boolean showSql) {
        return initialize(entitiesPath, user, password, host, "5432", database, showSql);
    }
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param host The database host. Cannot be null or blank.
     * @param port The database port. If null or blank, defaults to 5432.
     * @param database The database name. Cannot be null or blank.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String host, String port, String database) {
        return initialize(entitiesPath, user, password, host, port, database, false);
    }
    
    /**
     * Initializes the database connection and entity manager factory.
     *
     * @param entitiesPath The path of the package containing the entity classes. Cannot be null or blank.
     * @param user The database username. Cannot be null or blank.
     * @param password The database password. Can be null or blank (not recommended).
     * @param host The database host. Cannot be null or blank.
     * @param port The database port. If null or blank, defaults to 5432.
     * @param database The database name. Cannot be null or blank.
     * @param showSql Whether to show SQL statements in the logs.
     *
     * @return {@code true} if initialization was successful, {@code false} otherwise.
     */
    public static boolean initialize(String entitiesPath, String user, String password, String host, String port, String database, boolean showSql) {
        String url = host + (port != null && !port.isBlank() ? ":" + port : "5432") + "/" + database;
        return initialize(entitiesPath, user, password, url, showSql);
    }
    
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
        if (sessionFactory == null) throw new IllegalStateException("The database is not initialized. Call Database.initialize(...) first.");
        return sessionFactory.openSession();
    }
    
    /**
     * Obtain a {@link HibernateCriteriaBuilder} which may be used to {@linkplain HibernateCriteriaBuilder#createQuery(Class) construct} {@linkplain org.hibernate.query.criteria.JpaCriteriaQuery criteria queries}.
     *
     * @return The criteria builder.
     *
     * @throws IllegalStateException If the database has not been initialized yet.
     */
    public static HibernateCriteriaBuilder getCriteriaBuilder() {
        if (sessionFactory == null) throw new IllegalStateException("The database is not initialized. Call Database.initialize(...) first.");
        return sessionFactory.getCriteriaBuilder();
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
    
    public static <E> SelectionQuery<E> querySelect(Class<E> clazz, Function2<Predicate, HibernateCriteriaBuilder, Root<E>> func) {
        return querySelect(clazz, func, null);
    }
    
    public static <E> SelectionQuery<E> querySelect(Class<E> clazz, Function2<Predicate, HibernateCriteriaBuilder, Root<E>> queryFunc, Function2<Order, HibernateCriteriaBuilder, Root<E>> orderFunc) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        Root<E> root = query.from(clazz);
        query.select(root).where(queryFunc.get(criteriaBuilder, root));
        if (orderFunc != null) query.orderBy(orderFunc.get(criteriaBuilder, root));
        
        return getSession().createSelectionQuery(query);
    }
    
    public static <E> MutationQuery queryUpdate(Class<E> clazz, Function2<Predicate, HibernateCriteriaBuilder, Root<E>> whereFunc, Consumer2<CriteriaUpdate<E>, Root<E>> setFunc, Session session) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaUpdate<E> update = criteriaBuilder.createCriteriaUpdate(clazz);
        Root<E> root = update.from(clazz);
        Predicate where = whereFunc.get(criteriaBuilder, root);
        update.where(where);
        setFunc.apply(update, root);
        
        return session.createMutationQuery(update);
    }
    
    public static <E> MutationQuery queryDelete(Class<E> clazz, Function2<Predicate, HibernateCriteriaBuilder, Root<E>> func, Session session) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaDelete<E> delete = criteriaBuilder.createCriteriaDelete(clazz);
        Root<E> root = delete.from(clazz);
        Predicate where = func.get(criteriaBuilder, root);
        delete.where(where);
        
        return session.createMutationQuery(delete);
    }
}
