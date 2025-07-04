package joseta.database;

import joseta.*;
import joseta.database.entry.*;

import arc.files.*;
import arc.util.*;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.query.criteria.*;
import org.hibernate.tool.schema.*;

import jakarta.persistence.criteria.*;

public class Databases {
    private static SessionFactory sessionFactory;

    private Databases() {
        Fi file = Fi.get(Vars.sqlFilePath);
        if (!file.exists())
            try { file.write().close(); }
            catch (IOException e) { Log.err("Could not create the SQL file at '@': @", file.absolutePath(), e); }

        HibernatePersistenceConfiguration configuration = new HibernatePersistenceConfiguration("BotDatabase")
            .managedClasses(GuildEntry.class,
                            UserEntry.class,
                            ConfigEntry.class,
                            SanctionEntry.class,
                            MessageEntry.class,
                            MarkovMessageEntry.class)
            .jdbcDriver("org.sqlite.JDBC")
            .jdbcCredentials(Vars.sqlUsername, Vars.sqlPassword)
            .jdbcUrl(Vars.sqlUrl)
            .showSql(true, true, true)
            .schemaToolingAction(Action.NONE)
            .property("hibernate.dialect", org.hibernate.community.dialect.SQLiteDialect.class);
        
        sessionFactory = configuration.createEntityManagerFactory();
        sessionFactory.getSchemaManager().create(true);
    }

    public static Session getSession() {
        if (sessionFactory == null) new Databases(); // Initialize the session factory if it has not been initialized yet
        return sessionFactory.openSession();
    }
    public static HibernateCriteriaBuilder getCriteriaBuilder() {
        if (sessionFactory == null) new Databases(); // Initialize the session factory if it has not been initialized yet
        return sessionFactory.getCriteriaBuilder();
    }


    public static void create(Object object) {
        try (Session session = getSession();) {
            Transaction transaction = session.beginTransaction();
            session.persist(object);
            transaction.commit();
        }
    }

    public static void create(Object... objects) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            for (Object object : objects) session.persist(object);
            transaction.commit();
        }
    }

    public static <E> E createOrUpdate(E object) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            E persistent = session.merge(object);
            transaction.commit();
            return persistent;
        }
    }

    public static void delete(Object object) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(object);
            transaction.commit();
        }
    }

    public static <E> E get (Class<E> clazz, Object id) {
        try (Session session = getSession()) { return session.find(clazz, id); }
    }

    public static <E> List<E> getAll(Class<E> clazz) {
        HibernateCriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        return getSession().createSelectionQuery(query).getResultList();
    }
}
