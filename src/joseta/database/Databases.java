package joseta.database;

import joseta.*;
import joseta.database.entry.*;

import java.util.*;

import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.query.criteria.*;
import org.hibernate.tool.schema.*;

import jakarta.persistence.criteria.*;

public class Databases {
    private static Databases instance;
    
    private final SessionFactory sessionFactory;

    private Databases() {
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
            .showSql(false, true, true)
            .schemaToolingAction(Action.UPDATE)
            .property("hibernate.dialect", org.hibernate.community.dialect.SQLiteDialect.class);

        sessionFactory = configuration.createEntityManagerFactory();
        sessionFactory.getSchemaManager().create(true);
    }

    public static Databases getInstance() {
        if (instance == null) {
            instance = new Databases();
        }
        return instance;
    }

    public Session getSession() { return sessionFactory.openSession(); }
    public HibernateCriteriaBuilder getCriteriaBuilder() { return sessionFactory.getCriteriaBuilder(); }


    public void create(Object object) {
        try (Session session = this.getSession();) {
            Transaction transaction = session.beginTransaction();
            session.persist(object);
            transaction.commit();
        }
    }

    public void create(Object... objects) {
        try (Session session = this.getSession()) {
            Transaction transaction = session.beginTransaction();
            for (Object object : objects) session.persist(object);
            transaction.commit();
        }
    }

    public <E> E createOrUpdate(E object) {
        try (Session session = this.getSession()) {
            Transaction transaction = session.beginTransaction();
            E persistent = session.merge(object);
            transaction.commit();
            return persistent;
        }
    }

    public void delete(Object object) {
        try (Session session = this.getSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(object);
            transaction.commit();
        }
    }

    public <E> E get (Class<E> clazz, Object id) {
        try (Session session = getSession()) { return session.find(clazz, id); }
    }

    public <E> List<E> getAll(Class<E> clazz) {
        HibernateCriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        return this.getSession().createSelectionQuery(query).getResultList();
    }
}
