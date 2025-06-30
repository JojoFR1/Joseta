package joseta.database;

import joseta.database.entry.*;

import org.hibernate.*;
import org.hibernate.boot.registry.*;
import org.hibernate.cfg.*;

public class Databases {
    private static Databases instance;
    private final String databaseUrl = "jdbc:sqlite:resources/test.db";

    private Session session;

    private Databases() {
        Configuration configuration = new Configuration();
        
        configuration.addAnnotatedClasses(GuildEntry.class,
                                          UserEntry.class,
                                          ConfigEntry.class,
                                          SanctionEntry.class,
                                          MessageEntry.class,
                                          MarkovMessageEntry.class);
        configuration.setProperty("hibernate.connection.driver_class", org.sqlite.JDBC.class)
            .setProperty("hibernate.dialect", org.hibernate.community.dialect.SQLiteDialect.class)
            .setProperty("hibernate.connection.url", databaseUrl)
            .setProperty("hibernate.hbm2ddl.auto", "update")
            .setProperty("hibernate.show_sql", "false");

 
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
        SessionFactory sessionFactory = configuration.buildSessionFactory(builder.build());
        session = sessionFactory.openSession();
    }

    public static Databases getInstance() {
        if (instance == null) {
            instance = new Databases();
        }
        return instance;
    }

    public Session getSession() { return session; }

    public void create(Object object) {
        Transaction transaction = session.beginTransaction();
        session.persist(object);
        transaction.commit();
    }

    public <E> E createOrUpdate(E object) {
        Transaction transaction = session.beginTransaction();
        E persistent = session.merge(object);
        transaction.commit();
        return persistent;
    }

    public void delete(Object object) {
        Transaction transaction = session.beginTransaction();
        session.remove(object);
        transaction.commit();
    }

    public <E> E get (Class<E> clazz, long id) {
        return session.find(clazz, id);
    }
}
