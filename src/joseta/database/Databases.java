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

    public void create(Object object) {
        Session session = this.getSession();
        Transaction transaction = session.beginTransaction();
        session.persist(object);
        transaction.commit();
    }

    public void create(Object... objects) {
        Session session = this.getSession();
        Transaction transaction = session.beginTransaction();
        for (Object object : objects) session.persist(object);
        transaction.commit();
    }

    public <E> E createOrUpdate(E object) {
        Session session = this.getSession();
        Transaction transaction = session.beginTransaction();
        E persistent = session.merge(object);
        transaction.commit();
        return persistent;
    }

    public void delete(Object object) {
        Session session = getSession();
        Transaction transaction = session.beginTransaction();
        session.remove(object);
        transaction.commit();
    }

    public <E> E get (Class<E> clazz, Object id) {
        Session session = getSession();
        return session.find(clazz, id);
    }

    public <E> List<E> getAll(Class<E> clazz) {
        HibernateCriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        return this.getSession().createSelectionQuery(query).getResultList();
    }

    private <E> void test(Class<E> clazz) {
        HibernateCriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
        // criteriaBuilder.at
        CriteriaQuery<E> query = criteriaBuilder.createQuery(clazz);
        
        this.getSession().createSelectionQuery(query).getResultList();

        CriteriaDelete<E> deleteQuery = criteriaBuilder.createCriteriaDelete(clazz);
        this.getSession().createMutationQuery(deleteQuery).executeUpdate();

        CriteriaUpdate<E> updateQuery = criteriaBuilder.createCriteriaUpdate(clazz);
        this.getSession().createMutationQuery(updateQuery).executeUpdate();
    
        sessionFactory.inTransaction(session -> {
            session.persist(new GuildEntry(0L, "Test Guild", null, 10L));
        });

        // sessionFactory.inSession(session -> {
        //     var builder = sessionFactory.getCriteriaBuilder();
        //     var queryA = builder.createQuery(String.class);
        //     var book = queryA.from(GuildEntry.class);
        //     query.select(builder.concat(builder.concat(book.get(GuildEntry_.guildId), builder.literal(": ")),
        //             book.get(Book_.title)));
        //     out.println(session.createSelectionQuery(query).getSingleResult());
        // });
    }
}
