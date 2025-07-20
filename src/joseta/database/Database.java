package joseta.database;

import joseta.*;
import joseta.database.entry.*;

import arc.files.*;
import arc.func.*;
import arc.util.*;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.query.*;
import org.hibernate.query.criteria.*;
import org.hibernate.tool.schema.*;

import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Order;

public class Database {
    private static SessionFactory sessionFactory;

    private static void initializeSessionFactory() {
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
                .property("hibernate.dialect", org.hibernate.community.dialect.SQLiteDialect.class)
                .schemaToolingAction(Action.UPDATE) //TODO still tries to create table
                .property("spring.jpa.hibernate.ddl-auto", "update");

        sessionFactory = configuration.createEntityManagerFactory();
        sessionFactory.getSchemaManager().create(true);
    }

    public static Session getSession() {
        if (sessionFactory == null) initializeSessionFactory();
        return sessionFactory.openSession();
    }
    public static HibernateCriteriaBuilder getCriteriaBuilder() {
        if (sessionFactory == null) initializeSessionFactory();
        return sessionFactory.getCriteriaBuilder();
    }

    public static void create(Object object) {
        try (Session session = getSession()) {
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

    public static <E> SelectionQuery<E> querySelect(Class<E> clazz, Func2<HibernateCriteriaBuilder, Root<E>, Predicate> func) {
        return querySelect(clazz, func, null);
    }

    public static <E> SelectionQuery<E> querySelect(Class<E> clazz, Func2<HibernateCriteriaBuilder, Root<E>, Predicate> queryFunc, Func2<HibernateCriteriaBuilder, Root<E>, Order> orderFunc) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        Root<E> root = query.from(clazz);
        query.select(root).where(queryFunc.get(criteriaBuilder, root));
        if (orderFunc != null) query.orderBy(orderFunc.get(criteriaBuilder, root));

        return getSession().createSelectionQuery(query);
    }

    public static <E> MutationQuery queryUpdate(Class<E> clazz, Func2<HibernateCriteriaBuilder, Root<E>, Predicate> func) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaUpdate<E> query = criteriaBuilder.createCriteriaUpdate(clazz);
        Root<E> root = query.from(clazz);
        Predicate where = func.get(criteriaBuilder, root);
        query.where(where);

        return getSession().createMutationQuery(query);
    }

    public static <E> MutationQuery queryDelete(Class<E> clazz, Func2<HibernateCriteriaBuilder, Root<E>, Predicate> func) {
        HibernateCriteriaBuilder criteriaBuilder = Database.getCriteriaBuilder();
        CriteriaDelete<E> query = criteriaBuilder.createCriteriaDelete(clazz);
        Root<E> root = query.from(clazz);
        Predicate where = func.get(criteriaBuilder, root);
        query.where(where);

        return getSession().createMutationQuery(query);
    }
}
