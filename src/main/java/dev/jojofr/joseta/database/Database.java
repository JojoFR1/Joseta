package dev.jojofr.joseta.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jojofr.joseta.database.daos.MessageDao;
import dev.jojofr.joseta.database.entities.SanctionEntity;
import dev.jojofr.joseta.utils.Log;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Types;

public class Database {
    private static Jdbi jdbi;
    
    public static boolean initialize(String user, String password, String host, String port, String database) {
        if (user == null || user.isBlank()) {
            Log.err("Database user is not provided.");
            return false;
        }
        if (password == null || password.isBlank()) Log.warn("Database password is not provided.");
        
        if (host == null || host.isBlank()) Log.warn("Database host is not provided. Using default host 'localhost'.");
        if (port == null || port.isBlank()) Log.warn("Database port is not provided. Using default port 5432.");
        if (database == null || database.isBlank()) {
            Log.err("Database name is not provided.");
            return false;
        }
        
        String url = "jdbc:postgresql://"
                    + (host != null && !host.isBlank() ? host : "localhost") + ":"
                    + (port != null && !port.isBlank() ? port : "5432") + "/"
                    + database;
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setPoolName("JosetaHikariPool");
        
        try {
            HikariDataSource dataSource = new HikariDataSource(config);
            
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .loggers("slf4j")
                .load()
                .migrate();
            
            jdbi = Jdbi.create(dataSource);
            jdbi.installPlugin(new SqlObjectPlugin());
            jdbi.installPlugin(new PostgresPlugin());
            
            jdbi.registerColumnMapper(SanctionEntity.SanctionType.class, (rs, col, ctx) -> {
                String value = rs.getString(col);
                if (value == null) return null;
                
                return switch (value.charAt(0)) {
                    case 'W' -> SanctionEntity.SanctionType.WARN;
                    case 'T' -> SanctionEntity.SanctionType.TIMEOUT;
                    case 'K' -> SanctionEntity.SanctionType.KICK;
                    case 'B' -> SanctionEntity.SanctionType.BAN;
                    default -> throw new IllegalArgumentException("Unknown SanctionType code: " + value);
                };
            });
            jdbi.registerColumnMapper(MessageDao.EntityType.class, (rs, col, ctx) -> {
                String value = rs.getString(col);
                if (value == null) return null;
                
                return MessageDao.EntityType.valueOf(value);
            });
            jdbi.registerArgument(new AbstractArgumentFactory<SanctionEntity.SanctionType>(Types.CHAR) {
                @Override
                protected Argument build(SanctionEntity.SanctionType value, ConfigRegistry config) {
                    return (position, statement, ctx) -> statement.setString(position, String.valueOf(value.code));
                }
            });
            jdbi.registerArgument(new AbstractArgumentFactory<MessageDao.EntityType>(Types.VARCHAR) {
                @Override
                protected Argument build(MessageDao.EntityType value, ConfigRegistry config) {
                    return (position, statement, ctx) -> statement.setString(position, value.name());
                }
            });
            
            return true;
        } catch (Exception e) {
            Log.err("Database initialization failed.", e);
            return false;
        }
    }
    
    public static Jdbi get() {
        if (jdbi == null) throw new IllegalStateException("The database is not initialized. Call Database.initialize(...) first.");
        return jdbi;
    }
    
    public static <T> T withHandle(HandleCallback<T, RuntimeException> callback) {
        return get().inTransaction(callback);
    }
    
    public static void useHandle(HandleConsumer<RuntimeException> callback) {
        get().useHandle(callback);
    }
}
