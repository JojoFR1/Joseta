package joseta.database;

import jakarta.persistence.*;
import joseta.annotations.*;
import joseta.database.entities.*;
import org.hibernate.*;
import org.hibernate.jpa.*;
import org.hibernate.tool.schema.*;
import org.reflections.*;

import java.util.*;

public class Database {
    private static SessionFactory sessionFactory;

    public static void initialize(String user, String password, String url) {
        Reflections reflections = new Reflections("joseta.database.entities");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Entity.class);

        HibernatePersistenceConfiguration configuration = new HibernatePersistenceConfiguration("DiscordDatabase")
            .managedClasses(classes)
            .jdbcCredentials(user, password)
            .jdbcUrl("jdbc:postgresql://" + url)
            .jdbcPoolSize(16)
            .schemaToolingAction(Action.UPDATE)
            .showSql(true, true, true);

        sessionFactory = configuration.createEntityManagerFactory();
    }

    public static Session getSession() {
        if (sessionFactory == null) throw new IllegalStateException("Database not initialized. Call Database.initialize(...) first.");
        return sessionFactory.openSession();
    }

    public static <E> E test(E object) {
        try (Session session = getSession()) {
            Transaction transaction = session.beginTransaction();
            E persistent = session.merge(object);
            transaction.commit();
            return persistent;
        }
    }
}
